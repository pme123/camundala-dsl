package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import zio.console.*
import zio.*
import scala.language.postfixOps

case class BpmnRunner(runnerConfig: RunnerConfig)
    extends FromCamundaBpmn,
      CompareBpmns,
      ToCamundaBpmn,
      DslPrinter,
      DSL:

  def run() =
    for {
      _ <- putStrLn("Start Bpmn Runner")
      _ <- DslPrinterRunner(
        runnerConfig
      ).run()
      audit <- UIO(runnerConfig.bpmnsConfig.compareWith(bpmnsConfig))
      _ <- putStrLn(audit.log(AuditLevel.WARN))
      _ <- runnerConfig.toCamunda()
      _ <- putStrLn(s"Generated BPMN to ${runnerConfig.generatedFolder}")
    } yield ()

case class RunnerConfig(
    projectName: String,
    cawemoFolder: BpmnPath,
    withIdFolder: BpmnPath,
    generatedFolder: BpmnPath,
    bpmnsConfig: BpmnsConfig
)

object RunnerConfig extends DSL:

  final val cawemoFolder = "cawemo"

  final val withIdFolder = "cawemo/with-ids"

  final val generatedFolder = "src/main/resources"

  def apply(projectName: String, baseFolder: String = "."): RunnerConfig =
    RunnerConfig(
      projectName,
      path(s"$baseFolder/$cawemoFolder"),
      path(s"$baseFolder/$withIdFolder"),
      path(s"$baseFolder/$generatedFolder"),
      bpmnsConfig
    )
