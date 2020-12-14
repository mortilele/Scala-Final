import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import kafka.NotificationProducer
import org.slf4j.{Logger, LoggerFactory}
import server.{HttpServer, Node, ProducerRoutes}

object Main extends App {
  implicit val log: Logger = LoggerFactory.getLogger(getClass)
  val rootBehavior = Behaviors.setup[Nothing] { context =>

    val publisher = new NotificationProducer()(context.system, context.executionContext)
    val node = context.spawnAnonymous(Node(publisher))

    val router = new ProducerRoutes(node)(context.system, context.executionContext)
    val host = "localhost"
    val port = 9000
    HttpServer.startHttpServer(router.route, host, port)(context.system, context.executionContext)


//    publisher.startSendingMessages()
    Behaviors.empty
  }

  implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "NotificationProducerService")
}
