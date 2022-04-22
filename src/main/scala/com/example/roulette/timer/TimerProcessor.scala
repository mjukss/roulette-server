package com.example.roulette.timer

import cats.effect.kernel.Sync
import cats.implicits.{ toFlatMapOps, toFunctorOps }
import com.example.roulette.game.{ GameCache, GamePhaseProcessor }
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.response.Response.TimerNotification

object TimerProcessor {
  def getResponse[F[+_]: Sync](
      timerCache: TimerCache[F],
      gameCache: GameCache[F],
      playersCache: PlayersCache[F]
  ): F[Response] = timerCache.read.flatMap { timer =>
    if (timer.value < 1) GamePhaseProcessor.process(timerCache, gameCache, playersCache)
    else timerCache.decreaseTime() as TimerNotification(timer)
  }
}
