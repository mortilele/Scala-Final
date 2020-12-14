name := "NotificationProducer"

version := "0.1"

scalaVersion := "2.12.12"

val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.1"
val circeVersion = "0.13.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,

  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.5",
  "ch.qos.logback"    %  "logback-classic"             % "1.2.3",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,


  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0"
)
enablePlugins(GatlingPlugin)
scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")
val gatlingVersion = "3.4.2"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"
