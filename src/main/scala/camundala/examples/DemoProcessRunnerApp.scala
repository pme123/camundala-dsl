package camundala.examples

import camundala.bpmn.*
import camundala.dsl.DSL

object DemoProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("bpmns/process-cawemo.bpmn"),
        demoProcess.demoBpmn,
        path("camunda-demo/src/main/resources/demo-process.bpmn")
      )
    ).run()

object demoProcess extends DSL :
  val admin = group("admin")
    .name("Administrator")
    .groupType("system")
  val adminUser = user("admin")
    .name("Administrator")
    .firstName("-")
    .email("myEmail@email.ch")
    .group(admin.ref)

  val isBarVar = "isBar"
  lazy val demoBpmn =
    bpmn("bpmns/with-ids/process-cawemo.bpmn")
      .processes(
        process("TestDSLProcess")
          .starterGroup(
            admin.ref
          )
          .starterUser(
            adminUser.ref
          )
          .nodes(
            startEvent("StartProcess"),
            serviceTask("ServiceTask")
              .expression(s"execution.setVariable('$isBarVar', true)")
              .inputString("in1", "value1")
              .inputExpression("myBoolean", "true")
              .inputGroovy("groovyRef", "myGroovy.groovy")
              .inputGroovyInline("additon", "1 + 3"),
            userTask("UserTaskA")
              .form(formKey("my-form-key")),
            userTask("UserTaskB")
              .form(
                textField("name")
                  .label("Name")
                  .required
                  .minlength(3),
                textField("firstName")
                  .label("First Name")
              ),
            scriptTask("ScriptTask")
              .inlineGroovy("""println "hello there" """),
            exclusiveGateway("Fork").asyncAfter,
            exclusiveGateway("gatewayJoin"),
            endEvent("EndProcess")
          )
          .flows(
            sequenceFlow("IsNOTBar_Fork-UserTaskA")
              .expression(s"!$isBarVar"),
            sequenceFlow("IsBar_Fork-UserTaskB")
              .inlineGroovy(isBarVar),
            sequenceFlow("flow1_StartProcess-ServiceTask"),
            sequenceFlow("flow2_ServiceTask-Fork"),
            sequenceFlow("flow6_UserTaskB-Asdfdsf"),
            sequenceFlow("flow5_UserTaskA-gatewayJoin"),
            sequenceFlow("flow7_gatewayJoin-ScriptTask"),
            sequenceFlow("SequenceFlow_9_ScriptTask-EndProcess"),
            sequenceFlow("Flow_1i69u7d_Asdfdsf-gatewayJoin")
          )
      )