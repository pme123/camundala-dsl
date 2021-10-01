package camundala
package api

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*
import sttp.tapir.{Endpoint, Schema, SchemaType}

object TestApi extends APICreator {

  def title = "Test API"

  def version = "1.0"

  def apiEndpoints: Seq[ApiEndpoint] = Sample.apiEndpoints

}


object Sample extends EndpointDSL :
  val name = "sample-process"
  implicit override def tenantId: Option[String] = Some("MyTENANT")

  @description("My Sample input object to make the point.")
  case class SampleIn(
                       @description("Make sure it reflects the name of the Id.")
                       firstName: String = "Peter",
                       lastName: String = "Pan",
                       dateOfBirth: String = "1734-12-04",
                       nationality: String = "CH",
                       country: String = "CH",
                       address: Address = Address()
                     )

  case class Address(
                      street: String = "Merkurstrasse",
                      houseNumber: Option[String] = Some("16"),
                      place: String = "Lenzburg",
                      postcode: Int = 5600,
                      @description("Country as ISO Code")
                      country: String = "CH"
                    )

  case class SampleOut(
                        @description("Indication if this was a success")
                        success: Int = 0,
                        outputValue: String = "Just some text"
                      )


  lazy val standardSample: SampleIn = SampleIn()
  private val descr =
    s"""This runs the Sample Process.
       |""".stripMargin


  lazy val apiEndpoints: Seq[ApiEndpoint] =
    Seq(
      StartProcessInstance(
        name,
        descr,
        RequestInput(Map("standard" -> standardSample, "other input" -> SampleIn(firstName = "Heidi"))),
        RequestOutput.ok(Map("standard" -> SampleOut(), "other outpt" -> SampleOut(success = -1))),
        List(
          badRequest.example(CamundaError("BadStuffHappened", "There is a real Problem.")),
          notFound.defaultExample,
          serverError.example("InternalServerError", "Check the Server Logs!")
        ),
        Some("sample-example")
      )
    )
