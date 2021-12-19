package com.example.roulette.integration

import com.example.roulette.integration.HttpEndpointsTest._
import com.example.roulette.integration.setup.HttpStarter
import com.example.roulette.integration.setup.HttpStarter.{registerUri, removeUri}
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.{RegisterPlayer, RemovePlayer}
import munit.CatsEffectSuite

class HttpEndpointsTest extends CatsEffectSuite {

  test("Register and remove player") {
    assertIO(successfulRegistration, List(
      Some("""{"username":"player777420","responseType":"RegistrationSuccessful"}"""),
      Some("""{"responseType":"PlayerRemoved"}""")
    ))
  }
  test("Can't register when username taken") {
    assertIO(unsuccessfulRegistration, List(
      Some("""{"username":"player777420","responseType":"RegistrationSuccessful"}"""),
      None,
      Some("""{"responseType":"PlayerRemoved"}""")
    ))
  }
  test("Can't remove player when player does not exist") {
    assertIO(unsuccessfulRemovePlayer, List(None))
  }
}

object HttpEndpointsTest {

  val (username, password) = (Username("player777420"), Password("1234556"))

  val connections = List(
    (registerUri, RegisterPlayer(username, password)),
    (removeUri, RemovePlayer(username, password))
  )

  def successfulRegistration = HttpStarter.run(connections)

  def unsuccessfulRegistration = HttpStarter.run((registerUri, RegisterPlayer(username, password)) :: connections)

  def unsuccessfulRemovePlayer = HttpStarter.run((removeUri, RemovePlayer(username, password)) :: Nil)

}