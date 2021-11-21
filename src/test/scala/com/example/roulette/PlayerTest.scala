package com.example.roulette

import com.example.roulette.Bet.Chips
import com.example.roulette.Player.Username
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayerTest extends AnyWordSpec with Matchers {

  import PlayerTest._
  "Player json validation" should {
    "encode and decode Player" in {
      player1.asJson.noSpaces mustBe player1Json
      player2.asJson.noSpaces mustBe player2Json

      decode[Player](player1Json) mustBe Right(player1)
      decode[Player](player2Json) mustBe Right(player2)
    }
    "use left channel when invalid json string" in {
      decode[Player]("""{"username":"player-username"}""").isLeft mustBe true
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
  val player1: Player = Player(username)
  val player1Json = """{"username":"player-username","balance":200,"betPlaced":0}"""
  val player2: Player = Player(username, balance = Chips(400))
  val player2Json = """{"username":"player-username","balance":400,"betPlaced":0}"""
}