package consumerTests
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://127.0.0.1:8080/") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 YaBrowser/20.11.3.179 Yowser/2.5 Safari/537.36")

  val scn = scenario("Consumer http") // A scenario is a chain of requests and pauses
    .exec(http("request_getDiscounts")
      .get("notifications/1/discount"))
    .pause(7) // Note that Gatling has recorder real time pauses
    .exec(http("request_getNews")
      .get("notifications/1/news"))
    .pause(7) // Note that Gatling has recorder real time pauses
    .exec(http("request_getPersonal")
      .get("notifications/1/news"))
    .pause(7) // Note that Gatling has recorder real time pauses
    

  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}