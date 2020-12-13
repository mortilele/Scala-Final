import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import kafka.NotificationProducer
import org.slf4j.{Logger, LoggerFactory}
import utils.FileUtil

object Main extends App {
  implicit val log: Logger = LoggerFactory.getLogger(getClass)
  val fileName = "messages.txt"

  val fileContent = FileUtil.readFile(fileName)
  val rootBehavior = Behaviors.setup[Nothing] { context =>



    val publisher = new NotificationProducer(fileContent)(context.system, context.executionContext)
    publisher.startSendingMessages()
    Behaviors.empty
  }

  implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "NotificationProducerService")
}
