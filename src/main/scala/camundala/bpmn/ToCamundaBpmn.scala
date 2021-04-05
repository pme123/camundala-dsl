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
import org.camunda.bpm.model.{bpmn => camundaBpmn}

import scala.jdk.CollectionConverters.*
import java.io.File
import camundala.dsl.DSL.Givens.

given

import scala.language.implicitConversions
import zio.*

trait ToCamundaBpmn:

  // context function def f(using BpmnModelInstance): T
  type ToCamundable[T] = BpmnModelInstance ?=> T

  extension (bpmn: Bpmn)
    def toCamunda(outputPath: BpmnPath): IO[ToCamundaException, Unit] = {
      (for {
        modelInstance <- ZIO(camundaBpmn.Bpmn.readModelFromFile(new File(bpmn.path)))
        cProcesses <- ZIO(modelInstance.getModelElementsByType(classOf[camunda.Process]).asScala.toSeq)
        processes <- ZIO.collect(bpmn.processes.processes) {
          p =>
            p.toCamunda()(using modelInstance)
              .mapError(Some(_))
        }
        _ <- ZIO(camundaBpmn.Bpmn.writeModelToFile(new File(outputPath), modelInstance))
      } yield ())
        .mapError {
          case Some(ex: ToCamundaException) => ex
          case ex: ToCamundaException => ex
          case t: Throwable => 
            ToCamundaException(t.getMessage)
        }
    }

  extension (process: BpmnProcess)
    def toCamunda(): ToCamundable[IO[ToCamundaException, Unit]] =
      for {
        _ <- ZIO.collect(process.elements.elements) {
          e => e.toCamunda().mapError(Some(_))
        } 
        cProcess <- ZIO(summon[BpmnModelInstance].getModelElementById(process.ident).asInstanceOf[camunda.Process])
          .mapError(ex => ToCamundaException(ex.getMessage()))
        groups <- UIO(process.starterGroups.groups.map(_.toString))
        _ <- UIO(cProcess.setCamundaCandidateStarterGroupsList(groups.asJava))
        users <- UIO(process.starterUsers.users.map(_.toString))
        _ <- UIO(cProcess.setCamundaCandidateStarterUsersList(users.asJava))
      } yield ()

  extension (procElement: ProcessElement)
    def toCamunda(): ToCamundable[IO[ToCamundaException, Unit]] =
      ZIO(
        procElement match
          case pe: StartEvent =>
            val elem: camunda.StartEvent = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
          case pe: ServiceTask =>
            val elem: camunda.ServiceTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
          case pe: UserTask =>
            val elem: camunda.UserTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
          case pe: ScriptTask =>
            val elem: camunda.ScriptTask = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
          case pe: SequenceFlow =>
            val elem: camunda.SequenceFlow = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
          case eg: ExclusiveGateway =>
            val elem: camunda.ExclusiveGateway = summon[BpmnModelInstance].getModelElementById(eg.ident)
              eg
            .merge(elem)
          case pe: EndEvent =>
            val elem: camunda.EndEvent = summon[BpmnModelInstance].getModelElementById(pe.ident)
              pe
            .merge(elem)
      ).mapError(ex => ToCamundaException(ex.getMessage()))

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

  extension (event: EndEvent)
    def merge(elem: camunda.EndEvent): ToCamundable[Unit] = (/* nothing to do yet*/)

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
      flow.condition
        .map {
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
        }.foreach(_ => elem.setConditionExpression(expression))

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
    defaultValue.foreach(v => cff.setCamundaDefaultValue(v.toString))
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

case class ToCamundaException(msg: String)