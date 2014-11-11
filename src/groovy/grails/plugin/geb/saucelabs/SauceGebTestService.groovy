package grails.plugin.geb.saucelabs

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager
import com.saucelabs.ci.sauceconnect.SauceTunnelManager
import com.saucelabs.common.Utils
import com.saucelabs.saucerest.SauceREST
import geb.driver.SauceLabsDriverFactory
import grails.build.GrailsBuildListener
import org.codehaus.groovy.grails.cli.support.GrailsBuildEventListener
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.openqa.selenium.remote.RemoteWebDriver

/**
 * Manages Sauce Connect and updating of the test results. This class assumes Geb tests, which
 * assumes one session per test invocation. This class will not work if multiple sessions (driver instances)
 * are used in the same JVM. Sauce Connect v2 (Java based) will be started at the beginning of the test
 * phase and closed at the end. Test results of pass or fail will be updated at the end of the test phase.
 */
class SauceGebTestService implements GrailsBuildListener {
  boolean verboseMode = false
  boolean useSauceConnect = true

  /** Updated by a TestFailure event. */
  private boolean testsFailed = false
  /** Found by inspecting the constructed RemoteWebDriver */
  private String sessionId
  /** Found by the CloudDriverFactory.assembleProviderUrl(...) call */
  private String username
  /** Found by the CloudDriverFactory.assembleProviderUrl(...) call */
  private String accessKey
  /** Set from the GrailsApplication for populating the job name, if not already populated. */
  private String applicationName
  /** Set from the GrailsApplication for populating the build, if not already populated. */
  private String applicationVersion

  private SauceREST sauceREST
  private SauceTunnelManager sauceConnectManager

  void init(GrailsBuildEventListener eventListener) {
    eventListener.addGrailsBuildListener(this)
    addDriverMetaDataScraper()
  }

  /**
   * From https://gist.github.com/vorburger/3429822
   * Returns a free port number on localhost.
   *
   * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
   * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
   *
   * @return a free port number on localhost
   * @throws IllegalStateException if unable to find a free port
   */
  private static int findFreePort() {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      socket.setReuseAddress(true);
      int port = socket.getLocalPort();
      try {
        socket.close();
      } catch (IOException e) {
        // Ignore IOException on close()
      }
      return port;
    } catch (IOException e) {
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
        }
      }
    }
    throw new IOException("Could not find a free TCP/IP port for Sauce Connect");
  }

  /**
   * Open the connection to Sauce Connect.
   * @return true if the connection was established
   */
  boolean openSauceConnect() {
    if (!useSauceConnect) {
      return false
    }
    boolean connected = false
    try {
      SauceTunnelManager manager = new SauceConnectFourManager();
      File sauceConnectJar = null // it will find the JAR in the classpath
      String options = null
      String httpsProtocol = null
      manager.openConnection(username, accessKey, findFreePort(), sauceConnectJar, options, httpsProtocol, System.out, false /*verbose logging*/);
      sauceConnectManager = manager
      connected = true
    } catch (IOException e) {
      println "Error generated when launching Sauce Connect: ${e.message}"
    }
    connected
  }

  /**
   * Closes the Sauce Connect connection. This is safe to call whether a connection has been successfully
   * established or not.
   */
  void closeSauceConnect() {
    if (sauceConnectManager) {
      sauceConnectManager.closeTunnelsForPlan(username, null, System.out);
    }
  }

  void addDriverMetaDataScraper() {
    SauceLabsDriverFactory.metaClass.invokeMethod = { String name, Object[] args ->
      if (name == 'create') {
        // Get username and accessKey
        int i = args.length - 1;
        while (!(args[i] instanceof String) && i>0) i--;
        if (args[i] instanceof String) {
          username = args[i-1];
          accessKey = args[i];
          sauceREST = new SauceREST(username, accessKey)
          openSauceConnect()
        }

        // Update the capabilities with application information
        Map caps = args[args.length-1] instanceof Map ? args[args.length-1] : null
        if (!caps) {
          caps = [:]
          args = Arrays.copyOf(args, args.length+1)
          args[args.length-1] = caps
        }
        if (applicationName)
          caps.put('name', caps.get('name') ?: applicationName)
        if (applicationVersion)
          caps.put('build', caps.get('build') ?: applicationVersion)
      }

      def metaMethod = SauceLabsDriverFactory.metaClass.getMetaMethod(name, args)
      def result = null
      if(metaMethod) {
        result = metaMethod.invoke(delegate, args)
      }

      if (name == 'create' && result instanceof RemoteWebDriver) {
        RemoteWebDriver driver = result
        sessionId = driver.sessionId
        if (verboseMode) {
          println "sessionId = ${sessionId}"
        }
      }

      result
    }
  }

  void updateTestResults() {
    if (sessionId != null) {
      Map<String, Object> updates = new HashMap<String, Object>();
      updates.put("passed", !testsFailed);
      Utils.addBuildNumberToUpdate(updates);
      sauceREST.updateJobInfo(sessionId, updates);

      if (verboseMode) {
        String authLink = sauceREST.getPublicJobLink(sessionId);
        println("Job link: " + authLink);
      }
    }
  }

  void processApplication(GrailsApplication application) {
    applicationName = application.metadata.getApplicationName()
    applicationVersion = application.metadata.getApplicationVersion()
  }

  @Override
  void receiveGrailsBuildEvent(String name, Object... args) {
    switch (name) {
      case 'TestPhaseEnd':
        if (args[0] == 'functional') {
          updateTestResults()
          closeSauceConnect()
        }
        break

      case 'TestFailure':
        testsFailed = true
        break

      case 'PluginLoadStart':
        processApplication(args[0].application)
        break
    }
  }
}
