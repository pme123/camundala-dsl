package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test

class FromCamundaBpmnTest 
  extends FromCamundaBpmn 
  with ToCamundaBpmn:

  @Test def loadProcess(): Unit =
    val bpmn: Bpmn = fromCamunda(path("process-cawemo.bpmn"), path("process-cawemo-generated.bpmn"))
    println(bpmn.stringify())
    

