package ch.octo.cffpoc

import ch.octo.cffpoc.gtfs._
import ch.octo.cffpoc.gtfs.simulator.SimulatedPosition
import ch.octo.cffpoc.models._
import play.api.libs.json._

/**
 * Created by alex on 24/02/16.
 */
object Serializers {

  implicit val writesGeoLoc = Json.writes[GeoLoc]
  implicit val writesGeoLocBearing = Json.writes[GeoLocBearing]

  implicit val writesStop = Json.writes[Stop]

  implicit object writeAgencyName extends Writes[AgencyName] {
    override def writes(o: AgencyName): JsValue = JsString(o.value)
  }

  implicit object writeAgencyId extends Writes[AgencyId] {
    override def writes(o: AgencyId): JsValue = JsString(o.value)
  }

  implicit val writeRawAgency = Json.writes[RawAgency]

  implicit object writeStopName extends Writes[StopName] {
    override def writes(o: StopName): JsValue = JsString(o.value)
  }
  implicit object writeStopId extends Writes[StopId] {
    override def writes(o: StopId): JsValue = JsString(o.value)
  }
  implicit val writeRawStop = Json.writes[RawStop]

  implicit object writeTripId extends Writes[TripId] {
    override def writes(o: TripId): JsValue = JsString(o.value)
  }
  implicit object writeRouteShortName extends Writes[RouteShortName] {
    override def writes(o: RouteShortName): JsValue = JsString(o.value)
  }
  implicit val writeSimulatedPosition = Json.writes[SimulatedPosition]

  implicit object formatHasTimedPosition extends Writes[HasTimedPosition] {
    override def writes(o: HasTimedPosition): JsValue =
      o match {
        case t: TimedPosition => Json.writes[TimedPosition].writes(t)
        case t: TimedPositionIsMoving => Json.writes[TimedPositionIsMoving].writes(t)
        case t: TimedPositionWithStop => Json.writes[TimedPositionWithStop].writes(t)
      }
  }

  //  val hasTimedPositionReads: Reads[HasTimedPosition]= {
  //
  //  }
}

