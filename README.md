grails-geb-saucelabs
====================

Grails plugin to provide better [Sauce Labs](http://saucelabs.com) support when using [Geb](http://gebish.org).

1. Test results of pass or fail are updated for the session.
2. Application name and version are included as the job name and build, unless specified otherwise.
3. Sauce Connect is started and stopped when functional tests are run. This alleviates the need to run Sauce Connect outside of the grails process.

Include this plugin with the following, the plugin will find the Sauce Labs credentials from the SauceLabsDriverFactory calls.

```
plugins {
    test(":geb-saucelabs:0.1") {
        excludes "geb-core" // use the application geb version
    }
}
```
