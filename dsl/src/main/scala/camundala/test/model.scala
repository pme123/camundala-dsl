package camundala
package test

import camundala.model.{BpmnProcess, EndEvent, ServiceTask}

import collection.JavaConverters.*

case class TestConfig(
    deploymentResources: Set[String] = Set.empty,
    serviceRegistries: Set[ServiceRegistry] = Set.empty
)
case class ServiceRegistry(key: String, value: Any)

case class BpmnProcessTester(
    process: BpmnProcess,
    testConfig: TestConfig = TestConfig()
)

type TestStepObj[T] = HasProcessNode[T] | BpmnProcess

sealed trait BpmnTestStep

case class StartProcessStep(startEvent: StartEvent, data: TestData)
    extends BpmnTestStep

case class UserTaskStep(userTask: UserTask, data: TestData) extends BpmnTestStep

case class ServiceTaskStep(userTask: ServiceTask, data: Option[TestData] = None)
    extends BpmnTestStep

case class EndStep(endEvent: EndEvent, data: Seq[TestData] = Seq.empty)
    extends BpmnTestStep

trait TestData extends Product:

  def names(): Seq[String] = productElementNames.toSeq

  def asVars(): Map[String, Any] =
    productElementNames
      .zip(productIterator)
      .toMap

  def asJavaVars(): java.util.Map[String, Any] =
    asVars().asJava
