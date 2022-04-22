package com.example.roulette.request

import cats.effect.kernel.Concurrent
import cats.implicits.{ toFlatMapOps, toFunctorOps }
import com.example.roulette.player.Player.{ Password, Username }
import com.example.roulette.player.{ Player, PlayersCache }
import com.example.roulette.request.Request.{ RegisterPlayer, RemovePlayer }
import com.example.roulette.response.BadRequestMessage.{ UsernameDoesNotExist, UsernameTaken, WrongPassword }
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{ PlayerRemoved, RegistrationSuccessful }
import org.http4s
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import cats.implicits._

class HttpRequestProcessor[F[_]: Concurrent] extends Http4sDsl[F] {

  implicit val responseEncoder: EntityEncoder[F, Response] = jsonEncoderOf[F, Response]

  def handleRequest: PlayersCache[F] => PartialFunction[Request, F[http4s.Response[F]]] = playersCache => {
    case RegisterPlayer(username, password) => registerPlayer(username, password, playersCache)
    case RemovePlayer(username, password) => removePlayer(username, password, playersCache)
  }

  private def validateRegisterPlayer(
      username: Username,
      playersCache: PlayersCache[F]
  ): F[Either[Response, Response]] = playersCache.readOne(username).map {
    case Some(_) => Left(Response.BadRequest(UsernameTaken))
    case None    => Right(RegistrationSuccessful(username))
  }

  private def validateRemovePlayer(
      username: Username,
      password: Password,
      playersCache: PlayersCache[F]
  ): F[Either[Response, Response]] = playersCache.readOne(username).map {
    case None                                        => Left(Response.BadRequest(UsernameDoesNotExist))
    case Some(player) if player.password != password => Left(Response.BadRequest(WrongPassword))
    case Some(_)                                     => Right(PlayerRemoved)
  }

  private def registerPlayer(
      username: Username,
      password: Password,
      playersCache: PlayersCache[F]
  ) =
    validateRegisterPlayer(username, playersCache).flatMap {
      case Left(value)  => BadRequest(value)
      case Right(value) => playersCache.updateOne(Player(username, password)) *> Ok(value)
    }

  private def removePlayer(
      username: Username,
      password: Password,
      playersCache: PlayersCache[F]
  ): F[org.http4s.Response[F]] = validateRemovePlayer(username, password, playersCache).flatMap {
    case Left(value)  => BadRequest(value)
    case Right(value) => playersCache.removeOne(username) *> Ok(value)
  }
}

object HttpRequestProcessor {
  def make[F[_]: Concurrent]() = new HttpRequestProcessor[F]
}