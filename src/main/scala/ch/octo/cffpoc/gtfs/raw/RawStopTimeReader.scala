package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs.{ RawStopTime, ScheduleTime, StopId, TripId }
import org.joda.time.LocalTime

/**
 * Created by alex on 03/05/16.
 */

object RawStopTimeReader extends RawDataCollectionReader[RawStopTime] {

  override def builReadFunction(header: Array[String]): (Array[String]) => RawStopTime = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) =>
      RawStopTime(TripId(line(h2i("trip_id"))),
        StopId(line(h2i("stop_id"))),
        ScheduleTime(line(h2i("arrival_time"))),
        ScheduleTime(line(h2i(("departure_time"))))
      )
  }
}