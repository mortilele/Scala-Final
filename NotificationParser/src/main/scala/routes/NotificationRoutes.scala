package routes

import actors.UserNotification
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import enums.NotificationType
import validators.{NotificationTypeExists, ValidatorDirectives}
import dao.Collector.{Command, GetUserNotifications}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

trait Router {
  def route: Route
}


class NotificationRoutes(notificationDAO: ActorRef[Command])(implicit val system: ActorSystem[_], ex: ExecutionContext) extends Router
  with ValidatorDirectives
  with Directives {

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("app.routes.ask-timeout"))


  def getUserNotifications(userId: Int, notificationType: NotificationType.Value): Future[UserNotification.GetUserResponse] =
    notificationDAO.ask(GetUserNotifications(userId, notificationType, _))

  val userRoutes: Route =
    pathPrefix("notifications") {
      pathPrefix(IntNumber) { userId =>
        path(Segment) { notificationType =>
          get {
            validateWith(NotificationTypeExists)(notificationType) {
              onSuccess(getUserNotifications(userId, NotificationType.convertStringToClassInstance(notificationType))) { response =>
                complete(response.notifications)
              }
            }
          }
        }
      }
    }

  override def route: Route = {
    userRoutes
  }
}
