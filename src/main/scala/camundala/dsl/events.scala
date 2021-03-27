package camundala.dsl

import camundala.model.*

trait events:

  def startEvent(ident: String) =
    StartEvent(Ident(ident))

  
  def endEvent(ident: String) =
    EndEvent(Ident(ident))


