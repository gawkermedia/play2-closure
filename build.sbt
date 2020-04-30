import scalariform.formatter.preferences._

val playClassifier = "-play28"
// val googleSoyVersion = "2015-04-10"
val googleSoyVersion = "2016-08-09"

name := "play2-closure" + playClassifier
organization := "com.kinja.play"
version := s"2.0.0-soy-$googleSoyVersion" + (if (RELEASE_BUILD) "" else "-SNAPSHOT")

crossScalaVersions := Seq("2.13.1")
scalaVersion := crossScalaVersions.value.head

scalacOptions ++= Seq(
  "-unchecked",                        // Show details of unchecked warnings.
  "-deprecation",                      // Show details of deprecation warnings.
  "-encoding", "UTF-8",                // Set correct encoding for Scaladoc.
  "-feature",                          // Show details of feature warnings.
  "-explaintypes",                     // Explain type errors in more detail.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint",                            // Ensure best practices are being followed.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",                  // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-dead-code",                  // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-value-discard"               // Prevent accidental discarding of results in unit functions.
)

scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, scalaMajor)) if scalaMajor >= 13 =>
    Seq(
      "-Ymacro-annotations",
      "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
      "-Ywarn-unused:locals",             // Warn if a local definition is unused.
      "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
      "-Ywarn-unused:privates",           // Warn if a private member is unused.
      "-Ywarn-extra-implicit"             // Warn when more than one implicit parameter section is defined.
    )
  case Some((2, scalaMajor)) if scalaMajor >= 12 =>
    Seq(
      "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
      "-Ywarn-unused:locals",             // Warn if a local definition is unused.
      "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
      "-Ywarn-unused:privates",           // Warn if a private member is unused.
      "-Ywarn-extra-implicit"             // Warn when more than one implicit parameter section is defined.
    )
  case _ =>
    Seq(
      // "-Xfatal-warnings",  // All warnings should result in a compiliation failure.
      "-Yno-adapted-args", // No longer needed in Scala 2.13
      "-Xfuture"           // Deprecated in Scala 2.13
    )
})

scalariformAutoformat := true
scalariformPreferences := scalariformPreferences.value
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)

val specs2Version = "4.8.3"

libraryDependencies ++= Seq(
  "com.kinja" %% "soy" % s"4.0.0-soy-$googleSoyVersion",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4",
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-junit" % specs2Version % Test,
  "org.specs2" %% "specs2-mock" % specs2Version % Test,
  "org.specs2" %% "specs2-scalacheck" % specs2Version % Test
)

lazy val root = Project("play2-closure", file(".")).enablePlugins(PlayScala)
