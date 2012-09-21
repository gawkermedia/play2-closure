package com.kinja.play.plugins

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ClosurePluginSpec extends Specification {

	val app = FakeApplication(
		additionalPlugins = Seq(
			"com.kinja.play.plugins.ClosurePlugin"))

	"Render a test page page" should {
		"equal 'Hello world!'" in {
			running(app) {
				Closure.render("closuretest.index") === "Hello world!"
			}
		}
	}
}
