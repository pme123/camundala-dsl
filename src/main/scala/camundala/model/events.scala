package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class StartEvent(ident: Ident,
                      bpmnForm: Option[BpmnForm] = None)
  extends HasStringify
    with HasIdent
    with HasForm[StartEvent]
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}startEvent(${ident.stringify()})
       |${
        bpmnForm.map(_.stringify(intent + 1)).toSeq.mkString
    }""".stripMargin

  def elemType = NodeKey.startEvents
    
  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))


case class EndEvent(ident: Ident)
  extends HasStringify
    with HasIdent
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}endEvent(${ident.stringify()})""".stripMargin

  def elemType = NodeKey.endEvents
    