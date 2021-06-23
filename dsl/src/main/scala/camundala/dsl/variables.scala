package camundala
package dsl

import InOutVariable.*

trait variables:

  extension[T](hasInVariables: HasInVariables[T])

    def inVariable(inout: InOutVariable): T =
      hasInVariables.withIns(
        hasInVariables.inVariables :+ inout
      )

    def inSource(source: String, target: String): T =
      inVariable(
        Source(
          VariableName(source),
          VariableName(target)
        )
      )
    def inSourceExpression(sourceExpression: String, target: String): T =
      inVariable(SourceExpression(sourceExpression, VariableName(target)))

    def inAll(): T =
      inVariable(All)

  end extension

  extension [T](hasOutVariables: HasOutVariables[T])

    def outVariable(inout: InOutVariable): T =
      hasOutVariables.withOuts(
        hasOutVariables.outVariables :+ inout
      )

    def outSource(source: String, target: String): T =
      outVariable(
        Source(
          VariableName(source),
          VariableName(target)
        )
      )
    def outSourceExpression(sourceExpression: String, target: String): T =
      outVariable(SourceExpression(sourceExpression, VariableName(target)))

    def outAll(): T =
      outVariable(All)

  end extension
end variables
