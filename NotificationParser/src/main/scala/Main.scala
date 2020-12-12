import NotificationParser.GetRawNotification
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Main{
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val notificationParserActor = context.spawn(NotificationParser(), "NotificationParser")
      val collectorActor = context.spawn(Collector(), "NotificationTypesCollector")
      context.watch(notificationParserActor)
      notificationParserActor ! GetRawNotification(Notification("Get Discounts specific for u", 2), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Discount", 1), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Some interesting", 3), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Get Personal Discount for u", 4), collectorActor)
      notificationParserActor ! GetRawNotification(Notification("Global news!", 5), collectorActor)
      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}
