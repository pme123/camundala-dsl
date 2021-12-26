package camundala
package api

import camundala.bpmn.DecisionDmn
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.generic.auto.*

case class EvaluateDecision[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    decisionDmn: DecisionDmn[In, Out],
    restApi: CamundaRestApi[In, Out]
) extends ApiEndpoint[In, Out, EvaluateDecision[In, Out]]:

  val outStatusCode = StatusCode.Ok
  val apiName = "DecisionDmn"
  val decisionDefinitionKey = decisionDmn.decisionDefinitionKey

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): EvaluateDecision[In, Out] =
    copy(restApi = restApi)

  override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
    s"""
       |
       |Decision DMN:
       |- _decisionDefinitionKey_: `$decisionDefinitionKey`,
       |""".stripMargin

  def createPostman()(implicit
      tenantId: Option[String]
  ): Seq[Endpoint[?, ?, ?, ?]] =
    Seq(
      postmanBaseEndpoint
        .in(postPath(decisionDefinitionKey))
        .post
    )

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "decision-definition" / "key" / definitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "evaluate")
      .getOrElse(basePath / "evaluate") / s"--REMOVE:${restApi.name}--"

  override protected def inMapperPostman() =
    restApi.inMapper[EvaluateDecisionIn] { (example: In) =>
      EvaluateDecisionIn(
        CamundaVariable.toCamunda(example)
      )
    }
    
end EvaluateDecision

