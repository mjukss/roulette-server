package com.example.roulette.bet

import cats.Semigroup
import com.example.roulette.bet.Bet.{BetPosition, Chips}
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

@ConfiguredJsonCodec sealed trait Bet {
  val betAmount: Chips
  val positions: List[BetPosition]
}
  object Bet {
    final case class Straight(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Odd(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Even(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class High(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Low(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Row(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Dozen(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Red(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Black(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Split(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Street(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class SixLine(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Corner(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Trio(positions: List[BetPosition], betAmount: Chips) extends Bet
    final case class Basket(positions: List[BetPosition], betAmount: Chips)extends Bet

    implicit val genDevConfig: Configuration =
      Configuration.default.withDiscriminator("betType")

    final case class BetPosition(value: Int) extends AnyVal
    object BetPosition {
      implicit val betCodec: Codec[BetPosition] = deriveUnwrappedCodec[BetPosition]
    }

    implicit def chipsSemigroup: Semigroup[Chips] = (x: Chips, y: Chips) => Chips(x.value + y.value)


    final case class Chips(value: Int) extends AnyVal
    object Chips {
      implicit val chipCodec: Codec[Chips] = deriveUnwrappedCodec[Chips]

      implicit class ChipsOps(chips: Chips) {
        def *(num: Int): Chips = Chips(chips.value * num)
        def -(chips2: Chips): Chips = Chips(chips.value - chips2.value)
      }
    }
  }