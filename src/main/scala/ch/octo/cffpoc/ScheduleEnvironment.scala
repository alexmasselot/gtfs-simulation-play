package ch.octo.cffpoc

import javax.inject._

import ch.octo.cffpoc.gtfs.GTFSSystem
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import play.api.Configuration

/**
 * Created by alex on 07.02.17.
 * a singleton to handle the GTFS system, the global trips etc.
 */
@Singleton
class ScheduleEnvironment @Inject() (configuration: Configuration) {
  val path = configuration.getString("schedule.gtfs.path").get
  val gtfsSystem = GTFSSystem.load(path)

  val date = RawCalendarDateReader.dateFromString(configuration.getString("schedule.date").get)
  val trips = gtfsSystem.findAllTripsByDate(date)

}
