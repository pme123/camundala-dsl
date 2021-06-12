package camundala
package examples.twitter
package bpmn

object ExampleTwitterDslPrinterApp extends zio.App, DSL:

  val projectFolder = "./examples/twitter"

  def run(args: List[String]) =
    runnerLogic.exitCode

  import ExampleTwitter._ 

  private lazy val runnerLogic =
    DslPrinterRunner(
      RunnerConfig(
        "ExampleTwitter2",
        projectFolder
      )
    ).run()
end ExampleTwitterDslPrinterApp


