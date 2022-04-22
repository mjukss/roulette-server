package com.example.roulette.router.ws

import cats.effect.kernel.Concurrent
import cats.effect.std.Queue
import com.example.roulette.game.GameCache
import com.example.roulette.player.{ AuthCache, PlayersCache }
import com.example.roulette.request.WebSocketRequestProcessor.processWSRequest
import com.example.roulette.response.Response
import com.example.roulette.response.ResponseProcessor.getFilteredResponse
import fs2.concurrent.Topic
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import cats.implicits._

class WsRoutes[F[_]: Concurrent](
    gameCache: GameCache[F],
    playersCache: PlayersCache[F],
    queue: Queue[F, Option[Response]],
    publicTopic: Topic[F, Response]
)(implicit wsb: WebSocketBuilder2[F])
    extends Http4sDsl[F] {

  private def routes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          privateTopic <- Topic[F, WebSocketFrame]
          authCache    <- AuthCache[F]()
          webSocketResponse <- {
            val combinedStreams = publicTopic
              .subscribe(1000)
              .evalMapFilter(getFilteredResponse(_, authCache, playersCache))
              .merge(privateTopic.subscribe(1000))

            wsb.build(
              receive = processWSRequest(privateTopic, _, playersCache, gameCache, authCache, queue),
              send = combinedStreams
            )
          }
        } yield webSocketResponse
    }
  }
}

case object WsRoutes {
  def make[F[_]: Concurrent: WebSocketBuilder2](
      gameCache: GameCache[F],
      playersCache: PlayersCache[F],
      queue: Queue[F, Option[Response]],
      publicTopic: Topic[F, Response]
  ): HttpRoutes[F] = new WsRoutes(gameCache, playersCache, queue, publicTopic).routes
}
