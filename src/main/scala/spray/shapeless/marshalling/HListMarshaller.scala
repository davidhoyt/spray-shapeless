package spray.shapeless.marshalling

import spray._
import http._
import httpx._
import marshalling._

trait HListMarshaller[-L] extends Marshaller[L] {
  protected[this] val contentTypes: Seq[ContentType] = Seq()
  protected[this] val marshaller = Marshaller.of[L](contentTypes:_*)(given)

  def apply(value: L, ctx: MarshallingContext) =
    marshaller(value, ctx)

  protected[this] def given(hlist: L, contentType: ContentType, ctx: MarshallingContext): Unit
}

trait HListDefaultMarshaller[-L] extends HListMarshaller[L] {
  override protected[this] val marshaller =
    Marshaller[L] { (hlist, ctx) =>
      given(hlist, ContentTypes.NoContentType, ctx)
    }
}
