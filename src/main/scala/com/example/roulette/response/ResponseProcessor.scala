package com.example.roulette.response

import cats.Monad
import cats.data.OptionT
import cats.implicits.toFunctorOps
import com.example.roulette.player.Player.Username
import com.example.roulette.player.{PlayerUsernameCache, PlayersCache}
import com.example.roulette.response.BadRequestMessage.UsernameTaken
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame.Text

object ResponseProcessor {

  import Response._


  def getFilteredResponse[F[_] : Monad](
                                         response: Response,
                                         usernameCache: PlayerUsernameCache[F],
                                         playersCache: PlayersCache[F]
                                       ): F[Option[Text]] = {
    response match {
      case response: Response.BadRequest => usernameCache.read.map(_.map(username => toWebSocketText(username)(response)))
      case response => (for {
        username <- OptionT(usernameCache.read)
        player <- OptionT(playersCache.readOne(username))
        res <- OptionT.pure[F](toWebSocketText(player.username)(response))
      } yield res).value
    }
  }


  val toWebSocketText: Username => PartialFunction[Response, Text] = username => {
    case res@BadRequest(resUsername, msg)
      if (resUsername == username) && msg != UsernameTaken => textFromResponse(res)
    case res@BetPlaced(_, resUsername, _) if resUsername != username => textFromResponse(res.copy(bet = None))
    case res@PlayerJoinedGame(player, _, _)
      if player.username != username => textFromResponse(res.copy(gamePhase = None, players = None))
    case res: Response if !res.isInstanceOf[BadRequest] => textFromResponse(res)
  }

  def textFromResponse(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)
}
