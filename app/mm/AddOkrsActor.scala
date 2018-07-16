package mm

import akka.actor.{Actor, PoisonPill}
import com.google.inject.assistedinject.Assisted
import javax.inject.Inject
import mm.BotConversations.OkrCmd
import mm.model.WsMsg.PostedEvent
import mm.model.{KR, OKR}
import mm.util.Implicits._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

/** @see https://www.playframework.com/documentation/2.6.x/ScalaAkka#Dependency-injecting-actors */
object AddOkrsActor {
  trait Factory {
    def apply(cmd: OkrCmd): Actor
  }
}

/** handle conversation giữa okr bot với user `cmd.user` để user thực hiện OkrCmd `cmd` */
class AddOkrsActor @Inject()(c: MmConfig, ws: WSClient,
                             @Assisted cmd: OkrCmd) extends Actor {
  private[this] val logger = Logger(this.getClass)

  private def post(msg: String) =
    ws.url(c.hookInUrl).post(
      Json.obj(
        "channel" -> cmd.user,
        "text" -> msg
      )
    )
  private def broadcast(okrs: List[OKR]) = {
    val msg = "##### OKRs của " + cmd.team + "\n" + okrs.zipWithIndex.map {
      case (okr, i) =>
        s"+ O${i+1}: ${okr.o}\n" + okr.krs.zipWithIndex.map {
          case (kr, j) => s"  - KR${j+1}: ${kr.txt}"
        }.mkString("\n")
    }.mkString("\n")

    ws.url(c.hookInUrl).post(
      Json.obj(
        "username" -> cmd.user.unmention,
        "channel" -> cmd.channel,
        "text" -> msg
      )
    )
  }

  def receive: Receive = waitObjective(Nil)

  def waitObjective(okrs: List[OKR]): Receive = {
    case e: PostedEvent =>
      if (okrs.nonEmpty && e.data.postTyped.message == "!") {
        post("Thank you!")
        // TODO persist to db
        logger.info(s"OKRs: $okrs")
        broadcast(okrs)
        self ! PoisonPill
      } else {
        val okr = OKR(e.data.postTyped.message)
        post("KR1 ?")
        context.become(waitKr(okr +: okrs))
      }
  }
  def waitKr(okrs: List[OKR]): Receive = {
    case e: PostedEvent =>
      val okr = okrs.head
      okr.krs.length match {
        case 0 =>
          post("KR2 ?\n_Gửi `!` nếu không có KR nào nữa_")
          val kr = KR(e.data.postTyped.message)
          val updatedOkr = okr.copy(krs = okr.krs :+ kr)
          context.become(waitKr(updatedOkr +: okrs.tail))
        case n =>
          if (e.data.postTyped.message == "!") {
            post(s"Mục tiêu ${okrs.length + 1} ?\n_Gửi `!` nếu không có OKR nào nữa_")
            context.become(waitObjective(okrs))
          } else {
            post(s"KR${n+2} ?\n_Gửi `!` nếu không có KR nào nữa_")
            val kr = KR(e.data.postTyped.message)
            val updatedOkr = okr.copy(krs = okr.krs :+ kr)
            context.become(waitKr(updatedOkr +: okrs.tail))
          }
      }
  }

  post(s"""##### Cùng tái sáng lập 1 Sân Đình chuyên nghiệp!
          |Hãy thêm OKR cho ${cmd.team}:
          |Mục tiêu 1 ?""".stripMargin)
}
