package dao

import actors.UserNotification
import actors.UserNotification.GetUserResponse
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import enums.NotificationType
import models.Notification

object Collector {

  sealed trait Command

  final case class CollectParsedNotifications(notificationType: NotificationType.Value, notification: Notification) extends Command
  final case class GetUserNotifications(userId: Int, notificationType: NotificationType.Value, replyTo: ActorRef[UserNotification.GetUserResponse]) extends Command


  sealed trait Event
  final case class NotificationTypeCollected(notificationType: NotificationType.Value, notification: Notification) extends Event
  final case class GotUserNotifications() extends Event


  final case class State(notificationTypeDictionary: Map[NotificationType.Value, Seq[Notification]] = Map[NotificationType.Value, Seq[Notification]]())

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("collector2"),
      emptyState = State(),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )


  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case GetUserNotifications(userId, notificationType, replyTo) => Effect
        .persist(GotUserNotifications())
        .thenReply(replyTo) { _ => GetUserResponse(state.notificationTypeDictionary.getOrElse(notificationType, Seq.empty).filter(_.userId == userId))
      }
      case CollectParsedNotifications(notificationType, notification) => Effect.persist(NotificationTypeCollected(notificationType, notification))
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case GotUserNotifications() => state
      case NotificationTypeCollected(notificationType, notification) =>
        state.copy(
          state.notificationTypeDictionary.updated(notificationType,
            state.notificationTypeDictionary.getOrElse(notificationType, Seq.empty) :+ notification))
    }
  }

//  DEPRECATED
  private def collect(map: Map[NotificationType.Value, Seq[Notification]]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CollectParsedNotifications(notificationType, notification) =>
        collect(map.updated(notificationType, map.getOrElse(notificationType, Seq.empty) :+ notification))
      case GetUserNotifications(userId, notificationType, replyTo) =>
        Behaviors.setup[Command] { context =>
          val userNotification = context.spawn(UserNotification(), s"UserNotification$userId")
          userNotification ! UserNotification.ExtractUserNotifications(userId, map.getOrElse(notificationType, Seq.empty), replyTo)
          Behaviors.same
        }
    }

}
