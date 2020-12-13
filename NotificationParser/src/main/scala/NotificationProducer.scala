import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

class NotificationProducer()(implicit system: ActorSystem[_], ex: ExecutionContext) {
  var rawData : Array[String] = Array(
    "News : Google publish new version Of Android",
    "Available personal discount for Mac users",
    "On this Friday! Harry up to get your Discount",
    "Facts about your Windows Desktop!"
  )
  val config: Config = system.settings.config.getConfig("akka.kafka.producer")
  val server: String = system.settings.config.getString("akka.kafka.producer.kafka-clients.server")
  val topic: String = system.settings.config.getString("akka.kafka.producer.kafka-clients.topic")
  val generator: Random.type = scala.util.Random
  var published = 0
  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(server)

  def produce(): Future[String] = {
    val notId = generator.nextInt(4)-1
    val uId = generator.nextInt(10)
    val done: Future[Done] = {
      Source.single(rawData(notId), uId)
        .map(_.toString)
        .map(value => new ProducerRecord[String, String](topic, value))
        .runWith(Producer.plainSink(producerSettings))
    }
    done.onComplete {
      case Success(data) =>
        system.log.info(s"Published random number $data to topic $topic")
      case Failure(exception) =>
        system.log.error(exception.toString)
    }
    Future.successful("Request sent")
  }
  def publish(): Future[String] = {
    while(true){
      produce()
      Thread.sleep(generator.between(1000,10000))
    }
    Future.successful("Request sent")
  }
}