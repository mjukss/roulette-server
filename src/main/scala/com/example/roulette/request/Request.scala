package com.example.roulette.request

import com.example.roulette.bet.Bet
import io.circe
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.parser.decode

@ConfiguredJsonCodec sealed trait Request
object Request {
  final case class PlaceBet(bet: Bet) extends Request
  final case object ClearBets extends Request
  final case object RegisterPlayer extends Request
  final case object RemovePlayer extends Request

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("requestType")

  type RequestOrError = Either[circe.Error, Request]

  def fromString(string: String): RequestOrError = decode[Request](string)

}