package spray.shapeless

package object marshalling {
  import akka.util.Timeout

  import scala.language.implicitConversions
  import scala.concurrent.duration._
  import scala.util._

  import shapeless._
  import ops.hlist._
  import HList._

  import spray._
  import http._
  import httpx.marshalling._

  type ValueAndMarshaller = (Any, Marshaller[Any])

  /** Used in materializing a [[List]] of values associated with their [[Marshaller]].  */
  type ValueAndMarshallerFolder[L <: HList] = RightFolder.Aux[L, List[ValueAndMarshaller], marshallers.type, List[Any]]

  //object marshallers extends Poly {
  //  implicit def caseMarshaller[A, B <: List[Any]](implicit marshaller: Marshaller[A]) =
  //    use((a: A, lst: B) => {
  //      (a, marshaller) :: lst
  //    })
  //}

  private[spray] object marshallers extends Poly2 {
    implicit def caseMarshaller[A, B <: List[Any]](implicit marshaller: Marshaller[A]) =
      at[A, B] { (a, lst) =>
        (a, marshaller) :: lst
      }
  }

  def zipValuesWithMarshallers[L <: HList](l: L)(implicit folder: ValueAndMarshallerFolder[L]): List[ValueAndMarshaller] =
    l.foldRight(List.empty[ValueAndMarshaller])(marshallers).asInstanceOf[List[ValueAndMarshaller]]

  def renderValuesWithMarshallers[B](zipped: List[ValueAndMarshaller], z: B)(f: (B, Try[HttpData]) => B): B =
    zipped.foldLeft(z) {
      case (accum, (value, marshaller)) =>

        val marshalled =  marshal(value)(marshaller = marshaller, timeout = Timeout(1.minute))
        f(accum, marshalled.map(_.data))
    }

  private[spray] implicit def eitherToTry[T](either: Either[Throwable, T]): Try[T] =
    either.fold(Failure.apply, Success.apply)
}
