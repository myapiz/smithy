lazy val versions = new {
  val smithy4s = "0.18.23"
  val alloy = "0.3.11"
}

val production: Seq[ModuleID] = Seq(
  "com.disneystreaming.alloy" % "alloy-core" % versions.alloy,
  "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % versions.smithy4s,
  "com.disneystreaming.smithy4s" %% "smithy4s-json" % versions.smithy4s,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.7" % "provided"
)

val settings = Seq(
  organization := "com.myapiz",
  version := "0.0.1" + sys.props
    .getOrElse("buildNumber", default = "0-SNAPSHOT"),
  scalaVersion := "3.4.2",
  libraryDependencies ++= production,
  Test / publishArtifact := false
)

val publishSettings = Seq(
  publishMavenStyle := true,
  githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
  githubOwner := "myapiz",
  githubRepository := "smithy"
)

lazy val model = (project in file("model"))
  .settings(
    Seq(
      version := "0.0.1" + sys.props
        .getOrElse("buildNumber", default = "0-SNAPSHOT"),
      scalaVersion := "3.4.2",
      autoScalaLibrary := false
    ) ++ publishSettings
  )

lazy val smithy4s =
  (project in file("smithy4s"))
    .enablePlugins(Smithy4sCodegenPlugin)
    .dependsOn(model)
    .settings(settings ++ publishSettings)
    .configs(Test)

lazy val root = (project in file("."))
  .aggregate(model, smithy4s)
