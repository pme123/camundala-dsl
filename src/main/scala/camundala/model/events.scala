package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class StartEvent(ident: Ident,
                      bpmnForm: Option[BpmnForm] = None,
                      isAsyncBefore: Boolean = false,
                      isAsyncAfter: Boolean = false
                     )
  extends HasStringify
    with HasIdent
    with HasForm[StartEvent]
    with ProcessNode :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}startEvent(${ident.stringify()})${
      bpmnForm.map("\n" + _.stringify(intent + 1)).toSeq.mkString
    }""".stripMargin

  def elemType = NodeKey.startEvents

  def asyncBefore: StartEvent = copy(isAsyncBefore = true)

  def asyncAfter: StartEvent = copy(isAsyncAfter = true)

  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))


case class EndEvent(ident: Ident,
                    isAsyncBefore: Boolean = false,
                    isAsyncAfter: Boolean = false
                   )
  extends HasStringify
    with HasIdent
    with ProcessNode :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}endEvent(${ident.stringify()})""".stripMargin

  def elemType = NodeKey.endEvents

  def asyncBefore: EndEvent = copy(isAsyncBefore = true)

  def asyncAfter: EndEvent = copy(isAsyncAfter = true)
  