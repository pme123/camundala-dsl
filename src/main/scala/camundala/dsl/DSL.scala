package camundala.dsl

import camundala.model.BpmnGroup.GroupType
import camundala.model.BpmnUser._
import camundala.model.Constraint._
import camundala.model.GeneratedForm.FormField
import camundala.model.GeneratedForm.FormFieldType._
import camundala.model.TaskImplementation._
import camundala.model.{CandidateGroups, CandidateUsers, ProcessElement, _}
import camundala.dsl._

import scala.language.implicitConversions

trait DSL
  extends bpmns
    with processes
    with groups
    with users
    with events
    with forms
    with constraints
    with props
    with serviceTasks
    with taskImplementations
    with userTasks :

  def ident(id: String): Ident =
    Ident(id)

  def path(pathStr: String): BpmnPath =
    BpmnPath(pathStr)

  def name(name: String): Name =
    Name(name)

object DSL extends DSL :

  trait Implicits:

    given Conversion[Ident, String] = _.toString

    given Conversion[BpmnPath, String] = _.toString

    given Conversion[String, Ident] = ident(_)

    given Conversion[String, BpmnPath] = path(_)

  object Implicits extends Implicits

trait events:
  def startEvent(ident: Ident) =
    StartEvent(ident)

  def startEvent(ident: Ident, form: BpmnForm) =
    StartEvent(ident, Some(form))

trait forms:
  def form(formRef: Ident) =
    EmbeddedForm(formRef)

  def formKey(key: String) = DSL.ident(key)

  def form(formFields: FormField*) =
    GeneratedForm(formFields)

  def textField(id: Ident, props: FieldAttr*): FormField =
    FormField(id,
      constraints = Constraints(props.collect { case c: Constraint => c }),
      properties = Properties(props.collect { case p: Property => p })
    )

  def booleanField(id: Ident): FormField =
    FormField(id)
      .fieldType(BooleanType)

  def longField(id: Ident): FormField =
    FormField(id)
      .fieldType(LongType)

  def dateField(id: Ident): FormField =
    FormField(id)
      .fieldType(DateType)

  def enumField(id: Ident): FormField =
    FormField(id)
      .fieldType(EnumType)

trait constraints:
  def readonly: Constraint = Readonly

  def required: Constraint = Required

  def minlength(value: Int): Constraint = Minlength(value)

  def maxlength(value: Int): Constraint = Maxlength(value)

  def min(value: Int): Constraint = Min(value)

  def max(value: Int): Constraint = Max(value)

  def custom(name: Ident, config: Option[String]): Constraint = Custom(name, config)


trait serviceTasks:
  def serviceTask(ident: Ident,
                  taskImplementation: TaskImplementation) =
    ServiceTask(Task(ident), taskImplementation)

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


object runner2 extends App with DSL :

  import camundala.dsl.DSL._

  val adminGroup = group(ident("admin"), name("Administrator"), groupType("MyGROUP"))

  private val testUser: BpmnUser = user(
    username("pme123"),
    name("Muster"),
    firstName("Pascal"),
    email("pascal@muster.ch"),
    groupRefs(
      adminGroup.ref
    )
  )
  private val bpmnExample = bpmn(
    path("myPath"),
    process(
      ident("myIdent"),
      starterGroups(
        adminGroup.ref
      ),
      starterUsers(
        testUser.ref
      ),
      elements(
        startEvent(
          ident("LetsStart"),
          form(formKey("MyForm"))
        ),
        serviceTask(
          ident("ExpressionService"),
          expression("${myVar as String}", "myVar")
        ),
        serviceTask(
          ident("ExternalTask"),
          externalTask("my-topic")
        ),
        userTask(
          ident("MyUser"),
          form(textField(
            ident("textField1"),
            required,
            minlength(12),
            prop(
              ident("width"), "12")
          ),
            booleanField(ident("booleanField1")),
            longField(ident("longField1")),
            enumField(ident("enumField1"))
          ))
      )),
    process(ident("process2"))
  )

  println(
    bpmnsConfig(
      users(testUser),
      groups(adminGroup),
      bpmns(bpmnExample)
    ).stringify() 
  )

