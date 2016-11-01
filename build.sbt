import play.sbt.PlayImport._
import com.typesafe.sbt.packager.docker._
import play.sbt.routes.RoutesKeys._

enablePlugins(DockerPlugin)

enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).enablePlugins(PlayScala)


name := """cff-poc-streaming-backend"""
organization := "ch.octo"
version := "0.1.2"
scalaVersion := Version.scala
maintainer := "amasselot@octo.com"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

libraryDependencies ++= Seq(
  cache,
  filters,
  "com.typesafe.play" %% "play-json" % "2.5.8",
  "com.github.tototoshi" %% "scala-csv" % "1.3.0",
  "com.github.nscala-time" %% "nscala-time" % "2.10.0"
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

//mappings in Docker := mappings.value

packageName in Docker := s"${organization.value}/${name.value}"

dockerBaseImage := "java:8"

packageName in Docker := "alexmass/cff-play-realtime"

version in Docker := "0.2.5"

dockerExposedPorts := Seq(9000)

//mappings in Docker += {
//  val conf = baseDirectory.value / "conf" / "application-docker.conf"
//  conf -> "conf/application.conf"
//}

routesGenerator := InjectedRoutesGenerator
