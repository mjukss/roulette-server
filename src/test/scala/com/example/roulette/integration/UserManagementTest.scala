package com.example.roulette.integration

import com.example.roulette.integration.UserManagementTest._
import com.example.roulette.integration.setup.HttpStarter
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.{RegisterPlayer, RemovePlayer}
import munit.CatsEffectSuite
import org.http4s.implicits.http4sLiteralsSyntax

class UserManagementTest extends CatsEffectSuite {

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

object UserManagementTest {


  val uriRegister = uri"https://roulette-app-evo.herokuapp.com/register"
  val uriRemove = uri"https://roulette-app-evo.herokuapp.com/remove"

  val (username, password) = (Username("player777420"), Password("1234556"))

  val connections = List(
    (uriRegister, RegisterPlayer(username, password)),
    (uriRemove, RemovePlayer(username, password))
  )

  def successfulRegistration = HttpStarter.run(connections)

  def unsuccessfulRegistration = HttpStarter.run((uriRegister, RegisterPlayer(username, password)) :: connections)

  def unsuccessfulRemovePlayer = HttpStarter.run((uriRemove, RemovePlayer(username, password)) :: Nil)

}