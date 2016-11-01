package ch.octo.cffpoc.gtfs.simulator

import java.io.{ File, PrintWriter }

import ch.octo.cffpoc.gtfs.{ AgencyName, GTFSSystem, RouteShortName }
import ch.octo.cffpoc.gtfs.raw.RawCalendarDateReader
import org.apache.commons.logging.LogFactory

/**
 * Created by alex on 07.10.16.
 */
class SimulatorToFileApp extends SimulatorAppTrait {
  val LOGGER = LogFactory.getLog(SimulatorToFileApp.getClass)
  val path = "src/main/resources/gtfs_train"

  def run() = {
    val date = RawCalendarDateReader.dateFromString("20161005")

    //val agencyIdExclude = system.agencies.filter({ case (id, ra) =>  }).keys.toSet
    val agencyIdInclude = system.agencies.filter({
      case (id, ra) =>
        //ra.agencyName == AgencyName("SBB (Schweizerische Bundesbahnen SBB)")
        !BusAgenciesNotPost.set.contains(ra.agencyName.value)
      //true
    }).keys.toSet

    val tripsFiltered = loadTripsForDate(date)
      .filter(x => agencyIdInclude.contains(x.route.agencyId))
    LOGGER.info(s"total number of trips filtered by agencies ${tripsFiltered.size}")
    LOGGER.info(s"transforming ${tripsFiltered.size} trips into simulated positions")

    val simPos = SimulatedTripPositions.merge(tripsFiltered, date, 30, true)
    LOGGER.info(s"build a list of ${simPos.size} simulated positions")

    val outFile: String = s"$path/gtfs-simulated-pos.txt"
    val writer = new PrintWriter(new File(outFile))
    writer.append(s"secondOfDay\tlat\tlng\ttripId\tagencyId\trouteShortName\tstopId\n")
    simPos.positions.foreach(sp =>
      writer.append(s"""${sp.secondsOfDay}\t${sp.lat}\t${sp.lng}\t${sp.tripId.value}\t${sp.agencyId.value}\t${sp.routeShortName.value}\t${sp.stopId.getOrElse("")}\n""")
    )
    LOGGER.info(s"simulated position saved to $outFile")
  }
}

object SimulatorToFileApp extends App {
  val app = new SimulatorToFileApp()
  app.run()
}

