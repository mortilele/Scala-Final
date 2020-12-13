import Collector.GetUserNotifications
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

trait Router {
  def route: Route
}


class NotificationRoutes(notificationDAO: ActorRef[Collector.Command])(implicit val system: ActorSystem[_], ex: ExecutionContext) extends Router {

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("app.routes.ask-timeout"))


  def getUserNotifications(userId: Int, notificationType: NotificationType.Value): Future[UserNotification.GetUserResponse] =
    notificationDAO.ask(GetUserNotifications(userId, notificationType, _))

  val userRoutes: Route =
    pathPrefix("notifications") {
      path(IntNumber) { userId =>
        get {
          rejectEmptyResponse {
            onSuccess(getUserNotifications(userId, NotificationType.Discount)) { response =>
              complete(response.notifications)
            }
          }
        }
      }
    }

  override def route: Route = {
    userRoutes
  }
}
