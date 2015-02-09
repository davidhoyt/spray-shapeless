package spray.shapeless.marshalling

trait HListStandardMarshaller[-L] extends HListDefaultMarshaller[L]

object HListStandardMarshaller {
  import shapeless._
  import spray._
  import http._
  import httpx.marshalling._

  def apply[L <: HList](fn: (L, MarshallingContext) => Unit): HListStandardMarshaller[L] =
    new HListStandardMarshaller[L] {
      override protected[this] def given(hlist: L, contentType: ContentType, ctx: MarshallingContext): Unit =
        fn(hlist, ctx)
    }
}
