package ch.octo.cffpoc.gtfs

import org.joda.time.DateTime

/**
 * Created by alex on 06.10.16.
 */
package object simulator {

  object SimulatedPositionStatus extends Enumeration {
    val START, MOVING, END = Value
  }

  case class SimulatedPosition(secondsOfDay: Int,
      lat: Double,
      lng: Double,
      tripId: TripId,
      agencyId: AgencyId,
      routeShortName: RouteShortName,
      status: SimulatedPositionStatus.Value,
      stopId: Option[StopId]) {
    override def toString: String = f"${secondsOfDay / 3600}%02d:${(secondsOfDay / 60) % 60}%02d:${secondsOfDay % 60}%02d\t$lat%2.2f\t$lng%2.2f\t${routeShortName.value}\t$status\t${tripId.value}"
  }

}
