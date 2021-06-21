package camundala
package dev
package test

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
