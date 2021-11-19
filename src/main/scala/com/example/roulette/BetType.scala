package com.example.roulette

trait BetType

object BetType {
  case object STRAIGHT extends BetType// position can be 0 to 36
  case object ODD extends BetType
  case object EVEN extends BetType
  case object HIGH extends BetType
  case object LOW extends BetType
  case object ROW extends BetType // position 1 | 2 | 3
  case object DOZEN extends BetType // position 1 | 2 | 3
  case object RED extends BetType
  case object BLACK extends BetType

  //  If have enough time
  //  case object SPLIT
  //  case object STREET
  //  case object SIX_LINE
  //  case object CORNER
  //  case object TRIO
  //  case object BASKET
}
