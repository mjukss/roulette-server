package com.example.roulette.game

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait GamePhase

object GamePhase {
  case object BetsOpen extends GamePhase
  case object BetsClosed extends GamePhase

  implicit val phaseCodec: Codec[GamePhase] = deriveEnumerationCodec[GamePhase]
}
