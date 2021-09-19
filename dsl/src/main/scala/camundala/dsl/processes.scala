package camundala
package dsl

import camundala.model.{HasProcessNode, ScriptTask}

trait processes:

  def process(ident: String): BpmnProcess =
    BpmnProcess(Ident(ident))

  extension (process: BpmnProcess)

    def starterGroup(group: GroupRef | String) =
      process.copy(starterGroups =
        process.starterGroups :+ GroupRef(group.toString)
      )

    def starterGroups(
        group: GroupRef | String,
        groups: (GroupRef | String)*
    ): BpmnProcess =
      process.copy(starterGroups =
        process.starterGroups ++ (group +: groups).map(g =>
          GroupRef(g.toString)
        )
      )

    def starterGroups(): BpmnProcess =
      process

    def starterUser(user: UserRef | String) =
      process.copy(starterUsers =
        process.starterUsers :+ UserRef(user.toString)
      )

    def starterUsers(
        user: UserRef | String,
        users: (UserRef | String)*
    ): BpmnProcess =
      process.copy(starterUsers =
        process.starterUsers ++ (user +: users).map(g => UserRef(g.toString))
      )

    def starterUsers(): BpmnProcess =
      process

    def nodes(processNodes: HasProcessNode[_]*) =
      process.copy(processNodes = ProcessNodes(processNodes))

    def flows(processFlows: SequenceFlow*) =
      process.copy(flows = SequenceFlows(processFlows))
