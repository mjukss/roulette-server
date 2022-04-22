package com.example.roulette.player

import cats.Monad
import cats.implicits.catsSyntaxSemigroup
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.bet.BetProcessor.getWinnings
import com.example.roulette.game.GamePhase
import com.example.roulette.game.GamePhase.{ BetsClosed, BetsOpen }
import com.example.roulette.response.Response.LuckyNumber

object PlayerProcessor {
  private val playerAfterResult: LuckyNumber => ((String, Player)) => (String, Player) = luckyNumber =>
    kv => {
      val (key, player)                                                          = kv
      val Player(username, password, isOnline, balance, chipsPlaced, betsOption) = player

      betsOption.map { bets =>
        if (!isOnline) key -> player.copy(chipsPlaced = Chips(0), balance = balance |+| chipsPlaced, bets = None)
        else key           -> Player(username, password, isOnline, balance |+| getWinnings(luckyNumber, bets))
      } getOrElse key -> player
    }

  def getPlayersAfterPhase[F[_]: Monad](
      gamePhase: GamePhase,
      luckyNumber: LuckyNumber,
      playersCache: PlayersCache[F]
  ): F[Map[String, Player]] = {
    import cats.implicits.{ toFlatMapOps, toFunctorOps }

    gamePhase match {
      case BetsOpen => playersCache.readAll
      case BetsClosed =>
        for {
          players <- playersCache.readAll
          newPlayers = players.map(playerAfterResult(luckyNumber))
          result <- playersCache.updateAndGet(newPlayers)
        } yield result
    }
  }

  def getActivePlayers(players: Map[String, Player]): List[Player] = {
    players.values.filter(_.isOnline).toList
  }
}
