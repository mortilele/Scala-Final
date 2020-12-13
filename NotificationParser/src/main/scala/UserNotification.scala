import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserNotification {

  sealed trait Command
  case class ExtractUserNotifications(userId: Int, notifications: Seq[Notification], replyTo: ActorRef[GetUserResponse]) extends Command

  sealed trait Event
  final case class GetUserResponse(notifications: Seq[Notification]) extends Event

  def apply(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case ExtractUserNotifications(userId, notifications, replyTo) =>
        replyTo ! GetUserResponse(notifications.filter(_.userId == userId))
        Behaviors.stopped
    }
  }



}
