package com.example.roulette.bet

import cats.implicits.catsSyntaxSemigroup
import com.example.roulette.bet.Bet.Chips
import com.example.roulette.response.Response.LuckyNumber
import BetExtensions.BetOps

object BetProcessor {

  def getWinnings(luckyNumber: LuckyNumber, bets: List[Bet]): Chips =
    bets.map(calculatePayout(luckyNumber)).reduce(_ |+| _)

  private val calculatePayout = (luckyNumber: LuckyNumber) =>
    (bet: Bet) =>
      if (bet.positions.map(_.value) contains luckyNumber.value) bet.betAmount * bet.PAYOUT
      else Chips(0)

}
