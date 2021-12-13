package com.example.roulette.game

import cats.effect.kernel.Sync
import cats.effect.std.Random
import com.example.roulette.game.GamePhase.BetsClosed
import com.example.roulette.player.PlayerProcessor.{getActivePlayers, getPlayersAfterPhase}
import com.example.roulette.player.PlayersCache
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{LuckyNumber, PhaseChanged}
import com.example.roulette.timer.TimerCache
import com.example.roulette.wheel.WheelRange

object GamePhaseProcessor {
  def getPhaseChangedResponse[F[_] : Sync](wheelRange: WheelRange,
                                           timerCache: TimerCache[F],
                                           gameCache: GameCache[F],
                                           playersCache: PlayersCache[F]): F[Response] = {
    import cats.implicits._
    for {
      _ <- timerCache.resetTimer()
      gamePhase <- gameCache.changePhaseAndGet()
      randomNumber <- Random.scalaUtilRandom[F]
      num <- randomNumber.betweenInt(wheelRange.from, wheelRange.to)
      luckyNumber = LuckyNumber(num)
      players <- getPlayersAfterPhase(gamePhase, luckyNumber, playersCache)
      luckyNumberOption = Option.when(gamePhase == BetsClosed)(luckyNumber)
    } yield PhaseChanged(gamePhase, getActivePlayers(players), luckyNumberOption)
  }


}
