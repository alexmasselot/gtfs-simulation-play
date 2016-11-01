package ch.octo.cffpoc.gtfs.raw

import java.io.File

import ch.octo.cffpoc.gtfs._
import com.github.tototoshi.csv.CSVReader

/**
 * Created by alex on 02/05/16.
 */

object RawTripReader extends RawDataCollectionReader[RawTrip] {

  override def builReadFunction(header: Array[String]): (Array[String]) => RawTrip = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) => RawTrip(
      RouteId(line(h2i("route_id"))),
      ServiceId(line(h2i("service_id"))),
      TripId(line(h2i("trip_id"))),
      StopName(line(h2i("trip_headsign"))),
      TripShortName(line(h2i("trip_short_name")))
    )

  }
}
