import Collector.NotificationParsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object NotificationParser {
  sealed trait Command
  case class GetRawNotification(notification: Notification, replyTo: ActorRef[NotificationParsed]) extends Command

  def apply(): Behavior[Command] = parse()

  private def parse(): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetRawNotification(notification, replyTo) =>
        replyTo ! NotificationParsed(NotificationType.values.find(_.isPatternMatched(notification.body)) match {
          case Some(value) => value
          case None => NotificationType.Other
        }, notification)
        Behaviors.same
    }
}
