// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt.SbtScalariform._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.31-2.2.1-" + {if (System.getProperty("JENKINS_BUILD") == null) "SNAPSHOT" else "RELEASE"}

  val appDependencies = Seq(
    // Add your project dependencies here,
  )

  val localSettings = scalariformSettings ++ Seq(
    // Add your own project settings here
    libraryDependencies ++= Seq(
      ("com.google.template" % "soy" % "2012-12-21").exclude("asm", "asm"),
      "soy-plugins" %% "soy-plugins" % "0.4.6-RELEASE",
      "com.kinja" %% "soy" % "0.2.1-RELEASE"),
    resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.10.2",
    organization := "com.kinja.play",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    publishTo <<= (version)(version =>
      if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
      else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
    )
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    localSettings : _*
  )

}
