package com.example.roulette.timer

import cats.effect.kernel.Sync
import com.example.roulette.game.GamePhase.{BetsClosed, BetsOpen}
import com.example.roulette.game.{GameCache, GamePhase, GamePhaseProcessor}
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.response.Response.TimerNotification
import com.example.roulette.wheel.WheelRange

object TimerProcessor {
  def getResponse[F[_] : Sync](wheelRange: WheelRange,
                               timerCache: TimerCache[F],
                               gameCache: GameCache[F],
                               playersCache: PlayersCache[F]): F[Response] = {
    import cats.implicits.{toFlatMapOps, toFunctorOps}
    val timerAndGamePhase: F[(Timer, GamePhase)] = for {
      gamePhase <- gameCache.read
      timer <- timerCache.read
    } yield (timer, gamePhase)

    timerAndGamePhase.flatMap {
      case (Timer(time), BetsOpen)
        if time < 1 => GamePhaseProcessor.getPhaseChangedResponse(wheelRange, timerCache, gameCache, playersCache)
      case (Timer(time), BetsClosed)
        if time < 1 => GamePhaseProcessor.getPhaseChangedResponse(wheelRange, timerCache, gameCache, playersCache)
      case (t, _) => timerCache.decreaseTime().as(TimerNotification(t))
    }
  }

}
