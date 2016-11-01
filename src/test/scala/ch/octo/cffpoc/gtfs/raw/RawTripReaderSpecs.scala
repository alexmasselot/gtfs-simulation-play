package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs._
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawTripReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawTripReader"
  def load = RawTripReader.load("src/test/resources/gtfs/trips.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(4)
  }

  it should "get Some" in {
    load.find(_.tripId == TripId("3369:1")) should equal(Some(
      RawTrip(RouteId("12003.000011"), ServiceId("3369:1:1:s"), TripId("3369:1"), StopName("Palézieux"), TripShortName("12003")))
    )
  }

  it should "get None" in {
    load.find(_.tripId == TripId("4242:1")) should equal(None)
  }
}

