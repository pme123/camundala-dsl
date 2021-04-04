package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import zio.console.*
import zio.*

case class BpmnRunner(config: RunnerConfig)
  extends FromCamundaBpmn
    with ToCamundaBpmn :

  def run() =
    for {
      _ <- putStrLn("Start Bpmn Runner")
      bpmn <- fromCamunda()
      _ <- putStrLn(s"Imported BPMN ${bpmn.stringify()}")
    } yield ()

  def fromCamunda(): IO[FromCamundaException, Bpmn] =
    fromCamunda(config.bpmnPath, config.withIdsBpmnPath)

case class RunnerConfig(bpmnPath: BpmnPath, withIdsBpmnPath: BpmnPath)

object BpmnRunnerApp
  extends zio.App
    with DSL {

  def run(args: List[String]) =
    runnerLogic.exitCode

  val runnerLogic =
    BpmnRunner(RunnerConfig(path("bpmns/process-cawemo.bpmn"), path("bpmns/with-ids/process-cawemo.bpmn"))).run()
}