// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.sbt.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtScalariform._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "1.0.0-2.4.11" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

  val localSettings = scalariformSettings ++ Seq(
    version := appVersion,
    // Add your own project settings here
    libraryDependencies ++= Seq(
      "com.kinja" %% "soy" % "3.0.0",
      "org.specs2" %% "specs2-core" % "3.6.1-scalaz-7.0.6" % Test,
      "org.specs2" %% "specs2-junit" % "3.6.1-scalaz-7.0.6" % Test,
      "org.specs2" %% "specs2-mock" % "3.6.1-scalaz-7.0.6" % Test,
      "org.specs2" %% "specs2-scalacheck" % "3.6.1-scalaz-7.0.6" % Test
    ),
    resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.11.8",
    organization := "com.kinja.play",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    publishTo <<= version(version =>
      if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
      else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
    )
  )

  val main = sbt.Project(appName, file("."))
    .enablePlugins(play.sbt.PlayScala)
    .settings(localSettings : _*)

}
