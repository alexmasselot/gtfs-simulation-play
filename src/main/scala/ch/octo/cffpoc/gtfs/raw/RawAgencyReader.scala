package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs._

/**
 * Created by alex on 03/05/16.
 */
object RawAgencyReader extends RawDataCollectionReader[RawAgency] {

  override def builReadFunction(header: Array[String]): (Array[String]) => RawAgency = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) => RawAgency(
      AgencyId(line(h2i("agency_id"))),
      AgencyName(line(h2i("agency_name")))
    )
  }

}
