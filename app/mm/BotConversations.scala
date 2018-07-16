package mm

import akka.actor.{Actor, ActorRef, PoisonPill}
import javax.inject.Inject
import mm.model.WsMsg.PostedEvent
import mm.model.{SlashData, WsMsg}
import mm.util.Implicits._
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport

import scala.collection.concurrent.TrieMap
import scala.util.Random

object BotConversations {
  case class WsRef(ref: ActorRef)
  case object DoAuth
  case object Hello

  sealed trait Cmd
  /**
    * Command để nhắc user `user` add/update OKR cho team `team`
    * @param user user được nhắc (prefixed with "@")
    * @param team team name (vd "Team 52 lá")
    * @param channel kênh mà OKR sau khi add/update sẽ được post vào
    */
  case class OkrCmd(user: String, team: String, channel: String) extends Cmd
  object OkrCmd {
    def parse(s: String): Option[OkrCmd] = {
      s.split(',')
        .map(_.trim)
        .toList
        .filterNot(_.isEmpty) match {
        case user :: team :: channel :: Nil =>
          Some(OkrCmd(user.mention, team, channel))
        case _ => None
      }
    }
  }

  /** Lệnh nhắc add OKRs */
  case class AddOkrs(cmds: List[OkrCmd])
  object AddOkrs {
    // ex: d.text = `/o okr thanhbv,Team Shepherd,okr-shepherd; lamnt,Team 52 lá, team-52-la`
    def parse(d: SlashData): Option[AddOkrs] = d.text.split(' ').headOption match {
      case Some("okr") =>
        val cmds = d.text
          .substring("okr".length)
          .split(';')
          .toList
          .map(OkrCmd.parse)
          .withFilter(_.isDefined)
          .map(_.get)
        Some(AddOkrs(cmds))
      case _ => None
    }
  }
}

/** Actor manage all bot conversations */
class BotConversations @Inject()(c: MmConfig, addOkrsFactory: AddOkrsActor.Factory)
  extends Actor with InjectedActorSupport {

  import BotConversations._
  private[this] val logger = Logger(this.getClass)

  /** mutable state! but we use `context.become` here
    * @see https://github.com/alexandru/scala-best-practices/blob/master/sections/5-actors.md */
  private[this] var wsRef: ActorRef = _

  def receive: Receive = {
    case WsRef(ref) =>
      wsRef = ref
      context.become(waitAuth)
  }

  def waitAuth: Receive = {
    case DoAuth =>
      // https://api.mattermost.com/#tag/WebSocket
      wsRef ! WsMsg.authChallenge(c.personalToken)
      context.become(waitHello)
  }

  def waitHello: Receive = {
    case Hello => context.become(ready)
  }

  private[this] val conversations = TrieMap.empty[String, ActorRef]

  def ready: Receive = {
    case AddOkrs(cmds) =>
      logger.info(s"$cmds")
      for(cmd <- cmds) {
        val ref = injectedChild(
          addOkrsFactory(cmd),
          s"addOkrs${cmd.user}${Random.nextInt()}"
        )
        conversations.put(cmd.user, ref).foreach { old =>
          logger.warn(s"discard old command for ${cmd.user}")
          old ! PoisonPill
        }
      }

    case e: PostedEvent if e.data.isDirect =>
      conversations.get(e.data.sender_name.mention) match {
        case Some(ref) => ref ! e
        case None => // ignore
      }
  }
}
