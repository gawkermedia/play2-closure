// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Gawker Public Group" at "https://vip.gawker.com/nexus/content/groups/public"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-RC2")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.0.0")

addSbtPlugin("com.kinja.sbt" % "sbt-closure-templates" % "0.2-SNAPSHOT")
