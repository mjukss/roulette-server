package com.example.roulette.response

import io.circe.{Decoder, Encoder}

sealed trait BadRequestMessage

object BadRequestMessage {
  case object UsernameTaken extends BadRequestMessage
  case object RequestIsInvalid extends BadRequestMessage
  case object WrongPassword extends BadRequestMessage
  case object UserAlreadyPlaying extends BadRequestMessage
  case object UsernameDoesNotExist extends BadRequestMessage
  case object InsufficientFunds extends BadRequestMessage
  case object CanNotPlaceBetInThisGamePhase extends BadRequestMessage
  case object InvalidBet extends BadRequestMessage
  case class CustomBadRequestMessage(message: String) extends BadRequestMessage


  implicit lazy val encodeBadMessage: Encoder[BadRequestMessage] = Encoder.encodeString.contramap[BadRequestMessage] {
    case CustomBadRequestMessage(message) => message
    case other => other.toString
  }
  implicit lazy val decodeBadMessage: Decoder[BadRequestMessage] = Decoder.decodeString.map {
    case "InsufficientFunds" => InsufficientFunds
    case "UsernameTaken" => UsernameTaken
    case "UsernameDoesNotExist" => UsernameDoesNotExist
    case other => CustomBadRequestMessage(other)
  }
}