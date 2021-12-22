package com.example.roulette.boot

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import com.example.roulette.game.GameCache
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.timer.{TimerCache, TimerProcessor}
import fs2.Stream
import fs2.concurrent.Topic

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, Option[Response]]
      t <- Topic[IO, Response]
      playerCache <- PlayersCache[IO]()
      timerCache <- TimerCache[IO]()
      gameCache <- GameCache[IO]()

      exitCode <- {
        val timerStream = Stream
          .awakeEvery[IO](1.seconds)
          .evalMap { _ =>
            TimerProcessor.getResponse(
              timerCache,
              gameCache,
              playerCache)
          }
          .through(t.publish)

        val serverStream = RouletteServer.stream(playerCache, gameCache, q, t)

        val webSocketResponseStream = Stream
          .fromQueueNoneTerminated(q)
          .through(t.publish)

        Stream(serverStream, webSocketResponseStream, timerStream)
          .parJoinUnbounded
          .compile
          .drain
          .as(ExitCode.Success)
      }
    } yield exitCode
  }


}
