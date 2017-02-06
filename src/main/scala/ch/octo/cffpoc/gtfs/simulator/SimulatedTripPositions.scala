package ch.octo.cffpoc.gtfs.simulator

import ch.octo.cffpoc.gtfs.{ Trip, TripCollection }
import ch.octo.cffpoc.models.GeoLocBearing
import org.apache.commons.logging.LogFactory
import org.joda.time.LocalDate

import scala.annotation.tailrec
import scala.util.Random

/**
 * List of position with datetime & a tripId.
 * If the deault constructor is called directly, it is the responsibility of the user to provide.
 * us companion object method to build, interpolating trips
 */
class SimulatedTripPositions(val positions: List[SimulatedPosition] = Nil) {
  def size = positions.size

  def +(simulatedPosition: SimulatedPosition): SimulatedTripPositions = ???

  /**
   * add another SimulatedTripPositions.
   * the two are merged, with the datetime sorted.
   *
   * Please remind both SimulatedTripPositions input list are time sorted
   *
   * @param other the other list to be merged
   * @return
   */
  def ++(other: SimulatedTripPositions): SimulatedTripPositions = {
    @tailrec
    def fHandler(acc: List[SimulatedPosition], l1: List[SimulatedPosition], l2: List[SimulatedPosition]): List[SimulatedPosition] = {
      (l1, l2) match {
        case (Nil, Nil) => acc
        case (x1s, Nil) => acc ++ x1s
        case (Nil, x2s) => acc ++ x2s
        //case (x1s, x2s) if x2s.head.dateTime isAfter(x1s.last.dateTime) => x1s ::: x2s
        //case (x1s, x2s) if x1s.head.dateTime isAfter(x2s.last.dateTime) => x2s ::: x1s
        case (x1 :: x1s, x2 :: xs2) if x1.secondsOfDay < x2.secondsOfDay => fHandler(acc :+ x1, x1s, l2)
        case (x1s, x2 :: x2s) => fHandler(acc :+ x2, x1s, x2s)
      }
    }

    new SimulatedTripPositions(fHandler(Nil, positions, other.positions))
  }

}

object SimulatedTripPositions {
  val LOGGER = LogFactory.getLog(SimulatedTripPositions.getClass)

  /**
   * Converts a trip into a list of
   *
   * @param trip
   * @param date
   * @return
   */
  def apply(trip: Trip, date: LocalDate): SimulatedTripPositions = {
    val positions = trip.stopTimes.map(st => SimulatedPosition(
      st.timeArrival.getSecondOfDay,
      st.stop.lat,
      st.stop.lng,
      trip.tripId,
      trip.route.agencyId,
      trip.route.routeShortName,
      trip.route.routeLongName,
      trip.route.routeType,
      SimulatedPositionStatus.MOVING,
      Some(st.stop.stopId)
    )
    )
    new SimulatedTripPositions(positions)
  }

