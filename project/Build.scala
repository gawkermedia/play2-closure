// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt.SbtScalariform._
import com.kinja.sbt.closuretemplates.SbtSoy._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.9-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.google.template" % "soy" % "2011-12-22"
  )

  val localSettings = scalariformSettings ++ soySettings ++ Seq(
    // Add your own project settings here
    scalaVersion := "2.10.0-RC1",
    organization := "com.kinja.play",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    resourceGenerators in Test <+= SoyKeys.soyCompiler in Test
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    localSettings : _*
  )

}
