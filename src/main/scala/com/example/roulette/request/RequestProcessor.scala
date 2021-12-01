package com.example.roulette.request

import cats.Monad
import cats.data.OptionT
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup}
import com.example.roulette.bet.Bet
import com.example.roulette.bet.BetValidator.validateBet
import com.example.roulette.game.{GameCache, GamePhase}
import com.example.roulette.player.Player.Username
import com.example.roulette.player.{Player, PlayersCache}
import com.example.roulette.request.Request.RequestOrError
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response
import com.example.roulette.response.Response.{BadRequest, BetPlaced, BetsCleared, PlayerRegistered, PlayerRemoved}
import cats.implicits.{toFlatMapOps, toFunctorOps}

object RequestProcessor {

  def executeRequest[F[_] : Monad](usernameAndRequestOrError: (Username, RequestOrError),
                                   playersCache: PlayersCache[F],
                                   gameCache: GameCache[F]): F[Response] = {
    val (username, requestOrError) = usernameAndRequestOrError
    import Request._

    def getResponseF(player: Player, gamePhase: GamePhase, request: Request): F[Response] = request match {
      case PlaceBet(bet) => placeBet(player, gamePhase, username, bet, playersCache)
      case ClearBets => clearBets(player, playersCache)
      case RegisterPlayer => registerPlayer(playersCache, gameCache, username)
      case RemovePlayer => removePlayer(username, playersCache)
    }

    requestOrError match {
      case Left(error) => (BadRequest(username, CustomBadRequestMessage(error.getMessage)): Response).pure[F]
      case Right(RegisterPlayer) => registerPlayer(playersCache, gameCache, username)
      case Right(request) => (for {
        user <- OptionT(playersCache.readOne(username))
        gamePhase <- OptionT.liftF(gameCache.read)
        response <- OptionT.liftF(getResponseF(user, gamePhase, request))
      } yield response) getOrElse BadRequest(username, UsernameDoesNotExist)
    }


  }

  private def registerPlayer[F[_] : Monad](playersCache: PlayersCache[F], gameCache: GameCache[F], username: Username): F[Response] = {
    def register(): F[Response] = for {
      gamePhase <- gameCache.read
      _ <- playersCache.updateOne(Player(username))
      players <- playersCache.readAll
    } yield PlayerRegistered(Player(username), Some(gamePhase), Some(players.values.toList))

    OptionT(playersCache.readOne(username)).as(BadRequest(username, UsernameTaken)).getOrElseF(register())
  }

  private def removePlayer[F[_] : Monad](username: Username, playersCache: PlayersCache[F]): F[Response] = {
    playersCache.removeOne(username).as(PlayerRemoved(username))
  }


  private def clearBets[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    val updatedPlayer = Player(player.username, player.chipsPlaced |+| player.balance)
    for {
      _ <- playersCache.updateOne(updatedPlayer)
    } yield BetsCleared(player.username)
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

      case Invalid(msg) => (BadRequest(username, msg) : Response).pure[F]
    }
  }
}
