// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtScalariform._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.49-2.3.4" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

  val localSettings = scalariformSettings ++ Seq(
    version := appVersion,
    // Add your own project settings here
    libraryDependencies ++= Seq(
      ("com.google.template" % "soy" % "2012-12-21").exclude("asm", "asm"),
      "com.kinja" %% "soy" % "0.4.4"),
    resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.10.4",
    organization := "com.kinja.play",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    publishTo <<= (version)(version =>
      if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
      else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
    )
  )

  val main = sbt.Project(appName, file("."))
    .enablePlugins(play.PlayScala)
    .settings(localSettings : _*)

}
