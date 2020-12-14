package kafka

import actors.NotificationParser
import actors.NotificationParser.GetRawNotification
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import io.circe.parser.decode
import models.Notification
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import dao.Collector.CollectParsedNotifications
import io.circe.generic.decoding.DerivedDecoder.deriveDecoder

import scala.concurrent.ExecutionContextExecutor

object NotificationConsumer {

  sealed trait Command

  final case class startJob(replyTo: ActorRef[CollectParsedNotifications]) extends Command

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
          val committerSettings = CommitterSettings(system)


          // Stream for each partition
          val control = Consumer
            .committablePartitionedSource(consumerSettings, Subscriptions.topics(topic))
            .mapAsyncUnordered(3) {
              case (topicPartition, source) =>
                source
                  .map { consumerMessage =>
                    val value = consumerMessage.record.value()
                    val notification = decode[Notification](value)
                    notification match {
                      case Right(notificationObject) =>
                        notificationParserActor ! GetRawNotification(notificationObject, replyTo)
                      case Left(ex) => println(s"Ooops some errror here ${ex}")
                    }
                    consumerMessage.committableOffset
                  }
                  .runWith(Committer.sink(committerSettings))
            }
            .toMat(Sink.ignore)(DrainingControl.apply)
            .run()
          Behaviors.same
      }
    }

  }


}
