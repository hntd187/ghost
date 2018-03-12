name := "ghost"

version := "0.1"

scalaVersion := "2.12.4"

organization := "io.crossref"

libraryDependencies ++= Seq(
  "org.dispatchhttp"  %% "dispatch-core"           % "0.14.0",
  "org.dispatchhttp"  %% "dispatch-json4s-jackson" % "0.14.0",
  "com.outr"          %% "scribe"                  % "2.2.0",
  "com.outr"          %% "scribe-slf4j"            % "2.2.0",
  "org.xerial.snappy" % "snappy-java"              % "1.1.7.1"
)

scalafmtOnCompile in ThisBuild := true

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
