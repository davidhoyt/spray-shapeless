package spray.shapeless.marshalling

trait HListChunkedMarshaller[-L] extends HListDefaultMarshaller[L]

object HListChunkedMarshaller {
  import shapeless._
  import spray._
  import http._
  import httpx.marshalling._

  def apply[L <: HList](fn: (L, MarshallingContext) => Unit): HListChunkedMarshaller[L] =
    new HListChunkedMarshaller[L] {
      override protected[this] def given(hlist: L, contentType: ContentType, ctx: MarshallingContext): Unit =
        fn(hlist, ctx)
    }
}
