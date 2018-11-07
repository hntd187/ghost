import sbt._
import Keys._

object BuildOpts {

  lazy val commonScalaVersion = "2.12.7"
  lazy val dispatchVersion    = "0.14.0"
  lazy val scoptVersion       = "3.7.0"
  lazy val jsoniterVersion    = "0.28.1"
  lazy val snappyVersion      = "1.1.7.2"
  lazy val sparkVersion       = "2.3.0"
  lazy val scalatestVersion = "3.0.5"
  lazy val sttpVersion      = "1.3.5"
  lazy val circeVersion     = "0.10.0"
  lazy val scribeVersion    = "2.6.0"

  lazy val common = Seq(
    version                        := "0.1",
    organization                   := "io.crossref",
    mainClass := Some("io.crossref.request.te"),
    fork                           := false,
  )

  lazy val compileOpts = Seq(
    javaOptions ++= Seq(
      "-server",
      "-Xms5g",
      "-Xmx5g",
      "-XX:+AlwaysPreTouch",
      "-XX:+UseG1GC"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-encoding",
      "utf-8",
      "-deprecation",
      "-explaintypes",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-unchecked"
    )
  )

}
