package camundala.model

import camundala.model.BpmnProcess.ElemKey

trait HasGateway[T <: HasGateway[T]] extends HasProcessNode[T]

case class ExclusiveGateway(
    processNode: ProcessNode,
    defaultFlow: Option[ProcessElementRef] = None
    //   inFlows: Seq[SequenceFlow] = Seq.empty,
    //   outFlows: Seq[SequenceFlow] = Seq.empty
) extends HasGateway[ExclusiveGateway]:
  def withProcessNode(processNode: ProcessNode): ExclusiveGateway = copy(processNode = processNode)
  val elemKey: ElemKey = ElemKey.exclusiveGateways

object ExclusiveGateway:
  def apply(ident: String): ExclusiveGateway =
    ExclusiveGateway(ProcessNode(ident))

case class ParallelGateway(
    processNode: ProcessNode
    //  inFlows: Seq[SequenceFlow] = Seq.empty,
    //  outFlows: Seq[SequenceFlow] = Seq.empty
) extends HasGateway[ParallelGateway]:

  def withProcessNode(processNode: ProcessNode): ParallelGateway = copy(processNode = processNode)

  val elemKey: ElemKey = ElemKey.parallelGateways

object ParallelGateway:
  def apply(ident: String): ParallelGateway =
    ParallelGateway(ProcessNode(ident))