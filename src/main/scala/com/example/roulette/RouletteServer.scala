package com.example.roulette

import cats.effect.std.Queue
import cats.effect.{Async, ExitCode}
import com.example.roulette.Request.RequestOrError
import com.example.roulette.RouletteRoutes.gameRoutes
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.blaze.server.BlazeServerBuilder

object RouletteServer {
  def stream[F[_] : Async](queue: Queue[F, Option[RequestOrError]], t: Topic[F, Response]): Stream[F, ExitCode] = {
    BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpWebSocketApp(gameRoutes(queue, t)(_).orNotFound)
      .serve
  }
}