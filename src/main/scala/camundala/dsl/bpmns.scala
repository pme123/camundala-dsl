package camundala.dsl

import camundala.model.*

trait bpmns:

  def bpmnsConfig =  BpmnsConfig.none

  extension (bpmnsConfig: BpmnsConfig)
    def bpmns(bpmns: Bpmn*) =
      bpmnsConfig.copy(bpmns = Bpmns(bpmns))

    def groups(groups: BpmnGroup*) =
      bpmnsConfig.copy(groups = BpmnGroups(groups))

    def users(users: BpmnUser*) =
      bpmnsConfig.copy(users = BpmnUsers(users))   

  type BpmnAttributes = BpmnProcess

  def bpmn(path: String): Bpmn = Bpmn(BpmnPath(path), BpmnProcesses.none)

  extension (bpmn: Bpmn)
    def processes(processes: BpmnProcess*): Bpmn =
      bpmn.copy(processes = BpmnProcesses(processes))
