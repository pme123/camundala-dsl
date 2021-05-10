val scala2Version = "2.13.4"
val scala3Version = "3.0.0-RC3"
val zioVersion = "1.0.7"

lazy val root = project
  .in(file("."))
  .settings(
    name := "camundala-dsl",
    version := "0.1.0",

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
