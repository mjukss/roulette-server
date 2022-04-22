package com.example.roulette.router

import cats.data.Kleisli
import cats.effect.kernel.Concurrent
import cats.effect.std.Queue
import cats.implicits.toSemigroupKOps
import com.example.roulette.game.GameCache
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.router.rest.RestRoutes
import com.example.roulette.router.ws.WsRoutes
import fs2.concurrent.Topic
import org.http4s
import org.http4s.server.websocket.WebSocketBuilder2

class Router[F[_]: Concurrent: WebSocketBuilder2](
    gameCache: GameCache[F],
    playersCache: PlayersCache[F],
    queue: Queue[F, Option[Response]],
    topic: Topic[F, Response]
) {
  private val restRoutes = RestRoutes.make(playersCache)
  private val wsRoutes   = WsRoutes.make(gameCache, playersCache, queue, topic)

  private val allRoutes = (restRoutes <+> wsRoutes).orNotFound
}

object Router {
  def build[F[_]: Concurrent: WebSocketBuilder2](
      gameCache: GameCache[F],
      playersCache: PlayersCache[F],
      queue: Queue[F, Option[Response]],
      topic: Topic[F, Response]
  ): Kleisli[F, http4s.Request[F], http4s.Response[F]] = new Router[F](gameCache, playersCache, queue, topic).allRoutes
}
