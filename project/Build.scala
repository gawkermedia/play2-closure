// vim: sw=2 ts=2 softtabstop=2 expandtab :
import sbt._
import Keys._
import play.Project._

import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences.{ FormattingPreferences, IndentWithTabs }

import org.scalastyle.sbt.ScalastylePlugin

object ApplicationBuild extends Build {

	/**
	 * Reads a system property (-Dproperty=value JVM parameter) and converts true/falue values to boolean. If there is
	 * no such propery, the default is used.
	 * @param property Property name
	 * @param default Default value used when the property is not provided
	 * @return The property value
	 */
	private def getBooleanProperty(property: String, default: Boolean): Boolean =
		Option(System.getProperty(property)).map(_.toBoolean).getOrElse(default)

	/**
	 * Whether to format scala code or not.
	 */
	val FORMAT_CODE = getBooleanProperty("format.code", true)

	val appName				 = "play2-closure"
	val appVersion			= "0.35-2.2.1" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

	val appDependencies = Seq(
		// Add your project dependencies here,
	)

	val localSettings = customScalariformSettings ++ Seq(
		// Add your own project settings here
		libraryDependencies ++= Seq(
			("com.google.template" % "soy" % "2012-12-21").exclude("asm", "asm"),
			"soy-plugins" %% "soy-plugins" % "0.4.7-RELEASE",
			"com.kinja" %% "soy" % "0.3.0"),
		resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/",
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
		scalaVersion := "2.10.2",
		organization := "com.kinja.play",
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
		javacOptions ++= Seq("-Xlint:deprecation"),
		publishTo <<= (version)(version =>
			if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
			else														 Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
		)
	)

	/**
	 * Custom code formatting settings.
	 */
	def customScalariformSettings =
		if (FORMAT_CODE) {
			scalariformSettings ++ Seq(
				// code formatting
				ScalariformKeys.preferences := FormattingPreferences().
					setPreference(IndentWithTabs, true))
		} else Seq()

	val main = play.Project(appName, appVersion, appDependencies).settings(
		localSettings : _*
	)

}
