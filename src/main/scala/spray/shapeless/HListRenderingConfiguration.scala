package spray.shapeless

import _root_.spray._
import http._

trait HListRenderingConfiguration {
  def start: HttpData
  def separator: HttpData
  def end: HttpData
}

object HListRenderingConfiguration {
  case class Simple(start: HttpData, separator: HttpData, end: HttpData) extends HListRenderingConfiguration {
    override def toString: String = s"HListRenderingConfiguration(start = $start, separator = $separator, end = $end)"
  }

  val emptyHttpData = HttpData(Array.emptyByteArray)
  implicit val default = apply()

  def apply(start: HttpData = emptyHttpData, separator: HttpData = emptyHttpData, end: HttpData = emptyHttpData): HListRenderingConfiguration =
    Simple(start = start, separator = separator, end = end)
}