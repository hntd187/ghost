lazy val common = Seq(
  version                        := "0.1",
  scalaVersion                   := "2.12.5",
  organization                   := "io.crossref",
  scalafmtVersion                := "1.4.0",
  fork                           := true,
  scalafmtOnCompile in ThisBuild := true,
  scalaModuleInfo ~= (_.map(_.withOverrideScalaVersion(true)))
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

lazy val root = project
  .in(file("."))
  .settings(name := "ghost")
  .settings(common)
  .settings(compileOpts)
  .settings(libraryDependencies ++= Seq(
    "org.dispatchhttp"                      %% "dispatch-core"           % "0.14.0",
    "org.dispatchhttp"                      %% "dispatch-json4s-jackson" % "0.14.0",
    "com.outr"                              %% "scribe"                  % "2.3.2",
    "com.outr"                              %% "scribe-slf4j"            % "2.3.2",
    "com.github.scopt"                      %% "scopt"                   % "3.7.0",
    "com.github.plokhotnyuk.jsoniter-scala" %% "macros"                  % "0.24.1",
    "org.xerial.snappy"                     % "snappy-java"              % "1.1.7.1"
  ))

lazy val bench = project
  .dependsOn(root)
  .settings(name := "ghost-benches")
  .settings(common)
  .settings(compileOpts)
  .enablePlugins(JmhPlugin)
