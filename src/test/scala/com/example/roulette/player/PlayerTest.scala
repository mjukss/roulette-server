package com.example.roulette.player

import com.example.roulette.bet.Bet.Chips
import com.example.roulette.player.Player.{Password, Username}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayerTest extends AnyWordSpec with Matchers {

  import PlayerTest._

  "Player json validation" should {
    "encode and decode Player" in {
      player1.asJson.noSpaces mustBe player1JsonResponse
      player2.asJson.noSpaces mustBe player2JsonResponse

      decode[Player](player1Json) mustBe Right(player1)
      decode[Player](player2Json) mustBe Right(player2)
    }
    "encode and decode Username" in {
      username.asJson.noSpaces mustBe usernameJson
      decode[Username](usernameJson) mustBe Right(username)
    }
  }

}

object PlayerTest {
  val usernameJson = """"player-username""""
  val username: Username = Username("player-username")
  val player1: Player = Player(username, Password("12345"))
  val player1Json = """{"username":"player-username","password":"12345","isOnline":false,"balance":200,"chipsPlaced":0}"""
  val player1JsonResponse = """{"username":"player-username","balance":200,"chipsPlaced":0,"bets":null}"""
  val player2: Player = Player(username, Password("12345"), balance = Chips(400))
  val player2JsonResponse = """{"username":"player-username","balance":400,"chipsPlaced":0,"bets":null}"""
  val player2Json = """{"username":"player-username","password":"12345","isOnline":false,"balance":400,"chipsPlaced":0,"bets":null}"""
}