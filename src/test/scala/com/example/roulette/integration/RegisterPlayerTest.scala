package com.example.roulette.integration

import cats.effect.unsafe.implicits.global
import com.example.roulette.client.PlayerConnection
import com.example.roulette.client.ClientStarter.connectToServer
import com.example.roulette.integration.RegisterPlayerTest._
import com.example.roulette.player.Player
import com.example.roulette.player.Player.Username
import com.example.roulette.request.Request.RegisterPlayer
import com.example.roulette.response.BadRequestMessage.UsernameTaken
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{BadRequest, PlayerRegistered}
import org.scalatest.Inside.inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

class RegisterPlayerTest extends AnyWordSpec with Matchers {

  "Register player response" should {
    "return PlayerRegistered with list of players in game and game phase" in {
      val responseOption = logs.find(_.isInstanceOf[PlayerRegistered])

      inside(responseOption) {
        case Some(PlayerRegistered(player, Some(_), Some(players))) =>
          player mustBe player1
          players.contains(player1) mustBe true
      }
    }
    "return UsernameTaken when user already registered" in {
      inside(logs2.headOption) {
        case Some(BadRequest(username, message)) =>
          username mustBe username1
          message mustBe UsernameTaken
      }
    }
    "return players that are already in game (including myself)" in {
      val responseOption = logs3.find(_.isInstanceOf[PlayerRegistered])

      inside(responseOption) {
        case Some(PlayerRegistered(player, Some(_), Some(players))) =>
          player mustBe player2
          players.contains(player1) mustBe true
          players.contains(player2) mustBe true
      }
    }
  }
}

object RegisterPlayerTest {

  val username1: Username = Username("Player1")
  val username2: Username = Username("Player2")
  val player1: Player = Player(username1)
  val player2: Player = Player(username2)


  val playerConnection: PlayerConnection = PlayerConnection(
    username = username1,
    requests = List(RegisterPlayer),
    msgLimit = 3,
  )


  def logs: List[Response] = connectToServer(List(playerConnection)).unsafeRunSync()

  def logs2: List[Response] = connectToServer(
    List(
      playerConnection.copy(msgLimit = 0, stayConnected = 3.seconds),
      playerConnection.copy(msgLimit = 1, delay = 2.seconds),
    )
  ).unsafeRunSync()

  def logs3: List[Response] = connectToServer(
    List(
      playerConnection.copy(msgLimit = 0, stayConnected = 2.seconds),
      playerConnection.copy( username= username2, msgLimit = 1, delay = 1.seconds),
    )
  ).unsafeRunSync()

}

