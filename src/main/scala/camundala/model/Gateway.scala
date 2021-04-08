package camundala.model

import camundala.model.BpmnProcess.NodeKey

sealed trait Gateway extends ProcessNode

case class ExclusiveGateway(ident: Ident,
                            properties: Properties = Properties.none,
                            defaultFlow: Option[ProcessElementRef] = None,
                            //   inFlows: Seq[SequenceFlow] = Seq.empty,
                            //   outFlows: Seq[SequenceFlow] = Seq.empty
                            isAsyncBefore: Boolean = false,
                            isAsyncAfter: Boolean = false
                           )extends Gateway :
  val elemType: NodeKey = NodeKey.exclusiveGateways

  def asyncBefore: ExclusiveGateway = copy(isAsyncBefore = true)

  def asyncAfter: ExclusiveGateway = copy(isAsyncAfter = true)

  def stringify(intent: Int): String =
    val defaultFlowStr: Seq[String] = defaultFlow.map(d => s"defaultFlow(${d.stringify(0)})").toSeq
    stringifyElements(intent, s"exclusiveGateway(${ident.stringify()})", defaultFlowStr: _*)

case class ParallelGateway(ident: Ident,
                           properties: Properties = Properties.none,
                           //  inFlows: Seq[SequenceFlow] = Seq.empty,
                           //  outFlows: Seq[SequenceFlow] = Seq.empty
                           isAsyncBefore: Boolean = false,
                           isAsyncAfter: Boolean = false
                          )extends Gateway :
  val elemType: NodeKey = NodeKey.parallelGateways

  def asyncBefore: ParallelGateway = copy(isAsyncBefore = true)

  def asyncAfter: ParallelGateway = copy(isAsyncAfter = true)

  def stringify(intent: Int): String =
    stringifyElements(intent, "parallelGateway", ident.toString)


