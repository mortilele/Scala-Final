package actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import dao.Collector.CollectParsedNotifications
import enums.NotificationType
import models.Notification

object NotificationParser {

  sealed trait Command

  case class GetRawNotification(notification: Notification, replyTo: ActorRef[CollectParsedNotifications]) extends Command

  def apply(): Behavior[Command] = parse()

  private def parse(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetRawNotification(notification, replyTo) =>
        replyTo ! CollectParsedNotifications(NotificationType.values.find(_.isPatternMatched(notification.body)) match {
          case Some(value) => value
          case None => NotificationType.Other
        }, notification)
        Behaviors.same
    }
  }
}
