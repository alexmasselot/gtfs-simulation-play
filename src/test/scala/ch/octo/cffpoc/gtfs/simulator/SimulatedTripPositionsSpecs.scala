package ch.octo.cffpoc.gtfs.simulator

import ch.octo.cffpoc.gtfs.{StopTime, _}
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by alex on 06.10.16.
  */
class SimulatedTripPositionsSpecs extends FlatSpec with Matchers {
  val trip1: Trip = Trip(
    TripId("trip1"),
    RawRoute(RouteId("rt1"), AgencyId("agcy"), RouteShortName("rsn"), RouteLongName("route long name")),

    ServiceId("service1"),
    StopName("head stop1"),
    TripShortName("tsn1"),
    List(
      StopTime(RawStop(StopId("sid1-1"), StopName("stop1-1"), 45.0, 6.5), TripId("trip1"), ScheduleTime("13:59:00"), ScheduleTime("14:01:00")),
      StopTime(RawStop(StopId("sid1-2"), StopName("stop1-2"), 45.3, 6.1), TripId("trip1"), ScheduleTime("14:11:00"), ScheduleTime("14:11:00")),
      StopTime(RawStop(StopId("sid1-1"), StopName("stop-11"), 44.3, 7.1), TripId("trip1"), ScheduleTime("14:31:00"), ScheduleTime("14:31:00"))
    )
  )

  it should "crete empty SimulatedTripPositions" in {
    val simulatedTripPositions = new SimulatedTripPositions()
    simulatedTripPositions.size should be(0)
  }

  it should "three stops int SimulatedTripPositions" in {
    val simulatedTripPositions = SimulatedTripPositions(trip1, RawCalendarDateReader.dateFromString("20010116"))
    simulatedTripPositions.size should be(3)
    simulatedTripPositions.positions.last.lat should be(44.3)
  }

  it should " simulated, with 1 minute intervals" in {
    val simulatedTripPositions = SimulatedTripPositions(trip1, RawCalendarDateReader.dateFromString("20010116"), 60)
    simulatedTripPositions.size should be(34)
    simulatedTripPositions.positions.head.status should be(SimulatedPositionStatus.START)
    simulatedTripPositions.positions(1).status should be(SimulatedPositionStatus.MOVING)
    simulatedTripPositions.positions.last.status should be(SimulatedPositionStatus.END)

    List(0, 2, 3, 31, 32).map(i => simulatedTripPositions.positions(i))
      .map(_.lat).map(x => (x * 1000).round / 1000.0) should equal(List(45.0, 45.0, 45.03, 44.35, 44.3))

  }


  // a mock function to interpolate a SimulatedTripPositions, with minute increment
  def toSimTrip(id: String,
                fromTime: String,
                toTime: String,
                fromStop:String,
                toStop:String,
                fromLat: Double,
                toLat: Double,
                fromLng: Double,
                toLng: Double
               ): SimulatedTripPositions = {
    val tripId = TripId(id)
    val agencyId = AgencyId("agcy")
    val routeShortName = RouteShortName("rsn")


    val fromTS: Int = ScheduleTime(fromTime).getSecondOfDay
    val toTS: Int = ScheduleTime(toTime).getSecondOfDay

    def interpolate(t: Long, x0: Double, x1: Double): Double = {
      x0 + (t - fromTS).toDouble / (toTS - fromTS) * (x1 - x0)
    }
    val pos = (fromTS to toTS by 60).map({ t =>
      SimulatedPosition(t, interpolate(t, fromLat, toLat), interpolate(t, fromLng, toLng), tripId, agencyId, routeShortName, SimulatedPositionStatus.MOVING, None)
    }).toList

    val posBounded = List(
      List(SimulatedPosition(fromTS, fromLat, fromLng, tripId, agencyId, routeShortName, SimulatedPositionStatus.START, Some(StopId(fromStop)))),
      pos,
      List(SimulatedPosition(toTS, toLat, toLng, tripId, agencyId, routeShortName, SimulatedPositionStatus.END, Some(StopId(toStop))))
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
