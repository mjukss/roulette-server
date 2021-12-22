package com.example.roulette.boot

import cats.effect.kernel.Concurrent
import cats.effect.std.Queue
import cats.implicits.{toFlatMapOps, toFunctorOps}
import com.example.roulette.game.GameCache
import com.example.roulette.player.{PlayerUsernameCache, PlayersCache}
import com.example.roulette.request.HttpRequestProcessor
import com.example.roulette.request.Request.{RegisterPlayer, RemovePlayer}
import com.example.roulette.request.WebSocketRequestProcessor.processWSRequest
import com.example.roulette.response.Response
import com.example.roulette.response.ResponseProcessor.getFilteredResponse
import fs2.concurrent.Topic
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

object RouletteRoutes {
  def gameRoutes[F[_] : Concurrent](
                                     gameCache: GameCache[F],
                                     playersCache: PlayersCache[F],
                                     queue: Queue[F, Option[Response]],
                                     publicTopic: Topic[F, Response])(wsb: WebSocketBuilder2[F]
                                   ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      case req@POST -> Root / "register" => for {
        reqBody <- req.as[RegisterPlayer]
        response <- HttpRequestProcessor.registerPlayer(reqBody, playersCache)
      } yield response

      case req@POST -> Root / "remove" => for {
        reqBody <- req.as[RemovePlayer]
        response <- HttpRequestProcessor.removePlayer(reqBody, playersCache)
      } yield response

      case GET -> Root =>
        for {
          privateTopic <- Topic[F, WebSocketFrame]
          usernameCache <- PlayerUsernameCache()
          webSocketResponse <- {
            val combinedStreams = publicTopic.subscribe(1000)
              .evalMapFilter(getFilteredResponse(_, usernameCache, playersCache))
              .merge(privateTopic.subscribe(1000))

            wsb.build(
              receive = processWSRequest(privateTopic, _, playersCache, gameCache, usernameCache, queue),
              send = combinedStreams
            )
          }
        } yield webSocketResponse
    }
  }
}
