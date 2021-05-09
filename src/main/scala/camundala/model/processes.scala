package camundala.model

import camundala.model.BpmnProcess.ElemKey

case class BpmnProcesses(processes: Seq[BpmnProcess]):

  def :+(process: BpmnProcess): BpmnProcesses = BpmnProcesses(
    processes :+ process
  )

object BpmnProcesses:
  def none = BpmnProcesses(Nil)

case class BpmnProcess(
    ident: Ident,
    starterGroups: CandidateGroups = CandidateGroups.none,
    starterUsers: CandidateUsers = CandidateUsers.none,
    nodes: ProcessNodes = ProcessNodes.none,
    flows: SequenceFlows = SequenceFlows.none
) extends HasGroups[BpmnProcess]:

  val elements = nodes.elements ++ flows.elements

object BpmnProcess:

  sealed trait ElemKey:
    def name: String

    def order: Int

  object ElemKey:

    case object startEvents extends ElemKey:
      val name = "startEvent"
      val order = 1

      override def toString: String = "startEvents"

    case object userTasks extends ElemKey:
      val name = "userTask"
      val order = 2

      override def toString: String = "userTasks"

    case object serviceTasks extends ElemKey:
      val name = "serviceTask"
      val order = 3

      override def toString: String = "serviceTasks"

    case object businessRuleTasks extends ElemKey:
      val name = "businessRuleTask"
      val order = 4

      override def toString: String = "businessRuleTasks"

    case object sendTasks extends ElemKey:
      val name = "sendTask"
      val order = 5

      override def toString: String = "sendTasks"

    case object callActivities extends ElemKey:
      val name = "callActivity"
      val order = 6

      override def toString: String = "callActivities"

    case object exclusiveGateways extends ElemKey:
      val name = "exclusiveGateway"
      val order = 7

      override def toString: String = "exclusiveGateways"

    case object parallelGateways extends ElemKey:
      val name = "parallelGateway"
      val order = 7

      override def toString: String = "parallelGateways"

    case object endEvents extends ElemKey:
      val name = "endEvent"
      val order = 8

      override def toString: String = "endEvents"

    case object sequenceFlows extends ElemKey:
      val name = "sequenceFlow"
      val order = 9

      override def toString: String = "sequenceFlows"

trait ProcessElements:

  def elements: Seq[HasProcessElement[_]]

case class ProcessNodes(nodes: Seq[HasProcessNode[_]]) extends ProcessElements:

  val elements: Seq[HasProcessElement[_]] = nodes

object ProcessNodes:

  val none = ProcessNodes(Nil)

trait HasProcessElement[T <: HasProcessElement[T]] extends HasProperties[T]:
  def processElement: ProcessElement
  def ident: Ident = processElement.ident
  def elemKey: ElemKey
    
  def withProcessElement(processElement: ProcessElement): T

  def prop(prop: Property): T = withProcessElement(processElement.prop(prop))


case class ProcessElement(
    ident: Ident,
    properties: Properties = Properties.none
):

  def ref: ProcessElementRef = ProcessElementRef(ident.toString)

  def prop(prop: Property): ProcessElement =
    copy(properties = properties :+ prop)

opaque type ProcessElementRef = String

object ProcessElementRef:
  def apply(ref: String): ProcessElementRef = ref

trait HasProcessNode[T <: HasProcessNode[T]]
    extends HasProcessElement[T]
    with HasTransactionBoundary[T]:
      
  def processNode: ProcessNode

  def withProcessNode(processNode: ProcessNode): T

  def isAsyncBefore: Boolean = processNode.isAsyncBefore

  def isAsyncAfter: Boolean = processNode.isAsyncAfter

  def asyncBefore: T = withProcessNode(processNode.asyncBefore)

  def asyncAfter: T = withProcessNode(processNode.asyncAfter)

case class ProcessNode(
    processElement: ProcessElement,
    isAsyncBefore: Boolean = false,
    isAsyncAfter: Boolean = false
):

  val ident = processElement.ident
  val properties: Properties = processElement.properties

  def asyncBefore: ProcessNode = copy(isAsyncBefore = true)

  def asyncAfter: ProcessNode = copy(isAsyncAfter = true)

  def prop(prop: Property): ProcessNode =
    copy(processElement = processElement.prop(prop))

object ProcessNode:
  def apply(ident: String): ProcessNode =
    ProcessNode(ProcessElement(Ident(ident)))