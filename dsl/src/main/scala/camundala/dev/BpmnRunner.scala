package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import zio.console.*
import zio.*
import scala.language.postfixOps

case class BpmnRunner(runnerConfig: RunnerConfig)
    extends FromCamundaBpmn
    with CompareBpmns
    with ToCamundaBpmn
    with DslPrinter:

  def run() =
    for {
      _ <- putStrLn("Start Bpmn Runner")
      bpmnsConfig <- fromCamunda(runnerConfig)
      newRunnerConfig = runnerConfig.copy(bpmnsConfig = bpmnsConfig)
      _ <- putStrLn(newRunnerConfig.print().asString(0))
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
