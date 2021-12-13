package com.example.roulette.boot

import cats.effect.std.Queue
import cats.effect.{Async, ExitCode}
import com.example.roulette.boot.RouletteRoutes.gameRoutes
import com.example.roulette.game.GameCache
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.blaze.server.BlazeServerBuilder

object RouletteServer {
  def stream[F[_] : Async](playersCache: PlayersCache[F],
                           gameCache: GameCache[F],
                           queue: Queue[F, Option[Response]],
                           t: Topic[F, Response]): Stream[F, ExitCode] = {
    BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpWebSocketApp(gameRoutes(gameCache, playersCache, queue, t)(_).orNotFound)
      .serve
  }
}
