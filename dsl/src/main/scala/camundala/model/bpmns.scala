package camundala
package model

case class BpmnsConfig(bpmns: Bpmns,
                       dmns: Dmns,
                       groups: BpmnGroups,
                       users: BpmnUsers = BpmnUsers.none
                      )

object BpmnsConfig:
  def none = BpmnsConfig(
    Bpmns.none,
    Dmns.none,
    BpmnGroups.none,
    BpmnUsers.none
  )

case class Bpmns(bpmns: Seq[Bpmn]) :

  def :+(bpmn: Bpmn): Bpmns = Bpmns(bpmns :+ bpmn)

object Bpmns:
  def none = Bpmns(Nil)

case class Bpmn(ident: Ident,
                processes: BpmnProcesses
               ) :
  lazy val path = ident.toOriginal() + ".bpmn"
