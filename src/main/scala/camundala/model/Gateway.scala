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


