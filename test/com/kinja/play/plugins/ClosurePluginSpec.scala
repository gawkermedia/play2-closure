// vim: sw=2 ts=2 softtabstop=2 expandtab :
package com.kinja.play.plugins

import com.kinja.soy.Soy
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import com.kinja.TestApp

import com.google.template.soy.data.SoyListData
import com.google.template.soy.data.SoyMapData

class ClosurePluginSpec extends Specification with TestApp {

  "Render a test page" should {
    "equal 'Hello world!'" in {
      running(app) {
        closure.render("closuretest.index") === "Hello world!"
      }
    }
  }

  "Render a Long value" should {
    "equal '42'" in {
      running(app) {
        closure.render("closuretest.long", Soy.map("long" -> 42L)) === "42"
      }
    }
  }

  "Render Boolean values" should {
    "equal 'true false'" in {
      running(app) {
        closure.render("closuretest.bool", Soy.map("true" -> true, "nottrue" -> false)) === "true false"
      }
    }
  }

  "Render a list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        closure.render(
          "closuretest.list",
          Soy.map("name" -> Some("Test list"), "list" -> Soy.list(1, "2", 3, 4, "5", 6))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a list in list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        closure.render(
          "closuretest.listInList",
          Soy.map("name" -> Some("Test list"), "list" -> Soy.list(Soy.list(1, "2", 3, 4L, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a list in map test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        closure.render(
          "closuretest.listInMap",
          Soy.map("name" -> Some("Test list"), "map" -> Soy.map("items" -> Soy.list(1, "2", 3, 4L, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render an all soy data test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        closure.render(
          "closuretest.listInList",
          new SoyMapData(
            "name", "Test list",
            "list", new SoyListData(java.util.Arrays.asList(new SoyListData(java.util.Arrays.asList(1, 2, 3, 4, 5, 6)))) /*,
            "locale", "hu_HU"*/ )) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render an Option value" should {
    "equal 'None'" in {
      running(app) {
        closure.render("closuretest.option", Soy.map("value" -> Option.empty[String])) === "None"
      }
    }
    "equal 'Some'" in {
      running(app) {
        closure.render("closuretest.option", Soy.map("value" -> Some("Some value"))) === "Some value"
      }
    }
  }

  "Locale" should {
    "work" in {
      running(app) {
        closure.render("closuretest.locale") === "Submit"
        closure.render("closuretest.locale", Soy.map("locale" -> "hu_HU")) === "Hu Submit"
        closure.render("closuretest.locale", Soy.map("locale" -> "es_ES")) === "es Submit"
      }
    }
  }

  "Inject" should {
    "work" in {
      running(app) {
        closure.render("closuretest.inject", Soy.map(), Soy.map("foo" -> "bar")) === "bar"
      }
    }
  }

  "Empty delegate" should {
    "work" in {
      running(app) {
        closure.render("closuretest.option", Soy.map("delegate" -> Soy.map(), "value" -> Some("Some value"))) === "Some value"
      }
    }
  }
}
