addSbtPlugin(
  ("com.geirsson" % "sbt-scalafmt" % "1.5.1").cross(CrossVersion.full)
)
addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % "0.18.23"
)