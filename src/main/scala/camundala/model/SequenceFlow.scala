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

  def condition(cond: Condition) = copy(condition = Some(cond))

  def expression(expr: String) = copy(condition = Some(ExpressionCond(expr)))

  def groovy(scriptPath: ScriptPath) = copy(condition = Some(ScriptCond(s"$scriptPath.groovy")))

  def inlineGroovy(script: String) = copy(condition = Some(InlineScriptCond(script)))

  