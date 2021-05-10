package camundala.model

import camundala.model.BpmnProcess.ElemKey

sealed trait HasEvent[T] extends HasProcessNode[T]

case class StartEvent(
    processNode: ProcessNode,
    bpmnForm: Option[BpmnForm] = None
) extends HasEvent[StartEvent]
    with HasForm[StartEvent]:

  def elemKey = ElemKey.startEvents

  def withProcessNode(processNode: ProcessNode): StartEvent = 
    copy(processNode = processNode)

  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))

object StartEvent:
  def apply(ident: String): StartEvent =
    StartEvent(ProcessNode(ident))

case class EndEvent(
    processNode: ProcessNode,
    inputParameters: Seq[InOutParameter] = Seq.empty
) extends HasEvent[EndEvent]
    with HasInputParameters[EndEvent]:

  def elemKey = ElemKey.endEvents

  def inputs(params: InOutParameter*): EndEvent = copy(inputParameters = params)

  def withProcessNode(processNode: ProcessNode): EndEvent = 
    copy(processNode = processNode)

object EndEvent:
  def apply(ident: String): EndEvent =
    EndEvent(ProcessNode(ident))
