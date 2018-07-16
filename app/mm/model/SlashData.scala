package mm.model

import play.api.data.Form
import play.api.data.Forms._

/** @see https://developers.mattermost.com/integrate/slash-commands/ */
case class SlashData(channel_id: String,
                     channel_name: String,
                     command: String,
                     team_domain: String,
                     team_id: String,
                     text: String,
                     token: String,
                     user_id: String,
                     user_name: String)
object SlashData {
  val form = Form(
    mapping(
      "channel_id" -> text,
      "channel_name" -> text,
      "command" -> text,
      "team_domain" -> text,
      "team_id" -> text,
      "text" -> text,
      "token" -> text,
      "user_id" -> text,
      "user_name" -> text
    )(SlashData.apply)(SlashData.unapply)
  )
}