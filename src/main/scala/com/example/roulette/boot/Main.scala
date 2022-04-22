package com.example.roulette.boot

import cats.effect.std.Queue
import cats.effect.{ ExitCode, IO, IOApp }
import com.example.roulette.game.GameCache
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.timer.TimerCache
import fs2.concurrent.Topic

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      queue       <- Queue.unbounded[IO, Option[Response]]
      topic       <- Topic[IO, Response]
      playerCache <- PlayersCache[IO]()
      timerCache  <- TimerCache[IO]()
      gameCache   <- GameCache[IO]()
      exitCode    <- RouletteServer.make[IO](timerCache, playerCache, gameCache, queue, topic)
    } yield exitCode
  }

}
