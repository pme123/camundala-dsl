package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test

class FromCamundaBpmnTest 
  extends FromCamundaBpmn 
  with ToCamundaBpmn:

  @Test def fromCamunda(): Unit =
    val bpmn: Bpmn = fromCamunda(path("bpmns/process-cawemo.bpmn"), path("bpmns/with-ids/process-cawemo.bpmn"))
    println(bpmn.stringify())


  bpmn("process-cawemo.bpmn")
    .processes(
      process("process_TestDslProcess")
        .starterGroups(
  
        )
        .starterUsers(
  
        )
        .elements(
          serviceTask("serviceTask_ServiceTask")
            .externalTask("my-topic"),
          userTask("userTask_UserTaskA"),
          userTask("userTask_UserTaskB"),
          scriptTask("scriptTask_ScriptTask")
            .inlineGroovy(""""""),
          exclusiveGateway("exclusiveGateway_Fork"),
          exclusiveGateway("exclusiveGateway_75b51442"),
          sequenceFlow("sequenceFlow_abc90bd9_startEvent_StartProcess-serviceTask_ServiceTask"),
          sequenceFlow("sequenceFlow_77df807e_serviceTask_ServiceTask-exclusiveGateway_Fork"),
          sequenceFlow("sequenceFlow_IsNOTBar_exclusiveGateway_Fork-userTask_UserTaskA")
            .expression("approved"),
          sequenceFlow("sequenceFlow_IsBar_exclusiveGateway_Fork-userTask_UserTaskB"),
          sequenceFlow("sequenceFlow_68d9c1a9_userTask_UserTaskB-exclusiveGateway_75b51442"),
          sequenceFlow("sequenceFlow_7d157549_userTask_UserTaskA-exclusiveGateway_75b51442"),
          sequenceFlow("sequenceFlow_a12745a3_exclusiveGateway_75b51442-scriptTask_ScriptTask"),
          sequenceFlow("sequenceFlow_b12149c5_scriptTask_ScriptTask-endEvent_EndProcess")
        )
    )