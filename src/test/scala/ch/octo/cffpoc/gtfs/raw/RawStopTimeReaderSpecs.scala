package ch.octo.cffpoc.gtfs.raw

import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawStopTimeReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawStopTimeReader"
  def load = RawStopTimeReader.load("src/test/resources/gtfs/stop_times.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(95)
  }

}
