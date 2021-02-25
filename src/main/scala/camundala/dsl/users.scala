package camundala.dsl

import camundala.model.{BpmnUser, UserRef, _}
import camundala.model.BpmnUser._

trait users:

  def users(users:BpmnUser*) =
    BpmnUsers(users)
    
  def user(username: String) =
    UserRef(username)
    
  def user(username: Username, groupRefs: GroupRefs): BpmnUser =
    BpmnUser(username, groupRefs = groupRefs)

  def user(ident: Username, lastName: Name, firstName: FirstName, email: Email, groupRefs: GroupRefs): BpmnUser =
    BpmnUser(ident, Some(lastName), Some(firstName), Some(email), groupRefs)

  def username(name: String): Username = Username(name)

  def firstName(name: String): FirstName = FirstName(name)

  def email(email: String): Email = Email(email)

  def groupRefs(gRefs: GroupRef*) = GroupRefs(gRefs)

