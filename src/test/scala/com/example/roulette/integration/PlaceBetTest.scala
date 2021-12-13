package com.example.roulette.integration

import cats.effect.unsafe.implicits.global
import com.example.roulette.bet.Bet.{BetPosition, Chips, Straight}
import com.example.roulette.integration.setup.ClientStarter.connectToServer
import com.example.roulette.game.GamePhase.{BetsClosed, BetsOpen}
import com.example.roulette.integration.PlaceBetTest.logs
import com.example.roulette.integration.setup.PlayerConnection
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.{JoinGame, PlaceBet}
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{BadRequest, PlayerJoinedGame}
import org.scalatest.Inside.inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

// server must be running on port 8080
class PlaceBetTest extends AnyWordSpec with Matchers {
  "Place bet" should {
    "allow to place bet if game phase is BetsOpen" in {
      if (logs.exists(_.isInstanceOf[BadRequest])) {
        inside (logs.find(_.isInstanceOf[PlayerJoinedGame])) {
          case Some(PlayerJoinedGame(_, gamePhase, _)) =>
            gamePhase mustBe Some(BetsClosed)
        }
      } else {
        inside (logs.find(_.isInstanceOf[PlayerJoinedGame])) {
          case Some(PlayerJoinedGame(_, gamePhase, _)) =>
            gamePhase mustBe Some(BetsOpen)
        }
      }
    }

  }
}

object PlaceBetTest extends AnyWordSpec with Matchers {
  private val username1 = Username("Player1")
  private val betAmount = Chips(10)
  private val betPositionZero = BetPosition(0)
  private val placeBetRequest = PlaceBet(Straight(List(betPositionZero), betAmount))

  val playerConnection1: PlayerConnection = setup.PlayerConnection(
    username = username1,
    requests = List(JoinGame(username1, Password("12345")), placeBetRequest),
    msgLimit = 4,
  )

  val logs: List[Response] = connectToServer(List(playerConnection1)).unsafeRunSync()
}



