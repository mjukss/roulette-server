package com.example.roulette

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import com.example.roulette.Cache.{GameCache, PlayersCache, TimerCache}
import com.example.roulette.Request.RequestOrError
import fs2.Stream
import fs2.concurrent.Topic

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, Option[RequestOrError]]
      t <- Topic[IO, Response]
      playerCache <- PlayersCache[IO]()
      timerCache <-  TimerCache[IO]()
      gameCache <- GameCache[IO]()

      exitCode <- {
        val timerStream = Stream
          .awakeEvery[IO](1.seconds)
          .evalMap(_ => Response.fromTimerNotification(timerCache,gameCache, playerCache.readAll))
          .through(t.publish)

        import RequestProcessor.executeRequest
        val rawRequestStream = Stream
          .fromQueueNoneTerminated(q)
          .evalMap(executeRequest(_, playerCache, gameCache))
          .through(t.publish)

        Stream(rawRequestStream, RouletteServer.stream(playerCache, q, t), timerStream)
          .parJoinUnbounded
          .compile
          .drain
          .as(ExitCode.Success)
      }
    } yield exitCode
  }


}
