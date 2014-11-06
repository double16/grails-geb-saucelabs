
eventCompileEnd = { ->
  def service = Class.forName('grails.plugin.geb.saucelabs.SauceGebTestService').newInstance()
  service.init(eventListener)
}
