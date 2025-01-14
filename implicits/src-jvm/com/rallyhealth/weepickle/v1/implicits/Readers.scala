package com.rallyhealth.weepickle.v0.implicits

import java.time._

trait Readers extends DefaultReaders {

  implicit val LocalDateReader: Reader[LocalDate] = new MapStringReader(s => LocalDate.parse(s.toString))
  implicit val LocalTimeReader: Reader[LocalTime] = new MapStringReader(s => LocalTime.parse(s.toString))
  implicit val LocalDateTimeReader: Reader[LocalDateTime] = new MapStringReader(s => LocalDateTime.parse(s.toString))
  implicit val OffsetDateTimeReader: Reader[OffsetDateTime] = new MapStringReader(s => OffsetDateTime.parse(s.toString))
  implicit val InstantReader: Reader[Instant] = new MapStringReader(s => Instant.parse(s.toString))
}
