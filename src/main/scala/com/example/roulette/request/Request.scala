package com.example.roulette.request

import cats.effect.kernel.Concurrent
import com.example.roulette.bet.Bet
import com.example.roulette.player.Player.{ Password, Username }
import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import io.circe.parser.decode
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@ConfiguredJsonCodec sealed trait Request
object Request {
  final case class PlaceBet(bet: Bet)                                                          extends Request
  final case object ClearBets                                                                  extends Request
  final case class JoinGame(username: Username, password: Password)                            extends Request
  final case object ExitGame                                                                   extends Request
  final case class InvalidRequest(errorMessage: String)                                        extends Request
  @ConfiguredJsonCodec final case class RegisterPlayer(username: Username, password: Password) extends Request
  @ConfiguredJsonCodec final case class RemovePlayer(username: Username, password: Password)   extends Request

  implicit def registerEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, RegisterPlayer]   = jsonOf[F, RegisterPlayer]
  implicit def removePlayerEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, RemovePlayer] = jsonOf[F, RemovePlayer]

  implicit val requestConfig: Configuration =
    Configuration.default.withDiscriminator("requestType")

  def fromString(string: String): Request = decode[Request](string) match {
    case Left(error)    => InvalidRequest(error.getMessage)
    case Right(request) => request
  }

}
