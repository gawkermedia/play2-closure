package com.kinja.soy

import org.specs2.mutable._
import org.specs2.specification.Scope
import org.specs2.execute.{ AsResult, Result }
import play.api.test._
import play.api.test.Helpers._
import com.kinja.TestApp

import com.kinja.play.plugins.Closure

import com.google.template.soy.data.{ SoyMapData, SoyListData }

class SoyRenderSpec extends Specification with TestApp {

  "Closure renderer" should {
    "render SoyNull" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull))
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")))
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)))
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)))
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)))
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)))
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)))
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5f)))
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0f)))
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyFloat(-float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5f)))
        rendered must_== "Value is: -9409.5"
      }
    }
    "render SoyDouble(+double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(983.279)))
        rendered must_== "Value is: 983.279"
      }
    }
    "render SoyDouble(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(0.0)))
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyDouble(-double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(-167.5982)))
        rendered must_== "Value is: -167.5982"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())))
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))))
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.0f), SoyDouble(3.0), SoyNull))))
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()))
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")))
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.0f, 3.0, SoyNull)))
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
  }

  "Closure renderer with pre-build" should {
    "render SoyNull" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull).build)
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")).build)
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)).build)
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)).build)
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)).build)
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)).build)
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)).build)
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)).build)
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5f)).build)
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0f)).build)
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyFloat(-float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5f)).build)
        rendered must_== "Value is: -9409.5"
      }
    }
    "render SoyDouble(+double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(983.279)).build)
        rendered must_== "Value is: 983.279"
      }
    }
    "render SoyDouble(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(0.0)).build)
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyDouble(-double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(-167.5982)).build)
        rendered must_== "Value is: -167.5982"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())).build)
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))).build)
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))).build)
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.0f), SoyDouble(3.0), SoyNull))).build)
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()).build)
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)).build)
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")).build)
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.0f, 3.0, SoyNull)).build)
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
  }

  "Closure renderer with Soy.map( outer type" should {
    "render SoyNull" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull))
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")))
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)))
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)))
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)))
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)))
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)))
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5f)))
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0f)))
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyFloat(-float)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5f)))
        rendered must_== "Value is: -9409.5"
      }
    }
    "render SoyDouble(+double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(983.279)))
        rendered must_== "Value is: 983.279"
      }
    }
    "render SoyDouble(0)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(0.0)))
        rendered must_== "Value is: 0.0"
      }
    }
    "render SoyDouble(-double)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueNumber", Soy.map("value" -> SoyDouble(-167.5982)))
        rendered must_== "Value is: -167.5982"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())))
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))))
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.0f), SoyDouble(3.0), SoyNull))))
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()))
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")))
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = Closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.0f, 3.0, SoyNull)))
        rendered must_== "List is: a, 1, 2.0, 3.0, UNDEFINED"
      }
    }
  }
}
