package kafka

import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import io.circe.generic.auto._
import io.circe.syntax._
import models.Notification
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import utils.FileUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}


class NotificationProducer()(implicit system: ActorSystem[_], ex: ExecutionContext) {
  val config: Config = system.settings.config.getConfig("akka.kafka.producer")
  val server: String = system.settings.config.getString("akka.kafka.producer.kafka-clients.server")
  val topic: String = system.settings.config.getString("akka.kafka.producer.kafka-clients.topic")

  val fileName = "messages.txt"
  var messages: Seq[String] = FileUtil.readFile(fileName)
  var partitionOffset = 0

  val generator: Random.type = scala.util.Random

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(server)

  def getRandomNotification: Notification = {
    val notificationBody = messages(generator.nextInt(messages.length))
    val userId = generator.nextInt(2)
    Notification(notificationBody, userId)
  }


  def produce(): Unit = {
    // Round-robin partitions[0..2]
    partitionOffset = (partitionOffset + 1) % 3
    val done: Future[Done] = {
      Source.single(getRandomNotification)
        .map(value => new ProducerRecord[String, String](topic, partitionOffset, "keee", value.asJson.toString()))
        .runWith(Producer.plainSink(producerSettings))
    }

    done.onComplete {
      case Success(data) =>
        system.log.info(s"Published random notification $data to topic $topic")
      case Failure(exception) =>
        system.log.error(exception.toString)
    }
  }

  def startSendingMessages(): Unit = {
    var cnt = 0
    while (cnt < 10) {
      produce()
      cnt += 1
    }
  }

  def sendOneMessage(userId: Int, messageId: Int): Unit = {
    // Round-robin partitions[0..2]
    partitionOffset = (partitionOffset + 1) % 3
    val singleMessage: Future[Done] = {
      Source.single(Notification(messages(messageId), userId))
        .map(value => new ProducerRecord[String, String](topic, partitionOffset, "NotificationsKey", value.asJson.toString()))
        .runWith(Producer.plainSink(producerSettings))
    }

    singleMessage.onComplete {
      case Success(data) =>
        system.log.info(s"Published random notification $data to topic $topic")
      case Failure(exception) =>
        system.log.error(exception.toString)
    }
  }

  def addMessage(body: String): Unit = {
    FileUtil.writeFile(fileName, body)
    messages = messages :+ body
  }

  startSendingMessages()
}