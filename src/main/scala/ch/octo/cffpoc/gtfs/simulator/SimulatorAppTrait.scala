package ch.octo.cffpoc.gtfs.simulator

import java.io.{ File, PrintWriter }

import ch.octo.cffpoc.gtfs.{ AgencyName, GTFSSystem, RouteShortName }
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import org.apache.commons.logging.{ Log, LogFactory }
import org.joda.time.LocalDate

/**
 * Created by alex on 07.10.16.
 */
trait SimulatorAppTrait {
  def LOGGER: Log

  def path: String

  lazy val system = GTFSSystem.load(path)

  def loadTripsForDate(date: LocalDate) = {

    LOGGER.info(s"total number of trips ${system.trips.size}")
    LOGGER.info(s"findAllTripsByDate($date)")
    val tripsForDate = system.findAllTripsByDate(date)
    LOGGER.info(s"total number of trips filtered by date ${tripsForDate.size}")
    tripsForDate
  }

}

