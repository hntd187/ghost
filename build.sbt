import BuildOpts._

lazy val root = project
  .in(file("."))
  .settings(name := "ghost", scalaVersion := commonScalaVersion)
  .settings(common)
  .settings(compileOpts)
  .settings(libraryDependencies ++= Seq(
    "org.dispatchhttp"                      %% "dispatch-core"           % dispatchVersion,
    "org.dispatchhttp"                      %% "dispatch-json4s-jackson" % dispatchVersion,
    "com.outr"                              %% "scribe"                  % scribeVersion,
    "com.outr"                              %% "scribe-slf4j"            % scribeVersion,
    "com.github.scopt"                      %% "scopt"                   % scoptVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "macros"                  % jsoniterVersion,
    "org.xerial.snappy"                     % "snappy-java"              % snappyVersion
  ))

lazy val `spark-types` = project
  .settings(name := "ghost-spark-types", scalaVersion := "2.11.12")
  .settings(common)
  .settings(compileOpts)
  .settings(libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion)

lazy val bench = project
  .dependsOn(root)
  .settings(name := "ghost-benches", scalaVersion := commonScalaVersion)
  .settings(common)
  .settings(compileOpts)
  .enablePlugins(JmhPlugin)
