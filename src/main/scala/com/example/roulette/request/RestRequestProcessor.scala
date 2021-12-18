package com.example.roulette.request

import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps}
import com.example.roulette.player.{Player, PlayersCache}
import com.example.roulette.request.Request.RegisterPlayer
import com.example.roulette.response.BadRequestMessage.UsernameTaken
import com.example.roulette.response.Response
import com.example.roulette.response.Response.RegistrationSuccessful
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl

object RestRequestProcessor {

  def registerPlayer[F[_] : Concurrent](
                                         registerPlayerReq: RegisterPlayer,
                                         playersCache: PlayersCache[F],
                                       ): F[org.http4s.Response[F]] = {
    val RegisterPlayer(username, password) = registerPlayerReq
    val newPlayer = Player(username, password)
    val usernameTakenResponse: Response = Response.BadRequest(UsernameTaken)

    val dsl = Http4sDsl[F]
    import dsl._

    implicit val responseEncoder: EntityEncoder[F, Response] = jsonEncoderOf[F, Response]

    playersCache.readOne(username).flatMap {
      case Some(_) => BadRequest(usernameTakenResponse)
      case None => for {
        _ <- playersCache.updateOne(newPlayer)
        response <- Ok(RegistrationSuccessful(username): Response)
      } yield response
    }
  }
}
