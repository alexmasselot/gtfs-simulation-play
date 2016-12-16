package ch.octo.cffpoc.gtfs

import org.joda.time.{ Days, LocalDate }

import scala.collection.BitSet

/**
 * from a RawCalendar and an Iterator[RawCalendarDate], setup a memory efficient strucure can be be interrogated later on
 * Created by alex on 13.12.16.
 */
class ExceptionDater(val startDate: LocalDate, val endDate: LocalDate, val exceptions: Map[ServiceId, BitSet] = Map()) {
  val bsWidth = Days.daysBetween(startDate, endDate).getDays + 1

  /**
   * add an serviceId/localDate to the exception list
   *
   * @param serviceId
   * @param localDate
   * @return
   */
  def addException(serviceId: ServiceId, localDate: LocalDate): ExceptionDater = exceptions.get(serviceId) match {
    case None => new ExceptionDater(startDate, endDate, exceptions + (serviceId -> BitSet(bsWidth))).addException(serviceId, localDate)
    case Some(bs) => new ExceptionDater(startDate, endDate, exceptions.updated(serviceId, bs + date2i(localDate)))
  }

  /**
   * returns true if the service is running on the passed date
   *
   * @param serviceId
   * @param localDate
   * @return
   * @throws IndexOutOfBoundsException if the passed date is not within the ExceptionDater range
   */
  def isRunning(serviceId: ServiceId, localDate: LocalDate): Boolean = {
    if (localDate.isBefore(startDate) || localDate.isAfter(endDate)) {
      throw new IndexOutOfBoundsException(s"$localDate not  within [$startDate - $endDate]")
    }
    exceptions.get(serviceId) match {
      case None => return true
      case Some(bs) => bs(date2i(localDate))
    }
  }

  def countServices: Int = exceptions.size

  /**
   * get the number of days before localDate ad startDate, thus the index within the bitset
   *
   * @param localDate
   * @return
   */
  def date2i(localDate: LocalDate): Int = {
    if (localDate.isBefore(startDate) || localDate.isAfter(endDate)) {
      throw new IndexOutOfBoundsException(s"$localDate not  within [$startDate - $endDate]")
    }
    return Days.daysBetween(startDate, localDate).getDays
  }
}

object ExceptionDater {
  def load(calendar: RawCalendar, itExceptionDates: Iterator[RawCalendarDate]): ExceptionDater = {
    return itExceptionDates.foldLeft(new ExceptionDater(calendar.dateStart, calendar.dateEnd))((ed, rcd) => ed.addException(rcd.serviceId, rcd.date))
  }
}
