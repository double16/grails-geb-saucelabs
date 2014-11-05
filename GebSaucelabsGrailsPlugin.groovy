class GebSaucelabsGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.0 > *"
    def loadBefore = ['geb']

    def title = "Improved Geb-SauceLabs integration"
    def author = "Patrick Double"
    def authorEmail = "pat@patdouble.com"
    def description = '''\
Grails plugin to provide better Sauce Labs support when using Geb functional testing. Adds Sauce Connect,
test results, and application information to the Sauce tests.
'''
    def documentation = "https://github.com/double16/grails-geb-saucelabs/blob/master/README.md"
    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/double16/grails-geb-saucelabs/issues" ]
    def scm = [ url: "https://github.com/double16/grails-geb-saucelabs" ]
}
