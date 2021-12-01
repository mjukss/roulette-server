package com.example.roulette.bet

import cats.data.Validated
import cats.implicits.{catsSyntaxSemigroup, catsSyntaxValidatedId}
import com.example.roulette.bet.BetExtensions.BetOps
import com.example.roulette.game.GamePhase
import com.example.roulette.game.GamePhase.BetsClosed
import com.example.roulette.player.Player
import com.example.roulette.response.BadRequestMessage

object BetValidator {

  def validateBet(bet: Bet, player: Player, gamePhase: GamePhase): Validated[BadRequestMessage, Player] = {

    val isInsufficientFunds = player.balance.value < bet.betAmount.value

    if (gamePhase == BetsClosed) BadRequestMessage.CanNotPlaceBetInThisGamePhase.invalid
    else if (!isValidBet(bet)) BadRequestMessage.InvalidBet.invalid
    else if (isInsufficientFunds) BadRequestMessage.InsufficientFunds.invalid
    else player.copy(
      chipsPlaced = player.chipsPlaced |+| bet.betAmount,
      balance = player.balance - bet.betAmount,
      bets = Some(player.bets.getOrElse(Nil) :+ bet)
    ).valid
  }

  def isValidBet(bet: Bet): Boolean = isValidCombination(bet.COMBINATION, bet.positions.map(_.value))

 private def isValidCombination(allowedCombinations: List[List[Int]], providedCombination: List[Int]) = {
    allowedCombinations.map(_.diff(providedCombination)).contains(Nil) &&
      allowedCombinations.map(providedCombination.diff(_)).contains(Nil)
  }

}
