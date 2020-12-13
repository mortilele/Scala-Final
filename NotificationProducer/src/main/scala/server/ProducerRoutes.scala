package server

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import server.Node.Checked

import scala.concurrent.{ExecutionContext, Future}

trait Router {
  def route: Route
}

case class PostText(userId: Int, messageId: Int)

case class PostMessage(message: String)

class ProducerRoutes(node: ActorRef[Node.Command])(implicit val system: ActorSystem[_], ex: ExecutionContext) extends Router {

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("app.routes.ask-timeout"))
  implicit val scheduler: Scheduler = system.scheduler

  val producerRoutes: Route = {
    concat(
      pathPrefix("messages") {
        pathEndOrSingleSlash {
          concat(
            get {
              val processFuture: Future[Node.Messages] = node.ask(ref => Node.GetMessages(ref))(timeout, scheduler).mapTo[Node.Messages]
              onSuccess(processFuture) { response =>
                complete(response)
              }
            },
            post {
              entity(as[PostMessage]) { text =>
                val processFuture: Future[Node.Checked] = node.ask(
                  ref => Node.PostMessage(text.message, ref)
                )(timeout, scheduler).mapTo[Checked]
                onSuccess(processFuture) { res =>
                  complete(res)
                }
              }
            })
        }
      },
      pathPrefix("send_notification") {
        post {
          entity(as[PostText]) { postText =>
            val processFuture: Future[Node.Checked] = node.ask(
              ref => Node.SendMessage(postText.userId, postText.messageId, ref)
            )(timeout, scheduler).mapTo[Node.Checked]
            onSuccess(processFuture) { res =>
              complete(res)
            }
          }
        }
      }
    )
  }

  override def route: Route = {
    producerRoutes
  }
}
