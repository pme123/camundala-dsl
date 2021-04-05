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
      val bpmn = fromCamunda(path("bpmns/process-cawemo.bpmn"), path("bpmns/with-ids/process-cawemo.bpmn"))
      assertM(bpmn)(
        hasField("processes", (bpmn: Bpmn) => bpmn.processes.processes.size, equalTo(1)) &&
          hasField("elements", (bpmn: Bpmn) => bpmn.processes.processes.head.elements.elements.size, equalTo(15))
      )
    }
  )
  
