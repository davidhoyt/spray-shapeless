package spray.shapeless

import spray.routing.HttpServiceActor

class SprayShapeless extends HttpServiceActor {
  import shapeless._
  import spray.http._
  import spray.json._
  import spray.routing._
  import spray.shapeless._
  import spray.httpx.SprayJsonSupport._

  def receive = runRoute(exampleRoute)

  case class Example(value: String)
  object JsonProtocol extends DefaultJsonProtocol {
    implicit val ExampleFormat = jsonFormat1(Example)
  }

  def exampleRoute: Route = {
    get {
      pathEndOrSingleSlash {
        import JsonProtocol._
        import HListRendering._
        //import HListChunkedRendering._

        complete(StatusCodes.OK, "First\n" :: Example("this value") :: 0 :: "\nLast" :: HNil)

        //implicit val config = HListRenderingConfiguration(
        //  start = HttpData("-START-\n"),
        //  separator = HttpData("\n----\n"),
        //  end = HttpData("\n-END-")
        //)

        //val h = HNil
        //val h = "A" :: <a></a> :: TestIt("this value") :: HNil
        //val h = "A" :: HNil
        //
        //(respondWithHeaders(HttpHeaders.Server("test")) & respondWithMediaType(`text/html`)) {
        //  complete.apply(StatusCodes.OK, h)
        //}
      }
    }
  }
}

object SprayShapeless extends App {
  import akka.actor._
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent._
  import scala.concurrent.duration._
  import _root_.spray.http._

  implicit val timeout = Timeout(1.hour)
  implicit val system = ActorSystem("ShapelessSpray")

  val ref = system.actorOf(Props[SprayShapeless])
  import system.dispatcher

  case class Response(status: StatusCode, headers: List[HttpHeader], content: String)

  def request(req: HttpRequest)(implicit ec: ExecutionContext): Response = {
    case object NotifyMe
    class HttpResponseHandler extends Actor {
      var done = false
      var recipient: ActorRef = _

      var status: StatusCode = StatusCodes.InternalServerError
      var headers: List[HttpHeader] = List()
      var content: String = ""

      def sendToRecipient() = if (recipient ne null) recipient ! Response(status, headers, content)
      val receive: Receive = {
        case h: HttpResponse =>
          status = h.status
          headers = h.headers
          content = h.entity.asString
          done = true
          sendToRecipient()
        case c: ChunkedResponseStart =>
          status = c.message.status
          headers = c.message.headers
          content = c.message.entity.asString
        case MessageChunk(data, _) =>
          content += data.asString(HttpCharsets.`UTF-8`)
        case ChunkedMessageEnd(_, _) =>
          done = true
          sendToRecipient()
        case NotifyMe =>
          recipient = sender()
          if (done)
            sendToRecipient()
      }
    }

    val handler = system.actorOf(Props(new HttpResponseHandler))
    val f = ref tell (req, handler)
    val fs = (handler ? NotifyMe).mapTo[Response]
    Await.result(fs, 5.seconds)
  }

  println(request(HttpRequest(HttpMethods.GET, Uri("http://foobar.com/"))))

  system.shutdown()
  system.awaitTermination()
}
