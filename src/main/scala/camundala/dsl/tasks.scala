package camundala.dsl

import camundala.model.TaskImplementation.{DelegateExpression, Expression, ExternalTask, JavaClass}
import camundala.model._

trait serviceTasks:
  def serviceTask(ident: Ident,
                  taskImplementation: TaskImplementation) =
    ServiceTask(Task(ident), taskImplementation)

trait sendTasks:
  def sendTask(ident: Ident,
                  taskImplementation: TaskImplementation) =
    SendTask(Task(ident), taskImplementation)

trait taskImplementations:
  
  def expression(expr: String) =
    Expression(expr)

  def expression(expr: String, resultVariable: String) =
    Expression(expr, Some(resultVariable))
  
  def delegateExpression(expr: String) =
    DelegateExpression(expr)

  def javaClass(className: String) =
    JavaClass(className)

  def externalTask(topic: String) =
    ExternalTask(topic: String)

trait userTasks:
  def userTask(ident: Ident) =
    UserTask(ident)

  def userTask(ident: Ident, form: BpmnForm) =
    UserTask(Task(ident), Some(form))
