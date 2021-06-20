package camundala
package dev


import org.camunda.bpm.model.bpmn.Bpmn as CBpmn
import org.camunda.bpm.model.bpmn.instance.camunda.*

import dsl.DSL.Givens.given
import scala.language.implicitConversions

trait ToCamundaBpmn:

  // context function def f(using CBpmnModelInstance): T
  type ToCamundable[T] = CBpmnModelInstance ?=> T

  extension (runnerConfig: RunnerConfig)
    def toCamunda(): IO[ToCamundaException, Seq[Unit]] =
      IO.foreach(runnerConfig.bpmnsConfig.bpmns.bpmns) { (bpmn: Bpmn) =>
        bpmn.toCamunda(runnerConfig.withIdFolder, runnerConfig.generatedFolder)
      }

  extension (bpmn: Bpmn)
    def toCamunda(
        withIdFolder: BpmnPath,
        generatedFolder: BpmnPath
    ): IO[ToCamundaException, Unit] = {
      (for {
        modelInstance <- ZIO(
          CBpmn.readModelFromFile(
            new File(s"$withIdFolder/${bpmn.path}")
          )
        )
        cProcesses <- ZIO(
          modelInstance
            .getModelElementsByType(classOf[CProcess])
            .asScala
            .toSeq
        )
        processes <- ZIO.collect(bpmn.processes.processes) { p =>
          p.toCamunda()(using modelInstance)
            .mapError(Some(_))
        }
        _ <- ZIO(
          CBpmn.writeModelToFile(
            new File(s"$generatedFolder/${bpmn.path}"),
            modelInstance
          )
        )
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
        _ <- ZIO.collect(process.nodes.nodes ++ process.flows.flows) { n =>
          n.toCamunda().mapError(Some(_))
        }
        cProcess <- ZIO(
          summon[CBpmnModelInstance]
            .getModelElementById(process.ident)
            .asInstanceOf[CProcess]
        )
          .mapError(ex => ToCamundaException(ex.getMessage()))
        groups <- UIO(process.starterGroups.groups.map(_.toString))
        _ <- UIO(cProcess.setCamundaCandidateStarterGroupsList(groups.asJava))
        users <- UIO(process.starterUsers.users.map(_.toString))
        _ <- UIO(cProcess.setCamundaCandidateStarterUsersList(users.asJava))
      } yield ()

  extension [A](procElement: HasProcessElement[A])

    def toCamunda(): ToCamundable[IO[ToCamundaException, Unit]] =
      for {
        elem <- checkElem()
        _ <- mergeSpecElem
        //  _ <- mergeNode
        _ <- mergeProperties(elem)
        _ <- mergeTransactionBoundaries
      } yield ()

    private def checkElem()
        : ToCamundable[IO[ToCamundaException, CBaseElement]] =
      val elem: CBaseElement =
        summon[CBpmnModelInstance].getModelElementById(procElement.ident)
      if (elem == null)
        ZIO.fail(
          ToCamundaException(
            s"There is no existing ${procElement.elemKey.name} with this ident: ${procElement.ident}"
          )
        )
      else
        ZIO.succeed(elem)

    private def mergeSpecElem: ToCamundable[IO[ToCamundaException, Unit]] =
      ZIO(
        procElement match
          case pe: StartEvent =>
            val elem: CStartEvent =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
          case pe: ServiceTask =>
            val elem: CServiceTask =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
          case pe: UserTask =>
            val elem: CUserTask =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
          case pe: ScriptTask =>
            val elem: CScriptTask =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
          case pe: SequenceFlow =>
            val elem: CSequenceFlow =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
          case eg: ExclusiveGateway =>
            val elem: CExclusiveGateway =
              summon[CBpmnModelInstance].getModelElementById(eg.ident)
            eg.merge(elem)
          case pe: EndEvent =>
            val elem: CEndEvent =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            pe.merge(elem)
      ).mapError(handleException(procElement.elemKey.name))

    private def mergeProperties(
        elem: CBaseElement
    ): ToCamundable[IO[ToCamundaException, Unit]] =
      // HasProperties
      procElement match
        case pe: HasProperties[_] =>
          pe.propsToCamunda(elem)
            .mapError(handleException("Properties"))

    private def mergeParameters: ToCamundable[IO[ToCamundaException, Unit]] =
      ZIO { // HasInputParameters / HasOutputParameters
        val inout =
          summon[CBpmnModelInstance].newInstance(classOf[CamundaInputOutput])
        val elem: CBaseElement =
          summon[CBpmnModelInstance].getModelElementById(procElement.ident)
        procElement match
          case iop: (HasInputParameters[_] & HasOutputParameters[_]) =>
            if (iop.inputParameters.nonEmpty || iop.outputParameters.nonEmpty)
              elem.builder.addExtensionElement(inout).done
              iop.inputParamsToCamunda(elem, inout)
              iop.outputParamsToCamunda(elem, inout)
          case ip: HasInputParameters[_] =>
            if (ip.inputParameters.nonEmpty)
              elem.builder.addExtensionElement(inout).done
              ip.inputParamsToCamunda(elem, inout)
          case op: HasOutputParameters[_] =>
            if (op.outputParameters.nonEmpty)
              elem.builder.addExtensionElement(inout).done
              op.outputParamsToCamunda(elem, inout)
          case _ => ()
      }.mapError(handleException("Paramters"))

    private def mergeTransactionBoundaries
        : ToCamundable[IO[ToCamundaException, Unit]] =
      ZIO(
        procElement match
          case pe: HasTransactionBoundary[_] =>
            val elem: CFlowNode =
              summon[CBpmnModelInstance].getModelElementById(pe.ident)
            elem.setCamundaAsyncBefore(pe.isAsyncBefore)
            elem.setCamundaExclusive(pe.isAsyncBefore) // just support exclusive
            elem.setCamundaAsyncAfter(pe.isAsyncAfter)
          case _ => ()
      ).mapError(handleException("TransactionBoundaries"))

    private def handleException(
        msg: String
    ): Throwable => ToCamundaException = { ex =>
      ex.printStackTrace
      ToCamundaException(
        s"Problem merging $msg for $procElement\n${ex.getMessage}"
      )
    }

  extension [A](hasProperties: HasProperties[A])
    def propsToCamunda(
        elem: CBaseElement
    ): ToCamundable[zio.Task[Unit]] =
      zio.Task {
        if (hasProperties.properties.nonEmpty) {
          val props: CamundaProperties =
            summon[CBpmnModelInstance].newInstance(classOf[CamundaProperties])
          elem.builder.addExtensionElement(props).done
          hasProperties.properties.properties
            .foreach { case Property(ident, value) =>
              val cp =
                summon[CBpmnModelInstance].newInstance(classOf[CamundaProperty])
              cp.setCamundaName(ident.toString)
              cp.setCamundaValue(value)
              props.getCamundaProperties().add(cp)
            }
        }
      }

  extension [A](hasInputParams: HasInputParameters[A])
    def inputParamsToCamunda(
        elem: CBaseElement,
        inout: CamundaInputOutput
    ): ToCamundable[Unit] =
      hasInputParams.inputParameters
        .foreach { case InOutParameter(ident, value) =>
          val cp = summon[CBpmnModelInstance].newInstance(
            classOf[CamundaInputParameter]
          )
          cp.setCamundaName(ident.toString)
          inout.getCamundaInputParameters().add(cp)
          value.paramsToCamunda(cp)
        }

  extension [A](hasOutputParams: HasOutputParameters[A])
    def outputParamsToCamunda(
        elem: CBaseElement,
        inout: CamundaInputOutput
    ): ToCamundable[Unit] =
      hasOutputParams.outputParameters
        .foreach { case InOutParameter(ident, value) =>
          val cp = summon[CBpmnModelInstance].newInstance(
            classOf[CamundaOutputParameter]
          )
          cp.setCamundaName(ident.toString)
          inout.getCamundaOutputParameters().add(cp)
          value.paramsToCamunda(cp)
        }

  extension (value: VariableAssignment | ScriptImplementation)
    def paramsToCamunda(
        cp: CamundaGenericValueElement & ModelElementInstance
    ): ToCamundable[Unit] =
      import ScriptImplementation.*
      value match
        case VariableAssignment.StringVal(str) =>
          cp.setTextContent(str)
        case VariableAssignment.Expression(str) =>
          cp.setTextContent(str)
        case InlineScript(lang, str) =>
          val script: CamundaScript =
            summon[CBpmnModelInstance].newInstance(classOf[CamundaScript])
          script.setCamundaScriptFormat(lang.toString)
          script.setTextContent(str)
          cp.setValue(script)
        case ExternalScript(lang, resource) =>
          val script: CamundaScript =
            summon[CBpmnModelInstance].newInstance(classOf[CamundaScript])
          script.setCamundaScriptFormat(lang.toString)
          script.setCamundaResource(resource)
          cp.setValue(script)

  extension (task: ServiceTask)
    def merge(elem: CServiceTask): Unit =
      task.taskImplementation
        .merge(elem)

  extension (taskImpl: TaskImplementation)
    def merge(elem: CServiceTask): Unit =
      import TaskImplementation.*
      taskImpl match
        case Expression(expresssion, resultVariable) =>
          elem.setCamundaExpression(expresssion)
          resultVariable.foreach(elem.setCamundaResultVariable)
        case DelegateExpression(expresssion) =>
          elem.setCamundaDelegateExpression(expresssion)
        case JavaClass(className) =>
          elem.setCamundaClass(className)
        case ExternalTask(topic) =>
          elem.setCamundaType("external")
          elem.setCamundaTopic(topic)

  extension (event: StartEvent)
    def merge(elem: CStartEvent): ToCamundable[Unit] =
      val builder = elem.builder()
      event.maybeForm.foreach {
        case EmbeddedForm(formRef) =>
          builder.camundaFormKey(formRef.toString)
        case EmbeddedStaticForm(formPath) =>
          builder.camundaFormKey(formPath.toString)
        case GeneratedForm(fields) =>
          fields.foreach {
            createFormField(_, builder.camundaFormField().getElement)
          }
      }

  extension (event: EndEvent)
    def merge(elem: CEndEvent): ToCamundable[Unit] =
      (/* nothing to do yet*/ )

  extension (task: UserTask)
    def merge(elem: CUserTask): ToCamundable[Unit] =
      val builder = elem.builder()

      task.maybeForm.foreach {
        case EmbeddedForm(formRef) =>
          builder.camundaFormKey(formRef.toString)
        case EmbeddedStaticForm(formPath) =>
          builder.camundaFormKey(formPath.toString)
        case GeneratedForm(fields) =>
          fields.foreach {
            createFormField(_, builder.camundaFormField().getElement)
          }
      }

  extension (task: ScriptTask)
    def merge(elem: CScriptTask): Unit =
      import ScriptImplementation.*
      val builder = elem.builder()
      task.scriptImplementation match {
        case InlineScript(lang, script) =>
          builder
            .scriptFormat(lang.toString)
            .scriptText(script)
        case es @ ExternalScript(lang, _) =>
          elem.setCamundaResource(es.deployResource)
          elem.setScriptFormat(lang.toString)
      }
      task.resultVariable
        .map(_.toString)
        .foreach(
          builder.camundaResultVariable
        )

  extension (flow: SequenceFlow)
    def merge(elem: CSequenceFlow): ToCamundable[Unit] =
      import Condition.*
      val expression =
        summon[CBpmnModelInstance].newInstance(classOf[CConditionExpression])
      flow.condition
        .map {
          case ExpressionCond(expr) =>
            expression.setTextContent(expr)
          case sc @ ScriptCond(_, format) =>
            expression.setLanguage(format.toString)
            expression.setCamundaResource(sc.deployResource)
            expression.setType("bpmn2:tFormalExpression")
          case InlineScriptCond(script, format) =>
            expression.setLanguage(format.toString)
            expression.setTextContent(script)
            expression.setType("bpmn2:tFormalExpression")
        }
        .foreach(_ => elem.setConditionExpression(expression))

  extension (gateway: ExclusiveGateway)
    def merge(elem: CExclusiveGateway): ToCamundable[Unit] =
      gateway.defaultFlow
        .map { f =>
          val flow: CSequenceFlow =
            summon[CBpmnModelInstance].getModelElementById(f.toString)
          elem.setDefault(flow)
        }

  private def createFormField(
      formField: FormField,
      cff: CamundaFormField
  ): ToCamundable[Unit] =
    val FormField(
      id,
      label,
      fieldType,
      defaultValue,
      values,
      constraints,
      properties
    ) = formField
    cff.setCamundaId(id)
    cff.setCamundaLabel(label.map(_.str).getOrElse(""))
    cff.setCamundaType(fieldType.name)
    defaultValue.foreach(v => cff.setCamundaDefaultValue(v.toString))
    cff.getCamundaValues
      .addAll(values.enums.map { case GeneratedForm.EnumValue(k, v) =>
        val cv = summon[CBpmnModelInstance].newInstance(classOf[CamundaValue])
        cv.setCamundaId(k)
        cv.setCamundaName(v)
        cv
      }.asJava)
    val cvd = summon[CBpmnModelInstance].newInstance(classOf[CamundaValidation])
    cff.setCamundaValidation(cvd)
    cvd.getCamundaConstraints
      .addAll(constraints.constraints.map { c =>
        val cc =
          summon[CBpmnModelInstance].newInstance(classOf[CamundaConstraint])
        cc.setCamundaName(c.name)
        c.config.foreach(cc.setCamundaConfig)
        cc
      }.asJava)
    val cps = summon[CBpmnModelInstance].newInstance(classOf[CamundaProperties])
    cff.setCamundaProperties(cps)
    cps.getCamundaProperties
      .addAll(properties.properties.map { case Property(k, v) =>
        val cv = summon[CBpmnModelInstance].newInstance(classOf[CamundaProperty])
        cv.setCamundaId(k)
        cv.setCamundaValue(v)
        cv
      }.asJava)

case class ToCamundaException(msg: String)
