package ch.octo.cffpoc.gtfs

import ch.octo.cffpoc.gtfs.raw._
import org.apache.commons.logging.LogFactory
import org.joda.time.LocalDate

/**
 * Created by alex on 03/05/16.
 */
class GTFSSystem(val trips: TripCollection,
    val stops: Map[StopId, RawStop],
    val agencies: Map[AgencyId, RawAgency],
    exceptionDater: ExceptionDater) {

  def countTrips = trips.size

  def findAllTripsByDate(date: LocalDate): TripCollection = {
    trips.filter(t => exceptionDater.isRunning(t.serviceId, date))
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
  val FILENAME_CALENDAR = "calendar.txt"

  def loadAgencies(rootSrc: String): Map[AgencyId, RawAgency] = {
    LOGGER.info(s"loading $rootSrc/$FILENAME_AGENCY")
    val m = indexIt(
      RawAgencyReader.load(s"$rootSrc/$FILENAME_AGENCY"), { (a: RawAgency) => a.agencyId }, { (a: RawAgency) => a }
    )
    LOGGER.info(s"loaded ${m.size} agencies")
    m
  }

  def loadStops(rootSrc: String): Map[StopId, RawStop] = {
    LOGGER.info(s"loading $rootSrc/$FILENAME_STOPS")
    val m = indexIt(
      RawStopReader.load(s"$rootSrc/$FILENAME_STOPS"), { (a: RawStop) => a.stopId }, { (a: RawStop) => a }
    )
    LOGGER.info(s"loaded ${m.size} stops")
    m
  }

  def loadRoutes(rootSrc: String,
    oFilter: Option[(RawRoute) => Boolean] = None): Map[RouteId, RawRoute] = {
    val fname = s"$rootSrc/$FILENAME_ROUTES"
    LOGGER.info(s"loading $fname")
    val itRoutes = oFilter match {
      case None => RawRouteReader.load(fname)
      case Some(filter) => RawRouteReader.load(fname).filter(filter)
    }

    val m = indexIt(
      itRoutes, { (a: RawRoute) => a.routeId }, { (a: RawRoute) => a }
    )
    LOGGER.info(s"loaded ${m.size} routes")
    m
  }

  /**
   * group by date and point to s set of serviceId
   *
   * @param rootSrc
   * @return
   */
  private def loadExceptionDater(rootSrc: String, includeServiceIds: Set[ServiceId]): ExceptionDater = {
    LOGGER.info(s"loading $rootSrc/$FILENAME_CALENDAR_DATES")
    LOGGER.info(s"loading $rootSrc/$FILENAME_CALENDAR")
    val itCalendarDates = RawCalendarDateReader.load(s"$rootSrc/$FILENAME_CALENDAR_DATES")
    val calendar = RawCalendarReader.load(s"$rootSrc/$FILENAME_CALENDAR").next()
    ExceptionDater.load(calendar, itCalendarDates.filter(rcd => includeServiceIds.contains(rcd.serviceId)))
  }

  /**
   * associate a tripId with a list of StopTime (sorted by timeDeparture)
   *
   * @param rootSrc where to grab the files
   * @param stops   a map stopId => Stop
   * @return
   */
  private def loadStopTimesByTripId(rootSrc: String,
    stops: Map[StopId, RawStop],
    tripIds: Set[TripId]): Map[TripId, List[StopTime]] = {
    LOGGER.info(s"loading $rootSrc/$FILENAME_STOP_TIMES")
    RawStopTimeReader.load(s"$rootSrc/$FILENAME_STOP_TIMES")
      .filter(rst => tripIds.contains(rst.tripId))
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
    LOGGER.info(s"loading $rootSrc/$FILENAME_TRIPS")
    val tc = TripCollection(RawTripReader
      .load(s"$rootSrc/$FILENAME_TRIPS")
      .filter(t => routes.contains(t.routeId))
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
    LOGGER.info(s"loaded ${tc.size} trips")
    tc
  }

  /**
   * load trip and associate them with the routes, serviceId and the list of StopTime
   *
   * @param rootSrc
   * @param routes
   * @return a map tripId->Trip
   */
  private def loadTripIdsForRoutes(rootSrc: String,
    routes: Map[RouteId, RawRoute]): Set[TripId] = {
    LOGGER.info(s"loadTripIdsForRoutes $rootSrc/$FILENAME_TRIPS")
    RawTripReader
      .load(s"$rootSrc/$FILENAME_TRIPS")
      .filter(t => routes.contains(t.routeId))
      .map(_.tripId)
      .toSet
  }

  /**
   * Load the whle system.
   * This is indded the only function to be called from outside
   *
   * @param rootSrc
   * @return
   */
  private def p_load(rootSrc: String,
    filterRoute: Option[(RawRoute) => Boolean]): GTFSSystem = {

    val routes = loadRoutes(rootSrc, filterRoute)
    val stops = loadStops(rootSrc)
    val agencies = loadAgencies(rootSrc)
    val tripIds = loadTripIdsForRoutes(rootSrc, routes = routes)
    val stopTimesByTripId = loadStopTimesByTripId(rootSrc, stops, tripIds)
    val trips = loadTrips(rootSrc, stopTimesByTripId = stopTimesByTripId, routes = routes)
    val exceptionDater = loadExceptionDater(rootSrc, trips.toList.map(_.serviceId).toSet)
    LOGGER.info("finished loading")
    new GTFSSystem(trips, stops, agencies = agencies, exceptionDater = exceptionDater)
  }

  /**
   * Load the whole system, with a condition on routes
   *
   * @param rootSrc
   * @param filterRoute filter funciton based on the route (routetype, longname...)
   * @return
   */
  def load(rootSrc: String,
    filterRoute: (RawRoute) => Boolean): GTFSSystem =
    p_load(rootSrc, Some(filterRoute))

  /**
   * Load the whole system.
   *
   * @param rootSrc
   * @return
   */
  def load(rootSrc: String): GTFSSystem = p_load(rootSrc, None)

  /**
   * take a list of objects and build a map based on the function fIndex towards a value computed by fValue
   *
   * @param iterator the original list
   * @param fKey     how to get a key
   * @param fValue   how to get a value
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