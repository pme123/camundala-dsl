package camundala
package test

import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.{After, Before, Rule, Test}
import org.junit.Assert.assertEquals
import org.mockito.MockitoAnnotations
import org.mockito.Mockito

trait TestHelper:

  def tester: BpmnProcessTester
  @Rule
  def processEngineRule = new ProcessEngineRule

  @Before
  def deployment(): Unit =
    val deployment = repositoryService().createDeployment()
    val resources = tester.testConfig.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r =>
      deployment.addInputStream(
        r,
        getClass.getClassLoader.getResourceAsStream(r)
      )
    )
    deployment.deploy()

  @Before
  def setUp(): Unit =
    MockitoAnnotations.initMocks(this)
    val serviceRegistries = tester.testConfig.serviceRegistries
    println(s"ServiceRegistries: $serviceRegistries")
    serviceRegistries.foreach { case ServiceRegistry(key, value) =>
      Mocks.register(key, value)
    }

  @After def tearDown(): Unit =
    Mocks.reset()

  def testCase(steps: BpmnTestStep*): Unit =
    steps.foldLeft(Option.empty[ProcessInstance])((a, b) => b.run(a))

  extension (step: BpmnTestStep)
    def run(processInstance: Option[ProcessInstance]): Option[ProcessInstance] =
      (step, processInstance) match
        case (st: StartProcessStep, None) =>
          Some(st.run())
        case (st: UserTaskStep, Some(pi)) =>
          st.run(pi)
          Some(pi)
        case (st: ServiceTaskStep, Some(pi)) =>
          st.run(pi)
          Some(pi)
        case (st: EndStep, Some(pi)) =>
          st.run(pi)
          None
        case (st, pi) =>
          throw IllegalArgumentException(
            s"This TestStep combination is not supported: $st - $pi"
          )
  end extension

  extension (step: StartProcessStep)
    def run(): ProcessInstance =
      val StartProcessStep(startEvent, in) = step
      val processInstance = runtimeService.startProcessInstanceByKey(
        tester.process.identStr,
        in.asJavaVars()
      )
      assertThat(processInstance)
        .isStarted()
        .hasPassed(startEvent.identStr)
      processInstance
  end extension

  extension (step: UserTaskStep)
    def run(processInstance: ProcessInstance): Unit =
      val UserTaskStep(userTask, data) = step
      val t = task()
      assertThat(t)
        .hasDefinitionKey(userTask.identStr)
      userTask.maybeForm.foreach {
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
      BpmnAwareTests.complete(t, data.asJavaVars())
      assertThat(processInstance)
        .hasPassed(userTask.identStr)
  end extension

  extension (step: ServiceTaskStep)
    def run(processInstance: ProcessInstance): Unit =
      val ServiceTaskStep(serviceTask, data) = step
      assertThat(processInstance)
        .hasPassed(serviceTask.identStr)
  end extension

  extension (step: EndStep)
    def run(processInstance: ProcessInstance): Unit =
      val EndStep(endEvent, datas) = step
      assertThat(processInstance)
        .hasPassed(endEvent.identStr)
        .isEnded
      val variables = assertThat(processInstance).variables()
      for
        d <- datas
        _ = assertThat(processInstance).hasVariables(d.names(): _*)
        (k, v) <- d.asVars()
      yield variables.containsEntry(k, v)
  end extension

  private def processInstance() =
    runtimeService().createProcessInstanceQuery().active().singleResult()

end TestHelper
