package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.Condition._
import camundala.model.ScriptImplementation.ScriptPath

case class SequenceFlow(ident: Ident,
                        condition: Option[Condition] = None,
                        //  properties: Properties = Properties.none
                       )extends ProcessElement :
  def stringify(intent: Int):String = "sequenceFlow----"

  def elemType: NodeKey = NodeKey.sequenceFlows


  