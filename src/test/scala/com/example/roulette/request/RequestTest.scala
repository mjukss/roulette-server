package com.example.roulette.request

import com.example.roulette.bet.Bet.{Chips, Red}
import com.example.roulette.player.Player.Username
import io.circe
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RequestTest extends AnyWordSpec with Matchers {
  import RequestTest._

  "Request json validation" should {
    "encode and decode PlaceBet Request" in {
      requestDecoder(placeBetJson) mustBe Right(placeBet)
      placeBet.asJson.noSpaces mustBe placeBetJson
    }
    "encode and decode ClearBets Request" in {
      requestDecoder(clearBetsJson) mustBe Right(clearBets)
      clearBets.asJson.noSpaces mustBe clearBetsJson
    }
    "encode and decode RegisterPlayer Request" in {
      requestDecoder(registerPlayerJson) mustBe Right(registerPlayer)
      registerPlayer.asJson.noSpaces mustBe registerPlayerJson
    }
    "encode and decode RemovePlayer Request" in {
      requestDecoder(removePlayerJson) mustBe Right(removePlayer)
      removePlayer.asJson.noSpaces mustBe removePlayerJson
    }
    "decode string to either Request or Error" in {
      Request.fromString("invalid String").isLeft mustBe true
      Request.fromString(removePlayerJson).isRight mustBe true
    }
  }
}

object RequestTest {

  import Request._

  val requestDecoder: String => Either[circe.Error, Request] = decode[Request]

  val username: Username = Username("player1")
  val placeBet: Request = PlaceBet(Red(Chips(20)))
  val placeBetJson = """{"bet":{"betAmount":20,"betType":"Red"},"requestType":"PlaceBet"}"""

  val clearBets: Request = ClearBets
  val clearBetsJson = """{"requestType":"ClearBets"}"""

  val registerPlayer: Request = RegisterPlayer
  val registerPlayerJson = """{"requestType":"RegisterPlayer"}"""

  val removePlayer: Request = RemovePlayer
  val removePlayerJson = """{"requestType":"RemovePlayer"}"""


}
