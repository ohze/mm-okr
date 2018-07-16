package mm

import play.api.Configuration
import play.api.http.HeaderNames.AUTHORIZATION

case class MmConfig(host: String,
                    https: Boolean,
                    me: String, //shepherd user
                    hookIn: String,
                    personalToken: String,
                    _slashToken: String) {
  val url: String = (if (https) "https://" else "http://") + host
  val apiUrl: String = s"$url/api/v4"
  val wsUri: String = s"ws://$host/api/v4/websocket"
  val hookInUrl = s"$url/hooks/$hookIn"
  val bearerAuth: (String, String) = AUTHORIZATION -> s"Bearer $personalToken"
  val slashToken: String = "Token " + _slashToken
}
object MmConfig {
  def apply(c: Configuration): MmConfig = new MmConfig(
    c.get[String]("okr.mm.host"),
    c.get[Boolean]("okr.mm.https"),
    c.get[String]("okr.mm.me"),
    c.get[String]("okr.mm.hook.incoming"),
    c.get[String]("okr.mm.token"),
    c.get[String]("okr.mm.slash.token"),
  )
}
