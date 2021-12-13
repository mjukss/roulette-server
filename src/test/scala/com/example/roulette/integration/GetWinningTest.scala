package com.example.roulette.integration

import cats.effect.unsafe.implicits.global
import com.example.roulette.bet.Bet.{BetPosition, Chips, Straight}
import com.example.roulette.game.GamePhase.BetsClosed
import com.example.roulette.integration.GetWinningTest.logs
import com.example.roulette.integration.setup.ClientStarter.connectToServer
import com.example.roulette.integration.setup.PlayerConnection
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.{JoinGame, PlaceBet}
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{BetPlaced, LuckyNumber, PhaseChanged}
import org.scalatest.Inside.inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

// server must be running on port 8080 with command line args: `lucky-number 7`
class GetWinningTest extends AnyWordSpec with Matchers {
  "Get winnings" should {
    "collect winnings when betting on 7" in {

      val isBetPlaced = logs.exists(_.isInstanceOf[BetPlaced])

      isBetPlaced mustBe true


      val winningResponse = logs.find {
        case PhaseChanged(BetsClosed, _, _) => true
        case _ => false
      }

      inside(winningResponse) {
        case Some(PhaseChanged(_, _, luckyNumber)) =>
          luckyNumber mustBe Some(LuckyNumber(7))
      }

    }

    logs.foreach(println)
    //      inside(logs.find(_.isInstanceOf[BetPlaced])) {
    //        case Some(BetPlaced(chipsPlaced, username, bet)) =>
    //          gamePhase mustBe Some(BetsOpen)
    //
    //      }


  }
}

object GetWinningTest extends AnyWordSpec with Matchers {
  private val username1 = Username("Player1")
  private val username2 = Username("Player2")
  private val betAmount = Chips(10)
  private val betPositionSeven = BetPosition(7)
  private val placeBetRequest = PlaceBet(Straight(List(betPositionSeven), betAmount))

  val playerConnection1: PlayerConnection = setup.PlayerConnection(
    username = username1,
    requests = List(JoinGame(username2, Password("12345")), placeBetRequest),
    msgLimit = 20,
  )

  val playerConnection2: PlayerConnection = playerConnection1.copy(
    username = username2,
    delay = 15.seconds
  )

  val logs: List[Response] = connectToServer(List(playerConnection1, playerConnection2)).unsafeRunSync()
}








