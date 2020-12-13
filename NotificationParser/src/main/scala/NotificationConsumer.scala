import Collector.NotificationParsed
import NotificationParser.GetRawNotification
import akka.actor.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka._
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl.Sink
import io.circe.generic.decoding.DerivedDecoder.deriveDecoder
import io.circe.parser._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor

object NotificationConsumer {

  sealed trait Command
  final case class startJob(replyTo: ActorRef[NotificationParsed]) extends Command

  implicit val system: ActorSystem = ActorSystem("Consumer1")
  implicit val ec: ExecutionContextExecutor = system.dispatcher


  def apply(): Behavior[Command] = {
    Behaviors.setup[Command] { context =>
      val notificationParserActor = context.spawn(NotificationParser(), "NotificationParser")
      Behaviors.receiveMessage {
        case startJob(replyTo) =>

          val topic: String = system.settings.config.getString("akka.kafka.consumer.kafka-clients.topic")
          val bootstrapServers: String = system.settings.config.getString("akka.kafka.consumer.kafka-clients.server")
          val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
            .withBootstrapServers(bootstrapServers)
            .withGroupId("notifications-consumer")
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

          val consumer = Consumer
            .committableSource(consumerSettings, Subscriptions.topics(topic))
            .map { consumerMessage =>
              val value = consumerMessage.record.value()
              val notification = decode[Notification](value)
              notification match {
                case Right(notificationObject) =>
                  notificationParserActor ! GetRawNotification(notificationObject, replyTo)
                case Left(ex) => println(s"Ooops some errror here ${ex}")
              }
            }
            .toMat(Sink.seq)(DrainingControl.apply)
            .run()
          Behaviors.same
      }
    }

  }





}
