package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import zio.console.*
import zio.*

case class BpmnRunner(config: RunnerConfig)
  extends FromCamundaBpmn
    with CompareBpmns
    with ToCamundaBpmn :

  def run() =
    for {
      _ <- putStrLn("Start Bpmn Runner")
      bpmn <- fromCamunda()
      _ <- putStrLn(bpmn.print())
      audit <- UIO(config.workingBpmnDsl.compareWith(bpmn))
      _ <- putStrLn(audit.log(AuditLevel.WARN))
    } yield ()

  def fromCamunda(): IO[FromCamundaException, Bpmn] =
    fromCamunda(config.bpmnPath, config.withIdsBpmnPath)

case class RunnerConfig(
                         bpmnPath: BpmnPath,
                         withIdsBpmnPath: BpmnPath,
                         workingBpmnDsl: Bpmn)

object BpmnRunnerApp
  extends zio.App
    with DSL {

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("bpmns/process-cawemo.bpmn"),
        path("bpmns/with-ids/process-cawemo.bpmn"),
        demoBpmn
      )).run()

  private val fooVar: ProcessVarString = ProcessVarString("fooVar")

  private val sequenceFlowIsNotBar = sequenceFlow("flowIsNotBar")

  val demoBpmn =
    bpmn("bpmns/process-cawemo.bpmn")
      .processes(
        process("TestDSLProcess")
          .starterGroups(

          )
          .starterUsers(

          )
          .elements(
            startEvent("StartProcess"),
            userTask("UserTaskA"),
            userTask("UserTaskB"),
            scriptTask("ScriptTask")
              .inlineGroovy(""""""),
            exclusiveGateway("Fork"),
            exclusiveGateway("gatewayJoin"),
            endEvent("EndProcess"),
            sequenceFlow("flow1_StartProcess-ServiceTask"),
            sequenceFlow("flow2_ServiceTask-Fork"),
            sequenceFlow("IsNOTBar_Fork-UserTaskA"),
            sequenceFlow("IsBar_Fork-UserTaskB"),
            sequenceFlow("flow6_UserTaskB-gatewayJoin"),
            sequenceFlow("flow5_UserTaskA-gatewayJoin"),
            sequenceFlow("flow7_gatewayJoin-ScriptTask"),
            sequenceFlow("SequenceFlow_9_ScriptTask-EndProcess")
          )
      )
}