  /**
   * a hadler function to interpolate SimulatedPosition. Is for local object use
   *
   * @param trip         : the reference trip
   * @param date         : the date to start the trip with
   * @param deltaSeconds : a function to get the net increment of time (can be fix, random or whatever)
   * @return
   */
  def tripToSimulatedPositions(trip: Trip, date: LocalDate, deltaSeconds: () => Double): SimulatedTripPositions = {

    @tailrec
    def incrementPosHandler(acc: List[SimulatedPosition],
      currentPosition: SimulatedPosition,
      nextPositions: List[SimulatedPosition],
      deltaSeconds: () => Double,
      remain: Option[Int]): List[SimulatedPosition] = {

      nextPositions match {
        case Nil => acc
        case np :: nps =>
          val newTime = currentPosition.secondsOfDay + remain.getOrElse(deltaSeconds().toInt)
          if (newTime >= np.secondsOfDay) {
            incrementPosHandler(acc, np, nps, deltaSeconds, Some(newTime - np.secondsOfDay))
          } else {
            val x = 1.0 * (newTime - currentPosition.secondsOfDay) / (np.secondsOfDay - currentPosition.secondsOfDay)
            val oStop = if (currentPosition.stopId == np.stopId) np.stopId else None
            val newPos = SimulatedPosition(
              newTime,
              currentPosition.lat + x * (np.lat - currentPosition.lat),
              currentPosition.lng + x * (np.lng - currentPosition.lng),
              trip.tripId,
              trip.route.agencyId,
              trip.route.routeShortName,
              trip.route.routeLongName,
              trip.route.routeType,
              SimulatedPositionStatus.MOVING,
              oStop
            )
            incrementPosHandler(acc :+ newPos, newPos, nextPositions, deltaSeconds, None)
          }

      }
    }

    val simulatedStops = trip.stopTimes.flatMap(st => {
      val sp = SimulatedPosition(
        st.timeArrival.getSecondOfDay,
        st.stop.lat,
        st.stop.lng,
        trip.tripId,
        trip.route.agencyId,
        trip.route.routeShortName,
        trip.route.routeLongName,
        trip.route.routeType,
        SimulatedPositionStatus.MOVING,
        Some(st.stop.stopId))
      List(sp, sp.withSecondOfDay(st.timeDeparture.getSecondOfDay))
    }).drop(1).dropRight(1)

    val headStop = trip.stopTimes.head
    val lastStop = trip.stopTimes.last

    val timedPositions = incrementPosHandler(
      Nil,
      simulatedStops.head.withSecondOfDay(trip.stopTimes.head.timeDeparture.getSecondOfDay),
      simulatedStops.drop(1),
      deltaSeconds,
      None)

    //add stop and last trip position, with the according status
    val timedPostionPlusStart: List[SimulatedPosition] = List(
      List(SimulatedPosition(headStop.timeDeparture.getSecondOfDay,
        headStop.stop.lat,
        headStop.stop.lng,
        trip.tripId,
        trip.route.agencyId,
        trip.route.routeShortName,
        trip.route.routeLongName,
        trip.route.routeType,
        SimulatedPositionStatus.START,
        Some(headStop.stop.stopId)
      )),
      timedPositions,
      List(SimulatedPosition(lastStop.timeArrival.getSecondOfDay,
        lastStop.stop.lat,
        lastStop.stop.lng,
        trip.tripId,
        trip.route.agencyId,
        trip.route.routeShortName,
        trip.route.routeLongName,
        trip.route.routeType,
        SimulatedPositionStatus.END,
        Some(lastStop.stop.stopId)
      ))).flatten

    val simPositions: List[SimulatedPosition] =
      timedPostionPlusStart
        .foldLeft(List[SimulatedPosition]())((acc, a) => if (acc.size == 0 || acc.last != a) acc :+ a else acc)
    new SimulatedTripPositions(simPositions)
  }

  /**
   * take a trip a nd a list of simulated position, by interpolating positiong avery x seconds.
   * It can be either with fix increment (isRandomIncrement=false) or not.
   * In the latter case, increments are pseudo-random, distributed between 0 and 2*secondIncrement)
   *
   * @param trip
   * @param date
   * @param secondIncrement
   * @param isRandomIncrement
   * @return
   */
  def apply(trip: Trip, date: LocalDate, secondIncrement: Double, isRandomIncrement: Boolean = false): SimulatedTripPositions = {
    val fInc: () => Double = if (!isRandomIncrement) {
      () => secondIncrement
    } else {
      val rnd = new Random()
      () => rnd.nextDouble() * 2 * secondIncrement
    }
    tripToSimulatedPositions(trip, date, fInc)
  }

  def merge(trips: TripCollection, date: LocalDate, secondIncrement: Double, isRandomIncrement: Boolean = false): SimulatedTripPositions = {
    val n = trips.size
    LOGGER.info(s"transforming $n trips into simulated positions")
    val stps = trips.toList
      .sortBy(trip => trip.startsAt.getSecondOfDay)
      .zipWithIndex
      .map({
        case (trip, i) =>
          if (i > 0 && i % 10000 == 0) {
            LOGGER.info(s"merged $i/$n")
          }
          apply(trip, date, secondIncrement, isRandomIncrement)
      })

    LOGGER.info(s"build a list of ${stps.map(_.size).sum} positions. Sorting them...")
    val poss = stps.flatMap(_.positions)
      .sortBy(_.secondsOfDay)

    new SimulatedTripPositions(poss)
  }

  /**
   * merges a list of SimulatedTripPositions.
   * break recursively the sublist in two to achieve log_2(n) fusion
   *
   * @param stps
   * @return
   */
  def merge(stps: Seq[SimulatedTripPositions]): SimulatedTripPositions = {
    stps match {
      case x1 :: Nil => x1
      case xs =>
        val i = xs.size / 2
        val stps = merge(xs.take(i)) ++ merge(xs.drop(i))
        stps
    }
  }
}