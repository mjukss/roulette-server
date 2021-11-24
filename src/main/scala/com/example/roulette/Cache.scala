package com.example.roulette

import cats.Functor
import cats.effect.{Async, Ref}
import cats.implicits.toFunctorOps
import com.example.roulette.GamePhase.BetsOpen
import com.example.roulette.Player.Username
import com.example.roulette.Response.Timer

object Cache {
  class PlayersCache[F[_] : Functor](data: Ref[F, Map[String, Player]]) {
    def update(player: Player): F[Unit] = data.update(_.updated(player.username.value, player))
    def removeOne(username: Username): F[Unit] = data.update(_.removed(username.value))
    def readOne(username: Username): F[Option[Player]] = data.get.map(_.get(username.value))
    def readAll: F[Map[String, Player]] = data.get
  }

  object PlayersCache {
    def apply[F[_] : Async](): F[PlayersCache[F]] = Ref.of(Map.empty[String, Player]).map(new PlayersCache(_))
  }

  class TimerCache[F[_]](data: Ref[F, Timer]) {
    def read: F[Timer] = data.get
    def resetTimer(): F[Unit] = data.update(_.copy(15))
    def decreaseTime(): F[Unit] = data.update(t => t.copy(t.value - 1))
  }

  object TimerCache {
    def apply[F[_] : Async](): F[TimerCache[F]] = Ref.of(Timer(15)).map(new TimerCache(_))
  }

  class GameCache[F[_]](data: Ref[F, GamePhase]) {
    import GamePhase._
    def read: F[GamePhase] = data.get
    def changePhaseAndGet(): F[GamePhase] = data.updateAndGet {
      case BetsOpen => BetsClosed
      case BetsClosed => BetsOpen
    }
  }

  object GameCache {
    def apply[F[_]: Async](): F[GameCache[F]] = Ref.of(BetsOpen : GamePhase).map(new GameCache(_))
  }
}
