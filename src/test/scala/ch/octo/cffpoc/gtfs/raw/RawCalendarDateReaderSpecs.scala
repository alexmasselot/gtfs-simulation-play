package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs.ServiceId
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawCalendarDateReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawCalendarDateReader"
  def load = RawCalendarDateReader.load("src/test/resources/gtfs/calendar_dates.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(479)
  }

  it should "parseDate" in {
    val d = RawCalendarDateReader.dateFromString("20160116")
    d.getDayOfMonth should equal(16)
    d.getMonthOfYear should equal(1)
    d.getYear should equal(2016)
  }
}
