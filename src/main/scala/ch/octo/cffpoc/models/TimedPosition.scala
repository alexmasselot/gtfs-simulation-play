package ch.octo.cffpoc.models

import ch.octo.cffpoc.models.{ GeoLoc, GeoLocBearing }
import org.joda.time.{ DateTime, DateTimeZone }

/**
 * Created by alex on 01/03/16.
 */

sealed trait HasTimedPosition {
  val timestamp: DateTime
  val position: GeoLocBearing
}

case class TimedPosition(
    timestamp: DateTime,
    position: GeoLocBearing) extends HasTimedPosition {

  def toDateTime(dateTimeZone: DateTimeZone) = TimedPosition(timestamp.toDateTime(dateTimeZone), position)

}

case class TimedPositionIsMoving(
    timestamp: DateTime,
    position: GeoLocBearing,
    moving: Boolean) extends HasTimedPosition {

}

case class TimedPositionWithStop(
    timestamp: DateTime,
    position: GeoLocBearing,
    stop: Option[Stop]) extends HasTimedPosition {
}