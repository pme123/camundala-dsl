package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class StartEvent(ident: Ident,
                      bpmnForm: Option[BpmnForm] = None)
  extends HasIdent
    with HasMaybeForm[StartEvent]
    with ProcessElement :

  def elemType = NodeKey.startEvents
  
  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))

  def form(formKey: Ident): StartEvent = copy(bpmnForm = Some(EmbeddedForm(formKey)))

