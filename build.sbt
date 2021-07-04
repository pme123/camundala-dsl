val projectVersion = "0.1.0-SNAPSHOT"
val scala2Version = "2.13.4"
val scala3Version = "3.0.1-RC2"
val zioVersion = "1.0.8"
val org = "io.github.pme123"

lazy val root = project
  .in(file("."))
  .settings(
    name := "camundala"
  )
  .aggregate(dsl, exampleTwitter, exampleInvoice)

def projectSettings(projName: String): Seq[Def.Setting[_]] = Seq(
  name := s"camundala-$projName",
  organization := org,
  scalaVersion := scala3Version,
  version := projectVersion
)

lazy val dsl = project
  .in(file("./dsl"))
  .settings(projectSettings("dsl"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "org.camunda.bpm.model" % "camunda-bpmn-model" % camundaVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    // libraryDependencies += "eu.timepit" %% "refined" % "0.9.20",
    // To cross compile with Dotty and Scala 2
    // crossScalaVersions := Seq(scala3Version, scala2Version)
  )

// EXAMPLES
val springBootVersion = "2.4.4"
val camundaVersion = "7.15.0"
val h2Version = "1.4.200"
// Twitter
val twitter4jVersion = "4.0.7"
val camundaDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion,
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
  "com.h2database" % "h2" % h2Version,
  "org.camunda.bpm.assert" % "camunda-bpm-assert" % "10.0.0" % Test,
  "org.assertj" % "assertj-core" % "3.19.0" % Test,
  "org.mockito" % "mockito-core" % "3.1.0" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

lazy val exampleTwitter = project
  .in(file("./examples/twitter"))
  .settings(projectSettings("example-twitter"))
  .settings(
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(dsl)

lazy val exampleInvoice = project
  .in(file("./examples/invoice"))
  .settings(projectSettings("example-invoice"))
  .settings(
    // for invoice-example
    resolvers += "Sonatype OSS Camunda" at "https://app.camunda.com/nexus/content/repositories/camunda-bpm/",
    libraryDependencies ++= camundaDependencies,
      // https://mvnrepository.com/artifact/org.camunda.bpm.example/camunda-example-invoice
     // libraryDependencies += "org.camunda.bpm.example" % "camunda-example-invoice" % camundaVersion % Test
  )
  .dependsOn(dsl)
