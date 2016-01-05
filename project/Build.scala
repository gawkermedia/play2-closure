// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtScalariform._

object ApplicationBuild extends Build {

  val appName         = "play2-closure"
  val appVersion      = "0.56-2.3.9" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

  val localSettings = scalariformSettings ++ Seq(
    version := appVersion,
    // Add your own project settings here
    libraryDependencies ++= Seq(
      ("com.kinja" %% "soy" % "2.2.0")),
    resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    crossScalaVersions := Seq("2.10.4", "2.11.6"),
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
