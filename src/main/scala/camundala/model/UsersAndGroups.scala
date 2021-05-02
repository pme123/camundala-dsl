package camundala.model

import camundala.model.BpmnGroup.GroupType
import camundala.model.BpmnUser.{Email, FirstName, Username}

case class BpmnUsers(users: Seq[BpmnUser]):

  def :+(user: BpmnUser): BpmnUsers = BpmnUsers(users :+ user)

object BpmnUsers:
  def none = BpmnUsers(Nil)

case class BpmnUser(
    username: Username,
    maybeName: Option[Name] = None,
    maybeFirstName: Option[FirstName] = None,
    maybeEmail: Option[Email] = None,
    groupRefs: GroupRefs = GroupRefs.none
):

  val ref = UserRef(username.toString)

object BpmnUser:
  opaque type Username = String

  object Username:
    def apply(ident: String): Username = ident

  opaque type FirstName = String
    
  object FirstName:
    def apply(firstName: String): FirstName = firstName

  opaque type Email = String

  object Email:
    def apply(email: String): Email = email

opaque type GroupRef = String

object GroupRef:
  def apply(groupRef: String): GroupRef = groupRef

opaque type UserRef = String

object UserRef:
  def apply(userRef: String): UserRef = userRef

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

case class CandidateUsers(users: Seq[UserRef] = Nil):

  def isEmpty: Boolean = users.isEmpty
  def :+(user: UserRef) = CandidateUsers(users :+ user)

object CandidateUsers:
  val none: CandidateUsers = CandidateUsers()

trait HasGroups[T]

trait HasUsers[T]
