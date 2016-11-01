package ch.octo.cffpoc.gtfs

/**
 * Created by alex on 04/05/16.
 */
case class Trip(tripId: TripId, route: RawRoute, serviceId: ServiceId, tripHeadSign: StopName, tripShortName: TripShortName, stopTimes: List[StopTime]) {
  def startsAt = stopTimes.head.timeDeparture
}

