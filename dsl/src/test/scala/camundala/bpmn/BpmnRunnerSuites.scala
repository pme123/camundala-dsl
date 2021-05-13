package camundala.bpmn

import camundala.dsl.DSL
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console
import camundala.bpmn.demoProcess.demoBpmn

object BpmnRunnerSuites
  extends DefaultRunnableSpec
    with DSL :

  def spec = suite("BpmnRunnerSuites")(
    testM("run process") {
      val mockEnv: ULayer[Console] = (
        MockConsole.PutStrLn(equalTo(s"Start Bpmn Runner"), unit) ++
          MockConsole.PutStrLn(containsString("bpmn(\"./dsl/src/test/cawemo/with-ids/process-cawemo.bpmn\")")) ++
          MockConsole.PutStrLn(startsWithString("** Compare Audit Log:     **\n")) ++
          MockConsole.PutStrLn(equalTo("Generated BPMN to ./dsl/src/test/cawemo/output/process-cawemo.bpmn"))
        )
      val result = BpmnRunner(RunnerConfig(
        path(DemoProcessRunnerApp.demoProcessPath),
        demoBpmn,
        path(DemoProcessRunnerApp.demoProcessOutputPath)
      )).run()
        .provideLayer(mockEnv)
      assertM(result)(isUnit)
    }
  )



