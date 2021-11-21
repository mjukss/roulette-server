package com.example.roulette

import com.example.roulette.Player.Username
import io.circe
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.parser.decode

@ConfiguredJsonCodec sealed trait Request {
  val username: Username
}

object Request {
  final case class PlaceBet(username: Username, bet: Bet) extends Request
  final case class ClearBets(username: Username) extends Request
  final case class RegisterPlayer(username: Username) extends Request
  final case class RemovePlayer(username: Username) extends Request

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("requestType")

  def fromString(string: String): Either[circe.Error, Request] = decode[Request](string)

}