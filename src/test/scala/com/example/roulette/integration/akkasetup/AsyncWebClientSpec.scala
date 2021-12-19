package com.example.roulette.integration.akkasetup

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.example.roulette.integration.akkasetup.AsyncWebClientSpec.program
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.request.Request.RegisterPlayer
import io.circe.syntax.EncoderOps
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.{ExecutionContextExecutor, Future}

class AsyncWebClientSpec extends AnyWordSpec with ScalaFutures with IntegrationPatience {

  "WebClient when downloading images" should {
    "for a valid link return non-zero content " in {
      whenReady(program) { testImage =>
        testImage mustBe "cDone"
        // Do whatever you need
      }

    }
  }
}

object AsyncWebClientSpec {
  implicit val system: ActorSystem = ActorSystem()

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def httpResponse(username: Username, password: Password) = {

    import akka.http.scaladsl.client.RequestBuilding.Post

    val body = RegisterPlayer(username, password).asJson.noSpaces

    def req(endpoint: String) = Post(s"https://roulette-app-evo.herokuapp.com/$endpoint", body)

    Http().singleRequest(req("register")).flatMap {
      case HttpResponse(StatusCodes.OK, _, res, _) => Unmarshal(res).to[String]
      case HttpResponse(StatusCodes.BadRequest, _, res, _) => Unmarshal(res).to[String]
      case _ => Future("something wrong")
    }.andThen {
      case _ => Http().singleRequest(req("remove"))
    }
  }

  val username = Username("Usergg")
  val passsword = Password("Pass")


  def program = httpResponse(username, passsword)


}