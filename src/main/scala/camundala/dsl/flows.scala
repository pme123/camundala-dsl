package camundala.dsl

import camundala.model.*
import camundala.model.Condition.*
import camundala.model.ScriptImplementation.ScriptPath

trait flows:

  type FlowAttr = Condition
  
  def sequenceFlow(ident: Ident,
                   flowAttrs: FlowAttr*) =
    SequenceFlow(ident,
      condition = flowAttrs.collect { case c: Condition => c }.headOption
    )

  def flow(ident: Ident,
           flowAttrs: FlowAttr*) =
    sequenceFlow(ident, flowAttrs:_*)

  def expressionCond(expr: String) = ExpressionCond(expr)

  def groovyCond(scriptPath: ScriptPath) = ScriptCond(s"$scriptPath.groovy")

  def inlineGroovyCond(script: String) = InlineScriptCond(script)