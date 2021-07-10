package camundala
package examples.invoice
package bpmn

object ExampleInvoice2RunnerApp extends zio.App, DSL:


  def run(args: List[String]) =
    runnerLogic.exitCode

  import ExampleInvoice2._


  private lazy val runnerLogic =

    BpmnRunner(
      RunnerConfig(
        "ExampleInvoice2",
        path(cawemoFolder),
        path(withIdFolder),
        path(generatedFolder),
        config
      )
    ).run()

end ExampleInvoice2RunnerApp


