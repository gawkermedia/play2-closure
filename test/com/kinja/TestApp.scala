package com.kinja

import _root_.play.api.inject.guice.GuiceApplicationBuilder
import _root_.play.api.inject.bind
import com.kinja.play.{ ClosureComponent, ClosureComponentImpl }

trait TestApp {

  val app = new GuiceApplicationBuilder()
    .configure(
      "closureplugin.assetPath" -> "/var/tmp/kinja-mantle",
      "closureplugin.plugins" -> Seq(
        "com.google.template.soy.SoyModule"))
    .bindings(bind[ClosureComponent].to[ClosureComponentImpl])
    .build()

  def closure: ClosureComponent = app.injector.instanceOf(classOf[ClosureComponent])
}
