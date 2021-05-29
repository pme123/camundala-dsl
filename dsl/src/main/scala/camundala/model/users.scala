package camundala.model

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

opaque type UserRef = String

object UserRef:
  def apply(userRef: String): UserRef = userRef

case class CandidateUsers(users: Seq[UserRef] = Nil):

  def isEmpty: Boolean = users.isEmpty
  def :+(user: UserRef) = CandidateUsers(users :+ user)
  def ++(usrs: Seq[UserRef]) = CandidateUsers(users ++ usrs)

object CandidateUsers:
  val none: CandidateUsers = CandidateUsers()

trait HasUsers[T]
