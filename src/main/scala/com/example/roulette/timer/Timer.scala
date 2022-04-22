package com.example.roulette.timer

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

final case class Timer(value: Int) extends AnyVal
object Timer {
  implicit val timerCodec: Codec[Timer] = deriveUnwrappedCodec[Timer]
}
