package camundala
package dsl

import InOutVariable.*

trait variables:

  extension[T](hasInVariables: HasInVariables[T])

    def inVariable(inout: InOutVariable): T =
      hasInVariables.withIns(
        hasInVariables.inVariables :+ inout
      )

    def inSource(sourceTarget: String): T =
      inSource(sourceTarget, sourceTarget)

    def inSource(source: String, target: String): T =
      inVariable(
        Source(
          VariableName(source),
          VariableName(target)
        )
      )

    def inSourceExpression(sourceExpression: String, target: String): T =
      inVariable(SourceExpression(sourceExpression, VariableName(target)))

    def inAll: T =
      inVariable(All())

    def inSourceLocal(sourceTarget: String): T =
      inSourceLocal(sourceTarget, sourceTarget)

    def inSourceLocal(source: String, target: String): T =
      inVariable(
        Source(
          VariableName(source),
          VariableName(target),
          true
        )
      )

    def inSourceExpressionLocal(sourceExpression: String, target: String): T =
      inVariable(SourceExpression(sourceExpression, VariableName(target), true))

    def inAllLocal: T =
      inVariable(All(true))

  end extension

  extension [T](hasOutVariables: HasOutVariables[T])

    def outVariable(inout: InOutVariable): T =
      hasOutVariables.withOuts(
        hasOutVariables.outVariables :+ inout
      )

    def outSource(sourceTarget: String): T =
      outSource(sourceTarget, sourceTarget)

    def outSource(source: String, target: String): T =
      outVariable(
        Source(
          VariableName(source),
          VariableName(target)
        )
      )
    def outSourceExpression(sourceExpression: String, target: String): T =
      outVariable(SourceExpression(sourceExpression, VariableName(target)))

    def outAll: T =
      outVariable(All())

    def outSourceLocal(sourceTarget: String): T =
      outSourceLocal(sourceTarget, sourceTarget)

    def outSourceLocal(source: String, target: String): T =
      outVariable(
        Source(
          VariableName(source),
          VariableName(target),
          true
        )
      )
      
    def outSourceExpressionLocal(sourceExpression: String, target: String): T =
      outVariable(
        SourceExpression(sourceExpression, VariableName(target), true)
      )

    def outAllLocal: T =
      outVariable(All(true))

  end extension
end variables
