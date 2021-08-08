package camundala
package test

import camundala.model.BpmnProcess

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

case class BpmnTestCase(name: String, steps: Seq[BpmnTestStep] = Nil)

type TestStepObj[T] = HasProcessNode[T] | BpmnProcess

sealed trait BpmnTestStep

case class StartProcessStep(startEvent: StartEvent, data: TestData)
  extends BpmnTestStep

case class UserTaskStep(userTask: UserTask, data: TestData)
  extends BpmnTestStep

trait TestData extends Product:

  def asVars(): Map[String, Any] =
    productElementNames
      .zip(productIterator)
      .toMap

  def asJavaVars(): java.util.Map[String, Any] =
    asVars()
      .asJava

