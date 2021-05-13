val projectVersion = "0.1.0-SNAPSHOT"
val scala2Version = "2.13.4"
val scala3Version = "3.0.0-RC3"
val zioVersion = "1.0.7"
val org = "io.github.pme123"


lazy val root = project
  .in(file("."))
  .settings()
  .aggregate(dsl, exampleTwitterServer, exampleTwitterBpmn)

lazy val dsl = project
  .in(file("./dsl"))
  .settings(
    name := "camundala-dsl",
    organization := org,
    scalaVersion := scala3Version,
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

// EXAMPLES
val springBootVersion = "2.4.4"
val camundaSpringBootStarterVersion = "7.15.0"
val h2Version = "1.4.200"
// Twitter
val twitter4jVersion = "4.0.7"

lazy val exampleTwitterServer = project
  .in(file("./examples/twitter/server"))
  .settings(
    name := "example-twitter-server",
    organization := org,
    scalaVersion := scala3Version,
    version := projectVersion,

    libraryDependencies ++= Seq(
      "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion,
      "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion,
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaSpringBootStarterVersion,
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion,
      "com.h2database" % "h2" % h2Version
    )
  ).dependsOn(dsl)

lazy val exampleTwitterBpmn = project
  .in(file("./examples/twitter/bpmn"))
  .settings(
    name := "example-twitter-bpmn",
    organization := org,
    scalaVersion := scala3Version,
    version := projectVersion,
  ).dependsOn(exampleTwitterServer)