package ch.octo.cffpoc.gtfs

/**
 * Created by alex on 03/09/16.
 */
class TripCollection(trips: Map[TripId, Trip]) {
  def size = trips.size
  def apply(tripId: TripId) = trips(tripId)
  def toList = trips.values.toList
  def map[T](f: (Trip) => T) = trips.values.map(f)

  def filter(f: (Trip) => Boolean): TripCollection = TripCollection(trips.filter((p) => f(p._2)))

}

object TripCollection {
  def apply(trips: Map[TripId, Trip]) = new TripCollection(trips)
}
