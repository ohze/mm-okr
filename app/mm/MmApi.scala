package mm

import javax.inject.{Inject, Singleton}
import mm.model.User
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

// FIXME not used
/** @see https://api.mattermost.com/ */
@Singleton
class MmApi @Inject()(ws: WSClient, c: MmConfig,
                      implicit val ec: ExecutionContext) {
  private def getUsers = {
    ws.url(c.apiUrl + "/users")
      .withHttpHeaders(c.bearerAuth)
      .get()
      .map(res => res
        .json
        .as[List[User]]
        .filterNot(_.username != c.me)
      )
  }

  private def post(msg: String, channel: String) = {
    val req = ws.url(c.apiUrl + "/posts").withHttpHeaders(c.bearerAuth)
    req.post(
      Json.obj(
        "channel_id" -> channel,
        "message" -> msg
      )
    )/*.map { res =>
      res.status match {
        case CREATED => Created(res.json)
        case s => Status(s)
      }
    }*/
  }
}
