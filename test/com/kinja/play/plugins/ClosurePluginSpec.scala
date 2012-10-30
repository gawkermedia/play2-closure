package com.kinja.play.plugins

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class TestResource(name: String)

class TestListResource(items: List[TestResource])

class ClosurePluginSpec extends Specification {

	val app = FakeApplication(
		additionalPlugins = Seq(
			"com.kinja.play.plugins.ClosurePlugin"))

	"Render a test page" should {
		"equal 'Hello world!'" in {
			running(app) {
				Closure.render("closuretest.index") === "Hello world!"
			}
		}
	}

	"Render a Long value" should {
		"equal '42'" in {
			running(app) {
				Closure.render("closuretest.long", Map("long" -> 42L)) === "42"
			}
		}
	}

	"Render a list test page" should {
		"equal 'Test list: 1, 2, 3, 4, 6'" in {
			running(app) {
				Closure.render(
					"closuretest.list",
					Map("name" -> Some("Test list"), "list" -> List(1, 2, 3, 4, 5, 6))) === "Test list: 1, 2, 3, 4, 5, 6"
			}
		}
	}

	"Render a list in list test page" should {
		"equal 'Test list: 1, 2, 3, 4, 6'" in {
			running(app) {
				Closure.render(
					"closuretest.listInList",
					Map("name" -> Some("Test list"), "list" -> List(List(1, 2, 3, 4, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
			}
		}
	}

	"Locale" should {
		"be en_US by default" in {
			running(app) {
				Closure.getLocale === "en_US"
			}
		}

		"equal hu_HU" in {
			running(app) {
				Closure.setLocale("hu_HU")
				Closure.getLocale === "hu_HU"
			}
		}
	}
}
