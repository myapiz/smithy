import Dependencies._
import Settings._

lazy val model = (project in file("model"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(Settings.settings: _*)

lazy val smithy4s =
  (project in file("smithy4s"))
    .dependsOn(model)
    .settings(Settings.settings: _*)
    .configs(Test)
