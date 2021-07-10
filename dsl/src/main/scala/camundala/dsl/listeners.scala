package camundala
package dsl

trait listeners:

  def taskListener = TaskListener()
  def execListener = ExecutionListener()

  extension [T](hasListener: HasTaskListeners[T])
    //create, assignment, complete, delete, update, timeout

    def taskListeners(listeners: (TaskListener)*): T =
      hasListener.withTaskListeners(listeners)

  extension [T](hasListener: HasExecutionListeners[T])
    //create, assignment, complete, delete, update, timeout

    def execListeners(listeners: ExecutionListener*): T =
      hasListener.withExecutionListeners(listeners)

  extension (listener: TaskListener)

    def create = listener.copy(eventType = TaskListenerEventType.create)
    def assignment = listener.copy(eventType = TaskListenerEventType.assignment)
    def complete = listener.copy(eventType = TaskListenerEventType.complete)
    def delete = listener.copy(eventType = TaskListenerEventType.delete)
    def update = listener.copy(eventType = TaskListenerEventType.update)
    def timeout = listener.copy(eventType = TaskListenerEventType.timeout)

  extension [T](hasListenerType: HasListenerType[T])

    def expression(expr: String) =
      hasListenerType.withListenerType(TaskImplementation.Expression(expr))

    def delegateExpression(expr: String) =
      hasListenerType.withListenerType(
        TaskImplementation.DelegateExpression(expr)
      )

    def javaClass(className: String) =
      hasListenerType.withListenerType(TaskImplementation.JavaClass(className))

    def groovyRef(scriptPath: ScriptImplementation.ScriptPath): T =
      hasListenerType.withListenerType(
        ScriptImplementation.ExternalScript(
          ScriptLanguage.Groovy,
          s"$scriptPath.groovy"
        )
      )

    def inlineGroovy(script: String): T =
      hasListenerType.withListenerType(
        ScriptImplementation.InlineScript(ScriptLanguage.Groovy, script)
      )

    def javascriptRef(
        scriptPath: ScriptImplementation.ScriptPath
    ): T =
      hasListenerType.withListenerType(
        ScriptImplementation.ExternalScript(
          ScriptLanguage.Javascript,
          s"$scriptPath.groovy"
        )
      )

    def inlineJavascript(script: String): T =
      hasListenerType.withListenerType(
        ScriptImplementation.InlineScript(ScriptLanguage.Javascript, script)
      )

  extension (listener: ExecutionListener)

    def start = listener.copy(eventType = ExecListenerEventType.start)
    def end = listener.copy(eventType = ExecListenerEventType.end)
