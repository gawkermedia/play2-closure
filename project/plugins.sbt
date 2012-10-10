// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Gawker Public Group" at "https://vip.gawker.com/nexus/content/groups/public"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.0.4")

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

addSbtPlugin("com.kinja.play" % "soy-sbt-plugin" % "0.3-SNAPSHOT")
