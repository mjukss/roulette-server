package com.example.roulette.player

import cats.Applicative
import cats.effect.Ref
import cats.implicits.toFunctorOps
import com.example.roulette.player.Player.Username

class PlayerUsernameCache[F[_]](data: Ref[F, Option[Username]]) {
  def updateAndGet(username: Username): F[Username] = data.modify(_ => (Some(username), username))
  def read: F[Option[Username]] = data.get
}

object PlayerUsernameCache {
  def apply[F[_] : Ref.Make : Applicative](): F[PlayerUsernameCache[F]] =
    Ref.of(Option.empty[Username]).map(new PlayerUsernameCache[F](_))
}