import play.sbt.PlayImport.jdbc
import sbt._
import sbt.Keys._

object D {
  val scala = "2.12.6"

  val settings = Seq(
    libraryDependencies ++= Seq(jdbc,
      //"com.typesafe.akka"     %% "akka-slf4j"               % V.akka,
      "org.playframework.anorm" %% "anorm"                  % "2.6.2",
      "mysql"                   %  "mysql-connector-java"   % "5.1.46" % Runtime
    ),
    dependencyOverrides ++= Seq(
      "org.scala-lang"          % "scala-reflect"             % scala
    )
  )
}
