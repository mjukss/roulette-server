package com.example.roulette.bet

import cats.implicits.catsSyntaxSemigroup
import com.example.roulette.response.Response.LuckyNumber

object BetProcessor {

  import Bet._
  import BetValidator._

  def getWinnings(luckyNumber: LuckyNumber, bets: List[Bet]): Chips = {
    val luckyInt = luckyNumber.value
    import BetPayouts._
    bets.map {
      case bet@Straight(position, betAmount) if isValidStraight(position.value, luckyInt) => betAmount * bet.PAYOUT
      case bet@Odd(betAmount) if isValidOdd(luckyInt) => betAmount * bet.PAYOUT
      case bet@Even(betAmount) if isValidEven(luckyInt) => betAmount * bet.PAYOUT
      case bet@High(betAmount) if isHigh(luckyInt) => betAmount * bet.PAYOUT
      case bet@Low(betAmount) if isLow(luckyInt) => betAmount * bet.PAYOUT
      case bet@Red(betAmount) if isValidRed(luckyInt) => betAmount * bet.PAYOUT
      case bet@Black(betAmount) if isValidBlack(luckyInt) => betAmount * bet.PAYOUT
      case bet@Split(positions, betAmount) if isValidSplit(positions.map(_.value), luckyInt) => betAmount * bet.PAYOUT
      case bet@Corner(positions, betAmount) if isValidCorner(positions.map(_.value), luckyInt) => betAmount * bet.PAYOUT
      case bet@Street(positions, betAmount) if isValidStreet(positions.map(_.value), luckyInt) => betAmount * bet.PAYOUT
      case bet@SixLine(positions, betAmount) if isValidSixLine(positions.map(_.value), luckyInt) => betAmount * bet.PAYOUT
      case bet@Basket(betAmount) if isBasket(luckyInt) => betAmount * bet.PAYOUT
      case bet@Row(position, betAmount) => position match {
        case BetPosition(1) if isValidRow1(luckyInt) => betAmount * bet.PAYOUT
        case BetPosition(2) if isValidRow2(luckyInt) => betAmount * bet.PAYOUT
        case BetPosition(3) if isValidRow2(luckyInt) => betAmount * bet.PAYOUT
        case _ => Chips(0)
      }
      case bet@Dozen(position, betAmount) => position match {
        case BetPosition(1) if isValidFirstDozen(luckyInt) => betAmount * bet.PAYOUT
        case BetPosition(2) if isValidSecondDozen(luckyInt) => betAmount * bet.PAYOUT
        case BetPosition(3) if isValidThirdDozen(luckyInt) => betAmount * bet.PAYOUT
        case _ => Chips(0)
      }
      case bet@Trio(position, betAmount) => position match {
        case BetPosition(1) if isFirstTrio(luckyInt) => betAmount * bet.PAYOUT
        case BetPosition(2) if isSecondTrio(luckyInt) => betAmount * bet.PAYOUT
        case _ => Chips(0)
      }
      case _ => Chips(0)
    }.reduce(_ |+| _)

  }
}
