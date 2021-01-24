package camundala.model

import camundala.model.BpmnProcess.NodeKey


trait ProcessElement:
  def elemType: NodeKey

  def ident: Ident

case class BpmnProcess(
                        ident: Ident,
                        starterGroups: CandidateGroups = CandidateGroups.none,
                        starterUsers: CandidateUsers = CandidateUsers.none,
                        elements: Seq[ProcessElement] = Seq.empty
                      )
  extends HasGroups[BpmnProcess] :

  def canStart(group: BpmnGroup, groups: BpmnGroup*): BpmnProcess =
    copy(starterGroups = (starterGroups :+ group) ++ groups)

  def canStart(user: BpmnUser, users: BpmnUser*): BpmnProcess =
    copy(starterUsers = (starterUsers :+ user) ++ users)

  def elements(elem: ProcessElement, elems: ProcessElement*): BpmnProcess =
    copy(elements = (elements :+ elem) ++ elems)

  def userTasks(task: UserTask, tasks: UserTask*): BpmnProcess =
      copy(elements = (elements :+ task) ++ tasks)

  def ---(task: UserTask, tasks: UserTask*): BpmnProcess =
    userTasks(task, tasks: _*)

  def serviceTasks(task: ServiceTask, tasks: ServiceTask*): BpmnProcess =
    copy(elements = (elements :+ task) ++ tasks)

  def ---(task: ServiceTask, tasks: ServiceTask*): BpmnProcess =
    serviceTasks(task, tasks: _*)
/*
  def sendTasks(task: SendTask, tasks: SendTask*): BpmnProcess =
    copy(sendTasks = (sendTasks :+ task) ++ tasks)

  def ---(task: SendTask, tasks: SendTask*): BpmnProcess =
    sendTasks(task, tasks: _*)

  def businessRuleTasks(
                         task: BusinessRuleTask,
                         tasks: BusinessRuleTask*
                       ): BpmnProcess =
    copy(businessRuleTasks = (businessRuleTasks :+ task) ++ tasks)

  def ---(task: BusinessRuleTask, tasks: BusinessRuleTask*): BpmnProcess =
    businessRuleTasks(task, tasks: _*)

  def callActivities(
                      activity: CallActivity,
                      activities: CallActivity*
                    ): BpmnProcess =
    copy(callActivities = (callActivities :+ activity) ++ activities)

  def ---(activity: CallActivity, activities: CallActivity*): BpmnProcess =
    callActivities(activity, activities: _*)

  def startEvents(event: StartEvent, events: StartEvent*): BpmnProcess =
    copy(startEvents = (startEvents :+ event) ++ events)

  def ---(event: StartEvent, events: StartEvent*): BpmnProcess =
    startEvents(event, events: _*)

  def endEvents(event: EndEvent, events: EndEvent*): BpmnProcess =
    copy(endEvents = (endEvents :+ event) ++ events)

  def ---(event: EndEvent, events: EndEvent*): BpmnProcess =
    endEvents(event, events: _*)

  def sequenceFlows(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess =
    copy(sequenceFlows = (sequenceFlows :+ flow) ++ flows)

  def ---(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess =
    sequenceFlows(flow, flows: _*)
*/
  /**
   * create an ordered list of all nodes, grouped by there names
   */
  val allNodes: Seq[(NodeKey, Seq[ProcessElement])] =
    elements
      .groupBy(_.elemType)
      .toSeq
      .sortBy { (k, _) => k.order }

object BpmnProcess:
  private val process =
    BpmnProcess("dummy")
  val allNodeKeys: Seq[NodeKey] =
    process.allNodes.map(_._1)
  val emptyAllNodes: Map[NodeKey, Seq[ProcessElement]] =
    allNodeKeys.map(_ -> Seq.empty[ProcessElement]).toMap

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
