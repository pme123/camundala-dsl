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
      inputParam(InOutParameter(
          Name(key),
          StringVal(value)
        )
      )

    def inputExpression(key: String, value: String): T  =
      inputParam(InOutParameter(
          Name(key),
          Expression(value)
        )
      )
    
    def inputGroovy(key: String, resource: ScriptPath): T  =
      inputParam(InOutParameter(
          Name(key),
          ExternalScript(ScriptLanguage.Groovy, resource)
        )
      )
    
    def inputGroovyInline(key: String, script: String): T  =
      inputParam(InOutParameter(
          Name(key),
          InlineScript(ScriptLanguage.Groovy, script)
        )
      )
