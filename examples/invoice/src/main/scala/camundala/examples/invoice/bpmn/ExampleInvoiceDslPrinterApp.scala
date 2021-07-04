package camundala
package examples.invoice
package bpmn

object ExampleInvoiceDslPrinterApp extends zio.App, DSL:

  val projectFolder = "./examples/invoice"

  def run(args: List[String]) =
    runnerLogic.exitCode

  import ExampleInvoice._ 

  private lazy val runnerLogic =
    DslPrinterRunner(
      RunnerConfig(
        "ExampleInvoice2",
        projectFolder
      )
    ).run()
end ExampleInvoiceDslPrinterApp


