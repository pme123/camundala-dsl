package camundala.dsl

import camundala.model._
import VariableAssignment._
import ScriptImplementation._

trait parameters:

  extension[T](hasInputParams: HasInputParameters[T])

    def inputParam(inout: InOutParameter): T =
      hasInputParams.withInputs(
        hasInputParams.inputParameters :+ inout
      )

    def inputString(key: String, value: String): T =
      inputParam(
        InOutParameter(
          Name(key),
          StringVal(value)
        )
      )

    def inputExpression(key: String, value: String): T =
      inputParam(
        InOutParameter(
          Name(key),
          Expression(value)
        )
      )

    def inputGroovy(key: String, resource: ScriptPath): T =
      inputParam(
        InOutParameter(
          Name(key),
          ExternalScript(ScriptLanguage.Groovy, resource)
        )
      )

    def inputGroovyInline(key: String, script: String): T =
      inputParam(
        InOutParameter(
          Name(key),
          InlineScript(ScriptLanguage.Groovy, script)
        )
      )
  end extension

  extension [T](hasOutputParams: HasOutputParameters[T])

    def outputParam(inout: InOutParameter): T =
      hasOutputParams.withOutputs(
        hasOutputParams.outputParameters :+ inout
      )

    def outputString(key: String, value: String): T =
      outputParam(
        InOutParameter(
          Name(key),
          StringVal(value)
        )
      )

    def outputExpression(key: String, value: String): T =
      outputParam(
        InOutParameter(
          Name(key),
          Expression(value)
        )
      )

    def outputGroovy(key: String, resource: ScriptPath): T =
      outputParam(
        InOutParameter(
          Name(key),
          ExternalScript(ScriptLanguage.Groovy, resource)
        )
      )

    def outputGroovyInline(key: String, script: String): T =
      outputParam(
        InOutParameter(
          Name(key),
          InlineScript(ScriptLanguage.Groovy, script)
        )
      )
  end extension
end parameters
