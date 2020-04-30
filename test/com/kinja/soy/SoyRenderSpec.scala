package com.kinja.soy

import org.specs2.mutable._
import play.api.test.Helpers._
import com.kinja.TestApp

class SoyRenderSpec extends Specification with TestApp {

  "Closure renderer" should {
    "render SoyNull" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull))
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")))
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)))
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)))
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)))
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)))
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)))
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5)))
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyFloat(-double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5)))
        rendered must_== "Value is: -9409.5"
      }
    }
    "escape SoyString in HTML contexts" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString("<h1><b>foo</b></h1>")))
        rendered must_== "Value is: &lt;h1&gt;&lt;b&gt;foo&lt;/b&gt;&lt;/h1&gt;"
      }
    }
    "not escape SoyHtml in HTML contexts" in {
      running(app) {
        val rendered = closure.render("soyrender.valueHtml", Soy.map("value" -> SoyHtml("<h1><b>foo</b></h1>")))
        rendered must_== "Value is: <h1><b>foo</b></h1>"
      }
    }
    "escape SoyString in URI contexts" in {
      running(app) {
        val rendered = closure.render("soyrender.valueUriString", Soy.map("value" -> SoyString("ben&jerry")))
        rendered must_== """<a href="http://foo.com?bar=ben%26jerry&baz=b"></a>"""
      }
    }
    "not escape SoyUri in URI contexts" in {
      running(app) {
        val rendered = closure.render("soyrender.valueUri", Soy.map("value" -> SoyUri("?bar=&baz=b")))
        rendered must_== """<a href="http://foo.com?bar=&amp;baz=b"></a>"""
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())))
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))))
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.1), SoyFloat(3.1), SoyNull))))
        rendered must_== "List is: a, 1, 2.1, 3.1, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()))
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")))
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.5, 3.5, SoyNull)))
        rendered must_== "List is: a, 1, 2.5, 3.5, UNDEFINED"
      }
    }
  }

  "Closure renderer with pre-build" should {
    "render SoyNull" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull).build)
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")).build)
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)).build)
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)).build)
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)).build)
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)).build)
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)).build)
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)).build)
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5)).build)
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0)).build)
        rendered must_== "Value is: 0"
      }
    }
    "render SoyFloat(-double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5)).build)
        rendered must_== "Value is: -9409.5"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())).build)
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))).build)
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))).build)
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.4), SoyFloat(3.4), SoyNull))).build)
        rendered must_== "List is: a, 1, 2.4, 3.4, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()).build)
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)).build)
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")).build)
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.6, 3.6, SoyNull)).build)
        rendered must_== "List is: a, 1, 2.6, 3.6, UNDEFINED"
      }
    }
  }

  "Closure renderer with Soy.map( outer type" should {
    "render SoyNull" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNull", Soy.map("value" -> SoyNull))
        rendered must_== "Value is: NULL"
      }
    }
    "render SoyString(string)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString("test string")))
        rendered must_== "Value is: test string"
      }
    }
    "render SoyString(null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueString", Soy.map("value" -> SoyString(null)))
        rendered must_== "Value is: UNDEFINED"
      }
    }
    "render SoyBoolean(true)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(true)))
        rendered must_== "Value is: TRUE"
      }
    }
    "render SoyBoolean(false)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueBoolean", Soy.map("value" -> SoyBoolean(false)))
        rendered must_== "Value is: FALSE"
      }
    }
    "render SoyInt(+int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(1984500239)))
        rendered must_== "Value is: 1984500239"
      }
    }
    "render SoyInt(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyInt(-int)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyInt(-98983423)))
        rendered must_== "Value is: -98983423"
      }
    }
    "render SoyFloat(+double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(8329.5)))
        rendered must_== "Value is: 8329.5"
      }
    }
    "render SoyFloat(0)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(0.0)))
        rendered must_== "Value is: 0"
      }
    }
    "render SoyFloat(-double)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueNumber", Soy.map("value" -> SoyFloat(-9409.5)))
        rendered must_== "Value is: -9409.5"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq())))
        rendered must_== "List is: EMPTY"
      }
    }
    "render SoyList(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyInt(1), SoyInt(2), SoyInt(3), SoyInt(4), SoyInt(5), SoyInt(6)))))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render SoyList(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyString("b"), SoyString("c")))))
        rendered must_== "List is: a, b, c"
      }
    }
    "render SoyList(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> SoyList(Seq(SoyString("a"), SoyInt(1), SoyFloat(2.2), SoyFloat(3.2), SoyNull))))
        rendered must_== "List is: a, 1, 2.2, 3.2, UNDEFINED"
      }
    }
    "render SoyList()" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list()))
        rendered must_== "List is: EMPTY"
      }
    }
    "render Soy.list(int, int, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list(1, 2, 3, 4, 5, 6)))
        rendered must_== "List is: 1, 2, 3, 4, 5, 6"
      }
    }
    "render Soy.list(string, string, ...)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", "b", "c")))
        rendered must_== "List is: a, b, c"
      }
    }
    "render Soy.list(string, int, float, double, null)" in {
      running(app) {
        val rendered = closure.render("soyrender.valueList", Soy.map("list" -> Soy.list("a", 1, 2.8, 3.8, SoyNull)))
        rendered must_== "List is: a, 1, 2.8, 3.8, UNDEFINED"
      }
    }
  }
}
