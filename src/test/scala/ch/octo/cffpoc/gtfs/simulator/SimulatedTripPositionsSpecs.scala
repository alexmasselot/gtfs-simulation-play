package ch.octo.cffpoc.gtfs.simulator

import ch.octo.cffpoc.gtfs.{ StopTime, _ }
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import org.joda.time.DateTime
import org.scalactic.{ Equivalence, TolerantNumerics, TypeCheckedTripleEquals }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 06.10.16.
 */
class SimulatedTripPositionsSpecs extends FlatSpec with Matchers {

  val trip1: Trip = Trip(
    TripId("trip1"),
    RawRoute(RouteId("rt1"), AgencyId("agcy"), RouteShortName("rsn"), RouteLongName("route long name"), RouteType.GONDOLA),
    ServiceId("service1"),
    StopName("head stop1"),
    TripShortName("tsn1"),
    List(
      StopTime(RawStop(StopId("sid1-1"), StopName("stop1-1"), 45.0, 6.5), TripId("trip1"), ScheduleTime("13:59:00"), ScheduleTime("14:01:00")),
      StopTime(RawStop(StopId("sid1-2"), StopName("stop1-2"), 45.3, 6.1), TripId("trip1"), ScheduleTime("14:10:00"), ScheduleTime("14:10:00")),
      StopTime(RawStop(StopId("sid1-2"), StopName("stop1-2"), 45.3, 6.1), TripId("trip1"), ScheduleTime("14:20:00"), ScheduleTime("14:25:00")),
      StopTime(RawStop(StopId("sid1-1"), StopName("stop-11"), 44.3, 7.1), TripId("trip1"), ScheduleTime("14:31:00"), ScheduleTime("14:31:00"))
    )
  )
  val trip2Stops: Trip = Trip(
    TripId("trip2"),
    RawRoute(RouteId("rt1"), AgencyId("agcy"), RouteShortName("rsn"), RouteLongName("route long name"), RouteType.GONDOLA),
    ServiceId("service1"),
    StopName("head stop1"),
    TripShortName("tsn1"),
    List(
      StopTime(RawStop(StopId("sid2-1"), StopName("stop2-1"), 45.0, 6.5), TripId("trip2"), ScheduleTime("00:00:00"), ScheduleTime("00:10:00")),
      StopTime(RawStop(StopId("sid2-2"), StopName("stop2-2"), 44.3, 7.1), TripId("trip2"), ScheduleTime("00:20:00"), ScheduleTime("00:20:00"))
    )
  )
  val trip3StopsShortBreak: Trip = Trip(
    TripId("trip3short"),
    RawRoute(RouteId("rt1"), AgencyId("agcy"), RouteShortName("rsn"), RouteLongName("route long name"), RouteType.GONDOLA),
    ServiceId("service1"),
    StopName("head stop1"),
    TripShortName("tsn1"),
    List(
      StopTime(RawStop(StopId("sid3s-1"), StopName("stop3s-1"), 45.0, 6.5), TripId("trip3s"), ScheduleTime("00:00:00"), ScheduleTime("00:10:00")),
      StopTime(RawStop(StopId("sid3s-2"), StopName("stop3s-2"), 44.0, 7), TripId("trip3s"), ScheduleTime("00:13:00"), ScheduleTime("00:13:00")),
      StopTime(RawStop(StopId("sid3s-3"), StopName("stop3s-3"), 43, 8), TripId("trip3s"), ScheduleTime("00:20:00"), ScheduleTime("00:20:00"))
    )
  )

  val trip3StopsLongBreak: Trip = Trip(
    TripId("trips3long"),
    RawRoute(RouteId("rt1"), AgencyId("agcy"), RouteShortName("rsn"), RouteLongName("route long name"), RouteType.GONDOLA),
    ServiceId("service1"),
    StopName("head stop1"),
    TripShortName("tsn1"),
    List(
      StopTime(RawStop(StopId("sid3l-1"), StopName("stop3l-1"), 45.0, 6.5), TripId("trip3l"), ScheduleTime("00:00:00"), ScheduleTime("00:10:00")),
      StopTime(RawStop(StopId("sid3l-2"), StopName("stop3l-3"), 44.0, 7), TripId("trip3l"), ScheduleTime("00:13:00"), ScheduleTime("00:17:00")),
      StopTime(RawStop(StopId("sid3l-3"), StopName("stop3l-3"), 43, 8), TripId("trip3l"), ScheduleTime("00:20:00"), ScheduleTime("00:20:00"))
    )
  )
  it should "crete empty SimulatedTripPositions" in {
    val simulatedTripPositions = new SimulatedTripPositions()
    simulatedTripPositions.size should be(0)
  }

