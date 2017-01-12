package ch.octo.cffpoc.gtfs.simulator.actors

import ch.octo.cffpoc.gtfs.Trip

/**
 * Created by alex on 26.10.16.
 */
object SimulatorMessages {
  case object StopSimulation
  case object EndOfTripSimulation
  case object StartSimultationSchedule
  case class StartScheduleTrip(trip: Trip)
}
