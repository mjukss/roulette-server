package com.example.roulette

import cats.effect.{IO, Ref}
import com.example.roulette.Player.Username
import com.example.roulette.Response.Timer

object Cache {
  class PlayersCache(data: Ref[IO, Map[String, Player]]) {
    def addOne(username: Username, player: Player): IO[Unit] = data.update(_.updated(username.value, player))
    def removeOne(username: Username): IO[Unit] = data.update(_.removed(username.value))
    def readOne(username: Username): IO[Option[Player]] = data.get.map(_.get(username.value))
    def readAll: IO[Map[String, Player]] = data.get
  }

  object PlayersCache {
    def apply(): IO[PlayersCache] = IO.ref(Map.empty[String, Player]).map(new PlayersCache(_))
  }

  class TimerCache(data: Ref[IO, Timer]) {
    def getTime: IO[Timer] = data.get
    def resetTimer(): IO[Unit] = data.update(_.copy(15))
    def decreaseTime(): IO[Unit] = data.update(t => t.copy(t.value - 1))
  }

  object TimerCache {
    def apply(): IO[TimerCache] = IO.ref(Timer(0)).map(new TimerCache(_))
  }
}
