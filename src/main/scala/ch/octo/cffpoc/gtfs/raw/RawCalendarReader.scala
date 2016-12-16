package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs.{ RawCalendar, RawCalendarDate, ServiceId }
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

/**
 * Created by alex on 29/04/16.
 */

object RawCalendarReader extends RawDataCollectionReader[RawCalendar] {

  val dateFormatter = DateTimeFormat.forPattern("yyyyMMdd")

  def dateFromString(s: String) = {
    LocalDate.parse(s, dateFormatter)
  }

  override def builReadFunction(header: Array[String]): (Array[String]) => RawCalendar = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) => RawCalendar(dateFromString(line(h2i("start_date"))), dateFromString(line(h2i("end_date"))))
  }

}