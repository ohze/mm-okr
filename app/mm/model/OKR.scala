package mm.model

import java.time.Instant
import mm.model.KrType.BinKrTpe

case class OKR(
                o: String,
                krs: List[KR] = Nil,
                createAt: Instant = Instant.now(),
                updateAt: Instant = Instant.now(),
                // dueAt: Instant
              )
/** @param checkIns a grade is also a check-in */
case class KR(
               txt: String,
               tpe: KrType = BinKrTpe(),
               checkIns: List[KrCheckIn] = Nil
             ) {
  def score: Float = if (checkIns.isEmpty) 0 else checkIns.last.score
}

/**
  * contains a calculable score, != score provided by owner when check-in `KrCheckIn.score`
  */
sealed trait KrType {
  def score: Float
}
object KrType {
  case class BinKrTpe(done: Boolean = false) extends KrType {
    def score: Float = if (done) 1 else 0
  }
  case class PercentKrTpe(start: Float = 0, target: Float = 100, current: Float) extends KrType {
    def score: Float = (current - start) / (target - start)
  }
  case class MeasureKrTpe(start: Float = 0, target: Float, current: Float) extends KrType {
    def score: Float = (current - start) / (target - start)
  }
}
case class KrCheckIn(
  createAt: Instant,
  score: Float,
  msg: String
                    )

///**
//  * @param isPersonal note: leader should also have personal OKRs
//  * @param mmChannel note: if isPersonal then mmChannel is directChannel with okr bot account `shepherd`
//  */
//case class OkrOwner(
//                     isPersonal: Boolean,
//                     mmChannel: String
//                   )
//
///**
//  * @param cadence chu kỳ OKR trong công ty. Vd 10 tuần | 2 tháng | 1 quý
//  * @param brainstormAt schedule thời điểm brainstorm annual OKRs & OKR kỳ 1 của công ty.
//  *                     Trước time này 1 ngày, MmOkr sẽ post vào channel okr-shepherd để nhắc nhở.
//  *
//  *                     shepherd leader có thể config brainstormAt bằng slash command `/o c brainstorm` (o: okr, c: config)
//  *                     Khi đó, MmOkr sẽ response với 1 message cho phép shepherd leader chọn ???
//  */
//case class OkrProcess(
//                cadence: Period = Period.ofWeeks(10),
////                begin_at: LocalDate = LocalDate.of(2018, 7, 23),
//                brainstormAt: LocalDate
//                ) {
//}
//sealed trait OkrCadence
//object OkrCadence {
//  case class ByWeek(len: Int = 10) extends OkrCadence
//  case class ByMonth(len: Int = 2) extends OkrCadence
//  case class ByQuater(len: Int = 1) extends OkrCadence
//}
