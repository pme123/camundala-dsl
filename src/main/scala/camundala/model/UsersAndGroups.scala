package camundala.model

case class BpmnUser(username: Ident, maybeName: Option[String] = None, maybeFirstName: Option[String] = None, maybeEmail: Option[Email] = None, groups: Seq[BpmnGroup] = Nil) {
  def name(name: String): BpmnUser = copy(maybeName = Some(name))

  def firstName(name: String): BpmnUser = copy(maybeFirstName = Some(name))

  def email(email: Email): BpmnUser = copy(maybeEmail = Some(email))

  def isInGroups(group: BpmnGroup, groups: BpmnGroup*): BpmnUser = copy(groups = (groups :+ group) ++ groups)

}

case class BpmnGroup(ident: Ident, maybeName: Option[String] = None, `type`: Ident = BpmnGroup.Camundala) {
  def name(name: String): BpmnGroup = copy(maybeName = Some(name))

  def groupType(groupType: Ident): BpmnGroup = copy(`type` = groupType)
}

object BpmnGroup {
  val Camundala: Ident = "Camundala"
}

case class CandidateGroups(groups: Seq[BpmnGroup] = Nil) :
  def :+(elem: BpmnGroup): CandidateGroups =
    copy(groups = groups :+ elem)

  def ++(elems: Seq[BpmnGroup]): CandidateGroups =
    copy(groups = groups ++ elems)


object CandidateGroups :
  val none: CandidateGroups = CandidateGroups()

case class CandidateUsers(users: Seq[BpmnUser] = Nil) :
  def :+(elem: BpmnUser): CandidateUsers =
    copy(users = users :+ elem)

  def ++(elems: Seq[BpmnUser]): CandidateUsers =
    copy(users = users ++ elems)
    
object CandidateUsers :
  val none: CandidateUsers = CandidateUsers()

trait HasGroups[T] 


trait HasUsers[T] 

