package com.example.roulette.response

import com.example.roulette.player.Player.Username
import com.example.roulette.response.BadRequestMessage.UsernameTaken
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame.Text

object ResponseProcessor {
  import Response._


  val toWebSocketText: Username => PartialFunction[Response, Text] = username => {
      case res@BadRequest(resUsername, msg)
        if (resUsername == username) && msg != UsernameTaken => textFromResponse(res)
      case res@BetPlaced(_, resUsername, _) if resUsername != username => textFromResponse(res.copy(bet = None))
      case res@PlayerRegistered(player, _, _)
        if player.username != username => textFromResponse(res.copy(gamePhase = None, players = None))
      case res: Response if !res.isInstanceOf[BadRequest] => textFromResponse(res)
  }

  private def textFromResponse(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)
}
