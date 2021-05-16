package camundala.dsl

import camundala.model._

trait transactions :

  extension [T](hasTransBoundary: HasTransactionBoundary[T])

    def unary_~ : T = hasTransBoundary.asyncBefore

    def ~ : T = hasTransBoundary.asyncAfter
