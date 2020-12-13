import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Collector {
  sealed trait Command
  final case class NotificationParsed(notificationType: NotificationType.Value, notification: Notification) extends Command
  final case class GetUserNotifications(userId: Int, notificationType: NotificationType.Value, replyTo: ActorRef[UserNotification.GetUserResponse]) extends Command


  def apply(): Behavior[Command] = collect(Map[NotificationType.Value, Seq[Notification]]())

  private def collect(map: Map[NotificationType.Value, Seq[Notification]]): Behavior[Command] =
    Behaviors.receiveMessage {
      case NotificationParsed(notificationType, notification) =>
        println("Received", notificationType)
        collect(map.updated(notificationType, map.getOrElse(notificationType, Seq.empty) :+ notification))
      case GetUserNotifications(userId, notificationType, replyTo) =>
        Behaviors.setup[Command] { context =>
          val userNotification = context.spawn(UserNotification(), s"UserNotification$userId")
          userNotification ! UserNotification.ExtractUserNotifications(userId, map.getOrElse(notificationType, Seq.empty), replyTo)
          Behaviors.same
        }
    }
}
