package com.example.roulette

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import com.example.roulette.Cache.TimerCache
import fs2.Stream
import fs2.concurrent.Topic

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, Option[RawRequest]]
      t <- Topic[IO, Response]
      timerCache <- TimerCache()

      exitCode <- {
        val timerStream = Stream.awakeEvery[IO](1.seconds)
          .as(timerCache)
          .evalMap(Response.fromTimerNotification)
          .through(t.publish)

        val rawRequestStream = Stream
          .fromQueueNoneTerminated(q)
          .map(Response.fromRawRequest)
          .through(t.publish)

        val combinedStream = Stream(rawRequestStream, RouletteServer.stream(q, t), timerStream).parJoinUnbounded

        combinedStream.compile.drain.as(ExitCode.Success)
      }
    } yield exitCode
  }


}
