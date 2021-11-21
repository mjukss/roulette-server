package com.example.roulette

import com.example.roulette.Response.{BadRequest, LuckyNumber, Timer}
import io.circe
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec


class ResponseTest extends AnyWordSpec with Matchers {

  import ResponseTest._

  "Response json validation" should {
    "encode and decode BetPlaced Response" in {
      responseDecoder(betPlacedJson) mustBe Right(betPlaced)
      betPlaced.asJson.noSpaces mustBe betPlacedJson
    }
    "encode and decode BetsCleared Response" in {
      responseDecoder(betsClearedJson) mustBe Right(betsCleared)
      betsCleared.asJson.noSpaces mustBe betsClearedJson
    }
    "encode and decode PlayerRegistered Response" in {
      responseDecoder(playerRegisteredJson) mustBe Right(playerRegistered)
      playerRegistered.asJson.noSpaces mustBe playerRegisteredJson
    }
    "encode and decode PlayerRemoved Response" in {
      responseDecoder(playerRemovedJson) mustBe Right(playerRemoved)
      playerRemoved.asJson.noSpaces mustBe playerRemovedJson
    }
    "encode and decode TimerNotification Response" in {
      responseDecoder(timerNotificationJson) mustBe Right(timerNotification)
      timerNotification.asJson.noSpaces mustBe timerNotificationJson
    }
    "encode and decode Result Response" in {
      responseDecoder(resultJson) mustBe Right(result)
      result.asJson.noSpaces mustBe resultJson
    }
    "encode and decode BadRequest Response" in {
      responseDecoder(badRequestJson) mustBe Right(BadRequest)
      (BadRequest : Response).asJson.noSpaces mustBe badRequestJson
    }

    "encode and decode Timer" in {
      timer.asJson.noSpaces mustBe timerJson
      decode[Timer](timerJson) mustBe Right(timer)
    }

    "encode and decode LuckyNumber" in {
      luckyNumber.asJson.noSpaces mustBe luckyNumberJson
      decode[LuckyNumber](luckyNumberJson) mustBe Right(luckyNumber)
    }
  }

}

object ResponseTest {

  import Response._

  val responseDecoder: String => Either[circe.Error, Response] = decode[Response]

  val betPlaced: Response = BetPlaced(Nil)
  val betPlacedJson = """{"players":[],"responseType":"BetPlaced"}"""

  val betsCleared: Response = BetsCleared(Nil)
  val betsClearedJson = """{"players":[],"responseType":"BetsCleared"}"""

  val playerRegistered: Response = PlayerRegistered(Nil)
  val playerRegisteredJson = """{"players":[],"responseType":"PlayerRegistered"}"""

  val playerRemoved: Response = PlayerRemoved(Nil)
  val playerRemovedJson = """{"players":[],"responseType":"PlayerRemoved"}"""

  val timer: Timer = Timer(0)
  val timerJson: String = "0"

  val timerNotification: Response = TimerNotification(timer)
  val timerNotificationJson = """{"time":0,"responseType":"TimerNotification"}"""

  val luckyNumber: LuckyNumber = LuckyNumber(7)
  val luckyNumberJson: String = "7"

  val result: Response = Result(luckyNumber)
  val resultJson = """{"luckyNumber":7,"responseType":"Result"}"""

  val badRequestJson = """{"responseType":"BadRequest"}"""
}