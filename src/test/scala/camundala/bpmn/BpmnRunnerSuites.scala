package camundala.bpmn

import camundala.dsl.DSL
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console
import BpmnRunnerApp.demoBpmn

object BpmnRunnerSuites
  extends DefaultRunnableSpec
    with DSL :

  def spec = suite("BpmnRunnerSuites")(
    testM("run process") {
      val mockEnv: ULayer[Console] = (
        MockConsole.PutStrLn(equalTo(s"Start Bpmn Runner"), unit) ++
          MockConsole.PutStrLn(startsWithString("** Generated BPMN DSL:     **")) ++
          MockConsole.PutStrLn(startsWithString("** Compare Audit Log:     **\n"))
        )
      val result = BpmnRunner(RunnerConfig(
        path("bpmns/process-cawemo.bpmn"),
        path("bpmns/with-ids/process-cawemo.bpmn"),
        demoBpmn
      )).run()
        .provideLayer(mockEnv)
      assertM(result)(isUnit)
    }
  )
    

