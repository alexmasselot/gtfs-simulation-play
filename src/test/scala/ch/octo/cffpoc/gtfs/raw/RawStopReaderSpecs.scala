package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs.{ RawStop, StopId, StopName }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawStopReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawStopReader"
  def load = RawStopReader.load("src/test/resources/gtfs/stops.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(4480)
  }

  it should "get Some" in {
    load.find(_.stopId == StopId("8507472:3")) should equal(Some(
      RawStop(StopId("8507472:3"), StopName("Ausserberg"), lat = 46.312102, lng = 7.844347))
    )
  }

  it should "get None" in {
    load.find(_.stopId == StopId("4242:1")) should equal(None)
  }
}