object BusAgenciesNotPost {
  val set = List("TRN/Auto (Service d'automobiles TRN(vr))",
    "TL (Transport publics de la Région Lausannoise)",
    "TRN-tn (Transports Publics Neuchâtelois SA (tn))",
    "WB (Waldenburgerbahn)",
    "TRAVYS-y (Transports Vallée de Joux-Yverdon-Ste-Croix (ystec))",
    "WSB (Wynental-und Suhrental-Bahn)",
    "AWA (Autobetrieb Weesen-Amden)",
    "AB-ab (Appenzeller Bahnen (ab))",
    "BGB (Busbetrieb Gemeinde Bergün)",
    "PRH (PRO REGIO HUTTWIL Verkehrsverein)",
    "MTB (Mountain Tour Bergün GmbH)",
    "RVBW (Regionale Verkehrsbetriebe Baden-Wettingen)",
    "AOT (Autokurse Oberthurgau)",
    "BBA (Busbetrieb Aarau)",
    "AAGS (Auto AG Schwyz)",
    "RVSH (Regionale Verkehrsbetriebe Schaffhausen)",
    "Extrabus (Events / Manifestations / Eventi)",
    "Extrabus (Interimsfahrplan / Horaire interimaire / Orario ad interim)",
    "BNP (Bus Nyon-Prangins)",
    "VBD (Verkehrsbetrieb der Landschaft Davos)",
    "BLS-ths (BLS AG (ths))",
    "TRAVYS-o (Transports Vallée de Joux-Yverdon-Ste-Croix (oc))",
    "TMR-mc (Transports de Martigny et Régions (mc))",
    "LFüB (Fürgangen-Bellwald)",
    "VBSG (Verkehrsbetriebe der Stadt St.Gallen)",
    "ARAG (Automobil Rottal AG)",
    "TPC/Auao (Service d'automobiles TPC (aomc auto))",
    "TPN (Transports Publics de la Région Nyonnaise)",
    "FART Aut (Autolinee FART)",
    "AAGU (Auto AG Uri)",
    "AAGL (Autobus AG Liestal)",
    "BLWE (Busbetrieb Lichtensteig-Wattwil-Ebnat-Kappel)",
    "AFA (Autoverkehr Frutigen-Adelboden)",
    "AAGR (Auto AG Rothenburg)",
    "SBB Bus (Automobildienst SBB)",
    "TRN/tc (Transports Publics Neuchâtelois SA (tc))",
    "BOGG (Busbetrieb Olten-Gösgen-Gäu)",
    "AVB (Aroser Verkehrsbetriebe)",
    "TRN/Autr (Service d'automobiles TRN (rvt Auto))",
    "ALEX (Aletsch-Express Riederalp-Bettmeralp)",
    "LEE Bus (Autobusbetrieb LEE)",
    "VBH (Verkehrsbetriebe Herisau)",
    "SMC (Sierre-Montana-Crans)",
    "OTL (Office du tourisme de Leysin)",
    "SMF-lsm (Sportbahnen Melchsee-Frutt (lsm))",
    "TRI (Riddes-Isérables)",
    "BLT (Baselland Transport)",
    "BOB (Berner Oberland-Bahnen)",
    "BDWM-bd (BDWM Transport (bd))",
    "WSU (Walker's Söhne Urnerboden)",
    "AUT (Andermatt-Urserntal Tourismus GmbH)",
    "SBG (Südbadenbus GmbH)",
    "BOG (Busbetrieb Oberems-Gruben)",
    "WTZ (Walter Tschannen Zofingen)",
    "RBS Auto (Autobusbetrieb RBS)",
    "TSD-asdt (Theytaz Excursions Sion)",
    "TPC/Autb (Service d'automobiles TPC (bvb auto))",
    "LLB (Auto Leuk-Leukerbad)",
    "AS (Autobetrieb Sernftal)",
    "ARL (Autolinee Regionali Luganesi)",
    "BRER (Busbetrieb Rapperswil-Eschenbach-Rüti ZH)",
    "VBG (Verkehrsbetriebe Glattal)",
    "RhB (Rhätische Bahn)",
    "RA (Regionalps)",
    "VBL (Verkehrsbetriebe Luzern)",
    "BVB (Basler Verkehrsbetriebe)",
    "SVB Auto (Städtische Verkehrsbetriebe Bern)",
    "TPL (Trasporti Pubblici Luganesi)",
    "AMSA (Autolinea Mendrisiense SA)",
    "SNL Auto (Servizio d'automobili)",
    "AVJ (Autotransports de la Vallée de Joux)",
    "BKG (Busbetrieb Kandersteg - Gasterntal)",
    "BOS/rtb (Bus Ostschweiz (Rheintal))",
    "MGB-fo (Matterhorn Gotthard Bahn (fo))",
    "CJ (Chemins de fer du Jura)",
    "TRN-cmn (Transports Publics Neuchâtelois SA (cmn))",
    "SSM (Skilift Schilt AG 8753 Mollis)",
    "SBB Auto (SBB Auto Bahnersatz)",
    "EBZ (Elektrobus Zermatt)",
    "ZB Bus F (ZB Bus Fahrplanbedürfnisse)",
    "GSUR (Gemeinde Sur)",
    "BüBu (Bürgerbus Hellsau-Höchstetten-Willadingen-Koppigen)",
    "AB Auto (Automobildienst Appenzeller Bahnen)",
    "FCK (Funicar Kursbetriebe AG Biel)",
    "BPB (Bus Personico-Bodio)",
    "SBB (SBB GmbH)",
    "TRAVYS/a (Transports Vallée de Joux-Yverdon-Ste-Croix (auto ystec))",
    "RhB Auto (Autoverkehr RhB)",
    "ABl (Autolinee Bleniesi)",
    "SBW (Stadtbus Winterthur)",
    "BSU (Busbetrieb Solothurn und Umgebung)",
    "TPG (Transports Publics Genevois)",
    "SBC (Stadtbus Chur)",
    "MBC Auto (Automobiles MBC)",
    "ASGS (Autotransports Sion-Grône-Sierre)",
    "VB (Verkehrsbetriebe der Stadt Biel)",
    "AB-tb (Appenzeller Bahnen (tb))",
    "VBZ (Verkehrsbetriebe Zürich)",
    "STI (Verkehrsbetriebe STI AG)",
    "TILO (Treni Regionali Ticino Lombardia)",
    "SBF (Stadtbus Frauenfeld)",
    "FS Domo (Iselle transito-Domodossola)",
    "VNB (Verein Naturpark Beverin)",
    "BALA (Bus alpin Lombachalp)",
    "VSK-bkk (Verkehrsbetriebe Kreuzlingen)",
    "VBSH (Verkehrsbetriebe Schaffhausen)",
    "TMR Auto (Service d'automobiles TMR)",
    "TPF Auto (Service d'automobiles TPF)",
    "CJ Auto (Automobiles CJ)",
    "BA-bgh (Bergbahnen Adelboden AG)",
    "BBO (Gemeinde St. Gallenkappel)",
    "ZVB (Zugerland Verkehrsbetriebe)",
    "VZO (Verkehrsbetriebe Zürichsee und Oberland)",
    "VBZ    F (Verkehrsbetriebe Zürich INFO+)",
    "SZU Auto (Automobildienst SZU)",
    "BLT Auto (Auto BLT)",
    "Kröbu (Kröschenbrunnen Bus)",
    "RBL (Regionalbus Lenzburg)",
    "BLAG (Busland AG)",
    "ASM Auto (Automobildienste Aare Seeland mobil)",
    "VMCV (Transports publics Vevey-Montreux-Chillon-Villeneuve)",
    "BDWM/Aut (BDWM Transport (wm Auto))",
    "TRAVYS/t (Transports Vallée de Joux-Yverdon-Ste-Croix (tpyg))",
    "BGU (Busbetrieb Grenchen und Umgebung)",
    "REGO (Regiobus Gossau SG)",
    "OBSM (Ortsbus St. Moritz)",
    "BS (Bus Sierrois)",
    "SUTU (Sumvitg Turissem)",
    "OUW (Bürgerbus Kleindietwil-Oeschenbach-Walterswil)",
    "GHU (Bürgerbus Gondiswil-Huttwil-Ufhusen)",
    "WETA (Bürgerbus Walperswil-Epsach-Täuffelen-Aarberg)",
    "BLS BusF (BLS Bus Fahrplanbedürfnisse)").toSet
}