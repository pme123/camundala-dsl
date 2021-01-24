package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class SequenceFlow(ident: Ident,
                        condition: Option[Condition] = None,
                      //  properties: Properties = Properties.none
                       ) extends ProcessElement :

  def elemType: NodeKey = NodeKey.sequenceFlows

  def condition(cond: Condition) = copy(condition = Some(cond))
