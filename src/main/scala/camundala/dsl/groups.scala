package camundala.dsl

import camundala.model._
import camundala.model.BpmnGroup._

trait groups:

  def groups(groups:BpmnGroup*) =
    BpmnGroups(groups)
    
  def group(refStr: String): GroupRef = GroupRef(refStr)

  def group(ident: Ident, name: Name, groupType: GroupType): BpmnGroup =
    BpmnGroup(ident, Some(name), groupType)

  def groupType(typ: String): GroupType =
    GroupType(typ)
