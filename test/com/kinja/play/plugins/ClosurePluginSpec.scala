// vim: sw=2 ts=2 softtabstop=2 expandtab :
package com.kinja.play.plugins

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

  "Render Boolean values" should {
    "equal 'true false'" in {
      running(app) {
        Closure.render("closuretest.bool", Map("true" -> true, "nottrue" -> false)) === "true false"
      }
    }
  }

  "Render a list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.list",
          Map("name" -> Some("Test list"), "list" -> List(1, "2", 3, 4, "5", 6))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a list in list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.listInList",
          Map("name" -> Some("Test list"), "list" -> List(List(1, "2", 3, 4L, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a soy list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.list",
          Map("name" -> Some("Test list"), "list" -> new SoyListData(java.util.Arrays.asList(1, 2, 3, 4, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a soy list in list test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.listInList",
          Map("name" -> Some("Test list"), "list" -> List(new SoyListData(java.util.Arrays.asList(1, 2, 3, 4, 5, 6))))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a list in map test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.listInMap",
          Map("name" -> Some("Test list"), "map" -> Map("items" -> List(1, "2", 3, 4L, 5, 6)))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a soy list in map test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.listInMap",
          Map("name" -> Some("Test list"), "map" -> Map("items" -> new SoyListData(java.util.Arrays.asList(1, 2, 3, 4, 5, 6))))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render a soy list in soy map test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
          "closuretest.listInMap",
          Map("name" -> Some("Test list"), "map" -> new SoyMapData("items", (new SoyListData(java.util.Arrays.asList(1, 2, 3, 4, 5, 6)))))) === "Test list: 1, 2, 3, 4, 5, 6"
      }
    }
  }

  "Render an all soy data test page" should {
    "equal 'Test list: 1, 2, 3, 4, 6'" in {
      running(app) {
        Closure.render(
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
        Closure.render("closuretest.option", Map("value" -> None)) === "None"
      }
    }
    "equal 'Some'" in {
      running(app) {
        Closure.render("closuretest.option", Map("value" -> Some("Some value"))) === "Some value"
      }
    }
  }

  "Locale" should {
    "work" in {
      running(app) {
        Closure.render("closuretest.locale") === "Submit"
        Closure.render("closuretest.locale", Map("locale" -> "hu_HU")) === "Hu Submit"
        Closure.render("closuretest.locale", Map("locale" -> "es_ES")) === "es Submit"
      }
    }
  }

  "Inject" should {
    "work" in {
      running(app) {
        Closure.render("closuretest.inject", Map[String, Any](), Map("foo" -> "bar")) === "bar"
      }
    }
  }

  "Empty delegate" should {
    "work" in {
      running(app) {
        Closure.render("closuretest.option", Map("delegate" -> Set(), "value" -> Some("Some value"))) === "Some value"
      }
    }
  }

  "Render the imageUrl function" should {
    "equal 'http://i.kinja-img.com/gawker-media/image/upload/t_bar/foojpg.jpg'" in {
      running(app) {
        Closure.render("closuretest.function") === "http://i.kinja-img.com/gawker-media/image/upload/t_bar/foojpg.jpg"
      }
    }
  }
}
