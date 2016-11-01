package ch.octo.cffpoc.controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import ch.octo.cffpoc.gtfs.GTFSSystem
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Created by alex on 30/03/16.
 */

class SimulationController @Inject() (configuration: Configuration, actorSystem: ActorSystem) extends Controller {
  //@Inject() (val environment: play.api.Environment, val configuration: play.api.Configuration) extends AkkaController {

  lazy val gtfsSystem = GTFSSystem.load(configuration.getString("gtfs.system.path").get)

  def agencies = Action {
    Ok("haha") //Json.toJson(gtfsSystem.agencies.toList))
  }

}
