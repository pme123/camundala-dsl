package camundala
package dev

import camundala.dev
import camundala.dsl.DSL

object DemoProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  val cawemoFolderPath = "dsl/src/test/cawemo"
  val withIdFolderPath = "./dsl/src/test/cawemo/with-ids"
  val generatedFolderPath = "./dsl/src/test/cawemo/output"

  lazy val demoConfig =
    RunnerConfig(
      "DemoProcess",
      path(cawemoFolderPath),
      path(withIdFolderPath),
      path(generatedFolderPath),
      demoProcess.bpmnsProjectConfig
    )

  lazy val runnerLogic =
    BpmnRunner(demoConfig).run()

object demoProcess extends DSL:

  val bpmnIdent =
    "demo__process"
  val admin = group("admin")
    .name("Administrator")
    .groupType("system")
  val adminUser = user("admin")
    .name("Administrator")
    .firstName("-")
    .email("myEmail@email.ch")
    .group(admin.ref)

  val dev = user("dev")
    .name("Dev")
    .firstName("-")
    .email("dev@email.ch")

  lazy val bpmnsProjectConfig = bpmnsConfig
    .users(adminUser, dev)
    .groups(admin)
    .bpmns(demoBpmn)
  val isBarVar = "isBar"
  lazy val demoBpmn =
    bpmn(bpmnIdent)
      .processes(
        process("TestDSLProcess")
          .starterGroups(
            admin.ref
          )
          .starterUsers(
            adminUser.ref,
            dev.ref
          )
          .nodes(
            startEvent("StartProcess"),
            serviceTask("ServiceTask")
              .expression(s"execution.setVariable('$isBarVar', true)")
              .inputString("in1", "value1")
              .inputExpression("myBoolean", "true")
              .inputGroovy("groovyRef", "myGroovy.groovy")
              .inputGroovyInline("additon", "1 + 3")
              .outputString("in1", "value1")
              .outputExpression("myBoolean", "true")
              .outputGroovy("groovyRef", "myGroovy.groovy")
              .outputGroovyInline("additon", "1 + 3"),
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
              .inputString("endFlag", "finished")
          )
          .flows(
            sequenceFlow("IsNOTBar__Fork__UserTaskA")
              .expression(s"!$isBarVar"),
            sequenceFlow("IsBar__Fork__UserTaskB")
              .inlineGroovy(isBarVar),
            sequenceFlow("flow1__StartProcess__ServiceTask"),
            sequenceFlow("flow2__ServiceTask__Fork"),
            sequenceFlow("flow6__UserTaskB__Asdfdsf"),
            sequenceFlow("flow5__UserTaskA__gatewayJoin"),
            sequenceFlow("flow7__gatewayJoin__ScriptTask"),
            sequenceFlow("SequenceFlow_9__ScriptTask__EndProcess"),
            sequenceFlow("Flow_1i69u7d__Asdfdsf__gatewayJoin")
          )
      )
