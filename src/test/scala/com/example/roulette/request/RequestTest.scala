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
    "encode and decode ExitGame Request" in {
      requestDecoder(exitGameJson) mustBe Right(exitGame)
      exitGame.asJson.noSpaces mustBe exitGameJson
    }
    "encode and decode JoinGame Request" in {
      requestDecoder(joinGameJson) mustBe Right(joinGame)
      joinGame.asJson.noSpaces mustBe joinGameJson
    }
    "encode and decode InvalidRequest Request" in {
      requestDecoder(invalidRequestJson) mustBe Right(invalidRequest)
      invalidRequest.asJson.noSpaces mustBe invalidRequestJson
    }

  }
}

object RequestTest {

  import Request._

  val requestDecoder: String => Either[circe.Error, Request] = decode[Request]

  val username: Username = Username("player1")
  val password: Password = Password("12345")
  val placeBet: Request = PlaceBet(Straight(List(BetPosition(1)), Chips(20)))
  val placeBetJson = """{"bet":{"positions":[1],"betAmount":20,"betType":"Straight"},"requestType":"PlaceBet"}"""

  val clearBets: Request = ClearBets
  val clearBetsJson = """{"requestType":"ClearBets"}"""

  val registerPlayer: Request = RegisterPlayer(username, Password("12345"))
  val registerPlayerJson = """{"username":"player1","password":"12345","requestType":"RegisterPlayer"}"""

  val exitGame: Request = ExitGame
  val exitGameJson = """{"requestType":"ExitGame"}"""

  val joinGame: Request = JoinGame(username, password)
  val joinGameJson = """{"username":"player1","password":"12345","requestType":"JoinGame"}"""

  val invalidRequest: Request = InvalidRequest("error")
  val invalidRequestJson = """{"errorMessage":"error","requestType":"InvalidRequest"}"""



}
