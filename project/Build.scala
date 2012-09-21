import sbt._
import Keys._
import PlayProject._

import com.typesafe.sbtscalariform.ScalariformPlugin._
import scalariform.formatter.preferences._

object ApplicationBuild extends Build {

    val appName         = "play2-closure"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
		// Add your project dependencies here,
		"com.google.template" % "soy" % "2011-12-22"
    )

	val localSettings = Seq(
		// Add your own project settings here      
		organization := "com.kinja",
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

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
		localSettings : _*
    )

}
