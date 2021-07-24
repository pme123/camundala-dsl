package camundala
package test

import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.junit.Before

trait TestHelper:

  extension(bpmnsConfig: BpmnsConfig)
    def deploymentResources: Set[String] =
      (bpmnsConfig.bpmns.deploymentResources ++ bpmnsConfig.dmns.deploymentResources).toSet

  end extension

  extension (dmns: Dmns)
    def deploymentResources: Seq[String] =
      dmns.dmns.map(_.path)
  end extension

  extension (bpmns: Bpmns)
    def deploymentResources: Seq[String] =
      bpmns.bpmns.flatMap(_.deploymentResources)
  end extension

  extension (bpmn: Bpmn)
    def deploymentResources: Seq[String] =
      bpmn.path +: bpmn.processes.deploymentResources

  end extension

  extension (bpmnProcesses: BpmnProcesses)
    def deploymentResources: Seq[String] = {
      bpmnProcesses.processes.flatMap(_.nodes.deploymentResources)
    }

  end extension

  extension (nodes: ProcessNodes)
    def deploymentResources: Seq[String] =
      nodes.nodes.collect { case hasForm: HasMaybeForm[_] =>
        hasForm.deploymentResource
      }.flatten
  end extension

  extension [A](maybeForm: HasMaybeForm[A])
    def deploymentResource: Option[String] =
      maybeForm.maybeForm.collect { case form: EmbeddedStaticForm =>
        s"static/${form.formPathStr.replace("embedded:app:", "")}"
      }
  end extension

  def bpmnsConfigToTest: BpmnsConfig
  @Before
  def deployment(): Unit =
    val deployment = repositoryService().createDeployment()
    val resources = bpmnsConfigToTest.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r => deployment.addInputStream(r, getClass().getClassLoader().getResourceAsStream(r)))
    deployment.deploy()

end TestHelper
