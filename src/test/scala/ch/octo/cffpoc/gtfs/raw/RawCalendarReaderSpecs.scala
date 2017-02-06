package ch.octo.cffpoc.gtfs.raw

import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawCalendarReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawCalendarReader"
  def load = RawCalendarReader.load("src/test/resources/gtfs/calendar.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(1)
  }

  it should "parse dates" in {
    val d1 = RawCalendarDateReader.dateFromString("20151213")
    val d2 = RawCalendarDateReader.dateFromString("20161212")

    val c = load.next()
    c.dateStart should equal(d1)
    c.dateEnd should equal(d2)
  }
}
