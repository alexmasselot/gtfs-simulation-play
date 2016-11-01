package ch.octo.cffpoc.gtfs.raw

import java.io.File

import ch.octo.cffpoc.gtfs.{ RawStop, StopId, StopName }
import com.github.tototoshi.csv.CSVReader

/**
 * Created by alex on 02/05/16.
 */

object RawStopReader extends RawDataCollectionReader[RawStop] {

  override def builReadFunction(header: Array[String]): (Array[String]) => RawStop = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) => {
      val fl = line.toList.mkString(",")
      RawStop(
        StopId(line(h2i("stop_id"))),
        StopName(line(h2i("stop_name"))),
        lat = line(h2i("stop_lat")).toDouble,
        lng = line(h2i("stop_lon")).toDouble
      )
    }
  }
}

