package camundala
package examples.invoice.bpmn

import camunda.*
import os.pwd

object InvoiceInitCamundaBpmnApp extends InitCamundaBpmn, App:

  val projectPath = pwd / "examples" / "invoice"

  run("Invoice")

end InvoiceInitCamundaBpmnApp


