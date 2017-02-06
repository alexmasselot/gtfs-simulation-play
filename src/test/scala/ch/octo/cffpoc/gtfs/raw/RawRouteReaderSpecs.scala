package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs.{ RawStop, RouteType, StopId, StopName }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by alex on 17/02/16.
 */
class RawRouteReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawRouteReader"

  def load = RawRouteReader.load("src/test/resources/gtfs/routes.txt")

  it should "load" in {
    val cd = load
  }

  it should "size" in {
    load.size should equal(2)
  }
  it should "have read route type" in {
    val route = load.next()
    route.routeType should equal(RouteType.RAIL)
  }

  it should "get routeType BUS" in {
    RouteType.apply(3) should equal(RouteType.BUS)
  }
  it should "get routeType GONDOLA" in {
    RouteType.apply(6) should equal(RouteType.GONDOLA)
  }
}
