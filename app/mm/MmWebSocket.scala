package mm

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import javax.inject.{Inject, Named, Singleton}
import mm.BotConversations.{DoAuth, Hello, WsRef}
import mm.model.WsMsg.PostedEvent
import mm.util.Implicits._
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.util.{Failure, Success}

/** Mattermost WebSocket client
  * One instance of this class will be eagerly created (see `mm.PlayModule`)
  * and eagerly run (`MmWebSocket.run` is called when created in MmWebSocket's constructor - see bellow)
  *
  * In `run` method, an ActorRef `wsRef` will be created.
  * Everything send to this ref will be sent out to the MM web socket endpoint
  *
  * Then, wsRef will be sent to `bot` (`bot-conversations` actor),
  * following by sending `DoAuth` message to the Bot.
  *
  * @see https://api.mattermost.com/#tag/WebSocket */
@Singleton
class MmWebSocket @Inject()(c: MmConfig,
                            implicit val system: ActorSystem,
                            @Named("bot-conversations") bot: ActorRef) {
  private[this] val logger = Logger(this.getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  /** @see https://stackoverflow.com/questions/40345697/how-to-use-akka-http-client-websocket-send-message
    * @see https://doc.akka.io/docs/akka-http/current/client-side/websocket-support.html#websocketclientflow */
  def run(): Unit = {
    val messageSource: Source[Message, ActorRef] =
      Source.actorRef[TextMessage.Strict](bufferSize = 10, OverflowStrategy.fail)

    // Future[Done] is the materialized value of Sink.foreach,
    // emitted when the stream completes
    val messageSink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
      case TextMessage.Strict(txt) =>
        val msg = Json.parse(txt)
        logger.debug(s"Strict:${
          (msg \ "event")
            .asOpt[String]
            .fold("[]")(e => s"[$e]")
        }")
        (msg \ "event").asOpt[String] match {
          case Some("hello") => bot ! Hello
          case Some("posted") =>
            val d = msg.as[PostedEvent]
            bot ! d
            logger.info(s"posted: ${msg.pretty}\n${d.data.post.toJson.pretty}")
          case _ => // ignore
        }
      case msg => logger.debug(s"non Strict:[$msg]")
    }

    val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(uri = c.wsUri))

    // wsRef is an ActorRef. Everything send to this ref will be sent out to the MM web socket endpoint
    // closed is a Future[Done] with the stream completion from the incoming sink
    val ((wsRef, upgradeResponse), closed) =
      messageSource
        .viaMat(webSocketFlow)(Keep.both) // keep wsRef(?) and the materialized Future[WebSocketUpgradeResponse]
        .toMat(messageSink)(Keep.both) // also keep the Future[Done]
        .run()

    bot ! WsRef(wsRef)

    // just like a regular http request we can access response status which is available via upgrade.response.status
    // status code 101 (Switching Protocols) indicates that server support WebSockets
    val connected = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }
    connected.onComplete {
      case Success(Done) => bot ! DoAuth
      case Failure(e) => logger.error("can't connect", e)
    }
  }

  // eagerly run
  run()
}