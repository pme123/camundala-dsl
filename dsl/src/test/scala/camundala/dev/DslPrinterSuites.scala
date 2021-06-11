package camundala.dev

import camundala.dsl.DSL.*
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console
import zio.test
import zio.test.mock.{Expectation, MockConsole, MockSystem}
import camundala.dev.demoProcess.bpmnsProjectConfig

object DslPrinterSuites extends DefaultRunnableSpec with DslPrinter:

  def spec = suite("DslPrinterSuites")(
    test("run printer") {

      val result = DemoProcessRunnerApp.demoConfig.print()
      println(result.asString())
      assert(result)(
        hasField(
          "prints",
          (p: Print) =>
            p match {
              case Print.PrintArray(prints, _) => prints.size
              case _ => -1
            }, equalTo(10)
        )
      )
    }
  )
