import sbt._
import Keys._

import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

object BuildOpts {

  lazy val commonScalaVersion = "2.12.5"
  lazy val dispatchVersion    = "0.14.0"
  lazy val scribeVersion      = "2.3.2"
  lazy val scoptVersion       = "3.7.0"
  lazy val jsoniterVersion    = "0.24.1"
  lazy val snappyVersion      = "1.1.7.1"
  lazy val sparkVersion       = "2.3.0"

  lazy val common = Seq(
    version                        := "0.1",
    organization                   := "io.crossref",
    scalafmtVersion                := "1.4.0",
    fork                           := true,
    scalafmtOnCompile in ThisBuild := true
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
