package camundala.model

case class BpmnsConfig(bpmns: Bpmns,
                       groups: BpmnGroups,
                       users: BpmnUsers = BpmnUsers.none
                      )

object BpmnsConfig:
  def none = BpmnsConfig(
    Bpmns.none,
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
