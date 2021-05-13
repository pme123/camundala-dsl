val projectVersion = "0.1.0-SNAPSHOT"
val scala2Version = "2.13.4"
val scala3Version = "3.0.0-RC3"
val zioVersion = "1.0.7"
val org = "io.github.pme123"

lazy val root = project
  .in(file("."))
  .aggregate(dsl, exampleTwitter)

lazy val dsl = project
  .in(file("./dsl"))
  .settings(
    organization := org,
    name := "camundala-dsl",
    version := projectVersion,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "org.camunda.bpm.model" % "camunda-bpmn-model" % "7.14.0",
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    // libraryDependencies += "eu.timepit" %% "refined" % "0.9.20",
    // To make the default compiler and REPL use Dotty
    scalaVersion := scala3Version,

    // To cross compile with Dotty and Scala 2
    crossScalaVersions := Seq(scala3Version, scala2Version)
  )

lazy val exampleTwitter = project
  .in(file("./examples/twitter/bpmn"))
  .settings(
    organization := org,
    name := "example-twitter",
    version := projectVersion,

    scalaVersion := scala3Version,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  ).dependsOn(dsl)