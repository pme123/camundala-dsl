package camundala.bpmn

import camundala.dsl.DSL
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console
import camundala.examples.demoProcess.demoBpmn

object BpmnRunnerSuites
  extends DefaultRunnableSpec
    with DSL :

  def spec = suite("BpmnRunnerSuites")(
    testM("run process") {
      val mockEnv: ULayer[Console] = (
        MockConsole.PutStrLn(equalTo(s"Start Bpmn Runner"), unit) ++
          MockConsole.PutStrLn(containsString("bpmn(\"bpmns/with-ids/process-cawemo.bpmn\")")) ++
          MockConsole.PutStrLn(startsWithString("** Compare Audit Log:     **\n")) ++
          MockConsole.PutStrLn(equalTo("Generated BPMN to bpmns/process-generated.bpmn"))
        )
      val result = BpmnRunner(RunnerConfig(
        path("bpmns/process-cawemo.bpmn"),
        demoBpmn,
        path("bpmns/process-generated.bpmn")
      )).run()
        .provideLayer(mockEnv)
      assertM(result)(isUnit)
    }
  )



