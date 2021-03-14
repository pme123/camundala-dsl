package camundala.model

import camundala.model.BpmnProcess.NodeKey

case class StartEvent(ident: Ident,
                      bpmnForm: Option[BpmnForm] = None)
  extends HasStringify
    with HasIdent
    with HasForm[StartEvent]
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}startEvent(
       |${
      (Seq(ident.stringify(intent + 1)) ++
        bpmnForm.map(_.stringify(intent + 1)).toSeq).mkString(",\n")
    }
       |${intentStr(intent)})""".stripMargin

  def elemType = NodeKey.startEvents
    
  def form(form: BpmnForm): StartEvent = copy(bpmnForm = Some(form))


