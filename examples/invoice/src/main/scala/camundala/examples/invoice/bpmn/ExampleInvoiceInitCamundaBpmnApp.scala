package camundala
package examples.invoice.bpmn

import camunda.*
import os.pwd

object ExampleInvoiceInitCamundaBpmnApp extends InitCamundaBpmn:

  val projectPath = pwd / "examples" / "invoice"

  run()

  import ExampleInvoice2.*

end ExampleInvoiceInitCamundaBpmnApp


