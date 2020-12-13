package server

import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.util.Timeout
import kafka.NotificationProducer

import scala.concurrent.duration.DurationInt

object Node {

  trait Command

  val NodeServiceKey: ServiceKey[Command] = ServiceKey[Command]("node-service-key")

  case class Check(replyTo: ActorRef[Command]) extends Command

  case class Checked(text: String) extends Command

  case class GetMessages(replyTo: ActorRef[Command]) extends Command

  case class Messages(seq: Seq[String]) extends Command

  case class SendMessage(userId: Int, messageId: Int, replyTo: ActorRef[Command]) extends Command

  case class PostMessage(message: String, replyTo: ActorRef[Command]) extends Command

  def apply(publisher: NotificationProducer): Behavior[Command] = {
    Behaviors.setup[Command] { context =>
      implicit def system: ActorSystem[Nothing] = context.system

      implicit def scheduler: Scheduler = context.system.scheduler

      implicit lazy val timeout: Timeout = Timeout(5.seconds)
      //      context.system.receptionist ! Receptionist.Register(NodeServiceKey, context.self)

      Behaviors.receiveMessage { message => {
        message match {
          case Check(replyTo) =>
            replyTo ! Checked("Hello")
          case GetMessages(replyTo) =>
            replyTo ! Messages(publisher.messages)
          case SendMessage(userId, messageId, replyTo) =>
            publisher.sendOneMessage(userId, messageId)
            replyTo ! Checked("Message Send!")
          case PostMessage(message, replyTo) =>
            publisher.addMessage(message)
            replyTo ! Checked("Message added!")
        }
        Behaviors.same
      }
      }
    }
  }
}
