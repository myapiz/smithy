import sbt._
import Keys._

object Settings {

  lazy val settings = Seq(
    organization := "com.myapiz",
    version := "0.0.1" + sys.props
      .getOrElse("buildNumber", default = "0-SNAPSHOT"),
    scalaVersion := "3.4.2",
    publishMavenStyle := true,
    libraryDependencies ++= Dependencies.production,
    Test / publishArtifact := false
  )

  lazy val testSettings = Seq(
    Test / fork := false
  )

}
