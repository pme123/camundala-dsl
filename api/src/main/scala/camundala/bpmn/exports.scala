package camundala
package bpmn

val camundaVersion = "7.15"


// os
export os.{pwd, Path, read}

// sttp
export sttp.model.StatusCode

// circe
export io.circe.{Decoder, Encoder, Json}
// One import for this ADT/JSON codec
export org.latestbit.circe.adt.codec.JsonTaggedAdt

// tapir
export sttp.tapir.EndpointIO.Example
export sttp.tapir.EndpointOutput.Void
export sttp.tapir.Endpoint
export sttp.tapir.endpoint
export sttp.tapir.EndpointOutput
export sttp.tapir.EndpointInput
export sttp.tapir.oneOf
export sttp.tapir.oneOfMappingValueMatcher
export sttp.tapir.path
export sttp.tapir.query
export sttp.tapir.Schema
export sttp.tapir.stringToPath
export sttp.tapir.Schema.annotations.description

def throwErr(err: String) =
  throw new IllegalArgumentException(err)