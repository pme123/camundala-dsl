package camundala
package api

val camundaVersion = "7.15"

// sttp
export sttp.model.StatusCode

// circe
export io.circe.{Decoder, Encoder}

// tapir
export sttp.tapir.EndpointIO.Example
export sttp.tapir.EndpointOutput.Void
export sttp.tapir.endpoint
export sttp.tapir.EndpointOutput
export sttp.tapir.json.circe.jsonBody
export sttp.tapir.oneOf
export sttp.tapir.oneOfMappingValueMatcher
export sttp.tapir.path
export sttp.tapir.Schema
export sttp.tapir.stringToPath
export sttp.tapir.Schema.annotations.description
