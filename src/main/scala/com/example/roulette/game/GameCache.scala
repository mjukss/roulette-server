package com.example.roulette.game

import cats.effect.{Async, Ref}
import com.example.roulette.game.GamePhase.BetsOpen
import cats.implicits.toFunctorOps

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