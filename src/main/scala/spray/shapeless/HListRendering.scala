package spray.shapeless

import marshalling._

object HListRendering {
  import scala.language.implicitConversions
  import scala.util._
  import shapeless._
  import spray._
  import http._
  import httpx.marshalling._

  implicit def forHList[L <: HList](implicit configuration: HListRenderingConfiguration = HListRenderingConfiguration.default, folder: ValueAndMarshallerFolder[L]): HListStandardMarshaller[L] =
    HListStandardMarshaller[L]((hlist, ctx) => {
      val valuesWithMarshallers = zipValuesWithMarshallers(hlist)

      ctx.marshalTo(HttpEntity {
        val dataBuilder = HttpData.newBuilder += configuration.start
        var firstRendered = false
        renderValuesWithMarshallers(valuesWithMarshallers, dataBuilder) {
          case (builder, Success(data)) =>
            if (firstRendered)
              builder += configuration.separator
            else
              firstRendered = true

            builder += data
          case (builder, _) =>
            if (firstRendered)
              builder += configuration.separator
            else
              firstRendered = true

            builder
        }
        dataBuilder += configuration.end
        dataBuilder.result()
      })
    })

  implicit def toResponseMarshallable[L <: HList](hlist: => L)(implicit configuration: HListRenderingConfiguration = HListRenderingConfiguration.default, marshaller: HListStandardMarshaller[L] = null, folder: ValueAndMarshallerFolder[L]): ToResponseMarshallable = {
    val hlistMarshaller: HListStandardMarshaller[L] =
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
