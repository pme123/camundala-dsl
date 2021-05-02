package camundala.dsl

import camundala.model._

trait processes:

  def process(ident: String): BpmnProcess =
    BpmnProcess(Ident(ident))

  extension (process: BpmnProcess)
    def starterGroup(group: GroupRef | String) =
      process.copy(starterGroups = process.starterGroups :+ GroupRef(group.toString))

    def starterUser(user: UserRef | String) =
      process.copy(starterUsers = process.starterUsers :+ UserRef(user.toString))

    def nodes(processNodes: ProcessNode*) =
      process.copy(nodes = ProcessNodes(processNodes))

    def flows(processFlows: SequenceFlow*) =
      process.copy(flows = SequenceFlows(processFlows))
