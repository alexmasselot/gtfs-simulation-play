import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._
import NativePackagerHelper._

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)


lazy val root = (project in file(".")).enablePlugins(PlayScala)


name := """gtfs-simulation-play"""
organization := "ch.octo"
version := "0.1.3"
scalaVersion := Version.scala
maintainer := "amasselot@octo.com"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

libraryDependencies ++= Seq(
  cache,
  filters,
  "com.typesafe.play" %% "play-json" % "2.5.12",
  "com.github.tototoshi" %% "scala-csv" % "1.3.0",
  "com.github.nscala-time" %% "nscala-time" % "2.10.0",
  "com.yammer.metrics" % "metrics-core" % "2.1.2"
)

scalaSource in Compile := baseDirectory.value / "src/main/scala"

scalaSource in Test := baseDirectory.value / "src/test/scala"

releaseSettings

scalariformSettings

initialCommands in console :=
  """
    |import org.apache.spark._
    |import org.apache.spark.streaming._
    |import org.apache.spark.streaming.StreamingContext._
    |import org.apache.spark.streaming.dstream._
    |import akka.actor.{ActorSystem, Props}
    |import com.typesafe.config.ConfigFactory
    | """.stripMargin

mappings in Universal ++= directory("data")

routesGenerator := InjectedRoutesGenerator
