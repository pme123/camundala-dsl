package camundala
package test

import camundala.model.HasTaskImplementation
import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.{After, Before}
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*

trait TestHelper:
/*
  extension(bpmnsConfig: BpmnsConfig)
    def deploymentResources: Set[String] =
      (bpmnsConfig.bpmns.deploymentResources ++ bpmnsConfig.dmns.deploymentResources).toSet
    def serviceRegistries: Set[ServiceRegistry] =
      bpmnsConfig.bpmns.serviceRegistries.toSet
  end extension

  extension (dmns: Dmns)
    def deploymentResources: Seq[String] =
      dmns.dmns.map(_.path)
  end extension

  extension (bpmns: Bpmns)
    def deploymentResources: Seq[String] =
      bpmns.bpmns.flatMap(_.deploymentResources)
    def serviceRegistries: Seq[ServiceRegistry] =
      bpmns.bpmns.flatMap(_.serviceRegistries)
  end extension

  extension (bpmn: Bpmn)
    def deploymentResources: Seq[String] =
      bpmn.path +: bpmn.processes.deploymentResources

    def serviceRegistries: Seq[ServiceRegistry] =
      bpmn.processes.serviceRegistries

  end extension

  extension (bpmnProcesses: BpmnProcesses)
    def deploymentResources: Seq[String] =
      bpmnProcesses.processes.flatMap(_.nodes.deploymentResources)

    def serviceRegistries: Seq[ServiceRegistry] =
      bpmnProcesses.processes.flatMap(_.nodes.serviceRegistries)

  end extension

  extension (nodes: ProcessNodes)
    def deploymentResources: Seq[String] =
      nodes.nodes.collect { case hasForm: HasMaybeForm[_] =>
        hasForm.deploymentResource
      }.flatten
    def serviceRegistries: Seq[ServiceRegistry] =
      nodes.nodes.collect { case hasTaskImpl: HasTaskImplementation[_] =>
        hasTaskImpl.serviceRegistry
      }.flatten
  end extension

  extension [A](maybeForm: HasMaybeForm[A])
    def deploymentResource: Option[String] =
      maybeForm.maybeForm.collect { case form: EmbeddedStaticForm =>
        s"static/${form.formPathStr.replace("embedded:app:", "")}"
      }
  end extension
  
  extension [A](hasTaskImpl: HasTaskImplementation[A])
    def serviceRegistry: Option[ServiceRegistry] =
      import TaskImplementation.DelegateExpression
      hasTaskImpl.taskImplementation match {
        case de: DelegateExpression =>
          Some(ServiceRegistry(de.expression, mock(de.getClass)))
        case _ => None  
      }

  end extension
*/
//  def bpmnsConfigToTest: BpmnsConfig
  def tester: BpmnProcessTester

  @Before
  def deployment(): Unit =
    val deployment = repositoryService().createDeployment()
    val resources = tester.testConfig.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r => deployment.addInputStream(r, getClass().getClassLoader().getResourceAsStream(r)))
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

end TestHelper
