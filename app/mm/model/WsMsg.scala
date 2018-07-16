package mm.model

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.model.ws.TextMessage
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json, Reads}

/** @see https://api.mattermost.com/#tag/WebSocket */
object WsMsg {
  case class Broadcast(
                        omit_users: Option[Seq[String]],
                        user_id: String,
                        channel_id: String,
                        team_id: String)
  object Broadcast {
    implicit val read: Reads[Broadcast] = Json.reads[Broadcast]
  }


  case class PostedEvent(event: String, broadcast: Broadcast, data: PostedData)
  object PostedEvent {
    implicit val read: Reads[PostedEvent] = Json.reads[PostedEvent]
  }
  case class Post(id: String,
                  create_at: Long,
                  update_at: Long,
                  edit_at: Long,
                  delete_at: Long,
                  is_pinned: Boolean,
                  user_id: String,
                  channel_id: String,
                  root_id: String,
                  parent_id: String,
                  original_id: String,
                  message: String,
                 `type`: String,
                  props: JsObject,
                  hashtags: String,
                  pending_post_id: String
                 )
  object Post {
    implicit val read: Reads[Post] = Json.reads[Post]
  }

  case class PostedData(
                            channel_display_name: String,
                            channel_name: String,
                            channel_type: String, // "D" if direct channel
                            //                          mentions: Seq[String],
                            post: String,
                            sender_name: String,
                            team_id: String
                          ) {
    def isDirect: Boolean = channel_type == "D"
    lazy val postTyped: Post = Json.parse(post).as[Post]
  }
  object PostedData {
    implicit val read: Reads[PostedData] = Json.reads[PostedData]
  }

  ////////////////
  private val seq = new AtomicLong(1)

  def apply(action: String, dataFields: (String, JsValueWrapper)*): TextMessage.Strict = {
    val o = Json.obj(
      "seq" -> seq.getAndIncrement(),
      "action" -> action,
      "data" -> Json.obj(dataFields: _*)
    )
    TextMessage.Strict(o.toString())
  }

  def authChallenge(token: String): TextMessage.Strict = WsMsg(
    "authentication_challenge",
    "token" -> token
  )
}