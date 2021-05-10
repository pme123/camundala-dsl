package camundala.dsl

import camundala.model.*

trait events:

  def startEvent(ident: String) =
    StartEvent(ident)

  def endEvent(ident: String) =
    EndEvent(ident)


