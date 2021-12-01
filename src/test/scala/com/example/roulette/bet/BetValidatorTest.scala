package com.example.roulette.bet

import com.example.roulette.bet.Bet._
import com.example.roulette.bet.BetValidator.isValidBet
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BetValidatorTest extends AnyWordSpec with Matchers {

  import BetValidatorTest._

  "Bet position validation" should {
    "be valid straight at positions from 0 to 36" in {
      (0 to 36).map(num => isValidBet(Straight(List(BetPosition(num)), betAmount))).toSet mustBe Set(true)
    }
    "be invalid when not in range from 0 to 36" in {
      (37 to 50).map(num => isValidBet(Straight(List(BetPosition(num)), betAmount))).toSet mustBe Set(false)
    }
    "be valid odd from odd numbers in range from 1 to 35" in {
      val oddPositions = (1 to 35 by 2).toList.map(BetPosition(_))
      isValidBet(Odd(oddPositions, betAmount)) mustBe true
    }
    "be invalid if list does not contain all odd numbers in range from 1 to 35" in {
      val oddPositions = (1 to 34 by 2).toList.map(BetPosition(_))
      isValidBet(Odd(oddPositions, betAmount)) mustBe false
    }
    "be invalid if list contains more odd numbers than range from 1 to 35" in {
      val oddPositions = (1 to 37 by 2).toList.map(BetPosition(_))
      isValidBet(Odd(oddPositions, betAmount)) mustBe false
    }
    "be invalid if list contains any even numbers" in {
      val oddPositions = (1 to 6 by 2).toList.map(BetPosition(_))
      isValidBet(Odd(oddPositions, betAmount)) mustBe false
    }

    "be valid even from even numbers in range from 2 to 36" in {
      val evenPositions = (2 to 36 by 2).toList.map(BetPosition(_))
      isValidBet(Even(evenPositions, betAmount)) mustBe true
    }
    "be invalid if list does not contain all evens in range from 2 to 36" in {
      val even = (1 to 34 by 2).toList.map(BetPosition(_))
      isValidBet(Even(even, betAmount)) mustBe false
    }
    "be invalid if list contains more even numbers than range from 2 to 36" in {
      val evenPositions = (0 to 36 by 2).toList.map(BetPosition(_))
      isValidBet(Even(evenPositions, betAmount)) mustBe false
    }
    "be invalid if list contains any odd numbers" in {
      val evenPositions = (7 :: (0 to 34 by 2).toList).map(BetPosition(_))
      isValidBet(Even(evenPositions, betAmount)) mustBe false
    }

    "be valid high from positions in range from 1 to 18" in {
      val high = (1 to 18).toList.map(BetPosition(_))
      isValidBet(High(high, betAmount)) mustBe true
    }
    "be valid high from positions in range from 1 to 18 if numbers in different sequence" in {
      val high = (18 :: (1 to 17).toList).map(BetPosition(_))
      isValidBet(High(high, betAmount)) mustBe true
    }
    "be invalid high if list does not contain all in range from 1 to 18" in {
      val high = (3 to 18).toList.map(BetPosition(_))
      isValidBet(High(high, betAmount)) mustBe false
    }
    "be invalid high if contains more number than needed" in {
      val high = (0 to 18).toList.map(BetPosition(_))
      isValidBet(High(high, betAmount)) mustBe false
    }

    "be valid low from positions in range from 19 to 36" in {
      val low = (19 to 36).toList.map(BetPosition(_))
      isValidBet(Low(low, betAmount)) mustBe true
    }
    "be valid low from positions in range from 19 to 36 if numbers in different sequence" in {
      val low = (36 :: (19 to 35).toList).map(BetPosition(_))
      isValidBet(Low(low, betAmount)) mustBe true
    }
    "be invalid low if list does not contain all in range from 19 to 36" in {
      val low = (19 to 35).toList.map(BetPosition(_))
      isValidBet(Low(low, betAmount)) mustBe false
    }
    "be invalid low if contains more number than needed" in {
      val low = (16 to 36).toList.map(BetPosition(_))
      isValidBet(Low(low, betAmount)) mustBe false
    }

    "be valid first row" in {
      val row = (1 to 34 by 3).toList.map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe true
    }
    "be invalid first row if contains zero " in {
      val row = (0 :: (1 to 34 by 3).toList).map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe false
    }
    "be valid second row" in {
      val row = (2 to 35 by 3).toList.map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe true
    }
    "be invalid second row if last number is missing " in {
      val row = (2 to 32 by 3).toList.map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe false
    }
    "be valid third row" in {
      val row = (3 to 36 by 3).toList.map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe true
    }
    "be invalid third row when not enough numbers" in {
      val row = (3 to 36 by 6).toList.map(BetPosition(_))
      isValidBet(Row(row, betAmount)) mustBe false
    }

    "be valid first dozen" in {
      val dozen = (1 to 12).toList.map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe true
    }
    "be valid first dozen in different sequence" in {
      val dozen = ((6 to 12).toList ::: (1 to 5).toList).map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe true
    }
    "be valid second dozen" in {
      val dozen = (13 to 24).toList.map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe true
    }
    "be valid third dozen" in {
      val dozen = (25 to 36).toList.map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe true
    }
    "be invalid dozen when contains 0" in {
      val dozen = (0 to 12).toList.map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe false
    }
    "be invalid fourth dozen" in {
      val dozen = (37 to 48).toList.map(BetPosition(_))
      isValidBet(Dozen(dozen, betAmount)) mustBe false
    }
    "be valid red" in {
      val red = List(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        .map(BetPosition(_))
      isValidBet(Red(red, betAmount)) mustBe true
    }
    "be valid red when different sequence" in {
      val red = List(36, 3, 5, 9, 7, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 1)
        .map(BetPosition(_))
      isValidBet(Red(red, betAmount)) mustBe true
    }
    "be invalid red when missing 9" in {
      val red = List(36, 3, 5, 7, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 1)
        .map(BetPosition(_))
      isValidBet(Red(red, betAmount)) mustBe false
    }
    "be invalid red when contains 0" in {
      val red = List(0, 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        .map(BetPosition(_))
      isValidBet(Red(red, betAmount)) mustBe false
    }
    "be invalid red when contains duplicate number 1" in {
      val red = List(1, 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        .map(BetPosition(_))
      isValidBet(Red(red, betAmount)) mustBe false
    }
    "be valid black" in {
      val black = List(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)
        .map(BetPosition(_))
      isValidBet(Black(black, betAmount)) mustBe true
    }
    "be valid black when different sequence" in {
      val black = List(4, 2, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)
        .map(BetPosition(_))
      isValidBet(Black(black, betAmount)) mustBe true
    }
    "be invalid black when contains red 1" in {
      val black = List(1, 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)
        .map(BetPosition(_))
      isValidBet(Black(black, betAmount)) mustBe false
    }
    "be invalid black when contains duplicate 2" in {
      val black = List(2, 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)
        .map(BetPosition(_))
      isValidBet(Black(black, betAmount)) mustBe false
    }
    "be valid slits" in {
      val split1 = List(2, 1).map(BetPosition(_))
      val split2 = List(3, 6).map(BetPosition(_))
      val split3 = List(16, 17).map(BetPosition(_))
      val split4 = List(27, 26).map(BetPosition(_))
      val split5 = List(20, 23).map(BetPosition(_))
      val split6 = List(34, 35).map(BetPosition(_))
      val split7 = List(22, 25).map(BetPosition(_))
      val split8 = List(19, 16).map(BetPosition(_))
      List(split1, split2, split3, split4, split5, split6, split7, split8)
        .map(Split(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid slits" in {
      val split1 = List(1, 1).map(BetPosition(_))
      val split2 = List(34, 6).map(BetPosition(_))
      val split3 = List(15, 17).map(BetPosition(_))
      val split4 = List(0, 1).map(BetPosition(_))
      val split5 = List(21, 23).map(BetPosition(_))
      val split6 = List(1, 35).map(BetPosition(_))
      val split7 = List(40, 25).map(BetPosition(_))
      val split8 = List(11, 16).map(BetPosition(_))
      List(split1, split2, split3, split4, split5, split6, split7, split8)
        .map(Split(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }
    "be invalid split when combined two valid splits" in {
      val split1 = List(1, 2).map(BetPosition(_))
      val split2 = List(3, 5).map(BetPosition(_))

      isValidBet(Split(split1 ::: split2, betAmount)) mustBe false
    }
    "be valid corners no matter of sequence" in {
      val corner1 = List(2, 1, 4, 5).map(BetPosition(_))
      val corner2 = List(2, 3, 5, 6).map(BetPosition(_))
      val corner3 = List(4, 7, 5, 8).map(BetPosition(_))
      val corner4 = List(5, 6, 8, 9).map(BetPosition(_))
      val corner5 = List(16, 17, 19, 20).map(BetPosition(_))
      val corner6 = List(34, 35, 32, 31).map(BetPosition(_))
      val corner7 = List(22, 25, 23, 26).map(BetPosition(_))
      val corner8 = List(19, 16, 17, 20).map(BetPosition(_))
      List(corner1, corner2, corner3, corner4, corner5, corner6, corner7, corner8)
        .map(Corner(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid valid corners" in {
      val corner1 = List(1, 1, 4, 5).map(BetPosition(_))
      val corner2 = List(2, 3, 1, 6).map(BetPosition(_))
      val corner3 = List(4, 7, 1, 8).map(BetPosition(_))
      val corner4 = List(1, 6, 8, 9).map(BetPosition(_))
      val corner5 = List(16, 17, 21, 20).map(BetPosition(_))
      val corner6 = List(34, 3, 32, 31).map(BetPosition(_))
      val corner7 = List(22, 2, 23, 26).map(BetPosition(_))
      val corner8 = List(35, 36, 39, 38).map(BetPosition(_))
      List(corner1, corner2, corner3, corner4, corner5, corner6, corner7, corner8)
        .map(Corner(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }


    "be valid streets no matter of sequence" in {
      val streets = List(
        List(3, 2, 1),
        List(4, 5, 6),
        List(8, 7, 9),
        List(10, 11, 12),
        List(13, 14, 15),
        List(16, 17, 18),
        List(19, 20, 21),
        List(22, 23, 24),
        List(25, 26, 27),
        List(28, 29, 30),
        List(31, 32, 33),
        List(34, 35, 36)).map(_.map(BetPosition(_)))
      streets.map(Street(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid streets" in {
      val streets = List(
        List(1, 1, 2),
        List(0, 1, 2),
        List(8, 7, 10),
        List(10, 9, 7),
        List(7, 10, 13),
        List(31, 30, 33)).map(_.map(BetPosition(_)))
      streets.map(Street(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }
    "be valid six-line no matter of sequence" in {
      val sixLines = List(
        List(3, 2, 1, 4, 5, 6),
        List(4, 5, 6, 7, 8, 9),
        List(8, 7, 9, 10, 11, 12),
        List(10, 11, 12, 13, 14, 15),
        List(13, 14, 15, 16, 17, 18),
        List(16, 17, 18, 19, 20, 21),
        List(19, 20, 21, 22, 23, 24),
        List(22, 23, 24, 25, 26, 27),
        List(25, 26, 27, 28, 29, 30),
        List(28, 29, 30, 31, 32, 33),
        List(31, 32, 33, 34, 35, 36)).map(_.map(BetPosition(_)))
      sixLines.map(SixLine(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid six-line" in {
      val sixLines = List(
        List(3, 2, 1, 4, 5, 6, 7),
        List(5, 6, 7, 8, 9),
        List(7, 7, 9, 10, 11, 12),
        List(37, 38, 39, 34, 35, 36)).map(_.map(BetPosition(_)))
      sixLines.map(SixLine(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }
    "be valid trio no matter of sequence" in {
      val trios = List(
        List(2,1,0), List(0, 2, 3))
        .map(_.map(BetPosition(_)))
      trios.map(Trio(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid trio" in {
      val trios = List(
        List(3,2,1,0),  List(0, 2), List(0, 2, 4))
        .map(_.map(BetPosition(_)))
      trios.map(Trio(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }
    "be valid basket no matter of sequence" in {
      val baskets = List(
        List(2,1,0, 3), List(1, 0, 2, 3))
        .map(_.map(BetPosition(_)))
      baskets.map(Basket(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(true)
    }
    "be invalid basket" in {
      val baskets = List(
        List(2,1,0), List(1, 0, 2, 3, 4))
        .map(_.map(BetPosition(_)))
      baskets.map(Basket(_, betAmount)).map(isValidBet(_)).toSet mustBe Set(false)
    }
  }
}

object BetValidatorTest {
  val betAmount: Chips = Chips(20)
}