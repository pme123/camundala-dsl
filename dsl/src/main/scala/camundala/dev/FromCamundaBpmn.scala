package camundala
package dev

import org.camunda.bpm.model.bpmn.Bpmn as CBpmn

import scala.language.implicitConversions

def testList =
  println("Using a Java List in Scala")
  val javaList: java.util.List[String] = new ArrayList()
  val scalaSeq: Seq[String] = javaList.asScala.toSeq
  scalaSeq.foreach(println)
  for s <- scalaSeq do println(s)

trait FromCamundaBpmn extends DSL with DSL.Givens:

  // context function def f(using BpmnModelInstance): T
  type FromCamundable[T] = CBpmnModelInstance ?=> T
  type ZIdent = IO[FromCamundaException, String]

  def fromCamunda(
      fromCamundaConfig: FromCamundaConfig
  ): IO[FromCamundaException, Seq[Bpmn]] =
    for {
      cawemoFolder <- UIO(
        new File(fromCamundaConfig.cawemoFolder)
      )
      cawemoFiles <- cawemoBpmns(cawemoFolder)

      bpmns <- ZIO.foreach(cawemoFiles) { bpmnFile =>
        fromCamunda(
          bpmnFile,
          path(s"${fromCamundaConfig.withIdFolder}/${bpmnFile.getName}")
        )
      }
    } yield bpmns

  private def cawemoBpmns(cawemoFolder: File) =
    if (cawemoFolder.isDirectory)
      ZIO.succeed(
        cawemoFolder
          .listFiles(new FilenameFilter {
            def accept(dir: File, name: String): Boolean =
              name.endsWith(".bpmn")
          })
          .toSeq
      )
    else
      ZIO.fail(
        FromCamundaException(
          s"The configured Cawemo Folder must be a Directory (${cawemoFolder.getAbsolutePath} is not!)."
        )
      )

  private def fromCamunda(
      bpmnFile: File,
      outputPath: BpmnPath
  ): IO[FromCamundaException, Bpmn] = {
    (for {
      modelInstance <- ZIO(
        CBpmn.readModelFromFile(bpmnFile)
      )
      cProcesses <- ZIO(
        modelInstance
          .getModelElementsByType(classOf[CProcess])
          .asScala
          .toSeq
      )
      processes <- ZIO.collect(cProcesses) { p =>
        p.fromCamunda()(using modelInstance)
          .mapError(Some(_))
      }
      _ <- ZIO(
        CBpmn.writeModelToFile(new File(outputPath), modelInstance)
      )
    } yield bpmn(bpmnFile).processes(processes: _*))
      .mapError {
        case Some(ex: FromCamundaException) => ex
        case t: Throwable =>
          t.printStackTrace
          FromCamundaException(t.getMessage)
      }
  }

  extension (camundaProcess: CProcess)
    def fromCamunda(): FromCamundable[IO[FromCamundaException, BpmnProcess]] =
      for {
        ident <- camundaProcess.createIdent()
        startEvents <- createElements(classOf[CStartEvent], startEvent)
        userTasks <- createElements(classOf[CUserTask], userTask)
        serviceTasks <- createElements(
          classOf[CServiceTask],
          serviceTask
        )
        scriptTasks <- createElements(classOf[CScriptTask], scriptTask)
        callActivities <- createElements(classOf[CCallActivity], callActivity)
        businessRuleTasks <- createElements(
          classOf[CBusinessRuleTask],
          businessRuleTask
        )
        exclusiveGateways <- createElements(
          classOf[CExclusiveGateway],
          exclusiveGateway
        )
        parallelGateways <- createElements(
          classOf[CParallelGateway],
          parallelGateway
        )
        endEvents <- createElements(classOf[CEndEvent], endEvent)
        sequenceFlows <- createElements(
          classOf[CSequenceFlow],
          sequenceFlow
        )
      } yield process(ident)
        .nodes(
          startEvents ++
            userTasks ++
            serviceTasks ++
            scriptTasks ++
            callActivities ++
            businessRuleTasks ++
            exclusiveGateways ++
            parallelGateways ++
            endEvents: _*
        )
        .flows(sequenceFlows: _*)

    def createIdent(): ZIdent =
      for {
        ident <- identString(Option(camundaProcess.getName), camundaProcess)
        _ = camundaProcess.setId(ident)
      } yield ident

  def createElements[T <: CFlowElement, C](
      clazz: Class[T],
      constructor: String => C
  ): FromCamundable[IO[FromCamundaException, Seq[C]]] = {
    ZIO.collect(
      summon[CBpmnModelInstance]
        .getModelElementsByType(clazz)
        .asScala
        .toSeq
    ) { fe =>
      val zident: ZIdent = fe.createIdent()
      zident
        .mapError(e => Some(e))
        .map(ident => constructor(ident))
    }
  }

  extension (element: CFlowElement)
    def generateIdent(): ZIdent =
      identString(Option(element.getName), element)

    def createIdent(): ZIdent =
      for {
        ident <-
          element match
            case flow: CSequenceFlow =>
              flow.createIdent()
            case _ =>
              generateIdent()
        _ = element.setId(ident)
      } yield ident

  extension (element: CSequenceFlow)
    def createIdent(): ZIdent = {
      for {
        ident <- element.generateIdent()
        sourceIdent <- element.getSource.generateIdent()
        targetIdent <- element.getTarget.generateIdent()
        newIdent = s"${ident}__${sourceIdent}__${targetIdent}"
        _ = element.setId(newIdent)
      } yield newIdent
    }

  def identString(name: Option[String], camObj: CBaseElement): ZIdent =
    val elemKey: String =
      camObj.getElementType.getTypeName.capitalize.filter(c =>
        s"$c".matches("[A-Z]")
      )
    zio
      .Task(
        name match
          case Some(n) =>
            n.split("[^a-zA-Z0-9]")
              .map(_.capitalize)
              .mkString + elemKey
          case None =>
            camObj.getId
      )
      .mapError(ex =>
        ex.printStackTrace
        FromCamundaException(s"Could not create an Ident for $elemKey / $name")
        )

end FromCamundaBpmn

case class FromCamundaRunner(fromCamundaConfig: FromCamundaConfig)
    extends FromCamundaBpmn:

  def run(): ZIO[zio.console.Console, FromCamundaException, Seq[Bpmn]] =
    (for {
      _: Any <- putStrLn(
        s"Start From Camunda BPMNs from ${fromCamundaConfig.cawemoFolder}"
      )
      bpmns: Seq[Bpmn] <- fromCamunda(fromCamundaConfig)
      _: Any <- putStrLn(
        s"Generated BPMNs to ${fromCamundaConfig.withIdFolder}"
      )
    } yield (bpmns))
      .mapError { case t: Throwable =>
        t.printStackTrace
        FromCamundaException(t.getMessage)
      }

end FromCamundaRunner

case class FromCamundaConfig(
    cawemoFolder: BpmnPath,
    withIdFolder: BpmnPath
)

case class FromCamundaException(msg: String)
