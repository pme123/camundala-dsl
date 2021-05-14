package camundala.dev

import camundala.dsl.DSL
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console
import zio.test
import zio.test.mock.{Expectation, MockConsole, MockSystem}
import camundala.dev.demoProcess.demoBpmn

object DslPrinterSuites extends DefaultRunnableSpec with DslPrinter:

  def spec = suite("DslPrinterSuites")(
    test("run printer") {
      val result = demoBpmn.print()
      println(result.asString(0))
      assert(result)(
        hasField(
          "lines",
          (p: Print) =>
            p match {
              case Print.PrintObject(lines) => lines.size
              case _ => -1
            }, equalTo(2)
        )
      )
    }
  )
