package ch.octo.cffpoc.gtfs.simulator.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import ch.octo.cffpoc.gtfs.TripCollection
import ch.octo.cffpoc.gtfs.simulator.TimeAccelerator
import ch.octo.cffpoc.gtfs.simulator.actors.SimulatorMessages.StopSimulation
import org.joda.time.LocalDate

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

  val actorsTrip: List[ActorRef] = trips.toList.map({ trip =>
    context.actorOf(Props(new ActorDelayedSimulatedTrip(actorSink, timeAccelerator, trip, date, averagSecondIncrement)), name = s"simulated-trip-${trip.tripId.value}")
  })

  override def receive: Receive = {
    case StopSimulation =>
      actorsTrip.foreach(a => a ! StopSimulation)
      context stop self

  }
}
