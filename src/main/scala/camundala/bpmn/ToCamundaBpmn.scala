package camundala.bpmn

import camundala.model.GeneratedForm.FormFieldType.StringType
import camundala.model.GeneratedForm.{EnumField, FormFieldType, SimpleField, enumField}
import camundala.model.ScriptImplementation.{ExternalScript, InlineScript}
import camundala.model._
import camundala.model.TaskImplementation._
import org.camunda.bpm.model.bpmn.builder._
import org.camunda.bpm.model.bpmn.impl.instance.ExtensionElementsImpl
import org.camunda.bpm.model.bpmn.impl.instance.camunda.CamundaFormFieldImpl
import org.camunda.bpm.model.bpmn.instance.{BpmnModelElementInstance, Script}
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn => BpmnCamunda, instance => camunda}

import scala.jdk.CollectionConverters._
import java.io.File


extension (bpmn: Bpmn)
  def toCamunda(outputPath: BpmnPath): Unit =
    val modelInstance = BpmnCamunda.readModelFromStream(this.getClass.getClassLoader.getResourceAsStream(bpmn.bpmnPath))
    bpmn.processes.map(_.toCamunda(modelInstance))
    BpmnCamunda.writeModelToFile(new File(outputPath), modelInstance)

extension (process: BpmnProcess)
  def toCamunda(modelInstance: BpmnModelInstance): Unit = 
    process.elements.foreach((e: ProcessElement) => e.toCamunda(modelInstance))
    val cProcess: camunda.Process = modelInstance.getModelElementById(process.ident)
    val groups = process.starterGroups.groups.map(_.ident)
    cProcess.setCamundaCandidateStarterGroupsList(groups.asJava)
    val users = process.starterUsers.users.map(_.username)
    cProcess.setCamundaCandidateStarterUsersList(users.asJava)

extension (postElement: ProcessElement)
  def toCamunda(modelInstance: BpmnModelInstance): Unit = 
    postElement match
      case pe: StartEvent =>
        val elem: camunda.StartEvent = modelInstance.getModelElementById(pe.ident)
        pe.merge(elem)
      case pe: ServiceTask =>
        val elem: camunda.ServiceTask = modelInstance.getModelElementById(pe.ident)
        pe.merge(elem)
      case pe: UserTask =>
        val elem: camunda.UserTask = modelInstance.getModelElementById(pe.ident)
        pe.merge(elem)
      case pe: ScriptTask =>
        val elem: camunda.ScriptTask = modelInstance.getModelElementById(pe.ident)
        pe.merge(elem)

trait ToCamundaElem[T]:
  extension[E <: ModelElementInstance] (t: T)
    def merge(elem: E): T

given ToCamundaElem[ServiceTask] with
  extension[E <: camunda.ServiceTask] (task: ServiceTask) def merge(elem: E): Unit =
    task.taskImplementation
      .merge(elem)

given ToCamundaElem[TaskImplementation] with
  extension [E <: camunda.ServiceTask] (task: TaskImplementation) def merge(elem: E): Unit =
    task match
      case Expression(expresssion, resultVariable) =>
        elem.setCamundaExpression(expresssion)
        resultVariable.foreach(elem.setCamundaResultVariable)
      case DelegateExpression(expresssion) =>
        elem.setCamundaDelegateExpression(expresssion)
      case JavaClass(className) =>
        elem.setCamundaClass(className)
      case ExternalTask(topic) =>
        elem.setCamundaTopic(topic)

given ToCamundaElem[StartEvent] with
  extension [E <: camunda.StartEvent] (event: StartEvent) def merge(elem: E): Unit =
    val eventBuilder: StartEventBuilder = elem.builder()
    mergeForm(event, 
      formRef => eventBuilder.camundaFormKey(formRef),
      (id: Ident,
       label: String,
       fieldType: FormFieldType,
       defaultValue: String,
       constraints: Constraints,
       properties: Properties) =>
        eventBuilder
          .camundaFormField()
          .camundaId(id)
          .camundaLabel(label)
          .camundaType(fieldType.name)
          .getElement
    )

given ToCamundaElem[UserTask] with
  extension [E <: camunda.UserTask](task: UserTask) def merge(elem: E): Unit =
    val eventBuilder: UserTaskBuilder = elem.builder()
    mergeForm(task, 
      formRef => eventBuilder.camundaFormKey(formRef),
      (id: Ident,
       label: String,
       fieldType: FormFieldType,
       defaultValue: String,
       constraints: Constraints,
       properties: Properties) =>
        eventBuilder
          .camundaFormField()
          .camundaId(id)
          .camundaLabel(label)
          .camundaType(fieldType.name)
          .getElement
    )

given ToCamundaElem[ScriptTask] with
  extension [E <: camunda.ScriptTask](task: ScriptTask) 
    def merge(elem: E): Unit =
      val builder: ScriptTaskBuilder = elem.builder()
      task.scriptImplementation.foreach {
        case InlineScript(lang, script) =>
          builder
            .scriptFormat(lang.toString)
            .scriptText(script)
        case ExternalScript(lang, resource) =>
          
      }
      task.resultVariable.foreach(
        builder
          .camundaResultVariable
      )

// quite complex - because there is no generic Form Builder on Camunda
def mergeForm(hasMaybeForm: HasMaybeForm[_],
              formKeyMerge: Ident => Unit,
              formFieldMerge: (id: Ident,
                               label: String,
                               fieldType: FormFieldType,
                               defaultValue: String,
                               constraints: Constraints,
                               properties: Properties) => Unit) = {

    hasMaybeForm.bpmnForm.foreach {
      case EmbeddedForm(formRef) =>
        formKeyMerge(formRef)
      case GeneratedForm(fields) =>
        fields.foreach {
          case SimpleField(id, label, fieldType, defaultValue, constraints, properties) =>
            formFieldMerge(id, label, fieldType, defaultValue, constraints, properties)
          case EnumField(SimpleField(id, label, fieldType, defaultValue, constraints, properties), values) =>
            val enumField = formFieldMerge(id, label, fieldType, defaultValue, constraints, properties)
            () //TODO
        }

    }
}
