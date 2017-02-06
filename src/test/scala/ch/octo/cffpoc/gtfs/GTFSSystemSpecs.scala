package ch.octo.cffpoc.gtfs

import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class GTFSSystemSpecs extends FlatSpec with Matchers {
  behavior of "GTFSSystem"

  it should "indexIt OK" in {
    val l: List[(String, Int)] = List(("bb", 2), ("x", 42), ("aa", 1))
    val m: Map[Int, String] = Map(1 -> "aa", 2 -> "bb", 42 -> "x")

    GTFSSystem.indexIt(l.toIterator, { (x: (String, Int)) => x._2 }, { (x: (String, Int)) => x._1 }) should equal(m)

  }

  it should "indexIt Fail" in {
    val l: List[(String, Int)] = List(("bb", 2), ("x", 42), ("aa", 2))

    an[GTFSParsingException] should be thrownBy {
      GTFSSystem.indexIt(l.toIterator, { (x: (String, Int)) => x._2 }, { (x: (String, Int)) => x._1 })
    }
  }

  it should "loadAgency" in {
    GTFSSystem.loadAgencies("src/test/resources/gtfs").size shouldBe (410)
  }
  it should "loadStops" in {
    GTFSSystem.loadStops("src/test/resources/gtfs").size shouldBe (4480)
  }

  lazy val system = GTFSSystem.load("src/test/resources/gtfs")
  lazy val systemIR = GTFSSystem.load("src/test/resources/gtfs", (rr: RawRoute) => rr.routeLongName.value.startsWith("IR"))

  it should "load" in {
    val cd = system
  }

  it should "full: countTrips" in {
    system.countTrips should be(8)
  }
  it should "full: thorw exception when giving an outside data" in {
    an[IndexOutOfBoundsException] should be thrownBy {
      system.findAllTripsByDate(RawCalendarDateReader.dateFromString("20010116"))
    }
  }
  it should "full: get a trip subset when gicinv an exceptin date" in {
    system.findAllTripsByDate(RawCalendarDateReader.dateFromString("20160903"))
      .map(_.tripId)
      .toSet should equal(Set(TripId("3369:1"), TripId("3369:3"), TripId("682")))
  }

  it should "IR: routes only countTrips" in {
    systemIR.countTrips should be(4)
  }
  it should "IR: get a trip subset when gicinv an exceptin date" in {
    systemIR.findAllTripsByDate(RawCalendarDateReader.dateFromString("20160903"))
      .map(_.tripId)
      .toSet should equal(Set(TripId("682")))
  }
}
