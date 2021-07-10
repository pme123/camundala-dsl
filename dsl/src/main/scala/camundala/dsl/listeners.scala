package camundala
package dsl

trait listeners:

  def taskListener = TaskListener()

  extension [T](hasListener: HasTaskListeners[T])
    //create, assignment, complete, delete, update, timeout

    def listeners(listeners: TaskListener*): T =
      hasListener.withTaskListeners(TaskListeners(listeners))

  extension (listener: TaskListener)

    def create = listener.copy(eventType = TaskListenerEventType.create)
    def assignment = listener.copy(eventType = TaskListenerEventType.assignment)
    def complete = listener.copy(eventType = TaskListenerEventType.complete)
    def delete = listener.copy(eventType = TaskListenerEventType.delete)
    def update = listener.copy(eventType = TaskListenerEventType.update)
    def timeout = listener.copy(eventType = TaskListenerEventType.timeout)

    def expression(expr: String) =
      listener.copy(listenerType = TaskImplementation.Expression(expr))

    def delegateExpression(expr: String) =
      listener.copy(listenerType = TaskImplementation.DelegateExpression(expr))

    def javaClass(className: String) =
      listener.copy(listenerType = TaskImplementation.JavaClass(className))

    def groovyRef(scriptPath: ScriptImplementation.ScriptPath): TaskListener =
      listener.copy(listenerType =
        ScriptImplementation.ExternalScript(
          ScriptLanguage.Groovy,
          s"$scriptPath.groovy"
        )
      )

    def inlineGroovy(script: String): TaskListener =
      listener.copy(listenerType =
        ScriptImplementation.InlineScript(ScriptLanguage.Groovy, script)
      )

    def javascriptRef(
        scriptPath: ScriptImplementation.ScriptPath
    ): TaskListener =
      listener.copy(listenerType =
        ScriptImplementation.ExternalScript(
          ScriptLanguage.Javascript,
          s"$scriptPath.groovy"
        )
      )

    def inlineJavascript(script: String): TaskListener =
      listener.copy(listenerType =
        ScriptImplementation.InlineScript(ScriptLanguage.Javascript, script)
      )
