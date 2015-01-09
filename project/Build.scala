// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtScalariform._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.43-2.3.4" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

  val localSettings = scalariformSettings ++ Seq(
    version := appVersion,
    // Add your own project settings here
    libraryDependencies ++= Seq(
	  "com.google.inject" % "guice" % "3.0",
	  "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
	  "com.google.inject.extensions" % "guice-multibindings" % "3.0",
	  "com.google.guava" % "guava" % "17.0",
      "com.kinja" %% "soy" % "0.3.2"),
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
