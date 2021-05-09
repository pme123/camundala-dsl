package camundala.model

import camundala.model.BpmnProcess.ElemKey
sealed trait Event extends HasIdent with ProcessNode

case class StartEvent(
    ident: Ident,
    bpmnForm: Option[BpmnForm] = None,
    isAsyncBefore: Boolean = false,
    isAsyncAfter: Boolean = false
) extends Event
    with HasForm[StartEvent]:

  def elemType = ElemKey.startEvents

  def asyncBefore: StartEvent = copy(isAsyncBefore = true)

  def asyncAfter: StartEvent = copy(isAsyncAfter = true)

  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))

case class EndEvent(
    ident: Ident,
    inputParameters: Seq[InOutParameter] = Seq.empty,
    isAsyncBefore: Boolean = false,
    isAsyncAfter: Boolean = false
) extends Event
    with HasInputParameters[EndEvent]:

  def elemType = ElemKey.endEvents

  def inputs(params: InOutParameter*): EndEvent = copy(inputParameters = params)

  def asyncBefore: EndEvent = copy(isAsyncBefore = true)

  def asyncAfter: EndEvent = copy(isAsyncAfter = true)
