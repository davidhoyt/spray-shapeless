package spray.shapeless

import marshalling._

object HListChunkedRendering {
  import akka.actor._
  import akka.io.Tcp

  import scala.language.implicitConversions
  import scala.util._

  import shapeless._

  import spray._
  import http._
  import httpx.marshalling._

  implicit def forHList[L <: HList](implicit configuration: HListRenderingConfiguration, actorRefFactory: ActorRefFactory, folder: ValueAndMarshallerFolder[L]): HListChunkedMarshaller[L] =
    HListChunkedMarshaller[L]((hlist, ctx) => {

      case object Begin

      class ChunkingActor(valuesWithMarshallers: List[ValueAndMarshaller]) extends Actor with ActorLogging {
        var responder: ActorRef = null

        val receive: Receive = {
          case Begin =>

            try {
              responder = ctx.startChunkedMessage(HttpEntity(configuration.start))(self)

              //Send each rendered value as a separate chunk.
              var renderWorked = true
              var firstRendered = false

              def doSeparator() =
                if (firstRendered) {
                  val separatorData = configuration.separator
                  if (separatorData.nonEmpty)
                    responder ! MessageChunk(separatorData)
                } else {
                  firstRendered = true
                }

              val result = renderValuesWithMarshallers[Unit](valuesWithMarshallers, ()) {
                case (_, Success(data)) =>
                  doSeparator()
                  responder ! MessageChunk(data)

                case (_, Failure(error)) =>
                  log.error(error, "Error sending chunked HList")
                  renderWorked = false
                  doSeparator()

                case _ =>
                  renderWorked = false
                  doSeparator()
              }

              val endData = configuration.end
              if (endData.nonEmpty)
                responder ! MessageChunk(endData)

              if (!renderWorked)
                log.error("Unable to send complete HList: {}", valuesWithMarshallers map { case (value, _) => value })

            } finally stop()

          case _: Tcp.ConnectionClosed =>
            stop()

          case unknown =>
            log.error("Received unknown message in ChunkedHListMarshaller: {}", unknown)
            stop()
        }

        def stop(): Unit = {
          if (responder ne null)
            responder ! ChunkedMessageEnd
          context.stop(self)
        }
      }

      val zipped = zipValuesWithMarshallers(hlist)
      actorRefFactory.actorOf(Props(new ChunkingActor(zipped))) ! Begin
    })

  implicit def toResponseMarshallable[L <: HList](hlist: => L)(implicit configuration: HListRenderingConfiguration, actorRefFactory: ActorRefFactory, marshaller: HListChunkedMarshaller[L] = null, folder: ValueAndMarshallerFolder[L]): ToResponseMarshallable = {
    val hlistMarshaller: HListChunkedMarshaller[L] =
      if (marshaller ne null)
        marshaller
      else
        forHList[L]

    new ToResponseMarshallable {
      override def marshal(ctx: ToResponseMarshallingContext): Unit = {
        val rm = ToResponseMarshaller.fromMarshaller[L]()(hlistMarshaller)
        rm.apply(hlist, ctx)
      }
    }
  }
}
