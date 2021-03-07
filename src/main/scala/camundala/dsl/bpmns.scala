package camundala.dsl

import camundala.model.*

trait bpmns:
  type BpmnsConfigAttributes = Bpmns | BpmnUsers | BpmnGroups

  def bpmnsConfig(attributes: BpmnsConfigAttributes*) =
    BpmnsConfig(
      Bpmns(attributes.collect { case Bpmns(x) => x }.flatten),
      BpmnGroups(attributes.collect { case BpmnGroups(x) => x }.flatten),
      BpmnUsers(attributes.collect { case BpmnUsers(x) => x }.flatten)
    )

  def bpmns(bpmns: Bpmn*): Bpmns = Bpmns(bpmns)
      
  type BpmnAttributes = BpmnProcess

  def bpmn(bpmnPath: BpmnPath,
           attributes: BpmnAttributes*): Bpmn =
    Bpmn(bpmnPath,
      BpmnProcesses(attributes.collect { case x: BpmnProcess => x })
    )
