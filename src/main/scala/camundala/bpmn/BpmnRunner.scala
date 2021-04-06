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

object BpmnRunnerApp
  extends zio.App
    with DSL {

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("bpmns/process-cawemo.bpmn"),
        demoBpmn,
        path("camunda-demo/src/main/resources/demo-process.bpmn")
      )).run()

  val isBarVar = "isBar"
  val demoBpmn =
    bpmn("bpmns/with-ids/process-cawemo.bpmn")
      .processes(
        process("TestDSLProcess")
          .starterGroups(

          )
          .starterUsers(

          )
          .elements(
            startEvent("StartProcess"),
            serviceTask("ServiceTask")
              .expression(s"execution.setVariable('$isBarVar', true)"),
            userTask("UserTaskA"),
            userTask("UserTaskB"),
            scriptTask("ScriptTask")
              .inlineGroovy("""println "hello there" """),
            exclusiveGateway("Fork"),
            exclusiveGateway("gatewayJoin"),
            endEvent("EndProcess"),
            sequenceFlow("flow1_StartProcess-ServiceTask"),
            sequenceFlow("flow2_ServiceTask-Fork"),
            sequenceFlow("IsNOTBar_Fork-UserTaskA")
            .expression(s"!$isBarVar"),
            sequenceFlow("IsBar_Fork-UserTaskB")
            .inlineGroovy(isBarVar),
            sequenceFlow("flow6_UserTaskB-gatewayJoin"),
            sequenceFlow("flow5_UserTaskA-gatewayJoin"),
            sequenceFlow("flow7_gatewayJoin-ScriptTask"),
            sequenceFlow("SequenceFlow_9_ScriptTask-EndProcess")
          )
      )
}