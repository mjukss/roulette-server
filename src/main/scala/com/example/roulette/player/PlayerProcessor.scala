package com.example.roulette.player

import cats.Monad
import cats.implicits.catsSyntaxSemigroup
import com.example.roulette.bet.BetProcessor.getWinnings
import com.example.roulette.game.GamePhase
import com.example.roulette.game.GamePhase.{BetsClosed, BetsOpen}
import com.example.roulette.response.Response.LuckyNumber

object PlayerProcessor {
  private val playerAfterResult: LuckyNumber => ((String, Player)) => (String, Player) = luckyNumber => kv => {
    val (key, player) = kv
    val Player(username, balance, _, betsOption) = player

    betsOption.map { bets =>
      key -> Player(username, balance |+| getWinnings(luckyNumber, bets))
    } getOrElse key -> player
  }

  def getPlayersAfterPhase[F[_] : Monad](gamePhase: GamePhase,
                                         luckyNumber: LuckyNumber,
                                         playersCache: PlayersCache[F]): F[Map[String, Player]] = {
    import cats.implicits.{toFlatMapOps, toFunctorOps}

    gamePhase match {
      case BetsOpen => playersCache.readAll
      case BetsClosed => for {
        players <- playersCache.readAll
        newPlayers = players.map(playerAfterResult(luckyNumber))
        result <- playersCache.updateAndGet(newPlayers)
      } yield result
    }
  }

}
