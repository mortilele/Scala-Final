import NotificationConsumer.startJob
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

object Main{
  def main(args: Array[String]): Unit = {
    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val collectorActor = context.spawn(Collector(), "NotificationTypesCollector")
      context.watch(collectorActor)


      val router = new NotificationRoutes(collectorActor)(context.system, context.executionContext)

      val host = "localhost"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

      HttpServer.startHttpServer(router.route, host, port)(context.system, context.executionContext)

      val notificationConsumer = context.spawn(NotificationConsumer(), "Consumer")
      notificationConsumer ! startJob(collectorActor)

      Behaviors.empty
    }
    implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
//  TODO: CollectorActor has to save state in Cassandra
//  TODO: Kafka topic partition
    /*
    """
    Producer partition rule
    Consumers in one Group
    Consumers read messages by partition
    """
    */
//  TODO: Clustering Combine ActorSystems
//  TODO: UserNotificationActor save state until 30 seconds, die if didn't receive messages (via Receptionist)
    /*
    """

    """
    */
//  TODO: NotificationProducer /messages/ GET, POST request, /send_notification/ POST
}
