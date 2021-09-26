package camundala
package examples.twitter
package bpmn

object ExampleTwitterRunnerApp extends zio.App, DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  import TwitterProcesses._ 

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        "ExampleTwitter",
        path(cawemoFolder),
        path(withIdFolder),
        path(generatedFolder),
        config
      )
    ).run()
end ExampleTwitterRunnerApp


