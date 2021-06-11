package camundala.examples.twitter.bpmn

import camundala.dev.*

import camundala.dsl.DSL

import java.io.File
import camundala.model.BpmnsConfig

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


