package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs._

/**
 * Created by alex on 03/05/16.
 */
object RawRouteReader extends RawDataCollectionReader[RawRoute] {

  override def builReadFunction(header: Array[String]): (Array[String]) => RawRoute = {
    val h2i = header.zipWithIndex.toMap
    (line: Array[String]) =>
      RawRoute(
        RouteId(line(h2i("route_id"))),
        AgencyId(line(h2i("agency_id"))),
        RouteShortName(line(h2i("route_short_name"))),
        RouteLongName(line(h2i("route_long_name")))
      )
  }
}
