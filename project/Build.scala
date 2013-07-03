// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt.SbtScalariform._
import com.kinja.sbt.closuretemplates.SbtSoy._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.13-2.1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
  )

  val localSettings = scalariformSettings ++ soySettings ++ Seq(
    // Add your own project settings here
    libraryDependencies += "com.google.template" % "soy" % "2012-12-21",
    resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.10.0",
    organization := "com.kinja.play",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    resourceGenerators in Test <+= SoyKeys.soyCompiler in Test,
    publishTo <<= (version)(version =>
      if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
      else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
    )
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    localSettings : _*
  )

}
