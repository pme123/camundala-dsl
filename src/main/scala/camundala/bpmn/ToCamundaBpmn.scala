package camundala.bpmn

import camundala.model.Condition.*
import camundala.model.Constraint.Minlength
import camundala.model.GeneratedForm.FormFieldType.StringType
import camundala.model.GeneratedForm.*
import camundala.model.ScriptImplementation.{ExternalScript, InlineScript}
import camundala.model.*
import camundala.model.TaskImplementation.*
import org.camunda.bpm.model.bpmn.builder.*
import org.camunda.bpm.model.bpmn.impl.instance.ExtensionElementsImpl
import org.camunda.bpm.model.bpmn.impl.instance.camunda.CamundaFormFieldImpl
import org.camunda.bpm.model.bpmn.instance.{BpmnModelElementInstance, ConditionExpression, Script}
import org.camunda.bpm.model.bpmn.instance.camunda.{CamundaConstraint, CamundaFormData, CamundaFormField, CamundaProperties, CamundaProperty, CamundaValidation, CamundaValue}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn => BpmnCamunda, instance => camunda}

import scala.jdk.CollectionConverters.*
import java.io.File
import camundala.dsl.DSL.Givens.given
import scala.language.implicitConversions

trait ToCamundaBpmn :

  // context function def f(using BpmnModelInstance): T
  type ToCamundable[T] = BpmnModelInstance ?=> T

  extension (bpmn: Bpmn)
    def toCamunda(outputPath: BpmnPath): Unit =
      given modelInstance: BpmnModelInstance = BpmnCamunda.readModelFromStream (this.getClass.getClassLoader.getResourceAsStream (bpmn.path) )
      bpmn.processes.processes.map (_.toCamunda)
      BpmnCamunda.writeModelToFile (new File (outputPath), modelInstance)
  
  extension (process: BpmnProcess)
    def toCamunda: ToCamundable[Unit] =
      process.elements.elements.foreach((e: ProcessElement) => e.toCamunda)
      val cProcess: camunda.Process = summon[BpmnModelInstance].getModelElementById(process.ident)
      val groups = process.starterGroups.groups.map(_.toString)
      cProcess.setCamundaCandidateStarterGroupsList(groups.asJava)
      val users = process.starterUsers.users.map(_.toString)
      cProcess.setCamundaCandidateStarterUsersList(users.asJava)
  
  extension (postElement: ProcessElement)
    def toCamunda: ToCamundable[Unit] =
      postElement match
        case pe: StartEvent =>
          val elem: camunda.StartEvent = summon[BpmnModelInstance].getModelElementById(pe.ident)
          pe.merge(elem)
        case pe: ServiceTask =>
          val elem: camunda.ServiceTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
          pe.merge(elem)
        case pe: UserTask =>
          val elem: camunda.UserTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
          pe.merge(elem)
        case pe: ScriptTask =>
          val elem: camunda.ScriptTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
          pe.merge(elem)
        case pe: SequenceFlow =>
          val elem: camunda.SequenceFlow = summon[BpmnModelInstance].getModelElementById(pe.ident)
          pe.merge(elem)
        case eg: ExclusiveGateway =>
          val elem: camunda.ExclusiveGateway = summon[BpmnModelInstance].getModelElementById(eg.ident)
          eg.merge(elem)
  
  extension (task: ServiceTask)
    def merge(elem: camunda.ServiceTask): Unit =
      task.taskImplementation
        .merge(elem)
  
  extension (taskImpl: TaskImplementation)
    def merge(elem: camunda.ServiceTask): Unit =
      taskImpl match
        case Expression(expresssion, resultVariable) =>
          elem.setCamundaExpression(expresssion)
          resultVariable.foreach(elem.setCamundaResultVariable)
        case DelegateExpression(expresssion) =>
          elem.setCamundaDelegateExpression(expresssion)
        case JavaClass(className) =>
          elem.setCamundaClass(className)
        case ExternalTask(topic) =>
          elem.setCamundaTopic(topic)
  
  extension (event: StartEvent)
    def merge(elem: camunda.StartEvent): ToCamundable[Unit] =
      val builder: StartEventBuilder = elem.builder()
      event.bpmnForm.foreach {
        case EmbeddedForm(formRef) =>
          builder.camundaFormKey(formRef.toString)
        case GeneratedForm(fields) =>
          fields.foreach {
            createFormField(_, builder.camundaFormField().getElement)
          }
      }
  
  extension (task: UserTask)
    def merge(elem: camunda.UserTask): ToCamundable[Unit] =
      val builder: UserTaskBuilder = elem.builder()
      task.bpmnForm.foreach {
        case EmbeddedForm(formRef) =>
          builder.camundaFormKey(formRef.toString)
        case GeneratedForm(fields) =>
          fields.foreach {
            createFormField(_, builder.camundaFormField().getElement)
          }
      }
  
  extension (task: ScriptTask)
    def merge(elem: camunda.ScriptTask): Unit =
      val builder: ScriptTaskBuilder = elem.builder()
      task.scriptImplementation match {
        case InlineScript(lang, script) =>
          builder
            .scriptFormat(lang.toString)
            .scriptText(script)
        case es@ExternalScript(lang, _) =>
          elem.setCamundaResource(es.deployResource)
          elem.setScriptFormat(lang.toString)
      }
      task.resultVariable.map(_.toString).foreach(
        builder
          .camundaResultVariable
      )
  
  extension (flow: SequenceFlow)
    def merge(elem: camunda.SequenceFlow): ToCamundable[Unit] =
      val expression = summon[BpmnModelInstance].newInstance(classOf[ConditionExpression])
      elem.setConditionExpression(expression)
      flow.condition.foreach {
        case ExpressionCond(expr) =>
          expression.setTextContent(expr)
        case sc@ScriptCond(_, format) =>
          expression.setLanguage(format.toString)
          expression.setCamundaResource(sc.deployResource)
          expression.setType("bpmn2:tFormalExpression")
        case InlineScriptCond(script, format) =>
          expression.setLanguage(format.toString)
          expression.setTextContent(script)
          expression.setType("bpmn2:tFormalExpression")
      }
  
  extension (gateway: ExclusiveGateway)
    def merge(elem: camunda.ExclusiveGateway): ToCamundable[Unit] =
      gateway.defaultFlow
        .map { f =>
          val flow: camunda.SequenceFlow = summon[BpmnModelInstance].getModelElementById(f.toString)
          elem.setDefault(flow)
        }
  
  private def createFormField(formField: FormField, cff: CamundaFormField): ToCamundable[Unit] =
    val FormField(id, label, fieldType, defaultValue, values, constraints, properties) = formField
    cff.setCamundaId(id)
    cff.setCamundaLabel(label.map(_.str).getOrElse(""))
    cff.setCamundaType(fieldType.name)
    cff.getCamundaValues
      .addAll(values.enums.map { case EnumValue(k, v) =>
        val cv = summon[BpmnModelInstance].newInstance(classOf[CamundaValue])
        cv.setCamundaId(k)
        cv.setCamundaName(v)
        cv
      }.asJava)
    val cvd = summon[BpmnModelInstance].newInstance(classOf[CamundaValidation])
    cff.setCamundaValidation(cvd)
    cvd.getCamundaConstraints
      .addAll(constraints.constraints.map { c =>
        val cc = summon[BpmnModelInstance].newInstance(classOf[CamundaConstraint])
        cc.setCamundaName(c.name)
        c.config.foreach(cc.setCamundaConfig)
        cc
      }.asJava)
    val cps = summon[BpmnModelInstance].newInstance(classOf[CamundaProperties])
    cff.setCamundaProperties(cps)
    cps
      .getCamundaProperties
      .addAll(properties.properties.map { case Property(k, v) =>
        val cv = summon[BpmnModelInstance].newInstance(classOf[CamundaProperty])
        cv.setCamundaId(k)
        cv.setCamundaValue(v)
        cv
      }.asJava)
