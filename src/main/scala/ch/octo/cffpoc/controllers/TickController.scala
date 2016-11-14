package ch.octo.cffpoc.controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import akka.stream.scaladsl.Source
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, Controller }

import scala.concurrent.duration._

/**
 * Created by alex on 01.11.16.
 */
@Singleton
class TickController @Inject() () extends Controller {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  def tick = Action {
    Ok.chunked(jsonSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }

  def jsonSource: Source[JsValue, _] = {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("HH mm ss")

    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map({
      (tick) =>
        println(tick)
        Json.toJson(Map("ts" -> df.format(ZonedDateTime.now())))
    })
    s
  }

}
