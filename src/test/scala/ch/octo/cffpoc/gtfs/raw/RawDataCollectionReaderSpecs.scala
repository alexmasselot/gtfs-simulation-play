package ch.octo.cffpoc.gtfs.raw

import ch.octo.cffpoc.gtfs._
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by alex on 17/02/16.
 */
class RawDataCollectionReaderSpecs extends FlatSpec with Matchers {
  behavior of "RawDataCollectionReader"

  it should "split line, no comma" in {
    val l ="""xx,y y,z"""
    RawDataCollectionReader.splitLine(l).toList should equal(List("xx", "y y", "z"))
  }

  it should "split line, quotes" in {
    val l ="""xx,"y y",z"""
    RawDataCollectionReader.splitLine(l).toList should equal(List("xx", "y y", "z"))
  }

  it should "split line,  comma" in {
    val l ="""xx,"y, www, g",z"""
    RawDataCollectionReader.splitLine(l).toList should equal(List("xx", "y, www, g", "z"))  }


}

