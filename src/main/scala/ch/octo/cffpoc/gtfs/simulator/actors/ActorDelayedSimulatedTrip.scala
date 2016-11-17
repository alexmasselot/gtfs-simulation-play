package ch.octo.cffpoc.gtfs.simulator.actors

import akka.actor.{ Actor, ActorLogging, ActorRef }
import ch.octo.cffpoc.gtfs.Trip
import ch.octo.cffpoc.gtfs.simulator.actors.SimulatorMessages.{ EndOfTripSimulation, StartSimultationSchedule, StopSimulation }
import ch.octo.cffpoc.gtfs.simulator.{ SimulatedTripPositions, TimeAccelerator }
import org.joda.time.LocalDate

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Created by alex on 25.10.16.
 */
class ActorDelayedSimulatedTrip(actorSink: ActorRef,
    timeAccelerator: TimeAccelerator,
    trip: Trip,
    date: LocalDate,
    averagSecondIncrement: Double)(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  val stp = SimulatedTripPositions(trip, date, averagSecondIncrement, true)

  val scheduler = context.system.scheduler

  //delay its autostart
  val scheduled = scheduler.scheduleOnce(timeAccelerator.inMS(trip.startsAt.getSecondOfDay) milliseconds, self, StartSimultationSchedule)

  override def receive: Receive = {
    case StartSimultationSchedule =>
      log.info(s"launching trip ${trip.tripId}")
      stp.positions.foreach({ sp =>
        val in = timeAccelerator.inMS(sp.secondsOfDay)
        scheduler.scheduleOnce(in milliseconds, actorSink, sp)
      })
    case EndOfTripSimulation =>
      context.stop(self)
    case StopSimulation =>
      scheduled.cancel()
      context.stop(self)
  }
}
