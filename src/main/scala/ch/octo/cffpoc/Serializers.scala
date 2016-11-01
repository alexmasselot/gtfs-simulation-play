package ch.octo.cffpoc

import ch.octo.cffpoc.gtfs.{ AgencyId, AgencyName, RawAgency }
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

