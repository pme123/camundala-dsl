package camundala
package test

import camundala.model.{BpmnProcess, EndEvent, InOutObject, ServiceTask}

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

case class StartProcessStep(startEvent: StartEvent, data: InOutObject)
    extends BpmnTestStep

case class UserTaskStep(userTask: UserTask, data: InOutObject) extends BpmnTestStep

case class ServiceTaskStep(userTask: ServiceTask, data: Option[InOutObject] = None)
    extends BpmnTestStep

case class EndStep(endEvent: EndEvent, data: Seq[InOutObject] = Seq.empty)
    extends BpmnTestStep


