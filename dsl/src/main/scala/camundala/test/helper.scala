package camundala
package test

import org.camunda.bpm.engine.repository.DeploymentBuilder
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
        getClass().getClassLoader().getResourceAsStream(r)
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

  extension (testCase: BpmnTestCase)
    def run(): Unit =
      testCase.steps.foreach(_.run())
  end extension

  extension (step: BpmnTestStep)
    def run(): Unit =
      step match
        case st: StartProcessStep =>
          st.run()
        case st: UserTaskStep =>
          st.run()
  end extension

  extension (step: StartProcessStep)
    def run(): Unit =
      val StartProcessStep(startEvent, in) = step
      val processInstance = runtimeService.startProcessInstanceByKey(
        tester.process.identStr,
        in.asJavaVars()
      )
      assertThat(processInstance)
        .isStarted()
        .hasPassed(startEvent.identStr)
  end extension

  extension (step: UserTaskStep)
    def run(): Unit =
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
       assertEquals(prio, t.getPriority()) // no assertThat
      )
      userTask.maybeDueDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
        )
      userTask.maybeFollowUpDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
        )
      BpmnAwareTests.complete(t, data.asJavaVars())

  end extension

end TestHelper
