import sbt._
import Keys._
import PlayProject._

import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences.{ FormattingPreferences, IndentWithTabs }

import com.kinja.play.sbt.plugin.closure.SbtSoy._

object ApplicationBuild extends Build {

    val appName         = "play2-closure"
    val appVersion      = "0.8-SNAPSHOT"

    val appDependencies = Seq(
		// Add your project dependencies here,
		"com.google.template" % "soy" % "2011-12-22"
    )

	val localSettings = Seq(
		// Add your own project settings here
		scalaVersion := "2.10.0-RC1",
		organization := "com.kinja.play",
		resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository",
		resolvers += "Gawker Public Group" at "https://vip.gawker.com/nexus/content/groups/public/",
		publishTo <<= (version)(version =>
				if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://vip.gawker.com/nexus/content/repositories/snapshots/")
				else                             Some("Gawker Releases" at "https://vip.gawker.com/nexus/content/repositories/releases/")
		),
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
	) ++ scalariformSettings ++ Seq(
		ScalariformKeys.preferences := FormattingPreferences().setPreference(IndentWithTabs, true)
	)

	val plugin = Project(
		"soy-sbt-plugin",
		file("sbt-plugin"),
		settings = Defaults.defaultSettings ++ localSettings ++ Seq(
			resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
			version := "0.3-SNAPSHOT",
			sbtPlugin := true,
			libraryDependencies := appDependencies ++ Seq(
			)
		)
	)

	val extraSettings = localSettings ++ soySettings ++ Seq(
		resourceGenerators in Test <+= SoyKeys.soyCompiler in Test
	)

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
		extraSettings : _*
    )

}
