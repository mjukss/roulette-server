package com.example.roulette.game

import cats.Applicative
import cats.effect.Ref
import cats.implicits.toFunctorOps
import com.example.roulette.game.GamePhase.BetsOpen

class GameCache[F[_]](data: Ref[F, GamePhase]) {
  import GamePhase._
  def read: F[GamePhase] = data.get
  def changePhaseAndGet(): F[GamePhase] = data.updateAndGet {
    case BetsOpen => BetsClosed
    case BetsClosed => BetsOpen
  }
}

object GameCache {
  def apply[F[_] : Ref.Make : Applicative](): F[GameCache[F]] = Ref.of(BetsOpen: GamePhase).map(new GameCache(_))
}