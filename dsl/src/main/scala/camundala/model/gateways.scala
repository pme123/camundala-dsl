package camundala.model

trait HasGateway[T] extends HasProcessNode[T] 


case class ExclusiveGateway(
                             processNode: ProcessNode,
                             defaultFlow: Option[ProcessElementRef] = None
                             //   inFlows: Seq[SequenceFlow] = Seq.empty,
                             //   outFlows: Seq[SequenceFlow] = Seq.empty
                           ) extends HasGateway[ExclusiveGateway] :
  val elemKey: ElemKey = ElemKey.exclusiveGateways

  def withProcessNode(processNode: ProcessNode): ExclusiveGateway = 
    copy(processNode = processNode)

object ExclusiveGateway:
  def apply(ident: String): ExclusiveGateway =
    ExclusiveGateway(ProcessNode(ident))

case class ParallelGateway(
                            processNode: ProcessNode
                            //  inFlows: Seq[SequenceFlow] = Seq.empty,
                            //  outFlows: Seq[SequenceFlow] = Seq.empty
                          ) extends HasGateway[ParallelGateway]:
  val elemKey: ElemKey = ElemKey.parallelGateways

  def withProcessNode(processNode: ProcessNode): ParallelGateway = 
    copy(processNode = processNode)

object ParallelGateway:
  def apply(ident: String): ParallelGateway =
    ParallelGateway(ProcessNode(ident))