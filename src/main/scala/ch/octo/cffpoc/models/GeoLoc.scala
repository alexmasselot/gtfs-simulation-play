package ch.octo.cffpoc.models

/**
 * Created by alex on 29/02/16.
 */
sealed trait HasPosition {
  def lat: Double

  def lng: Double

  val earthRadius = 6371000

  /**
   * distance in meters with another location
   *
   * @param other
   * @return
   */
  def distanceMeters(other: HasPosition): Double = {
    val dLat = Math.toRadians(other.lat - lat)
    val dLng = Math.toRadians(other.lng - lng)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat)) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadius * c
  }
}

case class GeoLoc(lat: Double, lng: Double) extends HasPosition

case class GeoLocBearing(lat: Double, lng: Double, bearing: Option[Double]) extends HasPosition {
  override def toString = s"($lat, $lng) ↑$bearing"

}
