package com.example.roulette.request

import cats.Monad
import cats.data.{OptionT, Validated}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup, catsSyntaxValidatedId}
import com.example.roulette.bet.Bet
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.game.GamePhase.BetsClosed
import com.example.roulette.game.{GameCache, GamePhase}
import com.example.roulette.player.Player.Username
import com.example.roulette.player.{Player, PlayersCache}
import com.example.roulette.request.Request.RequestOrError
import com.example.roulette.response.BadRequestMessage._
import com.example.roulette.response.Response.{BadRequest, BetPlaced, BetsCleared, PlayerRegistered, PlayerRemoved}
import com.example.roulette.response.{BadRequestMessage, Response}

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

    import cats.implicits.{toFlatMapOps, toFunctorOps}
    def register(): F[Response] = for {
      gamePhase <- gameCache.read
      _ <- playersCache.updateOne(Player(username))
      players <- playersCache.readAll
    } yield PlayerRegistered(Player(username), Some(gamePhase), Some(players.values.toList))

    OptionT(playersCache.readOne(username)).as(BadRequest(username, UsernameTaken)).getOrElseF(register())
  }

  private def removePlayer[F[_] : Monad](username: Username, playersCache: PlayersCache[F]): F[Response] = {
    import cats.implicits.toFunctorOps
    playersCache.removeOne(username).as(PlayerRemoved(username))
  }


  private def clearBets[F[_] : Monad](player: Player, playersCache: PlayersCache[F]): F[Response] = {
    import cats.implicits.toFunctorOps
    playersCache.updateOne(player.copy(
      chipsPlaced = Chips(0),
      balance = player.chipsPlaced |+| player.balance,
      bets = None
    )).as(BetsCleared(player.username))
  }

  private def placeBet[F[_] : Monad](player: Player,
                                     gamePhase: GamePhase,
                                     username: Username,
                                     bet: Bet,
                                     playersCache: PlayersCache[F]): F[Response] = {
    import cats.implicits.toFunctorOps
    validateBet(gamePhase, bet, player) match {
      case Validated.Valid(player) => playersCache.updateOne(player).as(BetPlaced(player.chipsPlaced, username, Some(bet)))
      case Validated.Invalid(error) => (BadRequest(username, error): Response).pure[F]
    }
  }

  private def validateBet(gamePhase: GamePhase, bet: Bet, player: Player): Validated[BadRequestMessage, Player] = {
    if (player.balance.value < bet.betAmount.value) InsufficientFunds.invalid
    else if (gamePhase == BetsClosed) CanNotPlaceBetInThisGamePhase.invalid
    else player.copy(
      chipsPlaced = Chips(player.chipsPlaced.value + bet.betAmount.value),
      balance = Chips(player.balance.value - bet.betAmount.value),
      bets = Some(player.bets.getOrElse(Nil) :+ bet)
    ).valid
  }
}
