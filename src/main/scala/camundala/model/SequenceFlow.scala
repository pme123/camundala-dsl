package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.Condition._

case class SequenceFlows(flows: Seq[SequenceFlow])
  extends ProcessElements :
  
  val elements: Seq[ProcessElement] = flows
  
  def stringify(intent: Int): String =
    stringifyWrap(intent, ".flows", flows)

  def :+(process: SequenceFlow): SequenceFlows = SequenceFlows(flows :+ process)

object SequenceFlows:
  def none = SequenceFlows(Nil)


case class SequenceFlow(ident: Ident,
                        condition: Option[Condition] = None,
                        properties: Properties = Properties.none
                       )
  extends ProcessElement 
  with HasProperties[SequenceFlow]:
  
  def stringify(intent: Int):String = s"""${intentStr(intent)}sequenceFlow("${ident}")"""
  def prop(prop: Property): SequenceFlow = copy(properties = properties :+ prop)

  def elemType: NodeKey = NodeKey.sequenceFlows


  