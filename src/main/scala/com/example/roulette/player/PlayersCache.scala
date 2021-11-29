package com.example.roulette.player

import cats.Functor
import cats.effect.{Async, Ref}
import com.example.roulette.player.Player.Username
import cats.implicits.toFunctorOps

class PlayersCache[F[_] : Functor](data: Ref[F, Map[String, Player]]) {
    def updateOne(player: Player): F[Unit] = data.update(_.updated(player.username.value, player))
    def removeOne(username: Username): F[Unit] = data.update(_.removed(username.value))
    def readOne(username: Username): F[Option[Player]] = data.get.map(_.get(username.value))
    def readAll: F[Map[String, Player]] = data.get
    def updateAndGet(players: Map[String, Player]): F[Map[String, Player]] =
      data.updateAndGet(_ => players)
  }

  object PlayersCache {
    def apply[F[_] : Async](): F[PlayersCache[F]] = Ref.of(Map.empty[String, Player]).map(new PlayersCache(_))
  }