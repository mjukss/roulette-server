package com.example.roulette.request

import com.example.roulette.bet.Bet.{BetPosition, Chips, Straight}
import com.example.roulette.player.Player.{Password, Username}
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
//    "decode string to either Request or Error" in {
//      Request.fromString("invalid String").isLeft mustBe true
//      Request.fromString(removePlayerJson).isRight mustBe true
//    }
  }
}

object RequestTest {

  import Request._

  val requestDecoder: String => Either[circe.Error, Request] = decode[Request]

  val username: Username = Username("player1")
  val placeBet: Request = PlaceBet(Straight(List(BetPosition(1)), Chips(20)))
  val placeBetJson = """{"bet":{"positions":[1],"betAmount":20,"betType":"Straight"},"requestType":"PlaceBet"}"""

  val clearBets: Request = ClearBets
  val clearBetsJson = """{"requestType":"ClearBets"}"""

  val registerPlayer: Request = JoinGame(username, Password("12345"))
  val registerPlayerJson = """{"requestType":"JoinGame"}"""



}
