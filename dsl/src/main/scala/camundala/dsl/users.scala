package camundala
package dsl

import camundala.model.{UserRef, GroupRef, Name}
import camundala.model.BpmnUser.{Username, FirstName, Email}

trait users:

  def users(users: BpmnUser*) =
    BpmnUsers(users)

  def user(username: String): BpmnUser =
    BpmnUser(Username(username))

  extension (user: BpmnUser)

    def name(name: String): BpmnUser =
      user.copy(maybeName = Some(Name(name)))
    def firstName(name: String): BpmnUser =
      user.copy(maybeFirstName = Some(FirstName(name)))
    def email(email: String): BpmnUser =
      user.copy(maybeEmail = Some(Email(email)))
    def group(groupRef: GroupRef | String): BpmnUser =
      user.copy(groupRefs = user.groupRefs :+ GroupRef(groupRef.toString))

  end extension