  it should "three stops int SimulatedTripPositions" in {
    val simulatedTripPositions = SimulatedTripPositions(trip1, RawCalendarDateReader.dateFromString("20010116"))
    simulatedTripPositions.size should be(4)
    simulatedTripPositions.positions.last.lat should be(44.3)
  }

  def extractSPData[M](stp: SimulatedTripPositions, f: (SimulatedPosition) => M): List[M] = {
    stp.positions.map(f)
  }

  val stps2stops = SimulatedTripPositions(trip2Stops, RawCalendarDateReader.dateFromString("20010116"), 60)
  it should "2 stops trips with 1 minutes interval - size " in {
    stps2stops.size should be(11)
  }
  it should "2 stops trips with 1 minutes interval - times " in {
    extractSPData(stps2stops, _.secondsOfDay) should be(List(600, 660, 720, 780, 840, 900, 960, 1020, 1080, 1140, 1200))
  }

  val stps3stopsShortBreak = SimulatedTripPositions(trip3StopsShortBreak, RawCalendarDateReader.dateFromString("20010116"), 120)
  it should "3 stops, short break trips with 2 minutes interval - size " in {
    stps3stopsShortBreak.size should be(6)
  }
  it should "3 stops, short break trips with 2 minutes interval - times " in {
    extractSPData(stps3stopsShortBreak, _.secondsOfDay) should be(List(600, 720, 840, 960, 1080, 1200))
  }
  it should "3 stops, short break trips with 2 minutes interval - status " in {
    extractSPData(stps3stopsShortBreak, _.status) should be(List(SimulatedPositionStatus.START, SimulatedPositionStatus.MOVING, SimulatedPositionStatus.MOVING, SimulatedPositionStatus.MOVING, SimulatedPositionStatus.MOVING, SimulatedPositionStatus.END))
  }
  it should "3 stops, short break trips with 2 minutes interval - is at stop " in {
    extractSPData(stps3stopsShortBreak, _.stopId.isDefined) should be(List(true, false, false, false, false, true))
  }

  val stps3stopsLongBreak = SimulatedTripPositions(trip3StopsLongBreak, RawCalendarDateReader.dateFromString("20010116"), 120)
  it should "3 stops, long break trips with 2 minutes interval - size " in {
    stps3stopsLongBreak.size should be(6)
  }
  it should "3 stops, short long trips with 2 minutes interval - times " in {
    extractSPData(stps3stopsLongBreak, _.secondsOfDay) should be(List(600, 720, 840, 960, 1080, 1200))
  }
  it should "3 stops, long break trips with 2 minutes interval - StopId(options) " in {
    extractSPData(stps3stopsLongBreak, _.stopId) should be(List(Some(StopId("sid3l-1")), None, Some(StopId("sid3l-2")), Some(StopId("sid3l-2")), None, Some(StopId("sid3l-3"))))
  }
  it should "3 stops, long break trips with 2 minutes interval - latitiude " in {
    extractSPData(stps3stopsLongBreak, _.lat) should equal(List(45, 45 - 2.0 / 3, 44.0, 44.0, 44.0 - 1.0 / 3, 43))
  }
  it should " simulated, with 1 minute intervals" in {
    val simulatedTripPositions = SimulatedTripPositions(trip1, RawCalendarDateReader.dateFromString("20010116"), 60)
    simulatedTripPositions.size should be(31)
    simulatedTripPositions.positions.head.secondsOfDay should be(14 * 3600 + 1 * 60)
    simulatedTripPositions.positions.head.status should be(SimulatedPositionStatus.START)
    simulatedTripPositions.positions(1).status should be(SimulatedPositionStatus.MOVING)
    simulatedTripPositions.positions.last.status should be(SimulatedPositionStatus.END)

    List(0, 2, 3, 29, 30).map(i => simulatedTripPositions.positions(i))
      .map(_.lat) should equal(List(45.0, 45.06666666666666, 45.099999999999994, 44.46666666666667, 44.3))
  }

