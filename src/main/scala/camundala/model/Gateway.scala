package camundala.model

import camundala.model.BpmnProcess.NodeKey

sealed trait Gateway extends ProcessElement

case class ExclusiveGateway(ident: Ident,
                            properties: Properties = Properties.none,
                            defaultFlow: Option[ProcessElementRef] = None
                         //   inFlows: Seq[SequenceFlow] = Seq.empty,
                         //   outFlows: Seq[SequenceFlow] = Seq.empty
                           ) extends Gateway:
  val elemType: NodeKey = NodeKey.exclusiveGateways

  def stringify(intent: Int): String =
    val defaultFlowStr: Seq[String] = defaultFlow.map(d => s"defaultFlow(${d.stringify(0)})").toSeq
    stringifyElements(intent, s"exclusiveGateway(${ident.stringify()})", defaultFlowStr:_*)

case class ParallelGateway(ident: Ident,
                           properties: Properties = Properties.none,
                         //  inFlows: Seq[SequenceFlow] = Seq.empty,
                         //  outFlows: Seq[SequenceFlow] = Seq.empty
                          ) extends Gateway:
  val elemType: NodeKey = NodeKey.parallelGateways

  def stringify(intent: Int): String =
    stringifyElements(intent, "parallelGateway", ident.toString)


