package camundala
package dsl

import camundala.dsl.inOutObjects
import camundala.model.HasOutputObject

trait inOutObjects:

  extension[T](hasInput: HasInputObject[T])
    def input(inout: InOutObject): T =
      hasInput.withInput(inout)

  end extension

  extension [T](hasOutput: HasOutputObject[T])

    def output(inout: InOutObject): T =
      hasOutput.withOutput(inout)

  end extension
end inOutObjects
