package ch.octo.cffpoc.gtfs.simulator

import ch.octo.cffpoc.gtfs.GTFSSystem

/**
 * Created by alex on 06.10.16.
 */
class GTFSSimulator(val system: GTFSSystem) {
}

object GTFSSimulator {
  def apply(system: GTFSSystem): GTFSSimulator = new GTFSSimulator(system)
}