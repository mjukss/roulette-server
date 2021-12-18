package com.example.roulette.request

import com.example.roulette.bet.Bet
import com.example.roulette.player.Player.{Password, Username}
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.parser.decode

@ConfiguredJsonCodec sealed trait Request
object Request {
  final case class PlaceBet(bet: Bet) extends Request
  final case object ClearBets extends Request
  final case class JoinGame(username: Username, password: Password) extends Request
  final case object ExitGame extends Request
  final case class InvalidRequest(errorMessage: String) extends Request
  @ConfiguredJsonCodec final case class RegisterPlayer(username: Username, password: Password) extends Request

  implicit val requestConfig: Configuration =
    Configuration.default.withDiscriminator("requestType")

  def fromString(string: String): Request = decode[Request](string) match {
    case Left(error) =>  InvalidRequest(error.getMessage)
    case Right(request) => request
  }

}