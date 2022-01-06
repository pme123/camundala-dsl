import sbt.url

lazy val projectVersion = scala.io.Source.fromFile("version").mkString.trim
val scala2Version = "2.13.7"
val scala3Version = "3.1.0"
val zioVersion = "1.0.8"
val org = "io.github.pme123"

ThisBuild / versionScheme := Some("semver-spec")

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
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
  .configure(preventPublication)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ) ++ camundaTestDependencies ++
      tapirDependencies,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    // libraryDependencies += "eu.timepit" %% "refined" % "0.9.20",
    // To cross compile with Dotty and Scala 2
  )

val tapirVersion = "0.18.3"
val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.quicklens" %% "quicklens" % "1.7.5", // simple modifying case classes
  "org.latestbit" %% "circe-tagged-adt-codec" % "0.10.0", // to encode enums
  "com.lihaoyi" %% "os-lib" % "0.8.0",
  "org.planet42" %% "laika-core" % "0.18.0"
)
lazy val api = project
  .in(file("./api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(
    publishArtifact := true,
    libraryDependencies ++=
      tapirDependencies ++
        camundaTestDependencies ++
        gatlingDependencies,
    // To cross compile with Dotty and Scala 2
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )
//.enablePlugins(JavaAppPackaging)

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
val camundaTestDependencies = Seq(
  // provide Camunda interaction
  "org.camunda.bpm" % "camunda-engine" % camundaVersion % Provided,
  //
  //"org.camunda.bpm.dmn" % "camunda-engine-dmn" % camundaVersion % Provided,
  // provide test helper
  "org.camunda.bpm.assert" % "camunda-bpm-assert" % "10.0.0",
  "org.assertj" % "assertj-core" % "3.19.0",
  "org.camunda.bpm.extension" % "camunda-bpm-assert-scenario" % "1.1.1",
  "org.camunda.bpm.extension.mockito" % "camunda-bpm-mockito" % "5.14.0",
  // dmn testing
  //("org.camunda.bpm.extension.dmn.scala" % "dmn-engine" % "1.7.2-SNAPSHOT").cross(CrossVersion.for3Use2_13),
  "de.odysseus.juel" % "juel" % "2.1.3",
  //     "org.scalactic" %% "scalactic" % "3.2.9",
  //     "org.scalatest" %% "scalatest" % "3.2.9",
  //     "org.mockito" % "mockito-scala-scalatest_2.13" % "1.16.37",
  "org.mockito" % "mockito-core" % "3.1.0",
  "com.novocode" % "junit-interface" % "0.11"
)

val gatlingDependencies = Seq(
 "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.7.2",
 "io.gatling" % "gatling-test-framework" % "3.7.2"
).map(_.excludeAll(ExclusionRule("com.softwaremill.quicklens")))


lazy val exampleTwitter = project
  .in(file("./examples/twitter"))
  .settings(projectSettings("example-twitter"))
  .configure(preventPublication)
  .settings(
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(dsl, api)
  .enablePlugins(GatlingPlugin)

lazy val exampleInvoice = project
  .in(file("./examples/invoice"))
  .settings(projectSettings("example-invoice"))
  .configure(preventPublication)
  .settings(
    // for invoice-example
    resolvers += "Sonatype OSS Camunda" at "https://app.camunda.com/nexus/content/repositories/camunda-bpm/",
    libraryDependencies ++= camundaDependencies
    // https://mvnrepository.com/artifact/org.camunda.bpm.example/camunda-example-invoice
    // libraryDependencies += "org.camunda.bpm.example" % "camunda-example-invoice" % camundaVersion % Test
  )
  .dependsOn(dsl, api)
  .enablePlugins(GatlingPlugin)

lazy val developerList = List(
  Developer(
    id = "pme123",
    name = "Pascal Mengelt",
    email = "pascal.mengelt@gmail.com",
    url = url("https://github.com/pme123")
  )
)
/*
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
  developers := developerList
))
 */

lazy val publicationSettings: Project => Project = _.settings(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/pme123/camundala-dsl")),
  startYear := Some(2021),
  logLevel := Level.Debug,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/pme123/camundala-dsl"),
      "scm:git:github.com:/pme123/camundala-dsl"
    )
  ),
  developers := developerList
)

lazy val preventPublication: Project => Project =
  _.settings(
    publish := {},
    publishTo := Some(
      Resolver
        .file("Unused transient repository", target.value / "fakepublish")
    ),
    publishArtifact := false,
    publishLocal := {},
    packagedArtifacts := Map.empty
  ) // doesn't work - https://github.com/sbt/sbt-pgp/issues/42
