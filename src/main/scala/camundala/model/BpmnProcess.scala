package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class BpmnProcesses(processes: Seq[BpmnProcess])
  extends HasStringify :

  def stringify(intent: Int): String =
    stringifyWrap(intent, "processes", processes)

case class ProcessElements(elements: Seq[ProcessElement])
  extends HasStringify :
  
  def stringify(intent: Int): String =
    stringifyWrap(intent, "elements", elements)
    
object ProcessElements:

  val none = ProcessElements(Nil)

trait ProcessElement
  extends HasStringify :

  def elemType: NodeKey

  def ident: Ident


case class BpmnProcess(
                        ident: Ident,
                        starterGroups: CandidateGroups = CandidateGroups.none,
                        starterUsers: CandidateUsers = CandidateUsers.none,
                        elements: ProcessElements = ProcessElements.none
                      )
  extends HasGroups[BpmnProcess]
    with HasStringify :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}process(
       |${
      Seq(ident.stringify(intent + 1),
        stringifyWrap(intent + 1, "starterGroups", starterGroups),
        stringifyWrap(intent + 1, "starterUsers", starterUsers),
        elements.stringify(intent + 1)
      ).mkString(",\n")
    }
       |${intentStr(intent)})""".stripMargin

object BpmnProcess:

  sealed trait NodeKey:
    def name: String

    def order: Int


  object NodeKey:

    case object startEvents extends NodeKey :
      val name = "startEvent"
      val order = 1

      override def toString: String = "startEvents"

    case object userTasks extends NodeKey :
      val name = "userTask"
      val order = 2

      override def toString: String = "userTasks"

    case object serviceTasks extends NodeKey :
      val name = "serviceTask"
      val order = 3

      override def toString: String = "serviceTasks"

    case object businessRuleTasks extends NodeKey :
      val name = "businessRuleTask"
      val order = 4

      override def toString: String = "businessRuleTasks"

    case object sendTasks extends NodeKey :
      val name = "sendTask"
      val order = 5

      override def toString: String = "sendTasks"

    case object callActivities extends NodeKey :
      val name = "callActivity"
      val order = 6

      override def toString: String = "callActivities"

    case object exclusiveGateways extends NodeKey :
      val name = "exclusiveGateway"
      val order = 7

      override def toString: String = "exclusiveGateways"

    case object parallelGateways extends NodeKey :
      val name = "parallelGateway"
      val order = 7

      override def toString: String = "parallelGateways"

    case object endEvents extends NodeKey :
      val name = "endEvent"
      val order = 8

      override def toString: String = "endEvents"

    case object sequenceFlows extends NodeKey :
      val name = "sequenceFlow"
      val order = 9

      override def toString: String = "sequenceFlows"
