package camundala.model

import camundala.model.BpmnGroup.GroupType
import camundala.model.BpmnUser.{Email, FirstName, Username}

case class BpmnUsers(users: Seq[BpmnUser])
  extends HasStringify :

  def stringify(intent: Int): String =
    stringifyWrap(intent, "users", users)
    
case class BpmnUser(username: Username, 
                    maybeName: Option[Name] = None, 
                    maybeFirstName: Option[FirstName] = None, 
                    maybeEmail: Option[Email] = None, 
                    groupRefs: GroupRefs = GroupRefs.none)
  extends HasStringify:
  
  val ref = UserRef(username.toString)
  
  def stringify(intent: Int): String =
    s"""${intentStr(intent)}user(
       |${
      (Seq(username.stringify(intent + 1)) ++
        maybeName.map(_.stringify(intent + 1)).toSeq ++
        maybeFirstName.map(_.stringify(intent + 1)).toSeq ++
        maybeEmail.map(_.stringify(intent + 1)).toSeq :+
        groupRefs.stringify(intent + 1)
        ).mkString(",\n")
    }
       |${intentStr(intent)})""".stripMargin

object BpmnUser:
  opaque type Username = String

  object Username:
    def apply(ident: String): Username = ident

    extension (username: Username)
      def stringify(intent: Int): String = s"""${intentStr(intent)}username("$username")"""

  opaque type FirstName = String

  object FirstName:
    def apply(firstName: String): FirstName = firstName

    extension (firstName: FirstName)
      def stringify(intent: Int): String = s"""${intentStr(intent)}firstName("$firstName")"""

  opaque type Email = String

  object Email:
    def apply(email: String): Email = email

    extension (email: Email)
      def stringify(intent: Int): String = s"""${intentStr(intent)}email ("$email")"""

opaque type GroupRef = String

object GroupRef:
  def apply(groupRef: String): GroupRef = groupRef

  extension (groupRef: GroupRef)
    def stringify(intent: Int): String = s"""${intentStr(intent)}group("$groupRef")"""

opaque type UserRef = String

object UserRef:
  def apply(userRef: String): UserRef = userRef

  extension (userRef: UserRef)
    def stringify(intent: Int): String = s"""${intentStr(intent)}user("$userRef")"""

case class GroupRefs(groupRefs: Seq[GroupRef])
  extends HasStringify :
  
  def stringify(intent: Int): String =
    s"""${intentStr(intent)}groupRefs(
       |${groupRefs.map(_.stringify(intent + 1)).mkString(",\n")}
       |${intentStr(intent)})""".stripMargin

object GroupRefs:
  val none = GroupRefs(Nil)

case class BpmnGroups(groups: Seq[BpmnGroup])
  extends HasStringify :

  def stringify(intent: Int): String =
    stringifyWrap(intent, "groups", groups)
    
case class BpmnGroup(ident: Ident, maybeName: Option[Name] = None, `type`: GroupType = BpmnGroup.Camundala)
  extends HasStringify:
  
  def ref: GroupRef = GroupRef(ident.toString)

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}group(
       |${
      (ident.stringify(intent + 1) +:
        maybeName.map(_.stringify(intent + 1)).toSeq :+
        `type`.stringify(intent + 1)).mkString(",\n")
    }
       |${intentStr(intent)})""".stripMargin


object BpmnGroup:
  val Camundala: GroupType = GroupType("Camundala")
       
  opaque type GroupType = String

  object GroupType:
    def apply(groupType: String): GroupType = groupType

  extension (groupType: GroupType)
    def stringify(intent: Int): String = s"""${intentStr(intent)}groupType("$groupType")"""


case class CandidateGroups(groups: Seq[GroupRef] = Nil)
  extends HasStringify :

  def isEmpty: Boolean = groups.isEmpty

  def stringify(intent: Int) =
    groups.map(_.stringify(intent + 1)).mkString(",\n")

object CandidateGroups:
  val none: CandidateGroups = CandidateGroups()

case class CandidateUsers(users: Seq[UserRef] = Nil)
  extends HasStringify :

  def isEmpty: Boolean = users.isEmpty

  def stringify(intent: Int) =
    users.map(_.stringify(intent + 1)).mkString(",\n")

object CandidateUsers:
  val none: CandidateUsers = CandidateUsers()

trait HasGroups[T]


trait HasUsers[T] 

