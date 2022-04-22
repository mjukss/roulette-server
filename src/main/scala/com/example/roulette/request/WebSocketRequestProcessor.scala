package com.example.roulette.request

import cats.data.OptionT
import cats.data.Validated.{ Invalid, Valid }
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.implicits._
import cats.{ Applicative, Functor, Monad }
import com.example.roulette.bet.Bet
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.bet.BetValidator.validate
import com.example.roulette.game.{ GameCache, GamePhase }
import com.example.roulette.player.PlayerProcessor.getActivePlayers
import com.example.roulette.player.{ AuthCache, Player, PlayersCache }
import com.example.roulette.request.Request._
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response._
import com.example.roulette.response.{ BadRequestMessage, Response }
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{ Close, Text }

object WebSocketRequestProcessor {

  def processWSRequest[F[+_]: Concurrent](
      privateTopic: Topic[F, WebSocketFrame],
      stream: Stream[F, WebSocketFrame],
      playersCache: PlayersCache[F],
      gameCache: GameCache[F],
      authCache: AuthCache[F],
      queue: Queue[F, Option[Response]]
  ): Stream[F, Unit] = {
    val requestStream = webSocketFrameToRequest(stream)
    val playerOption  = OptionT(authCache.read).flatMap(username => OptionT(playersCache.readOne(username))).value

    requestStream
      .evalMapFilter { request =>
        (playerOption, gameCache.read, request.pure[F]).mapN {
          case (_, phase, request: JoinGame)        => joinGame(privateTopic, phase, request, authCache, playersCache)
          case (Some(player), phase, PlaceBet(bet)) => placeBet(privateTopic, player, phase, bet, playersCache)
          case (Some(player), _, ClearBets)         => clearBets(player, playersCache).map(Option.apply)
          case (Some(player), _, ExitGame)          => exitGame(player, playersCache).map(Option.apply)
          case (_, _, InvalidRequest(errorMessage)) =>
            invalidRequest(privateTopic, CustomBadRequestMessage(errorMessage))
          case _ => invalidRequest(privateTopic, RequestIsInvalid)
        }.flatten
      }
      .enqueueNoneTerminated(queue)
  }

  private def joinGame[F[_]: Monad](
      privateTopic: Topic[F, WebSocketFrame],
      gamePhase: GamePhase,
      request: JoinGame,
      usernameCache: AuthCache[F],
      playersCache: PlayersCache[F]
  ): F[Option[Response]] = {
    val JoinGame(username, password) = request

    def sendUserAlreadyPlayingResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(UserAlreadyPlaying)).as(None)

    def sendWrongPasswordResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(WrongPassword)).as(None)

    def sendUsernameDoesNotExistResponse(): F[Option[Response]] =
      sendPrivateResponse(privateTopic, BadRequest(UsernameDoesNotExist)).as(None)

    playersCache.readOne(username).flatMap {
      case None                                        => sendUsernameDoesNotExistResponse()
      case Some(player) if player.password != password => sendWrongPasswordResponse()
      case Some(player) if player.isOnline             => sendUserAlreadyPlayingResponse()
      case Some(player) =>
        for {
          _       <- usernameCache.updateAndGet(username)
          _       <- playersCache.updateOne(player.copy(isOnline = true))
          players <- playersCache.readAll
          _       <- sendPrivateResponse(privateTopic, WelcomeToGame(player, gamePhase, getActivePlayers(players)))
        } yield Option(PlayerJoinedGame(player))
    }
  }

  private def placeBet[F[+_]: Monad](
      privateTopic: Topic[F, WebSocketFrame],
      player: Player,
      gamePhase: GamePhase,
      bet: Bet,
      playersCache: PlayersCache[F]
  ): F[Option[Response]] =
    validate(bet, player, gamePhase) match {
      case Valid(updatedPlayer) => acceptBet(bet, playersCache, updatedPlayer)
      case Invalid(msg) => rejectBet(privateTopic, msg)
    }
  private def acceptBet[F[+_]: Monad](
      bet: Bet,
      playersCache: PlayersCache[F],
      player: Player
  ): F[Option[Response]] =
    playersCache.updateOne(player) as BetPlaced(player.chipsPlaced, player.username, Some(bet)).some

  private def rejectBet[F[+_]: Monad](
      privateTopic: Topic[F, WebSocketFrame],
      msg: BadRequestMessage
  ): F[Option[Response]] = sendPrivateResponse(privateTopic, BadRequest(msg)) as None

  private def invalidRequest[F[_]: Applicative](privateTopic: Topic[F, WebSocketFrame], badReqMsg: BadRequestMessage) =
    for {
      _ <- sendPrivateResponse(privateTopic, BadRequest(badReqMsg))
    } yield Option.empty[Response]

  private def clearBets[F[_]: Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer =
      player.copy(balance = player.chipsPlaced |+| player.balance, chipsPlaced = Chips(0), bets = None)
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield BetsCleared(player.username)
  }

  private def exitGame[F[_]: Monad](player: Player, playersCache: PlayersCache[F]): F[Response] =
    for {
      _ <- playersCache.updateOne(player.copy(isOnline = false))
    } yield PlayerLeftGame(player.username)

  private def sendPrivateResponse[F[_]: Functor](privateTopic: Topic[F, WebSocketFrame], response: Response) =
    privateTopic.publish1(responseToWebSocketText(response)).void

  private def responseToWebSocketText(response: Response): Text = Text(response.asJson.deepDropNullValues.noSpaces)

  private def webSocketFrameToRequest[F[_]](stream: Stream[F, WebSocketFrame]) = {
    stream.collect {
      case Text(text, _) => Request.fromString(text)
      case Close(_)      => ExitGame
    }
  }
}
