package mm

import play.api.libs.json.{Json, OFormat}

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
