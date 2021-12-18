package com.example.roulette.boot

import cats.effect.kernel.Concurrent
import cats.effect.std.Queue
import cats.implicits.{toFlatMapOps, toFunctorOps}
import com.example.roulette.game.GameCache
import com.example.roulette.player.{PlayerUsernameCache, PlayersCache}
import com.example.roulette.request.RestRequestProcessor
import com.example.roulette.request.Request.RegisterPlayer
import com.example.roulette.request.WebSocketRequestProcessor.processWSRequest
import com.example.roulette.response.Response
import com.example.roulette.response.ResponseProcessor.getFilteredResponse
import fs2.concurrent.Topic
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.{EntityDecoder, HttpRoutes}

object RouletteRoutes {
  def gameRoutes[F[_] : Concurrent](
                                     gameCache: GameCache[F],
                                     playersCache: PlayersCache[F],
                                     queue: Queue[F, Option[Response]],
                                     publicTopic: Topic[F, Response])(wsb: WebSocketBuilder2[F]
                                   ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    implicit val registerEntityDecoder: EntityDecoder[F, RegisterPlayer] = jsonOf[F, RegisterPlayer]

    HttpRoutes.of[F] {
      case req@POST -> Root / "register" => for {
        reqBody <- req.as[RegisterPlayer]
        response <- RestRequestProcessor.registerPlayer(reqBody, playersCache)
      } yield response

      case GET -> Root =>
        for {
          privateTopic <- Topic[F, WebSocketFrame]
          usernameCache <- PlayerUsernameCache()
          webSocketResponse <- wsb.build(
            receive = processWSRequest(privateTopic, _, playersCache, gameCache, usernameCache, queue),
            send = publicTopic
              .subscribe(1000)
              .evalMapFilter(getFilteredResponse(_, usernameCache, playersCache))
              .merge(privateTopic.subscribe(1000))
          )
        } yield webSocketResponse
    }
  }
}
