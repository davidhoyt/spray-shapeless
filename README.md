spray-shapeless
=============

[![Join the chat at https://gitter.im/davidhoyt/spray-shapeless](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/davidhoyt/spray-shapeless?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Provides Spray marshallers for rendering Shapeless HLists in a route. It works by implicitly resolving a marshaller for each member of the HList and then rendering each one-by-one. That ramifications for this are that if you're attempting to output a type that Spray doesn't know how to write, you'll get a compile error. Unfortunately the error is not at all useful right now and will perhaps be addressed at a later time.

This library adds the `spray.shapeless` package and will implicitly materialize a Spray marshaller when you import `spray.shapeless.HListRendering._` or `spray.shapeless.HListChunkedRendering._` for chunked output (each member of the HList represents a single HTTP chunk).

Here's a simplified example of its use:

```scala
import shapeless._
import spray.http._
import spray.json._
import spray.routing._
import spray.shapeless._
import spray.httpx.SprayJsonSupport._

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

        complete(StatusCodes.OK, "First\n" :: Example("render to json") :: "\nLast" :: HNil)
      }
    }
  }
```

When run, the response would be equivalent to:

```
First
{
  "value": "render to json"
}
Last
```
 
The test source contains a working example if you would like further clarification.
