import sbt.url

val projectVersion = "0.1.0-SNAPSHOT"
val scala2Version = "2.13.4"
val scala3Version = "3.0.2"
val zioVersion = "1.0.8"
val org = "io.github.pme123"

lazy val root = project
  .in(file("."))
  .settings(
    name := "camundala"
  )
  .aggregate(dsl, api, exampleTwitter, exampleInvoice)

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
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      // provide Camunda interaction
      "org.camunda.bpm" % "camunda-engine" % camundaVersion % Provided,
      // provide test helper
      "org.camunda.bpm.assert" % "camunda-bpm-assert" % "10.0.0",
      "org.assertj" % "assertj-core" % "3.19.0",
      //     "org.scalactic" %% "scalactic" % "3.2.9",
      //     "org.scalatest" %% "scalatest" % "3.2.9",
      //     "org.mockito" % "mockito-scala-scalatest_2.13" % "1.16.37",
      "org.mockito" % "mockito-core" % "3.1.0",
      "com.novocode" % "junit-interface" % "0.11"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    // libraryDependencies += "eu.timepit" %% "refined" % "0.9.20",
    // To cross compile with Dotty and Scala 2
  )

val tapirVersion = "0.18.3"
lazy val api = project
  .in(file("./api"))
  .settings(projectSettings("api"))
  .settings(
    publishArtifact := true,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "org.latestbit" %% "circe-tagged-adt-codec" % "0.10.0", // to encode enums
      "com.lihaoyi" %% "os-lib" % "0.7.8",
      "org.planet42" %% "laika-core" % "0.18.0"
    ),
    // To cross compile with Dotty and Scala 2
    scalacOptions ++= Seq(
      "-Xmax-inlines", "50"
    )
  ).enablePlugins(JavaAppPackaging)


// EXAMPLES
val springBootVersion = "2.4.4"
val camundaVersion = "7.15.0"
val h2Version = "1.4.200"
// Twitter
val twitter4jVersion = "4.0.7"
val camundaDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion,
  "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
  "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % "1.11.0",
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest" % camundaVersion,
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
  "com.h2database" % "h2" % h2Version
)

lazy val exampleTwitter = project
  .in(file("./examples/twitter"))
  .settings(projectSettings("example-twitter"))
  .settings(
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(dsl, api)

lazy val exampleInvoice = project
  .in(file("./examples/invoice"))
  .settings(projectSettings("example-invoice"))
  .settings(
    // for invoice-example
    resolvers += "Sonatype OSS Camunda" at "https://app.camunda.com/nexus/content/repositories/camunda-bpm/",
    libraryDependencies ++= camundaDependencies
    // https://mvnrepository.com/artifact/org.camunda.bpm.example/camunda-example-invoice
    // libraryDependencies += "org.camunda.bpm.example" % "camunda-example-invoice" % camundaVersion % Test
  )
  .dependsOn(dsl, api)

// https://github.com/djspiewak/sbt-github-actions
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))

ThisBuild / crossScalaVersions := Seq(scala3Version, scala2Version)

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

inThisBuild(List(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/pme123/camundala-dsl")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/pme123/camunda-dsl"),
      "scm:git:github.com:/pme123/camunda-dsl"
    )
  ),
  developers := List(
    Developer(
      id    = "pme123",
      name  = "Pascal Mengelt",
      email = "pascal.mengelt@gmail.com",
      url   = url("https://github.com/pme123")
    )
  )
))