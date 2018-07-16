package mm.model

import play.api.libs.json.{Json, OFormat}

/** @see https://api.mattermost.com/#tag/users */
case class User(
                 id: String,
                 username: String,
                 first_name: String,
                 last_name: String,
                 email: String
               )
object User {
  implicit val fmt: OFormat[User] = Json.format[User]
}
