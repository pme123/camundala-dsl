package camundala
package examples.invoice.bpmn

import bpmn.*
import domain.*
import camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object InvoiceGenerateCamundaBpmnApp extends GenerateCamundaBpmn, App:

  val projectPath = pwd / "examples" / "invoice"
  import InvoiceApi.*

  run(invoiceBpmn, Bpmn(withIdPath / "reviewInvoice.bpmn", ReviewInvoiceP))

  private lazy val invoiceBpmn: Bpmn = Bpmn(
    withIdPath / "invoice.v2.bpmn",
    InvoiceReceiptP
      .withElements(reviewInvoiceCA)
  )

end InvoiceGenerateCamundaBpmnApp
