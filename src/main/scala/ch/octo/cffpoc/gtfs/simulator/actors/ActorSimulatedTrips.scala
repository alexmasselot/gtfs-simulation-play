package ch.octo.cffpoc.gtfs.simulator.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import ch.octo.cffpoc.gtfs.TripCollection
import ch.octo.cffpoc.gtfs.simulator.{ SimulatedTripPositions, TimeAccelerator }
import ch.octo.cffpoc.gtfs.simulator.actors.SimulatorMessages.{ StartScheduleTrip, StartSimultationSchedule, StopSimulation }
import org.joda.time.LocalDate

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

/**
 * Created by alex on 25.10.16.
 */
class ActorSimulatedTrips(actorSink: ActorRef,
    timeAccelerator: TimeAccelerator,
    trips: TripCollection,
    date: LocalDate,
    averagSecondIncrement: Double)(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  val scheduler = context.system.scheduler

  val cancellables = trips.toList.map({ trip =>
    scheduler.scheduleOnce(timeAccelerator.inMS(trip.startsAt.getSecondOfDay) milliseconds, self, StartScheduleTrip(trip))
    //context.actorOf(Props(new ActorDelayedSimulatedTrip(actorSink, timeAccelerator, trip, date, averagSecondIncrement)), name = s"simulated-trip-${trip.tripId.value}")
  })

  override def receive: Receive = {
    case StartScheduleTrip(trip) =>
      log.debug(s"launching scheduled trip ${trip.tripId}")
      val stp = SimulatedTripPositions(trip, date, averagSecondIncrement, true)
      stp.positions.foreach({ sp =>
        val in = timeAccelerator.inMS(sp.secondsOfDay)
        scheduler.scheduleOnce(in milliseconds, actorSink, sp)
      })
    case StopSimulation =>
      cancellables.foreach(c => c.cancel())
      context stop self
  }
}
