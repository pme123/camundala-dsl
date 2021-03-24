package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.Condition._
import camundala.model.ScriptImplementation.ScriptPath

case class SequenceFlow(ident: Ident,
                        condition: Option[Condition] = None,
                        properties: Properties = Properties.none
                       )
  extends ProcessElement 
  with HasProperties[SequenceFlow]:
  
  def stringify(intent: Int):String = s"""${intentStr(intent)}sequenceFlow("${ident}")"""
  def prop(prop: Property): SequenceFlow = copy(properties = properties :+ prop)

  def elemType: NodeKey = NodeKey.sequenceFlows


  