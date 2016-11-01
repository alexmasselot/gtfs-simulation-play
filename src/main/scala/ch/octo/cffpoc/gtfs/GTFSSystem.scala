package ch.octo.cffpoc.gtfs

import ch.octo.cffpoc.gtfs.raw._
import org.apache.commons.logging.LogFactory
import org.joda.time.LocalDate

/**
 * Created by alex on 03/05/16.
 */
class GTFSSystem(val trips: TripCollection, val agencies: Map[AgencyId, RawAgency], exceptionDates: Map[LocalDate, Set[ServiceId]]) {

  def countTrips = trips.size

  def findAllTripsByDate(date: LocalDate): TripCollection = {
    exceptionDates.get(date) match {
      case None => trips
      case Some(skipDates) => trips.filter(t => !skipDates.contains(t.serviceId))
    }
  }
}

case class GTFSParsingException(message: String) extends Exception(message)

object GTFSSystem {
  val LOGGER = LogFactory.getLog(GTFSSystem.getClass)

  val FILENAME_STOPS = "stops.txt"
  val FILENAME_ROUTES = "routes.txt"
  val FILENAME_AGENCY = "agency.txt"
  val FILENAME_STOP_TIMES = "stop_times.txt"
  val FILENAME_TRIPS = "trips.txt"
  val FILENAME_CALENDAR_DATES = "calendar_dates.txt"

  def loadAgencies(rootSrc: String): Map[AgencyId, RawAgency] =
    indexIt(
      RawAgencyReader.load(s"$rootSrc/$FILENAME_AGENCY"), { (a: RawAgency) => a.agencyId }, { (a: RawAgency) => a }

    )

  def loadStops(rootSrc: String): Map[StopId, RawStop] =
    indexIt(
      RawStopReader.load(s"$rootSrc/$FILENAME_STOPS"), { (a: RawStop) => a.stopId }, { (a: RawStop) => a }

    )

  def loadRoutes(rootSrc: String): Map[RouteId, RawRoute] =
    indexIt(
      RawRouteReader.load(s"$rootSrc/$FILENAME_ROUTES"), { (a: RawRoute) => a.routeId }, { (a: RawRoute) => a }
    )

  /**
   * group by date and point to s set of serviceId
   *
   * @param rootSrc
   * @return
   */
  def loadExceptionDates(rootSrc: String): Map[LocalDate, Set[ServiceId]] = {
    RawCalendarDateReader.load(s"$rootSrc/$FILENAME_CALENDAR_DATES")
      .toList
      .groupBy(_.date)
      .map({ case (date, l) => date -> l.map(_.serviceId).toSet })
  }

  /**
   * associate a tripId with a list of StopTime (sorted by timeDeparture)
   *
   * @param rootSrc where to grab the files
   * @param stops   a map stopId => Stop
   * @return
   */
  def loadStopTimesByTripId(rootSrc: String, stops: Map[StopId, RawStop]): Map[TripId, List[StopTime]] = {
    RawStopTimeReader.load(s"$rootSrc/$FILENAME_STOP_TIMES")
      .map(rst => StopTime(stops(rst.stopId), rst.tripId, rst.timeArrival, rst.timeDeparture))
      .toList
      .groupBy(_.tripId)
    //.map({ case (id, l) => (id, l.sortBy(_.timeDeparture.g)) })
  }

  /**
   * load trip and associate them with the routes, serviceId and the list of StopTime
   *
   * @param rootSrc
   * @param stopTimesByTripId
   * @param routes
   * @return a map tripId->Trip
   */
  def loadTrips(rootSrc: String,
    stopTimesByTripId: Map[TripId, List[StopTime]],
    routes: Map[RouteId, RawRoute]): TripCollection = {

    TripCollection(RawTripReader.load(s"$rootSrc/$FILENAME_TRIPS")
      .zipWithIndex
      .map({
        case (rt, i) =>
          if (i > 0 && i % 10000 == 0) {
            LOGGER.info(s"loaded $i trips")
          }
          rt.tripId -> Trip(rt.tripId, routes(rt.routeId), rt.serviceId, rt.tripHeadSign, rt.tripShortName, stopTimesByTripId(rt.tripId))
      })
      .toMap
    )
  }

  /**
   * Load the whle system.
   * This is indded the only function to be called from outside
   *
   * @param rootSrc
   * @return
   */
  def load(rootSrc: String): GTFSSystem = {
    LOGGER.info(s"loading $rootSrc/$FILENAME_CALENDAR_DATES")
    val exceptionDates = loadExceptionDates(rootSrc)
    LOGGER.info(s"loading $rootSrc/$FILENAME_STOPS")
    val stops = loadStops(rootSrc)
    LOGGER.info(s"loading $rootSrc/$FILENAME_AGENCY")
    val agencies = loadAgencies(rootSrc)
    LOGGER.info(s"loading $rootSrc/$FILENAME_STOP_TIMES")
    val stopTimesByTripId = loadStopTimesByTripId(rootSrc, stops)
    LOGGER.info(s"loading $rootSrc/$FILENAME_ROUTES")
    val routes = loadRoutes(rootSrc)
    LOGGER.info(s"loading $rootSrc/$FILENAME_TRIPS")
    val trips = loadTrips(rootSrc, stopTimesByTripId = stopTimesByTripId, routes = routes)
    LOGGER.info("finished loading")
    new GTFSSystem(trips, agencies = agencies, exceptionDates = exceptionDates)
  }

  /**
   * take a list of objects and build a map based on the function fIndex towards a value computed by fValue
   *
   * @param iterator   the original list
   * @param fKey   how to get a key
   * @param fValue how to get a value
   * @tparam TO Originla bean type
   * @tparam TK key type
   * @tparam TV value type
   * @return a map TK -> TV
   * @throws GTFSParsingException if there are duplicated keys
   */
  def indexIt[TO, TK, TV](iterator: Iterator[TO],
    fKey: (TO) => TK,
    fValue: (TO) => TV): Map[TK, TV] = {
    val list = iterator.toList
    val n = list.size
    val map = list
      .map(x => fKey(x) -> fValue(x))
      .toMap

    if (n != map.size) {
      val s = list.map(fKey).toSet.diff(map.keySet)
      throw GTFSParsingException(s"not a unique index set while building the index. For example ${s.take(5)}")
    }
    map
  }
}