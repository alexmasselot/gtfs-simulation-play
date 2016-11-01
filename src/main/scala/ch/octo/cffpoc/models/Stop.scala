package ch.octo.cffpoc.models

/**
 * Created by alex on 26/02/16.
 */
case class Stop(id: Long, name: String, location: GeoLoc) {
  override def toString = s"$name ($id)"
}
