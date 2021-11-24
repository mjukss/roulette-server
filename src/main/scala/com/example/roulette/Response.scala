package com.example.roulette

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Random
import com.example.roulette.Cache.{GameCache, TimerCache}
import com.example.roulette.GamePhase.{BetsClosed, BetsOpen}
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}


@ConfiguredJsonCodec sealed trait Response

object Response {
  final case class BetPlaced(bet: Bet) extends Response
  final case object BetsCleared extends Response
  final case class PlayerRegistered(player: Player, gamePhase: GamePhase) extends Response
  final case class PlayerRemoved(player: Player) extends Response
  final case class BadRequest(message: BadRequestMessage) extends Response
  final case class TimerNotification(secTillNextPhase: Timer) extends Response
  final case class PhaseChange(gamePhase: GamePhase, players: List[Player], luckyNumber: Option[LuckyNumber]) extends Response


  final case class Timer(value: Int) extends AnyVal
  object Timer {
    implicit val timerCodec: Codec[Timer] = deriveUnwrappedCodec[Timer]
  }
  final case class LuckyNumber(value: Int) extends AnyVal

  object LuckyNumber {
    implicit val luckyNumberCodec: Codec[LuckyNumber] = deriveUnwrappedCodec[LuckyNumber]
  }
  implicit val genDevConfig: Configuration = {
    Configuration.default.withDiscriminator("responseType")
  }


  def fromTimerNotification[F[_] : Monad : Sync](timerCache: TimerCache[F], gameCache: GameCache[F], playersIo: F[Map[String, Player]]): F[Response] = {
    import cats.implicits._
    val timerAndLuckyNumber: F[(Timer, GamePhase, LuckyNumber, List[Player])] = for {
      randomNumber <- Random.scalaUtilRandom[F]
      num <- randomNumber.betweenInt(0, 37)
      gamePhase <- gameCache.read
      players <- playersIo
      timer <- timerCache.read
    } yield (timer, gamePhase, LuckyNumber(num), players.values.toList)

    def changeGamePhase(luckyNumber: Option[LuckyNumber], players: List[Player]): F[Response] = {
      timerCache.resetTimer() *>  gameCache.changePhaseAndGet().map(PhaseChange(_, players, luckyNumber))
    }

    timerAndLuckyNumber.flatMap {
      case (Timer(time), BetsOpen, luckyNumber, p) if time < 1 => changeGamePhase(Some(luckyNumber), p)
      case (Timer(time), BetsClosed, _, p) if time < 1 => changeGamePhase(None, p)
      case (t, _, _, _) => timerCache.decreaseTime().as(TimerNotification(t))
    }
  }

  def fromLuckyNumber[F[_]](): F[Response] = ???
}


