package camundala.dsl

import camundala.model.*

trait events:
  def startEvent(ident: Ident) =
    StartEvent(ident)

  def startEvent(ident: Ident, form: BpmnForm) =
    StartEvent(ident, Some(form))


