package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class BpmnProcesses(processes: Seq[BpmnProcess]) :

  def :+(process: BpmnProcess): BpmnProcesses = BpmnProcesses(processes :+ process)

object BpmnProcesses:
  def none = BpmnProcesses(Nil)

case class BpmnProcess(
                        ident: Ident,
                        starterGroups: CandidateGroups = CandidateGroups.none,
                        starterUsers: CandidateUsers = CandidateUsers.none,
                        nodes: ProcessNodes = ProcessNodes.none,
                        flows: SequenceFlows = SequenceFlows.none
                      )
  extends HasGroups[BpmnProcess] :

    val elements = nodes.elements ++ flows.elements

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

trait ProcessElements:
  
  def elements: Seq[ProcessElement]

case class ProcessNodes(nodes: Seq[ProcessNode])
  extends ProcessElements :
  
  val elements: Seq[ProcessElement] = nodes

object ProcessNodes:

  val none = ProcessNodes(Nil)

trait ProcessElement :
  
  def elemType: NodeKey

  def ident: Ident

  def ref: ProcessElementRef = ProcessElementRef(ident.toString)

opaque type ProcessElementRef = String

object ProcessElementRef:
  def apply(ref: String): ProcessElementRef = ref

trait ProcessNode
  extends ProcessElement


