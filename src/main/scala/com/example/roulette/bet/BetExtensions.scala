package com.example.roulette.bet

object BetExtensions {
  import Bet._
  implicit class BetOps(bet: Bet) {
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

    def COMBINATION: List[List[Int]] = bet match {
      case _: Straight => (0 to 36).toList.map(List(_))
      case _: Odd => (1 to 35 by 2).toList :: Nil
      case _: Even => (2 to 36 by 2).toList :: Nil
      case _: High => (1 to 18).toList :: Nil
      case _: Low => (19 to 36).toList :: Nil
      case _: Row => rows
      case _: Dozen => List(1 to 12, 13 to 24, 25 to 36).map(_.toList)
      case _: Red => red :: Nil
      case _: Black => black :: Nil
      case _: Split => (colSplits ::: rowSplits).flatMap(_.map(tuple2ToList))
      case _: Street => streets
      case _: SixLine => streets.zip(streets.tail).map(tuple2ToList(_).flatten)
      case _: Corner => corners
      case _: Trio => List(List(0, 1, 2), List(0, 2, 3))
      case _: Basket => List(0, 1, 2, 3) :: Nil
    }

    private def tuple2ToList[T](t: (T, T)): List[T] = List(t._1, t._2)
    private def tuple3ToList[T](t: (T,T, T)): List[T] = List(t._1, t._2, t._3)
    private def flattenTuple2[T]: (((T, T), (T, T))) => List[T] = tuple2ToList(_).flatMap(tuple2ToList)

    private val allColored = (1 to 36).toList
    private val black = List(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)
    private val red = allColored.diff(black)
    private val rows = List(1 to 34 by 3, 2 to 35 by 3, 3 to 36 by 3).map(_.toList)
    private val firstRow :: secondRow :: thirdRow :: Nil = rows
    private val colSplits = List(firstRow.zip(secondRow), secondRow.zip(thirdRow))
    private val rowSplits = rows.map(ls => ls.zip(ls.tail))
    private val streets = firstRow.lazyZip(secondRow).lazyZip(thirdRow).toList.map(tuple3ToList)
    private val corners = List(rowSplits.init, rowSplits.tail).flatMap { ls =>
      val r1 :: r2 :: Nil = ls
      r1.zip(r2).map(flattenTuple2)
    }
  }
}
