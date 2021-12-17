package com.example.roulette.request

import cats.Monad
import cats.data.OptionT
import cats.data.Validated.{Invalid, Valid}
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup, toFlatMapOps, toFunctorOps}
import com.example.roulette.bet.Bet
import com.example.roulette.bet.BetValidator.validateBet
import com.example.roulette.game.{GameCache, GamePhase}
import com.example.roulette.player.PlayerProcessor.getActivePlayers
import com.example.roulette.player.{Player, PlayerUsernameCache, PlayersCache}
import com.example.roulette.request.Request._
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response
import com.example.roulette.response.Response._
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.syntax.EncoderOps
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object RequestProcessor {

  def processFromClient[F[_] : Concurrent](
                                            privateTopic: Topic[F, WebSocketFrame],
                                            stream: Stream[F, WebSocketFrame],
                                            playersCache: PlayersCache[F],
                                            gameCache: GameCache[F],
                                            usernameCache: PlayerUsernameCache[F],
                                            queue: Queue[F, Option[Response]]
                                          ): Stream[F, Unit] = {

    val playerOptionT = OptionT(usernameCache.read).flatMap(username => OptionT(playersCache.readOne(username)))
    val requestStream = webSocketFrameToRequest(stream)

    def handleRegisteredUser(request: Request, phase: GamePhase) = {
      playerOptionT.flatMap { player =>
        OptionT {
          request match {
            case PlaceBet(bet) => placeBet(privateTopic, player, phase, bet, playersCache)
            case ClearBets => clearBets(player, playersCache).map(Option.apply)
            case ExitGame => exitGame(player, playersCache).map(Option.apply)
            case _: RegisterPlayer => for {
              _ <- sendPrivateResponse(privateTopic, BadRequest(CustomBadRequestMessage("Invalid request")))
            } yield Option.empty[Response]
            case _ => Option.empty[Response].pure[F]
          }
        }
      }.value
    }

    requestStream.evalMapFilter { request =>
      gameCache.read.flatMap { phase =>
        request match {
          case request: JoinGame => handleJoinGameReq(privateTopic, phase, request, usernameCache, playersCache)
          case InvalidRequest(errorMessage) =>
            for {
              _ <- sendPrivateResponse(privateTopic, BadRequest(CustomBadRequestMessage(errorMessage)))
            } yield Option.empty[Response]

          case request => handleRegisteredUser(request, phase)
        }
      }
    }.enqueueNoneTerminated(queue)
  }

  private def handleJoinGameReq[F[_] : Monad](privateTopic: Topic[F, WebSocketFrame], gamePhase: GamePhase, request: JoinGame, usernameCache: PlayerUsernameCache[F], playersCache: PlayersCache[F]): F[Option[Response]] = {
    playersCache.readOne(request.username).flatMap {
      case Some(player) if player.password != request.password => sendPrivateResponse(privateTopic, BadRequest(WrongPassword)).as(Option.empty[Response])
      case None => sendPrivateResponse(privateTopic, BadRequest(UsernameDoesNotExist)).as(Option.empty[Response])
      case Some(player) if player.isOnline => sendPrivateResponse(privateTopic, BadRequest(UserAlreadyPlaying)).as(Option.empty[Response])
      case Some(player) => usernameCache.updateAndGet(request.username).flatMap(_ => joinGame(player, playersCache, gamePhase).map(Option.apply))
    }
  }

  def processRegisterRequest[F[_] : Monad](
                                            registerPlayerReq: RegisterPlayer,
                                            playersCache: PlayersCache[F]
                                          ): F[Option[PlayerSuccessfullyRegistered]] =
    OptionT(playersCache.readOne(registerPlayerReq.username)).as(None).getOrElseF {
      playersCache
        .updateOne(Player(registerPlayerReq.username, registerPlayerReq.password))
        .as(Option(PlayerSuccessfullyRegistered(registerPlayerReq.username)))
    }

  private def joinGame[F[_] : Monad](player: Player, playersCache: PlayersCache[F], gamePhase: GamePhase): F[Response] = for {
    _ <- playersCache.updateOne(player.copy(isOnline = true))
    players <- playersCache.readAll
  } yield PlayerJoinedGame(player, Some(gamePhase), Some(getActivePlayers(players)))

  private def clearBets[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer = player.copy(balance = player.chipsPlaced |+| player.balance)
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield BetsCleared(player.username)
  }

  private def exitGame[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = for {
    _ <- playersCache.updateOne(player.copy(isOnline = false))
  } yield PlayerLeftGame(player.username)

  private def placeBet[F[_] : Monad](privateTopic: Topic[F, WebSocketFrame], player: Player, gamePhase: GamePhase, bet: Bet, playersCache: PlayersCache[F]): F[Option[Response]] =
    validateBet(bet, player, gamePhase) match {
      case Valid(updatedPlayer@Player(username, _, _, _, chipsPlaced, _)) =>
        playersCache.updateOne(updatedPlayer).as(Option(BetPlaced(chipsPlaced, username, Some(bet))))
      case Invalid(msg) => sendPrivateResponse(privateTopic, BadRequest(msg)).as(Option.empty[Response])
    }

  private def sendPrivateResponse[F[_]](privateTopic: Topic[F, WebSocketFrame], response: Response) =
    privateTopic.publish1(responseToWebSocketText(response))

  private def responseToWebSocketText(response: Response) = Text(response.asJson.deepDropNullValues.noSpaces)

  private def webSocketFrameToRequest[F[_]](stream: Stream[F, WebSocketFrame]): Stream[F, Request] = {
    stream.collect {
      case Text(text, _) => Request.fromString(text)
      case Close(_) => ExitGame
    }
  }
}
