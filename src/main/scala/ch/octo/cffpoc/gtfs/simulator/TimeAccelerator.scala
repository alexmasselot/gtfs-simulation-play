package ch.octo.cffpoc.gtfs.simulator

/**
 * Created by alex on 24.10.16.
 */
case class TimeAccelerator(millisStartTime: Long, secondOfDayStartSim: Int, fastFactor: Double) {

  def inMS(sod: Int, millisCurrent: Long = System.currentTimeMillis()): Long = {

    (1000 * (sod - secondOfDayStartSim) / fastFactor).toLong + millisStartTime - millisCurrent;
  }
}
