import sbt._
import Keys._

object Dependencies {

  lazy val version = new {
    val smithy4s = "0.18.23"
    val alloy = "0.3.11"
  }

  val production: Seq[ModuleID] = Seq(
    "com.disneystreaming.alloy" % "alloy-core" % version.alloy,
    "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % version.smithy4s,
    "com.disneystreaming.smithy4s" %% "smithy4s-json" % version.smithy4s,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.7" % "provided"
  )

}
