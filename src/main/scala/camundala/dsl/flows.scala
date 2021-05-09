package camundala.dsl

import camundala.model.*
import camundala.model.Condition.*
import camundala.model.ScriptImplementation.ScriptPath

trait flows:

  def sequenceFlow(ident: String) =
    SequenceFlow(Ident(ident))

  def flow(ident: String) =
    sequenceFlow(ident)

  extension(flow: SequenceFlow)

    def condition(cond: Condition): SequenceFlow = 
      flow.copy(condition = Some( cond))
    
    def expression(expr: String): SequenceFlow = 
      condition( ExpressionCond(expr))

    def groovy(scriptPath: ScriptPath): SequenceFlow = 
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
    