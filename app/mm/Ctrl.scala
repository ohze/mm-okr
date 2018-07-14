package mm

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, InjectedController, Request}

import scala.concurrent.ExecutionContext

@Singleton
class Ctrl @Inject()(ws: WSClient, c: Configuration, implicit val ec: ExecutionContext) extends InjectedController {
  val Api: String = c.get[String]("okr.mm.api")
  val BearerAuth: (String, String) = {
    val token = c.get[String]("okr.mm.token")
    "Authorization" -> s"Bearer $token"
  }

  def test: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val req = ws.url(s"$Api/users").withHttpHeaders(BearerAuth)
    req.get().map { res =>
      val users = res.json.as[List[User]]
      Ok(Json.toJson(users))
    }
  }
}
