// Comment to get more information during initialization
logLevel := Level.Warn

resolvers := Seq(
	"Kinja Public" at sys.env.getOrElse("KINJA_PUBLIC_REPO", "https://kinjajfrog.jfrog.io/kinjajfrog/sbt-virtual"),
	"sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

val credentialsFile = Path.userHome / ".ivy2" / ".kinja-artifactory.credentials"

credentials ++= (if (credentialsFile.exists) {
	Seq(Credentials(credentialsFile))
} else {
	sys.env.get("KINJA_JFROG_PASSWORD")
		.map(Credentials("Artifactory Realm", "kinjajfrog.jfrog.io", "kinja", _)).toSeq
})

// Automatic code formatting
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

// Kinja build plugin
addSbtPlugin("com.kinja.sbtplugins" %% "kinja-build-plugin" % "3.2.5")

// Use the Play sbt plugin for Play projects
// addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.4")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.1")
