package ch.octo.cffpoc.controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props }
import akka.event.Logging
import akka.stream.scaladsl.{ Source, SourceQueue }
import akka.stream.{ ActorMaterializer, Materializer, OverflowStrategy }
import ch.octo.cffpoc.gtfs.simulator.SimulatedPosition
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, Controller }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

/**
 * Created by alex on 01.11.16.
 */
@Singleton
class SchedulerController @Inject() (configuration: Configuration)(implicit actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext) extends Controller {

  val logger = LoggerFactory.getLogger("SchedulerController")

  def test = Action {
    Ok.chunked(jsonSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }

  class ActorForward(queue: SourceQueue[String]) extends Actor with ActorLogging {
    override def receive: Receive = {
      case x: String =>
        queue.offer(x)
    }
  }

  def peekMatValue[T, M](src: Source[T, M]): (Source[T, M], Future[M]) = {
    val p = Promise[M]
    val s = src.mapMaterializedValue { m =>
      p.trySuccess(m)
      m
    }
    (s, p.future)
  }

  class ActorSched(actorSink: ActorRef) extends Actor with ActorLogging {
    val scheduler = context.system.scheduler

    override def receive: Receive = {
      case (nbEvent: Int, withinMs: Int) =>
        log.info(s"sending $nbEvent with $withinMs millseconds")
        val t0 = System.currentTimeMillis()

        val dts = (0 to nbEvent)
          .map(i => (withinMs * Math.random() + 1).toInt)
          .sorted
          .map({
            d =>
              scheduler.scheduleOnce(d milliseconds, actorSink, "x")
              d
          })
          .last
        scheduler.scheduleOnce(withinMs milliseconds, actorSink, PoisonPill)

    }
  }

  def playbook = Action {

    val logging = Logging(actorSystem.eventStream, logger.getName)

    val (queueSource, futureQueue) = peekMatValue(Source.queue[String](100000, OverflowStrategy.fail))

    futureQueue.map { queue =>
      val actorForward = actorSystem.actorOf(Props(new ActorForward(queue)))

      val actorSched = actorSystem.actorOf(Props(new ActorSched(actorForward)))

      actorSched ! (100, 2000)

      queue.watchCompletion().map { done =>
        actorForward ! PoisonPill
        println("Scheduler canceled")
      }
    }

    Ok.chunked(queueSource via EventSource.flow)

  }

  def jsonSource: Source[JsValue, _] = {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("HH mm ss")

    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map({
      (tick) =>
        Json.toJson(Map("ts" -> df.format(ZonedDateTime.now())))
    })
    s
  }

}
