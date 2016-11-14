package ch.octo.cffpoc.controllers

import javax.inject.Inject

import akka.actor.{ ActorSystem, Props }
import akka.event.Logging
import akka.stream.{ ActorMaterializer, Materializer }
import akka.stream.OverflowStrategy._
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import ch.octo.cffpoc.gtfs.{ AgencyId, GTFSSystem, RouteShortName, TripId }
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import ch.octo.cffpoc.gtfs.simulator.{ SimulatedPosition, TimeAccelerator }
import ch.octo.cffpoc.gtfs.simulator.actors.ActorSimulatedTrips
import org.apache.commons.logging.LogFactory
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import scala.concurrent.ExecutionContext
import ch.octo.cffpoc.Serializers._

/**
 * Created by alex on 30/03/16.
 */

class SimulatorController @Inject() (configuration: Configuration)(implicit actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext) extends Controller {
  val logger = LoggerFactory.getLogger("SimulatorController")

  val path = "src/main/resources/gtfs_gondola"
  lazy val gtfsSystem = GTFSSystem.load(path)
  val date = RawCalendarDateReader.dateFromString("20161005")
  lazy val trips = gtfsSystem.findAllTripsByDate(date)

  def positions = Action {
    val logging = Logging(actorSystem.eventStream, logger.getName)

    val (ref, publisher) = Source.actorRef[SimulatedPosition](1000000, dropHead).toMat(Sink.asPublisher(true))(Keep.both).run()
    val jsonSource = Source.fromPublisher(publisher).map({
      (sp) =>
        Json.toJson(sp)
    })

    val ta = TimeAccelerator(System.currentTimeMillis(), 100, 1000)
    val actorSimulatedTrips = actorSystem.actorOf(Props(new ActorSimulatedTrips(ref, ta, trips, date, 20)))
    println(s"===> $ref")
    ref ! SimulatedPosition(3, 10.0, 20.0, TripId("tripid"), AgencyId("agencyId"), RouteShortName("rt"), None)

    Ok.chunked(jsonSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
