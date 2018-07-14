
lazy val coreSettings = Seq(
  coursierParallelDownloads := 16,
  //version := use git describe. see https://github.com/sbt/sbt-git
  git.useGitDescribe := true,
  scalaVersion := D.scala,
  name := "okr",
  organization := "com.sandinh",
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-target:jvm-1.8")
)

// move the args added in play.sbt.PlaySettings.defaultSettings into application.ini
lazy val fixPlayBashSettings = Seq(
  bashScriptExtraDefines ~= { d => d.filterNot(_.contains("-Duser.dir=")) },
  javaOptions in Universal += "-Duser.dir=" + (defaultLinuxInstallLocation in Docker).value
)

lazy val playSettings = fixPlayBashSettings ++ Seq(
  libraryDependencies ++= Seq(guice, ws),
  // set Dpidfile = /dev/null to prevent error:
  // `This application is already running (Or delete RUNNING_PID file)`
  // http://stackoverflow.com/a/29244028/457612
  javaOptions in Universal += "-Dpidfile.path=/dev/null",

  //@see https://www.playframework.com/documentation/2.5.x/Deploying
  sources in (Compile, doc) := Nil,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val dockerSettings = Seq(
  maintainer := "Bui Viet Thanh <thanhbv@sandinh.net>",
  //openjdk:8-jre-alpine | gcr.io/distroless/java:debug
  dockerBaseImage := "gcr.io/distroless/java",
  dockerExposedPorts := Seq(9100),
  // https://sbt-native-packager.readthedocs.io/en/stable/formats/universal.html#skip-packagedoc-task-on-stage
  mappings in (Compile, packageDoc) := Nil,
  dockerBuildOptions += "--pull",
  //dockerEntrypoint := Seq("/usr/bin/java", "-jar", "lib/" + (artifactPath in packageJavaLauncherJar).value.getName),
) ++ DockerHelper.settings

lazy val okr = project.in(file("."))
  .enablePlugins(PlayScala, DockerPlugin, AshScriptPlugin, GitVersioning)
  .disablePlugins(PlayFilters)
  .settings(
    coreSettings ++
    D.settings ++
    playSettings ++
    dockerSettings: _*)
