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
      val bpmnsConfig = fromCamunda(DemoProcessRunnerApp.demoConfig)
      assertM(bpmnsConfig)(
        hasField(
          "ident",
          (bpmnsConfig: BpmnsConfig) =>
            bpmnsConfig.bpmns.bpmns.head.ident,
          equalTo("demo__process")
        ) &&
          hasField(
            "processes",
            (bpmnsConfig: BpmnsConfig) =>
              bpmnsConfig.bpmns.bpmns.head.processes.processes.size,
            equalTo(1)
          ) &&
          hasField(
            "nodes",
            (bpmnsConfig: BpmnsConfig) =>
              bpmnsConfig.bpmns.bpmns.head.processes.processes.head.nodes.elements.size,
            equalTo(8)
          ) &&
          hasField(
            "flows",
            (bpmnsConfig: BpmnsConfig) =>
              bpmnsConfig.bpmns.bpmns.head.processes.processes.head.flows.elements.size,
            equalTo(9)
          )
      )
    }
  )
