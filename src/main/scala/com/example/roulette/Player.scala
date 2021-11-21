package com.example.roulette

import com.example.roulette.Bet.Chips
import com.example.roulette.Player.Username
import io.circe.Codec
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

@JsonCodec final case class Player(username: Username, balance: Chips = Chips(200), betPlaced: Chips = Chips(0))

object Player {
  final case class Username(value: String) extends AnyVal

  object Username {
    implicit val customUsernameEncoder: Codec[Username] = deriveUnwrappedCodec[Username]
  }
}