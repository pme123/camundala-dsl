package camundala.dsl

import camundala.model._
import camundala.model.BpmnGroup._

trait groups:

  def groups(groups:BpmnGroup*) =
    BpmnGroups(groups)
    
  def group(ident: String): BpmnGroup =
    BpmnGroup(Ident(ident))

  extension (group: BpmnGroup)

    def name(name: String): BpmnGroup =
      group.copy(maybeName = Some(Name(name)))
    def groupType(gType: String): BpmnGroup =
      group.copy(`type` = GroupType(gType))

  end extension
