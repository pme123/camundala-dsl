package camundala
package model

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.{Date, Locale}

private val camundaDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss"
private val camundaDateTimeFormatter = DateTimeFormatter
  .ofPattern(camundaDateTimeFormat)
  .withLocale(Locale.getDefault())
  .withZone(ZoneId.systemDefault())

def toCamundaDate(dateStr: String) =
  new Date(
    Instant
      .from(camundaDateTimeFormatter.parse(dateStr))
      .toEpochMilli
  )
