package com.example.roulette.timer

import cats.Applicative
import cats.effect.Ref
import cats.implicits.toFunctorOps

class TimerCache[F[_]](data: Ref[F, Timer]) {
  def read: F[Timer]          = data.get
  def resetTimer(): F[Unit]   = data.update(_.copy(15))
  def decreaseTime(): F[Unit] = data.update(t => t.copy(t.value - 1))
}

object TimerCache {
  def apply[F[_]: Ref.Make: Applicative](): F[TimerCache[F]] = Ref.of(Timer(15)).map(new TimerCache(_))
}
