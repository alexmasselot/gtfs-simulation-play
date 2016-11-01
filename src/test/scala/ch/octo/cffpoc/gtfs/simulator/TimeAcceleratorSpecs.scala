package ch.octo.cffpoc.gtfs.simulator

import org.scalatest.{FlatSpec, FunSuite, Matchers}

/**
  * Created by alex on 25.10.16.
  */
class TimeAcceleratorSpecs   extends FlatSpec with Matchers {
  behavior of "TimeAccelerator"

  it should "be right now" in {
    val ta = TimeAccelerator(1000L, 100, 1)
    ta.inMS(100, 1000) should be(0)
  }


  it should "same init time, factor=1" in {
    val ta = TimeAccelerator(1000L, 100, 1)
    ta.inMS(110, 1000) should be(10000L)
  }

  it should "same init time, factor=10" in {
    val ta = TimeAccelerator(1000L, 100, 10)
    ta.inMS(110, 1000) should be(1000L)
  }

  it should "shift init time, isntant, factor=1" in {
    val ta = TimeAccelerator(1000L, 100, 1)
    ta.inMS(110, 1000L+10000) should be(0)
  }
  it should "shift init time, future, factor=1" in {
    val ta = TimeAccelerator(1000L, 100, 1)
    ta.inMS(130, 1000L+10000) should be(20*1000L)
  }
  it should "shift init time, future, factor=10" in {
    val ta = TimeAccelerator(1000L, 100, 10)
    ta.inMS(130, 1000L+10000/10) should be(20*1000L/10)
  }
}
