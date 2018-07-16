package mm

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}
import mm.BotConversations.AddOkrs
import mm.model.SlashData
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController, Request}

@Singleton
class Ctrl @Inject()(c: MmConfig,
                     @Named("bot-conversations") bot: ActorRef
                    ) extends InjectedController {
  private def slashOk(msg: String) = Ok(Json.obj("text" -> msg))

  def o: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    request.headers.get(AUTHORIZATION) match {
      case Some(c.slashToken) =>
        val d = SlashData.form.bindFromRequest().get
        AddOkrs.parse(d) match {
          case Some(a) =>
            bot ! a
            slashOk(s"Đã nhắc các bạn lập OKR: ${a.cmds.mkString(", ")}")
          case _ =>
            slashOk("Em ứ hiểu anh/chị!")
        }
      case _ => Unauthorized
    }
  }
}
