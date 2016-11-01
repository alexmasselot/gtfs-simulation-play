package ch.octo.cffpoc

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.Matchers

/**
 * Created by alex on 10/03/16.
 */
trait DateMatchers extends Matchers {
  val fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss")

  def assertDateEquals(expDate: DateTime, actualDate: DateTime) = fmt.print(expDate) should equal(fmt.print(actualDate))

}
