package camundala.dsl

import camundala.model._

trait processes:

  def process(ident: String): BpmnProcess =
    BpmnProcess(Ident(ident))

  extension (process: BpmnProcess)
    def starterGroups(groups: GroupRef*) =
      process.copy(starterGroups = CandidateGroups(groups))

    def starterUsers(users: UserRef*) =
      process.copy(starterUsers = CandidateUsers(users))

    def nodes(processNodes: ProcessNode*) =
      process.copy(nodes = ProcessNodes(processNodes))

    def flows(processFlows: SequenceFlow*) =
      process.copy(flows = SequenceFlows(processFlows))
