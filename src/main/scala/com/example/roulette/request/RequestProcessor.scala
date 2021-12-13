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
import com.example.roulette.player.Player.{Password, Username}
import com.example.roulette.player.PlayerProcessor.getActivePlayers
import com.example.roulette.player.{Player, PlayerUsernameCache, PlayersCache}
import com.example.roulette.request.Request.{ExitGame, JoinGame, RegisterPlayer}
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{BadRequest, BetPlaced, BetsCleared, PlayerJoinedGame, PlayerLeftGame, PlayerSuccessfullyRegistered}
import fs2.Stream
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object RequestProcessor {

  def processRegisterRequest[F[_] : Monad](
                                            registerPlayerReq: RegisterPlayer,
                                            playersCache: PlayersCache[F]
                                          ): F[Option[PlayerSuccessfullyRegistered]] = {
    OptionT(playersCache.readOne(registerPlayerReq.username))
      .as(None)
      .getOrElseF {
        playersCache
          .updateOne(Player(registerPlayerReq.username, registerPlayerReq.password))
          .as(Option(PlayerSuccessfullyRegistered(registerPlayerReq.username)))
      }
  }

  def processFromClient[F[_] : Concurrent](
                                            stream: Stream[F, WebSocketFrame],
                                            playersCache: PlayersCache[F],
                                            gameCache: GameCache[F],
                                            usernameCache: PlayerUsernameCache[F],
                                            queue: Queue[F, Option[Response]]
                                          ): Stream[F, Unit] = {
    webSocketFrameToRequest(stream).evalMapFilter {
      case request@JoinGame(username, password) => (for {
        player <- OptionT(playersCache.readOne(username))
        if player.password == password
        _ <- OptionT.liftF(usernameCache.updateAndGet(username))
      } yield (player.username, request: Request)).value

      case request => for {
        username <- usernameCache.read
      } yield username.map((_, request))
    }
      .evalMap(req => RequestProcessor.executeRequest(req, playersCache, gameCache))
      .enqueueNoneTerminated(queue)
  }

  private def webSocketFrameToRequest[F[_]](stream: Stream[F, WebSocketFrame]): Stream[F, Request] = {
    stream.collect {
      case Text(text, _) => Request.fromString(text)
      case Close(_) => ExitGame
    }
  }

  private def executeRequest[F[_] : Monad](usernameAndRequest: (Username, Request),
                                           playersCache: PlayersCache[F],
                                           gameCache: GameCache[F]): F[Response] = {
    val (username, request) = usernameAndRequest
    import Request._

    val getResponseFromRegisteredPlayers: (Player, GamePhase) => PartialFunction[Request, F[Response]] = (player, gamePhase) => {
      case PlaceBet(bet) => placeBet(player, gamePhase, username, bet, playersCache)
      case ClearBets => clearBets(player, playersCache)
      case ExitGame => exitGame(player, playersCache)
      case JoinGame(_, _) => joinGame(player, playersCache, gameCache)
    }

    request match {
      case InvalidRequest(errorMessage) =>
        (BadRequest(username, CustomBadRequestMessage(errorMessage)): Response).pure[F]
      case request => (for {
        user <- OptionT(playersCache.readOne(username))
        gamePhase <- OptionT.liftF(gameCache.read)
        response <- OptionT.liftF(getResponseFromRegisteredPlayers(user, gamePhase)(request))
      } yield response) getOrElse BadRequest(username, UsernameDoesNotExist)

    }
  }

  def registerPlayer[F[_] : Monad](username: Username, password: Password, playersCache: PlayersCache[F]): F[Response] = {
    for {
      players <- playersCache.readAll
      response <- {
        val newPlayer = Player(username, password)

        if (players.isDefinedAt(username.value)) BadRequest(username, UsernameTaken).pure[F]
        else playersCache.updateOne(newPlayer).as(PlayerSuccessfullyRegistered(username))
      }
    } yield response
  }

  private def joinGame[F[_] : Monad](player: Player, playersCache: PlayersCache[F], gameCache: GameCache[F]): F[Response] = {
    for {
      gamePhase <- gameCache.read
      _ <- playersCache.updateOne(player.copy(isOnline = true))
      players <- playersCache.readAll
    } yield PlayerJoinedGame(player, Some(gamePhase), Some(getActivePlayers(players)))
  }


  private def clearBets[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer = player.copy(
      balance = player.chipsPlaced |+| player.balance
    )
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield BetsCleared(player.username)
  }

  private def exitGame[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer = player.copy(isOnline = false)
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield PlayerLeftGame(player.username)
  }

  private def placeBet[F[_] : Monad](player: Player,
                                     gamePhase: GamePhase,
                                     username: Username,
                                     bet: Bet,
                                     playersCache: PlayersCache[F]): F[Response] = {
    validateBet(bet, player, gamePhase) match {
      case Valid(updatedPlayer) => for {
        _ <- playersCache.updateOne(updatedPlayer)
        chipsPlaced = updatedPlayer.chipsPlaced
      } yield BetPlaced(chipsPlaced, username, Some(bet))

      case Invalid(msg) => (BadRequest(username, msg): Response).pure[F]
    }
  }
}
