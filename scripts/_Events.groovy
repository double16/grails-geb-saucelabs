
eventTestPhasesStart = { ->
  def service = Class.forName('grails.plugin.geb.saucelabs.SauceGebTestService').newInstance()
  if (buildConfig?.grails?.plugin?.'geb-saucelabs'?.useSauceConnect != null) {
    service.useSauceConnect = buildConfig.grails.plugin.'geb-saucelabs'.useSauceConnect
  }
  service.init(eventListener)
}
