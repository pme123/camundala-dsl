package camundala
package dsl

import Condition.*

trait flows:

  def sequenceFlow(ident: String) =
    SequenceFlow(ident)

  def flow(ident: String) =
    sequenceFlow(ident)

  extension(flow: SequenceFlow)

    def condition(cond: Condition): SequenceFlow = 
      flow.copy(condition = Some( cond))
    
    def expression(expr: String): SequenceFlow = 
      condition( ExpressionCond(expr))

    def groovy(scriptPath: ScriptImplementation.ScriptPath): SequenceFlow =
      condition( ScriptCond(s"$scriptPath.groovy"))

    def inlineGroovy(script: String): SequenceFlow = 
      condition(InlineScriptCond(script))

  def exclusiveGateway(ident: String) =
    ExclusiveGateway(ident)
    
  extension (exclGateway: ExclusiveGateway)
    def defaultFlow(ref: ProcessElementRef): ExclusiveGateway = 
      exclGateway.copy(defaultFlow = Some(ref))

  def parallelGateway(ident: String) = 
    ParallelGateway(ident)
    