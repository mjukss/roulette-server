package com.example.roulette.response

import cats.Monad
import cats.data.OptionT
import com.example.roulette.player.Player.Username
import com.example.roulette.player.{ AuthCache, PlayersCache }
import com.example.roulette.response.Response.{ BetPlaced, PlayerJoinedGame }
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame.Text

object ResponseProcessor {
  def getFilteredResponse[F[_]: Monad](
      response: Response,
      authCache: AuthCache[F],
      playersCache: PlayersCache[F]
  ): F[Option[Text]] = {
    (for {
      username     <- OptionT(authCache.read)
      _            <- OptionT(playersCache.readOne(username))
      textResponse <- OptionT.fromOption(responseToText(username, response))
    } yield textResponse).value
  }

  def responseToText(username: Username, response: Response): Option[Text] = {
    response match {
      case response: PlayerJoinedGame if response.player.username == username => None
      case response: BetPlaced if response.username != username               => Some(textFromResponse(response.copy(bet = None)))
      case response                                                           => Some(textFromResponse(response))
    }
  }

  private def textFromResponse(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)
}
