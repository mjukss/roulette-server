package com.example.roulette.bet

object BetPayouts {
  import Bet._
  implicit class BetPayouts(bet: Bet) {
    val PAYOUT: Int = bet match {
      case _: Straight => 36
      case _: Odd => 2
      case _: Even => 2
      case _: High => 2
      case _: Low => 2
      case _: Row => 3
      case _: Dozen => 3
      case _: Red => 2
      case _: Black => 2
      case _: Split => 18
      case _: Street => 12
      case _: SixLine => 6
      case _: Corner => 9
      case _: Trio => 12
      case _: Basket => 9
    }
  }
}
