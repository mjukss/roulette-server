package com.example.roulette.boot

import cats.effect.std.Queue
import cats.effect.{Async, ExitCode}
import cats.implicits.toFunctorOps
import com.example.roulette.game.GameCache
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.router.Router
import com.example.roulette.timer.{TimerCache, TimerProcessor}
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.CORS

import scala.concurrent.duration.DurationInt

class RouletteServer[F[_]: Async](
    timerCache: TimerCache[F],
    playersCache: PlayersCache[F],
    gameCache: GameCache[F],
    queue: Queue[F, Option[Response]],
    topic: Topic[F, Response]
) {

  private val port = sys.env.getOrElse("PORT", "8080").toInt
  private val host = "0.0.0.0"

  private val timerStream = Stream
    .awakeEvery[F](1.seconds)
    .evalMap(_ => TimerProcessor.getResponse(timerCache, gameCache, playersCache))
    .through(topic.publish)

  private val webSocketResponseStream = Stream
    .fromQueueNoneTerminated(queue)
    .through(topic.publish)

  private val serverStream = BlazeServerBuilder[F]
    .bindHttp(port, host)
    .withHttpWebSocketApp { implicit wsb =>
      CORS.policy
        .withAllowOriginAll(
          Router.build(gameCache, playersCache, queue, topic)
        )
    }
    .serve

  private val combinedStream =
    Stream(serverStream, webSocketResponseStream, timerStream).parJoinUnbounded.compile.drain
      .as(ExitCode.Success)
}

object RouletteServer {
  def make[F[_]: Async](
      timerCache: TimerCache[F],
      playersCache: PlayersCache[F],
      gameCache: GameCache[F],
      queue: Queue[F, Option[Response]],
      topic: Topic[F, Response]
  ): F[ExitCode] = new RouletteServer(timerCache, playersCache, gameCache, queue, topic).combinedStream
}
