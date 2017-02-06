package ch.octo.cffpoc.gtfs

import java.text.SimpleDateFormat
import java.util.Date

import org.joda.time.chrono.ISOChronology
import org.joda.time.{ DateTime, DateTimeZone, LocalDate }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class ScheduleTimeSpecs extends FlatSpec with Matchers {
  behavior of "ScheduleTime"

  it should "parseDate" in {
    val t = ScheduleTime("17:15:07")
    t.hours should equal(17)
    t.minutes should equal(15)
    t.seconds should equal(7)
  }

  it should "exception is thrown without two digit values" in {
    an[CannotParseScheduleTimeException] should be thrownBy {
      val t = ScheduleTime("7:15:07")
    }
  }

  it should "parseDate 24h" in {
    val t = ScheduleTime("24:15:07")
    t.hours should equal(24)
    t.minutes should equal(15)
    t.seconds should equal(7)
  }

  it should "parseDate 26h" in {
    val t = ScheduleTime("26:15:07")
    t.hours should equal(26)
    t.minutes should equal(15)
    t.seconds should equal(7)
  }

  it should "toDateTime during the day" in {

    ScheduleTime("22:15:07").toDateTime(new LocalDate(2016, 3, 1)) should be(DateTime.parse("2016-03-01T22:15:07.000"))
  }
}
