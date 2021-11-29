package com.example.roulette.timer

import cats.effect.{Async, Ref}
import cats.implicits.toFunctorOps

class TimerCache[F[_]](data: Ref[F, Timer]) {
    def read: F[Timer] = data.get
    def resetTimer(): F[Unit] = data.update(_.copy(15))
    def decreaseTime(): F[Unit] = data.update(t => t.copy(t.value - 1))
  }

  object TimerCache {
    def apply[F[_] : Async](): F[TimerCache[F]] = Ref.of(Timer(15)).map(new TimerCache(_))
  }