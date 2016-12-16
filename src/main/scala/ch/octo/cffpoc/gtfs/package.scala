package ch.octo.cffpoc

import org.joda.time.{ LocalDate }

/**
 * The RAW* desqcribe the raw data loaded from GTFS
 * we have the dependencies
 * * final: Agency, Service, Stop
 * * Route -> Agency
 * * CalendarDate -> Service
 * * Trip -> Route, Service
 * * StopTime -> Trip, Stop
 *
 * Created by alex on 29/04/16.
 */
package object gtfs {

  /**
   * get routes type. The order is important, dependending on the specs
   * https://developers.google.com/transit/gtfs/reference/routes-file
   */
  object RouteType extends Enumeration {
    type RouteType = Value
    val TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, GONDOLA, FUNICULAR = Value
  }

  case class ServiceId(value: String)

  case class RouteId(value: String)

  case class TripId(value: String)

  case class TripShortName(value: String)

  case class StopName(value: String)

  case class AgencyName(value: String)

  case class AgencyId(value: String)

  case class StopId(value: String)

  case class RouteShortName(value: String)

  case class RouteLongName(value: String)

  case class RawCalendarDate(serviceId: ServiceId, date: LocalDate)

  case class RawCalendar(dateStart: LocalDate, dateEnd: LocalDate)

  case class RawTrip(routeId: RouteId, serviceId: ServiceId, tripId: TripId, tripHeadSign: StopName, tripShortName: TripShortName)

  case class RawStop(stopId: StopId, stopName: StopName, lat: Double, lng: Double)

  case class RawStopTime(tripId: TripId, stopId: StopId, timeArrival: ScheduleTime, timeDeparture: ScheduleTime)

  case class RawAgency(agencyId: AgencyId, agencyName: AgencyName)

  case class RawRoute(routeId: RouteId, agencyId: AgencyId, routeShortName: RouteShortName, routeLongName: RouteLongName, routeType: RouteType.RouteType)

  case class StopTime(stop: RawStop, tripId: TripId, timeArrival: ScheduleTime, timeDeparture: ScheduleTime)

}
