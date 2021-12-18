package com.example.roulette.player

import com.example.roulette.bet.Bet
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.player.Player.{Password, Username}
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.{Codec, Decoder, Encoder}

final case class Player(
                         username: Username,
                         password: Password,
                         isOnline: Boolean = false,
                         balance: Chips = Chips(200),
                         chipsPlaced: Chips = Chips(0),
                         bets: Option[List[Bet]] = None,
                       )

object Player {
  implicit val decodeUser: Decoder[Player] = Decoder.forProduct6(
    "username",
    "password",
    "isOnline",
    "balance",
    "chipsPlaced",
    "bets")(Player.apply)

  implicit val encodeUser: Encoder[Player] =
    Encoder.forProduct4("username", "balance", "chipsPlaced", "bets") {
      case Player(username, _, _, balance, chipsPlaced, bets) => (username, balance, chipsPlaced, bets)
    }


  final case class Username(value: String) extends AnyVal

  object Username {
    implicit val usernameCodec: Codec[Username] = deriveUnwrappedCodec[Username]
  }

  final case class Password(value: String) extends AnyVal

  object Password {
    implicit val passwordCodec: Codec[Password] = deriveUnwrappedCodec[Password]
  }
}