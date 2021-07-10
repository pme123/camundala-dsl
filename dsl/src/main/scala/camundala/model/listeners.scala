package camundala.model

// Camunda Extension
case class TaskListener(
    eventType: TaskListenerEventType = TaskListenerEventType.create,
    listenerType: ListenerType = TaskImplementation.Expression("TODO")
)

enum TaskListenerEventType:
  case create, assignment, complete, delete, update, timeout

type ListenerType = TaskImplementation.Expression |
  TaskImplementation.DelegateExpression | TaskImplementation.JavaClass |
  ScriptImplementation

case class TaskListeners(listeners: Seq[TaskListener] = Seq.empty):
  def isEmpty = listeners.isEmpty
  def nonEmpty = listeners.nonEmpty
  def :+(listener: TaskListener): TaskListeners = copy(listeners :+ listener)

object TaskListeners {
  val none: TaskListeners = TaskListeners()
}

trait HasTaskListeners[T]:
  def taskListeners: TaskListeners

  def withTaskListener(listener: TaskListener): T

  def withTaskListeners(listener: TaskListeners): T

case class ExecutionListener(
    eventType: ExecutionListenerEventType,
    listenerType: ListenerType
)

enum ExecutionListenerEventType:
  case start, end

case class ExecutionListeners(listeners: Seq[ExecutionListener] = Seq.empty):
  def isEmpty = listeners.isEmpty
  def nonEmpty = listeners.nonEmpty
  def :+(listener: ExecutionListener): ExecutionListeners = copy(
    listeners :+ listener
  )

object ExecutionListeners {
  val none: ExecutionListeners = ExecutionListeners()
}

trait HasExecutionListeners[T]:
  def executionListeners: ExecutionListeners

  def withExecutionListener(listener: ExecutionListener): T
  def withExecutionListeners(listener: Seq[ExecutionListener]): T
