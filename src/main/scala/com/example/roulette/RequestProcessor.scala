package com.example.roulette

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup}
import cats.{Applicative, Monad}
import com.example.roulette.BadRequestMessage.{CustomBadRequestMessage, InsufficientFunds, UsernameDoesNotExist, UsernameTaken}
import com.example.roulette.Bet.Chips
import com.example.roulette.Cache.{GameCache, PlayersCache}
import com.example.roulette.Player.Username
import com.example.roulette.Request.RequestOrError
import com.example.roulette.Response.{BadRequest, BetPlaced, BetsCleared, PlayerRegistered, PlayerRemoved}


object RequestProcessor {

  def executeRequest[F[_] : Monad](requestOrError: RequestOrError, playersCache: PlayersCache[F], gameCache: GameCache[F]): F[Response] =
    requestOrError match {
      case Left(error) =>
        val message = CustomBadRequestMessage(error.getMessage)
        (BadRequest(message): Response).pure[F]
      case Right(request) => request match {
        case Request.PlaceBet(username, bet) => placeBet(playersCache, username, bet)
        case Request.ClearBets(username) => clearBets(playersCache, username)
        case Request.RegisterPlayer(username) => registerPlayer(playersCache, gameCache, username)
        case Request.RemovePlayer(username) => removePlayer(playersCache, username)
      }
    }

  private def registerPlayer[F[_] : Monad](playersCache: PlayersCache[F], gameCache: GameCache[F], username: Username): F[Response] = {
    import cats.implicits.{toFlatMapOps, toFunctorOps}

    def register(): F[Response] = for {
      gamePhase <- gameCache.read
      _ <- playersCache.update(Player(username))
    } yield PlayerRegistered(Player(username), gamePhase)

    OptionT(playersCache.readOne(username))
      .map(_ => BadRequest(UsernameTaken))
      .getOrElseF(register())
  }

  private def removePlayer[F[_] : Monad](playersCache: PlayersCache[F], username: Username): F[Response] = (for {
    player <- OptionT(playersCache.readOne(username))
    _ <- OptionT.liftF(playersCache.removeOne(username))
  } yield PlayerRemoved(player): Response) getOrElse BadRequest(UsernameTaken)


  private def clearBets[F[_] : Monad](playersCache: PlayersCache[F], username: Username): F[Response] = {
    (for {
      player <- OptionT(playersCache.readOne(username))
      _ <- OptionT.liftF(playersCache.update(player.copy(
        betPlaced = Chips(0),
        balance = player.betPlaced |+| player.balance,
        bets = Nil
      )))
    } yield BetsCleared: Response) getOrElse BadRequest(UsernameDoesNotExist)
  }

  private def placeBet[F[_] : Monad](playersCache: PlayersCache[F], username: Username, bet: Bet): F[Response] = {
    (for {
      player <- OptionT(playersCache.readOne(username))
      newPlayer <- validateBet(player, bet)
      _ <- OptionT.liftF(playersCache.update(newPlayer))
    } yield BetPlaced(bet): Response) getOrElse BadRequest(InsufficientFunds)
  }

  private def validateBet[F[_] : Applicative](player: Player, bet: Bet): OptionT[F, Player] = {
    val betAmount = bet.betAmount.value
    val balance = player.balance.value
    if (betAmount > balance) OptionT.none
    else OptionT.pure {
      player.copy(
        betPlaced = Chips(player.betPlaced.value + bet.betAmount.value),
        balance = Chips(balance - betAmount),
        bets = player.bets :+ bet
      )
    }
  }
}
