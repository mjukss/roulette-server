package com.example.roulette.player

import com.example.roulette.bet.Bet
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.player.Player.Username
import io.circe.Codec
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

@JsonCodec final case class Player(username: Username,
                                   balance: Chips = Chips(200),
                                   chipsPlaced: Chips = Chips(0),
                                   bets: Option[List[Bet]] = None)

object Player {
  final case class Username(value: String) extends AnyVal
  object Username {
    implicit val usernameCodec: Codec[Username] = deriveUnwrappedCodec[Username]
  }
}