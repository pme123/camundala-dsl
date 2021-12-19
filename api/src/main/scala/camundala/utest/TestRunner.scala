package camundala
package utest

import domain.*
import bpmn.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{
  assertThat,
  task
}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.assertEquals
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException

trait TestRunner extends TestDsl:

  def config: TestConfig
  //@Rule
  // def processEngineRule = new ProcessEngineRule

  @Before
  def init(): Unit =
    deployment()
    setUpRegistries()

  def deployment(): Unit =
    val deployment = repositoryService.createDeployment()
    val resources = config.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r =>
      deployment.addInputStream(
        r.toString,
        new java.io.ByteArrayInputStream(os.read.bytes(r))
      )
    )
    deployment.deploy()

  def setUpRegistries(): Unit =
    MockitoAnnotations.initMocks(this)
    val serviceRegistries = config.serviceRegistries
    println(s"ServiceRegistries: $serviceRegistries")
    serviceRegistries.foreach { case ServiceRegistry(key, value) =>
      Mocks.register(key, value)
    }

  def test[
      In <: Product,
      Out <: Product
  ](process: Process[In, Out])(activities: Activity[?, ?, ?]*): Unit =
    ProcessToTest(process, activities.toList).run()

  extension (processToTest: ProcessToTest[?, ?])
    def run(): Unit =
      val ProcessToTest(
        Process(InOutDescr(id, descr, in, out)),
        activities
      ) = processToTest
      val processInstance = runtimeService.startProcessInstanceByKey(
        id,
        in.asJavaVars()
      )
      assertThat(processInstance)
        .isStarted()
      // run manual tasks
      activities.foreach {
        case ut: UserTask[?, ?] => ut.run(processInstance)
        //(a: Activity[?,?,?]) => a.run(processInstance)
        case other =>
          throw IllegalArgumentException(
            s"This Activity is not supported: $other"
          )
      }
      // check process outputs
      val variables = assertThat(processInstance).variables()
      for
        (k, v) <- out.asVars()
        _ = assertThat(processInstance).hasVariables(k)
      yield variables.containsEntry(k, v)
      assertThat(processInstance).isEnded
  end extension

  extension (userTask: UserTask[?, ?])
    def run(processInstance: ProcessInstance): Unit =
      val UserTask(InOutDescr(id, descr, in, out)) = userTask
      val t = task()
      assertThat(t)
        .hasDefinitionKey(id)
      /*    userTask.maybeForm.foreach {
        case EmbeddedForm(formKey) =>
          assertThat(t).hasFormKey(formKey.toString)
        case _ => // nothing to test
      }
      userTask.maybeAssignee.foreach(assignee =>
        assertThat(t).isAssignedTo(assignee.toString)
      )
      userTask.candidateGroups.groups.foreach(group =>
        assertThat(t).hasCandidateGroup(group.toString)
      )
      userTask.candidateUsers.users.foreach(user =>
        assertThat(t).hasCandidateUser(user.toString)
      )
      userTask.maybePriority.foreach(prio =>
        assertEquals(prio, t.getPriority) // no assertThat
      )
      userTask.maybeDueDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
      )
      userTask.maybeFollowUpDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
      )
       */
      BpmnAwareTests.complete(t, out.asJavaVars())
      assertThat(processInstance)
        .hasPassed(id)
  end extension

  // From ProcessEngineTestCase
  protected lazy val configurationResource: String = "camunda.cfg.xml"
  protected lazy val processEngine =
    TestHelper.getProcessEngine(configurationResource)
  protected lazy val repositoryService = processEngine.getRepositoryService
  protected lazy val runtimeService = processEngine.getRuntimeService

end TestRunner