  // a mock function to interpolate a SimulatedTripPositions, with minute increment
  def toSimTrip(id: String,
    fromTime: String,
    toTime: String,
    fromStop: String,
    toStop: String,
    fromLat: Double,
    toLat: Double,
    fromLng: Double,
    toLng: Double): SimulatedTripPositions = {
    val tripId = TripId(id)
    val agencyId = AgencyId("agcy")
    val routeShortName = RouteShortName("rsn")
    val routeLongName = RouteLongName("rsn1234")
    val routeType = RouteType.GONDOLA

    val fromTS: Int = ScheduleTime(fromTime).getSecondOfDay
    val toTS: Int = ScheduleTime(toTime).getSecondOfDay

    def interpolate(t: Long, x0: Double, x1: Double): Double = {
      x0 + (t - fromTS).toDouble / (toTS - fromTS) * (x1 - x0)
    }

    val pos = (fromTS to toTS by 60).map({ t =>
      SimulatedPosition(t, interpolate(t, fromLat, toLat), interpolate(t, fromLng, toLng), tripId, agencyId, routeShortName, routeLongName, routeType, SimulatedPositionStatus.MOVING, None)
    }).toList

    val posBounded = List(
      List(SimulatedPosition(fromTS, fromLat, fromLng, tripId, agencyId, routeShortName, routeLongName, routeType, SimulatedPositionStatus.START, Some(StopId(fromStop)))),
      pos,
      List(SimulatedPosition(toTS, toLat, toLng, tripId, agencyId, routeShortName, routeLongName, routeType, SimulatedPositionStatus.END, Some(StopId(toStop))))
    ).flatten

    new SimulatedTripPositions(posBounded)
  }

  it should "create mock SimulatedTripPositions" in {
    val stp = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    stp.size should be(13)
    stp.positions(3).lat should be(10.2)
    stp.positions(3).lng should be(-5.2)
    math.floor(stp.positions(3).secondsOfDay / 60).toInt % 60 should be(1)
  }

  def checkDateAreOrdered(stp: SimulatedTripPositions) = {
    val dates = stp.positions.map(_.secondsOfDay)
    dates.take(dates.size - 1).zip(dates.tail)
      .filter(dp => dp._1 > dp._2) should equal(Nil)
  }

  it should "merge to STP should keep time ordered - no duplicate time" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp2 = toSimTrip("id2", "14:00:30", "14:05:30", "stA", "stB", 10, 11, -5, -6)

    val stp = stp1 ++ stp2
    stp.size should be(21)
    checkDateAreOrdered(stp)

    stp.positions.map(st => math.floor(st.secondsOfDay / 60).toInt % 60) should equal(List(59, 59, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 6, 7, 8, 9, 9))
    stp.positions.map(_.secondsOfDay % 60) should equal(List(0, 0, 0, 30, 30, 0, 30, 0, 30, 0, 30, 0, 30, 0, 30, 30, 0, 0, 0, 0, 0))
  }

  it should "merge to STP 2 starts after first last" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp2 = toSimTrip("id2", "15:00:30", "15:05:30", "stA", "stB", 10, 11, -5, -6)

    val stp = stp1 ++ stp2
    stp.size should be(21)
    checkDateAreOrdered(stp)

  }

  it should "merge to STP 1 starts after second last" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp2 = toSimTrip("id2", "15:00:30", "15:05:30", "stA", "stB", 10, 11, -5, -6)

    val stp = stp2 ++ stp1
    stp.size should be(21)
    checkDateAreOrdered(stp)

  }
  it should "merge to STP should keep time ordered - no duplicate time - reverse" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp2 = toSimTrip("id2", "14:00:30", "14:05:30", "stA", "stB", 10, 11, -5, -6)

    val stp = stp2 ++ stp1
    stp.size should be(21)
    checkDateAreOrdered(stp)
  }

  it should "merge to STP should keep time ordered - identic STP" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp = stp1 ++ stp1
    stp.size should be(26)
    checkDateAreOrdered(stp)
  }

  it should "merge a list of STP - 3" in {
    val stp1 = toSimTrip("id1", "13:59:00", "14:09:00", "stA", "stB", 10, 11, -5, -6)
    val stp2 = toSimTrip("id2", "14:00:30", "14:05:30", "stA", "stB", 10, 11, -5, -6)
    val stp3 = toSimTrip("id3", "14:03:00", "14:07:00", "stA", "stB", 10, 11, -5, -6)

    val stp = SimulatedTripPositions.merge(Seq(stp1, stp2, stp3))
    stp.size should be(28)
    checkDateAreOrdered(stp)
  }
}
