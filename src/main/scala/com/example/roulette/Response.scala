package com.example.roulette

import cats.effect.IO
import cats.effect.std.Random
import com.example.roulette.Cache.TimerCache
import com.example.roulette.Request.RequestOrError
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}


@ConfiguredJsonCodec sealed trait Response

object Response {
  final case class BetPlaced(players: List[Player]) extends Response
  final case class BetsCleared(players: List[Player]) extends Response
  final case class PlayerRegistered(players: List[Player]) extends Response
  final case class PlayerRemoved(players: List[Player]) extends Response
  final case class TimerNotification(time: Timer) extends Response
  final case class Result(luckyNumber: LuckyNumber) extends Response
  final case object BadRequest extends Response

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

  def fromTimerNotification(timerCache: TimerCache): IO[Response] = {
    val timerAndLuckyNumber: IO[(Timer, LuckyNumber)] = for {
      randomNumber <- Random.scalaUtilRandom[IO]
      num <- randomNumber.betweenInt(0, 37)
      timer <- timerCache.getTime
    } yield (timer, LuckyNumber(num))

    timerAndLuckyNumber.flatMap {
      case (Timer(0), luckyNumber) => timerCache.resetTimer().as(Result(luckyNumber))
      case (timer, _) => timerCache.decreaseTime().as(TimerNotification(timer))
    }
  }

  // TODO: need to implement players cache to finish this
  def fromRawRequest(requestOrError: RequestOrError): Response = requestOrError match {
    case Left(_) => BadRequest
    case Right(request) => request match {
      case Request.PlaceBet(_, _) => BetPlaced(Nil)
      case Request.ClearBets(_) => BetsCleared(Nil)
      case Request.RegisterPlayer(_) => PlayerRegistered(Nil)
      case Request.RemovePlayer(_) => PlayerRemoved(Nil)
    }
  }
}


