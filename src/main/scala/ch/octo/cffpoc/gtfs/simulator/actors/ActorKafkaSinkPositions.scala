package ch.octo.cffpoc.gtfs.simulator.actors

import akka.actor.{ Actor, ActorLogging }
import ch.octo.cffpoc.gtfs.simulator.SimulatedPosition

import scala.concurrent.ExecutionContext

/**
 * Created by alex on 25.10.16.
 */
class ActorKafkaSinkPositions()(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  override def receive: Receive = {
    case stp: SimulatedPosition => log.info(s"received $stp")
    case x => log.info(s"lost $x")
  }
}
