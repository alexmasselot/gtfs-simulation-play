package ch.octo.cffpoc.gtfs.raw

import java.io.File

import ch.octo.cffpoc.gtfs.{ RawCalendarDate, ServiceId }
import com.github.tototoshi.csv.CSVReader
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

/**
 * Created by alex on 29/04/16.
 */

object RawCalendarDateReader extends RawDataCollectionReader[RawCalendarDate] {

  val dateFormatter = DateTimeFormat.forPattern("yyyyMMdd")

  def dateFromString(s: String) = {
    LocalDate.parse(s, dateFormatter)
  }

  override def builReadFunction(header: Array[String]): (Array[String]) => RawCalendarDate = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) => RawCalendarDate(ServiceId(line(h2i("service_id"))), dateFromString(line(h2i("date"))))
  }

}