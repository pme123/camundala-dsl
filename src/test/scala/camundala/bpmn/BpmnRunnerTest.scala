package camundala.bpmn

import camundala.dsl.DSL
import zio.test.*
import zio.*
import Assertion.*
import zio.test.mock.MockConsole
import zio.test.mock.Expectation.*
import zio.test.mock.MockSystem
import zio.console.Console

object BpmnRunnerSuites 
  extends DefaultRunnableSpec 
  with DSL:
  
  def spec = suite("All tests")(runBpmn)

  lazy val runBpmn = suite("run BPMN")(
    testM("run process") {
      val mockEnv: ULayer[Console] = (
        MockConsole.PutStrLn(equalTo(s"Start Bpmn Runner"), unit) ++
          MockConsole.PutStrLn(startsWithString("Imported BPMN bpmn("))
        )
      val result = BpmnRunner(RunnerConfig(path("bpmns/process-cawemo.bpmn"), path("bpmns/with-ids/process-cawemo.bpmn"))).run()
        .provideLayer(mockEnv)
      assertM(result)(isUnit)
    }
  )
    

