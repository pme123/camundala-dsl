package camundala
package utest

import bpmn.*
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{
  assertThat,
  repositoryService,
  runtimeService,
  task
}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.assertEquals
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations

import scala.jdk.CollectionConverters.*

trait TestRunner:

  def config: TestConfig
  @Rule
  def processEngineRule = new ProcessEngineRule

  @Before
  def deployment(): Unit =
    val deployment = repositoryService().createDeployment()
    val resources = config.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r =>
      deployment.addInputStream(
        r.toString,
        new java.io.ByteArrayInputStream(os.read.bytes(r))
      )
    )
    deployment.deploy()

  @Before
  def setUp(): Unit =
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
        Process(InOutDescr(id, descr, in, out, hasManyOuts)),
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
      assertThat(processInstance).isEnded
      val variables = assertThat(processInstance).variables()
      for
        (k, v) <- out.asVars()
        _ = assertThat(processInstance).hasVariables(k)
      yield variables.containsEntry(k, v)
  end extension

  extension (userTask: UserTask[?, ?])
    def run(processInstance: ProcessInstance): Unit =
      val UserTask(InOutDescr(id, descr, in, out, hasManyOuts)) = userTask
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

  extension (product: Product)
    def names(): Seq[String] = product.productElementNames.toSeq

    def asVars(): Map[String, Any] =
      product.productElementNames
        .zip(product.productIterator)
        .toMap

    def asJavaVars(): java.util.Map[String, Any] =
      asVars().asJava
  end extension
