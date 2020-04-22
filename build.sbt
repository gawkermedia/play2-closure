
val playClassifier = "-play24"

name := "play2-closure" + playClassifier

organization := "com.kinja.play"

version := "2.0.0" + (if (RELEASE_BUILD) "" else "-SNAPSHOT")

crossScalaVersions := Seq("2.11.12")

scalaVersion := crossScalaVersions.value.head

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

val specs2Version = "3.7"

libraryDependencies ++= Seq(
  "com.kinja" %% "soy" % "3.0.0",
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-junit" % specs2Version % Test,
  "org.specs2" %% "specs2-mock" % specs2Version % Test,
  "org.specs2" %% "specs2-scalacheck" % specs2Version % Test
)

lazy val root = Project("play2-closure", file(".")).enablePlugins(PlayScala)
