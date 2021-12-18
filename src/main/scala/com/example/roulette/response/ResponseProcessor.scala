package com.example.roulette.response

import cats.Monad
import cats.data.OptionT
import com.example.roulette.player.Player.Username
import com.example.roulette.player.{PlayerUsernameCache, PlayersCache}
import com.example.roulette.response.Response.PlayerJoinedGame
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame.Text

object ResponseProcessor {
  def getFilteredResponse[F[_] : Monad](response: Response,
                                        usernameCache: PlayerUsernameCache[F],
                                        playersCache: PlayersCache[F]): F[Option[Text]] = {
    (for {
      username <- OptionT(usernameCache.read)
      _ <- OptionT(playersCache.readOne(username))
      textResponse <- OptionT.fromOption(responseToText(username, response))
    } yield textResponse).value
  }

  def responseToText(username: Username, response: Response): Option[Text] = {
    response match {
      case response: PlayerJoinedGame if response.player.username == username => None
      case response => Some(textFromResponse(response))
    }
  }

  private def textFromResponse(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)
}
