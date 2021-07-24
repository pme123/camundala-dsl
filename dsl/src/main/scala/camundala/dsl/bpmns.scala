package camundala
package dsl

trait bpmns:

  def bpmnsConfig =  BpmnsConfig.none

  extension (bpmnsConfig: BpmnsConfig)
    def bpmns(bpmn: Bpmn, bpmns: Bpmn*) =
      bpmnsConfig.copy(bpmns = Bpmns(bpmn +: bpmns))

    def bpmns(bpmns: Seq[Bpmn]) =
      bpmnsConfig.copy(bpmns = Bpmns(bpmns))

    def dmns(dmn: Dmn, dmns: Dmn*) =
      bpmnsConfig.copy(dmns = Dmns(dmn +: dmns))

    def dmns(dmns: Seq[Dmn]) =
      bpmnsConfig.copy(dmns = Dmns(dmns))

    def groups(groups: BpmnGroup*) =
      bpmnsConfig.copy(groups = BpmnGroups(groups))

    def users(users: BpmnUser*) =
      bpmnsConfig.copy(users = BpmnUsers(users))   

  type BpmnAttributes = BpmnProcess

  def bpmn(ident: String): Bpmn = Bpmn(Ident(ident), BpmnProcesses.none)
  def bpmn(bpmnFile: File): Bpmn = Bpmn(Ident(bpmnFile), BpmnProcesses.none)

  extension (bpmn: Bpmn)
    def processes(processes: BpmnProcess*): Bpmn =
      bpmn.copy(processes = BpmnProcesses(processes))
