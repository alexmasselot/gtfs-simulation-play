package ch.octo.cffpoc.gtfs.simulator.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import ch.octo.cffpoc.gtfs.TripCollection
import ch.octo.cffpoc.gtfs.simulator.TimeAccelerator
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

  trips.toList.foreach({ trip =>
    val actTrip = context.actorOf(Props(new ActorDelayedSimulatedTrip(actorSink, timeAccelerator, trip, date, averagSecondIncrement)), name = s"simulated-trip-${trip.tripId.value}")
  })
  override def receive: Receive = {
    case x => log.info(x.toString)
  }
}
