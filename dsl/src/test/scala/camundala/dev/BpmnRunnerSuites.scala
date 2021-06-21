package camundala
package dev
package test

object BpmnRunnerSuites extends DefaultRunnableSpec with DSL:

  def spec = suite("BpmnRunnerSuites")(
    testM("run process") {
      val mockEnv: ULayer[Console] = (
        MockConsole.PutStrLn(equalTo(s"Start Bpmn Runner"), unit) ++
          MockConsole.PutStrLn(equalTo(s"Start DSL Printer"), unit) ++
          MockConsole.PutStrLn(
            equalTo(s"Start From Camunda BPMNs from dsl/src/test/cawemo"),
            unit
          ) ++
          MockConsole.PutStrLn(
            equalTo(s"Generated BPMNs to ./dsl/src/test/cawemo/with-ids"),
            unit
          ) ++
          MockConsole.PutStrLn(
            equalTo(s"/* **************************************** */"),
            unit
          ) ++
          MockConsole.PutStrLn(containsString("bpmn(\"demo__process\")")) ++
          MockConsole.PutStrLn(
            equalTo(s"/* **************************************** */"),
            unit
          ) ++
          MockConsole.PutStrLn(
            equalTo(s"DSL Printed - copy content above to Scala file"),
            unit
          ) ++
          MockConsole.PutStrLn(
            startsWithString("** Compare Audit Log:     **\n")
          ) ++
          MockConsole.PutStrLn(
            equalTo(
              s"Generated BPMN to ${DemoProcessRunnerApp.generatedFolderPath}"
            )
          )
      )
      val result = DemoProcessRunnerApp.runnerLogic
        .provideLayer(mockEnv)
      assertM(result)(isUnit)
    }
  )
