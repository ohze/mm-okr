package mm.util

import play.api.libs.json.{JsValue, Json}

object Implicits {
  implicit final class StringEx(val s: String) extends AnyVal {
    @inline def toJson: JsValue = Json.parse(s)
    def mention: String =
      if (s.startsWith("@")) s
      else "@" + s
    def unmention: String =
      if (s.startsWith("@")) s.substring(1)
      else s
  }

  implicit class JsValueEx(val x: JsValue) extends AnyVal {
    @inline final def pretty: String = Json.prettyPrint(x)
  }
}
