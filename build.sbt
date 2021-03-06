import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType

ThisBuild / baseVersion := "0.1"

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / publishGithubUser := "mpilquist"
ThisBuild / publishFullName := "Michael Pilquist"

// ThisBuild / crossScalaVersions := List("3.0.0-M3", "3.0.0-RC1", "2.12.13", "2.13.5")
ThisBuild / crossScalaVersions := List("2.12.13", "2.13.5", "3.0.0-M3", "3.0.0-RC1")

ThisBuild / spiewakCiReleaseSnapshots := true

ThisBuild / spiewakMainBranches := List("main")

ThisBuild / homepage := Some(url("https://github.com/typelevel/literally"))

ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/typelevel/literally"),
    "git@github.com:typelevel/literally.git"
  )
)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js)
  .enablePlugins(NoPublishPlugin, SonatypeCiReleasePlugin)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(
    name := "literally",
    scalacOptions := scalacOptions.value.filterNot(_ == "-source:3.0-migration"),
    Compile / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
      )
    },
    Test / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + major))
      )
    }
  )
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .settings(
    libraryDependencies ++= {
      if (isDotty.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    }
  )

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core % "test->test")
  .settings(
    name := "tests",
    Compile / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
      )
    },
    Test / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + major))
      )
    },
    githubWorkflowArtifactUpload := false
  )
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .settings(
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.22" % Test
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )
