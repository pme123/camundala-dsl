package camundala
package dsl

import model.Name

trait groups:

  def groups(groups:BpmnGroup*) =
    BpmnGroups(groups)
    
  def group(ident: String): BpmnGroup =
    BpmnGroup(Ident(ident))

  extension (group: BpmnGroup)

    def name(name: String): BpmnGroup =
      group.copy(maybeName = Some(Name(name)))
    def groupType(gType: String): BpmnGroup =
      group.copy(`type` = BpmnGroup.GroupType(gType))

  end extension
