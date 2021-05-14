package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import zio.console.*
import zio.*
import scala.language.postfixOps

case class BpmnRunner(config: RunnerConfig)
    extends FromCamundaBpmn
    with CompareBpmns
    with ToCamundaBpmn
    with DslPrinter:

  def run() =
    for {
      _ <- putStrLn("Start Bpmn Runner")
      bpmn <- fromCamunda()
      _ <- putStrLn(bpmn.print().asString(0))
      audit <- UIO(config.workingBpmnDsl.compareWith(bpmn))
      _ <- putStrLn(audit.log(AuditLevel.WARN))
    // at the moment Printer only for the import from Cawemo _ <- putStrLn("BPMN DSL:\n" + config.workingBpmnDsl.print().asString(0))
      _ <- config.workingBpmnDsl.toCamunda(config.generatedBpmnPath)
      _ <- putStrLn(s"Generated BPMN to ${config.generatedBpmnPath}")
    } yield ()

  def fromCamunda(): IO[FromCamundaException, Bpmn] =
    fromCamunda(config.bpmnPath, config.workingBpmnDsl.path)

case class RunnerConfig(
    bpmnPath: BpmnPath,
    workingBpmnDsl: Bpmn,
    generatedBpmnPath: BpmnPath
)


