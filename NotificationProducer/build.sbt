enablePlugins(GatlingPlugin)
name := "NotificationProducer"

version := "0.1"

scalaVersion := "2.13.4"

val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.1"
val circeVersion = "0.13.0"
val gatlingVersion = "3.4.2"s
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,

  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.5",

  "ch.qos.logback"    %  "logback-classic"             % "1.2.3",
  "com.typesafe.akka" %% "akka-multi-node-testkit"    % akkaVersion % Test,
  "org.scalatest"     %% "scalatest"                  % "3.0.8"     % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed"   % akkaVersion % Test,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,


  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0"
)




