import BuildOpts._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

lazy val root = crossProject(JVMPlatform, JSPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(name := "ghost", scalaVersion := commonScalaVersion)
  .settings(common)
  .settings(compileOpts)
  .settings(libraryDependencies ++= Seq(
    "com.softwaremill.sttp" %%% "core"                            % sttpVersion,
    "com.softwaremill.sttp" %%% "circe"                           % sttpVersion,
    "com.softwaremill.sttp" %% "async-http-client-backend-future" % sttpVersion,
    "com.outr"              %%% "scribe"                          % scribeVersion,
    "com.outr"              %% "scribe-slf4j"                     % scribeVersion,
    "io.circe"              %%% "circe-core"                      % circeVersion,
    "io.circe"              %%% "circe-parser"                    % circeVersion,
    "io.circe"              %%% "circe-generic"                   % circeVersion,
    "io.circe"              %%% "circe-generic-extras"            % circeVersion,
    "org.scalatest"         %%% "scalatest"                       % scalatestVersion % Test,
    "com.github.scopt"      %% "scopt"                            % scoptVersion,
    "org.xerial.snappy"     % "snappy-java"                       % snappyVersion
  ))

lazy val `spark-types` = project
  .settings(name := "ghost-spark-types", scalaVersion := "2.11.12")
  .settings(common)
  .settings(compileOpts)
  .settings(libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion)

lazy val bench = project
  .dependsOn(root.jvm)
  .settings(name := "ghost-benches", scalaVersion := commonScalaVersion)
  .settings(common)
  .settings(compileOpts)
  .enablePlugins(JmhPlugin)
