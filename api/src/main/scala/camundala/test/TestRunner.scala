package camundala
package test

import domain.*
import bpmn.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{assertThat, managementService, repositoryService, runtimeService, task}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.{assertEquals, assertNotNull}
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations

import scala.jdk.CollectionConverters.*
import java.io.FileNotFoundException
import java.util

trait TestRunner extends TestDsl:

  def config: TestConfig
  @Rule
  def processEngineRule = new ProcessEngineRule

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
  ](process: Process[In, Out])(
      activities: (Activity[?, ?, ?] | CustomTests)*
  ): Unit =
    ProcessToTest(process, activities.toList).run()

  def custom(tests: => Unit): CustomTests = CustomTests(() => tests)

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
        case st: ServiceTask[?, ?] => st.run(processInstance)
        case dd: DecisionDmn[?, ?] => dd.run(processInstance)
        //(a: Activity[?,?,?]) => a.run(processInstance)
        case ct: CustomTests => ct.tests()
        case other =>
          throw IllegalArgumentException(
            s"This Activity is not supported: $other"
          )
      }
      checkOutput(out, processInstance)
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

  extension (serviceTask: ServiceTask[?, ?])
    def run(processInstance: ProcessInstance): Unit =
      val ServiceTask(InOutDescr(id, descr, in, out)) = serviceTask
      val archiveInvoiceJob = managementService.createJobQuery.singleResult
      assertNotNull(archiveInvoiceJob)
      managementService.executeJob(archiveInvoiceJob.getId)
      assertThat(processInstance)
        .hasPassed(id)
  end extension

  extension (decisionDmn: DecisionDmn[?, ?])
    def run(processInstance: ProcessInstance): Unit =
      val DecisionDmn(key, hitPolicy, InOutDescr(id, descr, in, out)) =
        decisionDmn

      checkOutput(out, processInstance)
  end extension

  private def checkOutput[T <: Product](
      out: T,
      processInstance: ProcessInstance
  ) =
    val variables = assertThat(processInstance).variables()
    for
      (k, v) <- out.asJavaVars().asScala
      _ = assertThat(processInstance).hasVariables(k)
    yield variables.containsEntry(k, v)

end TestRunner
