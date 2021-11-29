package com.example.roulette.response

import com.example.roulette.bet.Bet
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.game.GamePhase
import com.example.roulette.player.PlayerTest
import com.example.roulette.response.BadRequestMessage.CustomBadRequestMessage
import com.example.roulette.response.Response.LuckyNumber
import com.example.roulette.timer.Timer
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
      println(playerRegistered.asJson.noSpaces)
      responseDecoder(playerRegisteredJson) mustBe Right(playerRegistered)
      playerRegistered.asJson.noSpaces mustBe playerRegisteredJson
    }
    "encode and decode PlayerRemoved Response" in {
      responseDecoder(playerRemovedJson) mustBe Right(playerRemoved)
      playerRemoved.asJson.noSpaces mustBe playerRemovedJson
    }

    "encode and decode BadRequest Response" in {
      responseDecoder(badRequestJson) mustBe Right(badRequest)
      badRequest.asJson.noSpaces mustBe badRequestJson
    }

    "encode and decode Timer" in {
      timer.asJson.noSpaces mustBe timerJson
      decode[Timer](timerJson) mustBe Right(timer)
    }

    "encode and decode LuckyNumber" in {
      luckyNumber.asJson.noSpaces mustBe luckyNumberJson
      decode[LuckyNumber](luckyNumberJson) mustBe Right(luckyNumber)
    }
    "encode and decode TimerNotification Response" in {
      responseDecoder(timerNotificationJson) mustBe Right(timerNotification)
      timerNotification.asJson.noSpaces mustBe timerNotificationJson
    }
    "encode and decode PhaseChange Response" in {
      responseDecoder(phaseChangeJson) mustBe Right(phaseChange)
      phaseChange.asJson.noSpaces mustBe phaseChangeJson
    }
  }

}

object ResponseTest {

  import PlayerTest._
  import Response._

  val responseDecoder: String => Either[circe.Error, Response] = decode[Response]

  val betJson = """{"betAmount":40,"betType":"Red"}"""
  val bet: Bet = Bet.Red(Chips(40))


  val betPlaced: Response = BetPlaced(bet.betAmount, player1.username, None)
  val betPlacedJson = """{"chipsPlaced":40,"username":"player-username","responseType":"BetPlaced"}"""

  val betsCleared: Response = BetsCleared(player1.username)
  val betsClearedJson = """{"username":"player-username","responseType":"BetsCleared"}"""


  val playerRegistered: Response = PlayerRegistered(player1, Some(GamePhase.BetsOpen), Some(Nil))
  val playerRegisteredJson = """{"username":"player-username","gamePhase":"BetsOpen","players":[],"responseType":"PlayerRegistered"}"""

  val playerRemoved: Response = PlayerRemoved(player1.username)
  val playerRemovedJson = """{"username":"player-username","responseType":"PlayerRemoved"}"""

  val timer: Timer = Timer(0)
  val timerJson: String = "0"

  val luckyNumber: LuckyNumber = LuckyNumber(7)
  val luckyNumberJson: String = "7"

  val badRequestJson = """{"username":"player-username","message":"error","responseType":"BadRequest"}"""
  val badRequest: Response = BadRequest(player1.username,CustomBadRequestMessage("error"))


  val timerNotification: Response = TimerNotification(timer)
  val timerNotificationJson = """{"secTillNextPhase":0,"responseType":"TimerNotification"}"""

  val phaseChange: Response = PhaseChanged(GamePhase.BetsOpen, Nil, Some(luckyNumber))
  val phaseChangeJson = """{"gamePhase":"BetsOpen","players":[],"luckyNumber":7,"responseType":"PhaseChange"}"""

}