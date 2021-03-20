package camundala.bpmn

import FromCamundaBpmn.*
import camundala.dsl.DSL
import org.junit.Test

class FromCamundaBpmnTest extends DSL :

  @Test def loadProcess(): Unit =
    println(
    bpmn("process-cawemo.bpmn")
      .fromCamunda(path("generatedBpmn.bpmn"))
      .stringify()
    )
    

