package com.example.roulette.bet

object BetValidator {

  def isValidStraight: (Int, Int) => Boolean = (luckyNumber, betNumber) =>
    (0 to 36).contains(betNumber) && luckyNumber == betNumber

  def isBasket: Int => Boolean = List(0, 1, 2, 3).contains(_)

  def isFirstTrio: Int => Boolean = List(0, 1, 2).contains(_)
  def isSecondTrio: Int => Boolean = List(0, 1, 2).contains(_)

  def isValidEven: Int => Boolean = num => num % 2 == 0 && coloredNumbers.contains(num)
  def isValidOdd: Int => Boolean = num => num % 2 != 0 && coloredNumbers.contains(num)

  def isHigh: Int => Boolean = (1 to 18).contains(_)
  def isLow: Int => Boolean = (19 to 36).contains(_)

  def isValidRed: Int => Boolean = coloredNumbers.diff(blackNumbers).contains(_)
  def isValidBlack: Int => Boolean = blackNumbers.contains(_)

  def isValidRow1: Int => Boolean = firstColNumbers.contains(_)
  def isValidRow2: Int => Boolean = secondColNumbers.contains(_)
  def isValidRow3: Int => Boolean = thirdColNumbers.contains(_)

  def isValidFirstDozen: Int => Boolean = (1 to 12).contains(_)
  def isValidSecondDozen: Int => Boolean = (17 to 24).contains(_)
  def isValidThirdDozen: Int => Boolean = (25 to 36).contains(_)

  def isValidSixLine: (List[Int], Int) => Boolean = (providedCombination, winningInt) =>
    isValidCombination(winningInt, allowedSixLineCombinations, providedCombination)

  def isValidStreet: (List[Int], Int) => Boolean = (providedCombination, winningInt) =>
    isValidCombination(winningInt, allowedStreetCombinations, providedCombination)

  def isValidSplit: (List[Int], Int) => Boolean = (providedCombination, winningInt) =>
    isValidCombination(winningInt, allowedSplitCombinations, providedCombination)

  def isValidCorner: (List[Int], Int) => Boolean = (providedCombination, winningInt) =>
    isValidCombination(winningInt, allowedCornerCombinations, providedCombination)

  private def tuple2ToList[T]: ((T, T)) => List[T] = {
    case (t1, t2) => List(t1, t2)
  }

  private def tuple3ToList[T]: ((T, T, T)) => List[T] = {
    case (t1, t2, t3) => List(t1, t2, t3)
  }

  private def flattenTuple2[T]: (((T,T),(T, T))) => List[T] = tuple2ToList(_).flatMap(tuple2ToList)

  private def isValidCombination(winningInt: Int, allowedCombinations: List[List[Int]], providedCombination: List[Int]): Boolean =
    allowedCombinations.map(_.diff(providedCombination)).contains(Nil) && providedCombination.contains(winningInt)

  private val coloredNumbers: List[Int] = (1 to 36).toList
  private val blackNumbers = List(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)

  private val isNumInFirstCol: Int => Boolean = _ % 3 == 1
  private val isNumInSecondCol: Int => Boolean = _ % 3 == 2
  private val isNumInThirdCol: Int => Boolean = _ % 3 == 0

  private val firstColNumbers: List[Int] = coloredNumbers.filter(isNumInFirstCol)
  private val secondColNumbers: List[Int] = coloredNumbers.filter(isNumInSecondCol)
  private val thirdColNumbers: List[Int] = coloredNumbers.filter(isNumInThirdCol)

  private val firstColSplits: List[(Int, Int)] = firstColNumbers.zip(firstColNumbers.tail)
  private val secondColSplits: List[(Int, Int)] = secondColNumbers.zip(secondColNumbers.tail)
  private val thirdColSplits: List[(Int, Int)] = thirdColNumbers.zip(thirdColNumbers.tail)

  private val firsRowSplits: List[(Int, Int)] = firstColNumbers.zip(secondColNumbers)
  private val secondRowSplits: List[(Int, Int)] = secondColNumbers.zip(thirdColNumbers)

  private val allowedSplitCombinations: List[List[Int]] =
    (firsRowSplits ::: secondRowSplits ::: firstColSplits ::: secondColSplits ::: thirdColSplits).map(tuple2ToList)

  private val firstColCorners: List[List[Int]] = firstColSplits.zip(secondColSplits).map(flattenTuple2)

  private val secondColCorners: List[List[Int]] = secondColSplits.zip(thirdColSplits).map(flattenTuple2)

  private val allowedCornerCombinations: List[List[Int]] = firstColCorners ::: secondColCorners

  private val allowedStreetCombinations: List[List[Int]] =
    firstColNumbers.lazyZip(secondColNumbers).lazyZip(thirdColNumbers).toList.map(tuple3ToList)

  private val allowedSixLineCombinations: List[List[Int]] =
    allowedStreetCombinations.zip(allowedStreetCombinations.tail).map(tuple2ToList(_).flatten)

}
