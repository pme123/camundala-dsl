val scala2Version = "2.13.4"
val scala3Version = "3.0.0-M3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-cross",
    version := "0.1.0",

    libraryDependencies += "org.camunda.bpm.model" % "camunda-bpmn-model" % "7.14.0",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
      // libraryDependencies += "eu.timepit" %% "refined" % "0.9.20",
    // To make the default compiler and REPL use Dotty
    scalaVersion := scala3Version,

    // To cross compile with Dotty and Scala 2
    crossScalaVersions := Seq(scala3Version, scala2Version)
  )
