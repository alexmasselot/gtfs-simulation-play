package ch.octo.cffpoc.gtfs

import ch.octo.cffpoc.gtfs.raw.{ RawCalendarDateReader, RawCalendarReader }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class ExceptionDaterSpecs extends FlatSpec with Matchers {
  behavior of "ExceptionDater"

  def exceptionDater: ExceptionDater = {
    def itCal = RawCalendarReader.load("src/test/resources/gtfs/calendar.txt")

    def itCalDates = RawCalendarDateReader.load("src/test/resources/gtfs/calendar_dates.txt")

    return ExceptionDater.load(itCal.next(), itCalDates)
  }

  def exceptionDaterWithLimitiedServices: ExceptionDater = {
    def itCal = RawCalendarReader.load("src/test/resources/gtfs/calendar.txt")

    def itCalDates = RawCalendarDateReader.load("src/test/resources/gtfs/calendar_dates.txt")

    return ExceptionDater.load(itCal.next(), itCalDates)
  }

  it should "boundary dates" in {
    val ed = exceptionDater
    ed.startDate shouldEqual (RawCalendarDateReader.dateFromString("20151210"))
    ed.endDate shouldEqual (RawCalendarDateReader.dateFromString("20161215"))
  }

  it should "countService" in {
    val ed = exceptionDater
    ed.countServices shouldEqual (8)
  }

  it should "service be on" in {
    exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20160109")) shouldEqual (true)
  }
  it should "service be off, while service exist" in {
    exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20161016")) shouldEqual (false)
  }
  it should "service be off, while service does not exist" in {
    exceptionDater.isRunning(ServiceId("1234;9:9:x"), RawCalendarDateReader.dateFromString("20161016")) shouldEqual (true)
  }

  it should "service be on, date first date" in {
    exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20151213")) shouldEqual (false)
  }
  it should "service be on, date last date" in {
    exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20161212")) shouldEqual (false)
  }

  it should "exception is thrown without date before scope" in {
    an[IndexOutOfBoundsException] should be thrownBy {
      exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20151209"))
    }
  }
  it should "exception is thrown without date after scope" in {
    an[IndexOutOfBoundsException] should be thrownBy {
      exceptionDater.isRunning(ServiceId("3369:3:3:s"), RawCalendarDateReader.dateFromString("20161216"))
    }
  }

}
