package com.example.roulette.integration

import com.example.roulette.integration.JoinGameTest.{playerConnection, playerConnectionWrongPassword, registerPlayer, removePlayer}
import com.example.roulette.integration.setup.HttpStarter
import com.example.roulette.integration.setup.HttpStarter.{registerUri, removeUri}
import com.example.roulette.integration.setup.WSStarter.connectToWebSocket
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.{JoinGame, RegisterPlayer, RemovePlayer}
import munit.CatsEffectSuite
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class JoinGameTest extends CatsEffectSuite {

  test("Can join game and first received message should contain string 'WelcomeToGame'") {
    def logs = for {
      _ <- HttpStarter.run(registerPlayer :: Nil)
      logs <- connectToWebSocket(List(playerConnection))
      result = logs.map(log => log.contains("WelcomeToGame"))
      _ <- HttpStarter.run(removePlayer :: Nil)
    } yield result

    assertIO(logs, List(true))
  }

  test("should not join game with wrong password") {
    def logs = for {
      _ <- HttpStarter.run(registerPlayer :: Nil)
      logs <- connectToWebSocket(List(playerConnectionWrongPassword))
      _ <- HttpStarter.run(removePlayer :: Nil)
    } yield logs

    assertIO(logs, List("""{"message":"WrongPassword","responseType":"BadRequest"}"""))
  }

  test("Should not join game when username does not exist") {
    def logs = connectToWebSocket(List(playerConnection))

    assertIO(logs, List("""{"message":"UsernameDoesNotExist","responseType":"BadRequest"}"""))
  }

  test("Should not join game when player already playing") {
    def connections = List(
      playerConnection.copy(msgLimit = 0, stayConnected = 2.seconds),
      playerConnection.copy(delay = 1.second))

    def logs = for {
      _ <- HttpStarter.run(registerPlayer :: Nil)
      logs <- connectToWebSocket(connections)
      result = logs
      _ <- HttpStarter.run(removePlayer :: Nil)
    } yield result

    assertIO(logs, List("""{"message":"UserAlreadyPlaying","responseType":"BadRequest"}"""))
  }

}

object JoinGameTest {

  val username = Username("player77742033")
  val password = Password("1234556")

  val registerPlayer = (registerUri, RegisterPlayer(username, password))
  val removePlayer = (removeUri, RemovePlayer(username, password))

  val playerConnection = setup.PlayerConnection(
    username = username,
    requests = List(JoinGame(username, password)),
    msgLimit = 1,
  )

  val playerConnectionWrongPassword = setup.PlayerConnection(
    username = username,
    requests = List(JoinGame(username, Password("foo"))),
    msgLimit = 1,
  )

}


