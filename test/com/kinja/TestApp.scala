package com.kinja

import _root_.play.api.test._
import _root_.play.api.test.Helpers._

trait TestApp {

  val app = FakeApplication(
    additionalPlugins = Seq(
      "com.kinja.play.plugins.ClosurePlugin"),
    additionalConfiguration = Map(
      "closureplugin.assetPath" -> "/var/tmp/kinja-mantle",
      "closureplugin.status" -> "enabled"))
}
