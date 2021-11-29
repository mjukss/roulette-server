package com.example.roulette.boot

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import com.example.roulette.game.GameCache
import com.example.roulette.player.Player.Username
import com.example.roulette.player.PlayersCache
import com.example.roulette.request.Request.RequestOrError
import com.example.roulette.request.RequestProcessor
import com.example.roulette.response.Response
import com.example.roulette.timer.{TimerCache, TimerProcessor}
import fs2.Stream
import fs2.concurrent.Topic

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, Option[(Username, RequestOrError)]]
      t <- Topic[IO, Response]
      playerCache <- PlayersCache[IO]()
      timerCache <- TimerCache[IO]()
      gameCache <- GameCache[IO]()

      exitCode <- {
        val timerStream = Stream
          .awakeEvery[IO](1.seconds)
          .evalMap(_ => TimerProcessor.getResponse(timerCache, gameCache, playerCache))
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
