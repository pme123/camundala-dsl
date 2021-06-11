package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import zio.test.*
import zio.*
import zio.test.Assertion.*

object FromCamundaBpmnSuites
    extends DefaultRunnableSpec
    with FromCamundaBpmn
    with ToCamundaBpmn:

  def spec = suite("FromCamundaBpmnSuites")(
    testM("creates BPMN from Camunda BPMN") {
      val bpmns = FromCamundaRunner(
        FromCamundaConfig(
          DemoProcessRunnerApp.demoConfig.cawemoFolder,
          DemoProcessRunnerApp.demoConfig.withIdFolder
        )
      ).run()
      assertM(bpmns)(
        hasField(
          "ident",
         (bpmns: Seq[Bpmn]) => bpmns.head.ident,
          equalTo("demo__process")
        ) &&
          hasField(
            "processes",
            (bpmns: Seq[Bpmn]) => bpmns.head.processes.processes.size, equalTo(1)
          ) &&
          hasField(
            "nodes",
            (bpmns: Seq[Bpmn]) => bpmns.head.processes.processes.head.nodes.elements.size,
            equalTo(8)
          ) &&
          hasField(
            "flows",
            (bpmns: Seq[Bpmn]) => bpmns.head.processes.processes.head.flows.elements.size,
            equalTo(9)
          )
      )
    }
  )
