package camundala.model

import camundala.model.BpmnGroup.GroupType
import camundala.model.BpmnUser.{Email, FirstName, Username}

opaque type GroupRef = String

object GroupRef:
  def apply(groupRef: String): GroupRef = groupRef

case class GroupRefs(groupRefs: Seq[GroupRef]):
  def :+(group: GroupRef): GroupRefs = GroupRefs(groupRefs :+ group)

object GroupRefs:
  val none = GroupRefs(Nil)

case class BpmnGroups(groups: Seq[BpmnGroup]):

  def :+(group: BpmnGroup): BpmnGroups = BpmnGroups(groups :+ group)

object BpmnGroups:
  def none = BpmnGroups(Nil)

case class BpmnGroup(
    ident: Ident,
    maybeName: Option[Name] = None,
    `type`: GroupType = BpmnGroup.Camundala
):

  def ref: GroupRef = GroupRef(ident.toString)

object BpmnGroup:

  val Camundala: GroupType = GroupType("Camundala")

  opaque type GroupType = String

  object GroupType:
    def apply(groupType: String): GroupType = groupType

case class CandidateGroups(groups: Seq[GroupRef] = Nil):

  def isEmpty: Boolean = groups.isEmpty
  def :+(group: GroupRef) = CandidateGroups(groups :+ group)

object CandidateGroups:
  val none: CandidateGroups = CandidateGroups()

trait HasGroups[T]
