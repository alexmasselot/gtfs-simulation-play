package ch.octo.cffpoc.gtfs.simulator

import ch.octo.cffpoc.gtfs.{ Trip, TripCollection }
import ch.octo.cffpoc.models.GeoLocBearing
import ch.octo.cffpoc.position.TimedPosition
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
      targetPosition: SimulatedPosition,
      deltaSeconds: () => Double): List[SimulatedPosition] = {
      val newTime = currentPosition.secondsOfDay + deltaSeconds().toInt
      if (newTime >= targetPosition.secondsOfDay) {
        acc :+ targetPosition
      } else {
        val x = 1.0 * (newTime - currentPosition.secondsOfDay) / (targetPosition.secondsOfDay - currentPosition.secondsOfDay)
        val newPos = SimulatedPosition(newTime,
          currentPosition.lat + x * (targetPosition.lat - currentPosition.lat),
          currentPosition.lng + x * (targetPosition.lng - currentPosition.lng),
          trip.tripId,
          trip.route.agencyId,
          trip.route.routeShortName,
          None
        )
        incrementPosHandler(acc :+ newPos, newPos, targetPosition, deltaSeconds)
      }
    }

    val posTurn = trip.stopTimes.flatMap(st => List(
      SimulatedPosition(st.timeArrival.getSecondOfDay, st.stop.lat, st.stop.lng, trip.tripId, trip.route.agencyId,
        trip.route.routeShortName, Some(st.stop.stopId)),
      SimulatedPosition(st.timeDeparture.getSecondOfDay, st.stop.lat, st.stop.lng, trip.tripId, trip.route.agencyId,
        trip.route.routeShortName, Some(st.stop.stopId))
    ))

    val headStop = trip.stopTimes.head
    val timedPositions = posTurn
      .take(posTurn.size).zip(posTurn.tail)
      .flatMap({
        case (st1, st2) => {
          incrementPosHandler(Nil, st1, st2, deltaSeconds)
        }
      })
    val timedPostionPlusStart = timedPositions.+:(SimulatedPosition(headStop.timeArrival.getSecondOfDay,
      headStop.stop.lat,
      headStop.stop.lng,
      trip.tripId,
      trip.route.agencyId,
      trip.route.routeShortName,
      Some(headStop.stop.stopId)
    ))

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