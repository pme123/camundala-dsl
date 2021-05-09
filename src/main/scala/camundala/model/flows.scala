package camundala.model

import camundala.model.BpmnProcess.ElemKey
import camundala.model.Condition.*

case class SequenceFlows(flows: Seq[SequenceFlow]) extends ProcessElements:

  val elements: Seq[HasProcessElement[_]] = flows

  def :+(process: SequenceFlow): SequenceFlows = SequenceFlows(flows :+ process)

object SequenceFlows:
  def none = SequenceFlows(Nil)

case class SequenceFlow(
    processElement: ProcessElement,
    condition: Option[Condition] = None
) extends HasProcessElement[SequenceFlow]
    with HasProperties[SequenceFlow]:

  def elemKey: ElemKey = ElemKey.sequenceFlows

object SequenceFlow :
  
  def apply(ident: Ident): SequenceFlow =
    SequenceFlow(ProcessElement(ident))