package camundala
package examples.invoice
package bpmn

object ExampleInvoiceRunnerApp extends zio.App, DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  import ExampleInvoice._ 

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        "ExampleInvoice",
        path(cawemoFolder),
        path(withIdFolder),
        path(generatedFolder),
        config
      )
    ).run()
end ExampleInvoiceRunnerApp


