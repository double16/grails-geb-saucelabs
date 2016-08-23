grails-geb-saucelabs
====================

[![CircleCI](https://circleci.com/gh/double16/grails-geb-saucelabs.svg?style=svg&circle-token=fd11198035831f2dbf4e9e95ede1634cc11056f4)](https://circleci.com/gh/double16/grails-geb-saucelabs)

Grails plugin to provide additional [Sauce Labs](http://saucelabs.com) support when using [Geb](http://gebish.org).

1. Test results of pass or fail are updated.
2. Application name and version are included as the job name and build, unless specified otherwise.
3. Sauce Connect is started and stopped when functional tests are run. This alleviates the need to run Sauce Connect outside of the grails process.

Include this plugin with the following in BuildConfig.groovy. No configuration is required in Config.groovy. The credentials for Sauce Labs are scraped from SauceLabsDriverFactory.

```
plugins {
    test(":geb-saucelabs:0.2") {
        excludes "geb-core" // use the application specified geb version
    }
}
```

Sauce Connect will need to be provided outside of the Grails process if concurrent test processes are run. Add the following to BuildConfig.groovy to disable Sauce Connect. The tests will pick up an existing Sauce Connect tunnel.
See [grails-saucelabs-demo](https://github.com/double16/grails-saucelabs-demo) for an example of concurrent tests.
```
grails.plugin.'geb-saucelabs'.useSauceConnect = false
```

Changes
=======

* 0.1 - Initial revision

* 0.2 - Upgrade to Grails 2.4.4. Upgrade ci-sauce to 1.90, using version 4.3.6 of the Sauce Connect VPN.
