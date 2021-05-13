package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import zio.test.*
import zio.*
import zio.test.Assertion.*

object FromCamundaBpmnSuites
  extends DefaultRunnableSpec
    with FromCamundaBpmn
    with ToCamundaBpmn :

  def spec = suite("FromCamundaBpmnSuites")(
    testM("creates BPMN from Camunda BPMN") {
      val bpmn = fromCamunda(path(DemoProcessRunnerApp.demoProcessPath), path(demoProcess.demoProcessWithIdsPath))
      assertM(bpmn)(
        hasField("processes", (bpmn: Bpmn) => bpmn.processes.processes.size, equalTo(1)) &&
          hasField("nodes", (bpmn: Bpmn) => bpmn.processes.processes.head.nodes.elements.size, equalTo(8)) &&
          hasField("flows", (bpmn: Bpmn) => bpmn.processes.processes.head.flows.elements.size, equalTo(9))
      )
    }
  )
  
