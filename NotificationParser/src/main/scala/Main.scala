import NotificationParser.GetRawNotification
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

object Main{
  def main(args: Array[String]): Unit = {
    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val notificationParserActor = context.spawn(NotificationParser(), "NotificationParser")
      val collectorActor = context.spawn(Collector(), "NotificationTypesCollector")
      context.watch(notificationParserActor)


      val router = new NotificationRoutes(collectorActor)(context.system, context.executionContext)

      val host = "localhost"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

      HttpServer.startHttpServer(router.route, host, port)(context.system, context.executionContext)
      notificationParserActor ! GetRawNotification(Notification("Get Discounts specific for u", 2), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Get Discounts specific for u", 1), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Some interesting", 2), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Get Personal Discount for u", 2), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Global news!", 5), collectorActor)
      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
//  TODO: CollectorActor has to save state in Cassandra
//  TODO: Kafka topic partition
//  TODO: Dynamic adding/removing NotificationActor
//  TODO: Clustering Combine ActorSystems
//  TODO: Implement UserNotificationActor to response User Request
}
