name := "ghost"

version := "0.1"

scalaVersion := "2.12.4"

organization := "io.crossref"

fork := true
javaOptions ++= Seq(
  "-server",
  "-XX:NewSize=512m",
  "-XX:SurvivorRatio=6",
  "-XX:+AlwaysPreTouch",
  "-XX:+UseG1GC",
  "-XX:MaxGCPauseMillis=2000",
  "-XX:GCTimeRatio=4",
  "-XX:InitiatingHeapOccupancyPercent=30",
  "-XX:G1HeapRegionSize=8M",
  "-XX:ConcGCThreads=8",
  "-XX:G1HeapWastePercent=10",
  "-XX:+UseTLAB",
  "-XX:+ScavengeBeforeFullGC",
  "-XX:+DisableExplicitGC"
)

libraryDependencies ++= Seq(
  "org.dispatchhttp"                      %% "dispatch-core"           % "0.14.0",
  "org.dispatchhttp"                      %% "dispatch-json4s-jackson" % "0.14.0",
  "com.outr"                              %% "scribe"                  % "2.3.2",
  "com.outr"                              %% "scribe-slf4j"            % "2.3.2",
  "org.xerial.snappy"                     % "snappy-java"              % "1.1.7.1",
  "com.github.scopt"                      %% "scopt"                   % "3.7.0",
  "com.github.plokhotnyuk.jsoniter-scala" %% "macros"                  % "0.24.0"
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
