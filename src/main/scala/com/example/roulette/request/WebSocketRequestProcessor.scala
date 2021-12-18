package com.example.roulette.request

import cats.data.OptionT
import cats.data.Validated.{Invalid, Valid}
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup, catsSyntaxTuple3Semigroupal, toFlatMapOps, toFunctorOps}
import cats.{Applicative, Functor, Monad}
import com.example.roulette.bet.Bet
import com.example.roulette.bet.BetValidator.validateBet
import com.example.roulette.game.{GameCache, GamePhase}
import com.example.roulette.player.PlayerProcessor.getActivePlayers
import com.example.roulette.player.{Player, PlayerUsernameCache, PlayersCache}
import com.example.roulette.request.Request._
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response._
import com.example.roulette.response.{BadRequestMessage, Response}
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object WebSocketRequestProcessor {

  def processWSRequest[F[_] : Concurrent](privateTopic: Topic[F, WebSocketFrame],
                                          stream: Stream[F, WebSocketFrame],
                                          playersCache: PlayersCache[F],
                                          gameCache: GameCache[F],
                                          usernameCache: PlayerUsernameCache[F],
                                          queue: Queue[F, Option[Response]]): Stream[F, Unit] = {
    val requestStream = webSocketFrameToRequest(stream)
    val playerOption = OptionT(usernameCache.read).flatMap(username => OptionT(playersCache.readOne(username))).value

    requestStream.evalMapFilter { request =>
      (playerOption, gameCache.read, request.pure[F]).mapN {
        case (_, phase, request: JoinGame) => joinGame(privateTopic, phase, request, usernameCache, playersCache)
        case (Some(player), phase, PlaceBet(bet)) => placeBet(privateTopic, player, phase, bet, playersCache)
        case (Some(player), _, ClearBets) => clearBets(player, playersCache).map(Option.apply)
        case (Some(player), _, ExitGame) => exitGame(player, playersCache).map(Option.apply)
        case (_, _, InvalidRequest(errorMessage)) => invalidRequest(privateTopic, CustomBadRequestMessage(errorMessage))
        case _ => invalidRequest(privateTopic, RequestIsInvalid)
      }.flatten
    }.enqueueNoneTerminated(queue)
  }

  private def joinGame[F[_] : Monad](privateTopic: Topic[F, WebSocketFrame],
                                     gamePhase: GamePhase,
                                     request: JoinGame,
                                     usernameCache: PlayerUsernameCache[F],
                                     playersCache: PlayersCache[F]): F[Option[Response]] = {
    val JoinGame(username, password) = request

    def sendUserAlreadyPlayingResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(UserAlreadyPlaying)).as(None)

    def sendWrongPasswordResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(WrongPassword)).as(None)

    def sendUsernameDoesNotExistResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(UsernameDoesNotExist)).as(None)

    playersCache.readOne(username).flatMap {
      case None => sendUsernameDoesNotExistResponse()
      case Some(player) if player.password != password => sendWrongPasswordResponse()
      case Some(player) if player.isOnline => sendUserAlreadyPlayingResponse()
      case Some(player) => for {
        _ <- usernameCache.updateAndGet(username)
        _ <- playersCache.updateOne(player.copy(isOnline = true))
        players <- playersCache.readAll
        _ <- sendPrivateResponse(privateTopic, WelcomeToGame(player, gamePhase, getActivePlayers(players)))
      } yield Option(PlayerJoinedGame(player))
    }
  }

  private def placeBet[F[_] : Monad](privateTopic: Topic[F, WebSocketFrame],
                                     player: Player, gamePhase:
                                     GamePhase, bet: Bet,
                                     playersCache: PlayersCache[F]): F[Option[Response]] =
    validateBet(bet, player, gamePhase) match {
      case Valid(updatedPlayer@Player(username, _, _, _, chipsPlaced, _)) =>
        playersCache.updateOne(updatedPlayer).as(Option(BetPlaced(chipsPlaced, username)))
      case Invalid(msg) => sendPrivateResponse(privateTopic, BadRequest(msg)).as(Option.empty[Response])
    }

  private def invalidRequest[F[_] : Applicative](privateTopic: Topic[F, WebSocketFrame], badReqMsg: BadRequestMessage) = for {
    _ <- sendPrivateResponse(privateTopic, BadRequest(badReqMsg))
  } yield Option.empty[Response]

  private def clearBets[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer = player.copy(balance = player.chipsPlaced |+| player.balance)
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield BetsCleared(player.username)
  }

  private def exitGame[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = for {
    _ <- playersCache.updateOne(player.copy(isOnline = false))
  } yield PlayerLeftGame(player.username)

  private def sendPrivateResponse[F[_] : Functor](privateTopic: Topic[F, WebSocketFrame], response: Response) =
    privateTopic.publish1(responseToWebSocketText(response)).void

  private def responseToWebSocketText(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)

  private def webSocketFrameToRequest[F[_]](stream: Stream[F, WebSocketFrame]) = {
    stream.collect {
      case Text(text, _) => Request.fromString(text)
      case Close(_) => ExitGame
    }
  }
}
