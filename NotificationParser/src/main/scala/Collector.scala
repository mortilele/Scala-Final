import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Collector {
  sealed trait Command
  final case class NotificationParsed(notificationType: NotificationType.Value, notification: Notification) extends Command

  def apply(): Behavior[Command] = collect(Map[NotificationType.Value, Seq[Notification]]())

  private def collect(map: Map[NotificationType.Value, Seq[Notification]]): Behavior[Command] =
    Behaviors.receiveMessage {
      case NotificationParsed(notificationType, notification) =>
        println("Received", notificationType)
        println(map)
        collect(map.updated(notificationType, map.getOrElse(notificationType, Seq.empty) :+ notification))
    }
}
