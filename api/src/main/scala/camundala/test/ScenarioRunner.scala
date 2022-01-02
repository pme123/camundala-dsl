package camundala
package test

import camundala.bpmn.*
import camundala.domain.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.scenario.{ProcessScenario, Scenario}
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.util
import scala.jdk.CollectionConverters.*

trait ScenarioRunner extends CommonTesting:

  def test[
      In <: Product,
      Out <: Product
  ](process: Process[In, Out])(
      elements: (ProcessNode | CustomTests)*
  ): Unit =
    ProcessToTest(process, elements.toList).run()

  lazy val mockedProcess = mock(classOf[ProcessScenario])

  extension (processToTest: ProcessToTest[?, ?])

    def run(): Unit =
      val scenario = prepare()
      exec(scenario)

    private def prepare(): Scenario =
      val ProcessToTest(p: Process[?,?], elements) = processToTest
      println(s"HEAD: ${p.elements.headOption}")
      elements.foreach {
        case ut: UserTask[?, ?] => ut.prepare()
        case st: ServiceTask[?, ?] => st.prepare()
        case dd: DecisionDmn[?, ?] => dd.prepare()
        case ee: EndEvent => ee.prepare()
        case ct: CustomTests => // nothing to prepare
        case other =>
          throw new IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      p.prepare()

    private def exec(scenario: Scenario): Unit =
      val ProcessToTest(process, elements) = processToTest
      implicit val processInstance = scenario.instance(mockedProcess)
      elements.foreach {
        case ut: UserTask[?, ?] => ut.exec()
        case st: ServiceTask[?, ?] => st.exec()
        case dd: DecisionDmn[?, ?] => dd.exec()
        case ee: EndEvent => ee.exec()
        case ct: CustomTests => // not supported
        case other =>
          throw IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      process.exec()

  end extension

  extension (process: Process[?, ?])
    def prepare(): Scenario =
      val Process(InOutDescr(id, in, out, descr), _) = process
      Scenario
        .run(mockedProcess)
        .startByKey(process.id, process.in.asJavaVars())
        .execute()

    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance]).isEnded
      checkOutput(process.out)

  end extension

  extension (userTask: UserTask[?, ?])
    def prepare(): Unit =
      val UserTask(InOutDescr(id, in, out, descr)) = userTask
      when(mockedProcess.waitsAtUserTask(userTask.id))
        .thenReturn((task) => task.complete(userTask.out.asJavaVars()))
    def exec(): FromProcessInstance[Unit] = ()
  end extension

  extension (serviceTask: ServiceTask[?, ?])
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] = ()
  end extension

  extension (decisionDmn: DecisionDmn[?, ?])
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] = ()
  end extension

  extension (endEvent: EndEvent)
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] =
      verify(mockedProcess).hasFinished(endEvent.id)
  end extension

end ScenarioRunner
