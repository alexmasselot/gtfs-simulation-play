package ch.octo.cffpoc.gtfs.simulator

import java.io.{ File, PrintWriter }

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy._
import akka.stream.scaladsl.{ Flow, Source }
import ch.octo.cffpoc.gtfs.{ AgencyName, GTFSSystem, RouteShortName }
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import ch.octo.cffpoc.gtfs.simulator.actors.{ ActorSimulatedTrips, ActorSinkPositions }
import org.apache.commons.logging.LogFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

import scala.concurrent.ExecutionContext

/**
 * Created by alex on 07.10.16.
 */
class SimulatorToSinkApp extends SimulatorAppTrait {
  val LOGGER = LogFactory.getLog(SimulatorToSinkApp.getClass)
  val path = "src/main/resources/gtfs_gondola"

  def actorRefKafkaSink(system: ActorSystem)(implicit executioncontext: ExecutionContext, materilizer: ActorMaterializer): ActorRef = {

    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

    val source = Source.actorRef[SimulatedPosition](Int.MaxValue, fail)

    val ref = Flow[ProducerRecord[Array[Byte], String]]
      .to(Producer.plainSink(producerSettings))
      .runWith(source.map({ elem =>

        new ProducerRecord[Array[Byte], String]("simulated-positions", elem.toString)
      }))

    ref
  }

  def ActorRefPrintSink(system: ActorSystem)(implicit executioncontext: ExecutionContext) = {
    system.actorOf(Props(new ActorSinkPositions()), "position-sink")
  }

  def run() = {
    implicit val system = ActorSystem("GTFSSimulator")
    implicit val materializer = ActorMaterializer()

    import system.dispatcher

    val date = RawCalendarDateReader.dateFromString("20161005")
    val trips = loadTripsForDate(date)
    LOGGER.info(s"transforming ${trips.size} trips")

    val ta = TimeAccelerator(System.currentTimeMillis(), 100, 1000)
    val actorSink = actorRefKafkaSink(system)
    val actorSimulatedTrips = system.actorOf(Props(new ActorSimulatedTrips(actorSink, ta, trips, date, 20)))
  }
}

object SimulatorToSinkApp extends App {
  val app = new SimulatorToSinkApp()
  app.run()
}

