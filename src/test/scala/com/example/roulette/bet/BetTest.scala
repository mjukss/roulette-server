package com.example.roulette.bet

import com.example.roulette.bet.Bet.{BetPosition, Chips}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Bet._

class BetTest extends AnyWordSpec with Matchers {
  import BetTest._
  "Bet json validation" should {
    "encode and decode straight" in {
      straight.asJson.noSpaces mustBe straightJson
      decode[Bet](straightJson) mustBe Right(straight)
    }
    "encode and decode odd" in {
      odd.asJson.noSpaces mustBe oddJson
      decode[Bet](oddJson) mustBe Right(odd)
    }
    "encode and decode even" in {
      even.asJson.noSpaces mustBe evenJson
      decode[Bet](evenJson) mustBe Right(even)
    }
    "encode and decode high" in {
      high.asJson.noSpaces mustBe highJson
      decode[Bet](highJson) mustBe Right(high)
    }
    "encode and decode low" in {
      low.asJson.noSpaces mustBe lowJson
      decode[Bet](lowJson) mustBe Right(low)
    }
    "encode and decode row" in {
      row.asJson.noSpaces mustBe rowJson
      decode[Bet](rowJson) mustBe Right(row)
    }
    "encode and decode dozen" in {
      dozen.asJson.noSpaces mustBe dozenJson
      decode[Bet](dozenJson) mustBe Right(dozen)
    }
    "encode and decode red" in {
      red.asJson.noSpaces mustBe redJson
      decode[Bet](redJson) mustBe Right(red)
    }
    "encode and decode black" in {
      black.asJson.noSpaces mustBe blackJson
      decode[Bet](blackJson) mustBe Right(black)
    }
    "encode and decode split" in {
      split.asJson.noSpaces mustBe splitJson
      decode[Bet](splitJson) mustBe Right(split)
    }
    "encode and decode street" in {
      street.asJson.noSpaces mustBe streetJson
      decode[Bet](streetJson) mustBe Right(street)
    }
    "encode and decode six-line" in {
      sixLine.asJson.noSpaces mustBe sixLineJson
      decode[Bet](sixLineJson) mustBe Right(sixLine)
    }
    "encode and decode corner" in {
      corner.asJson.noSpaces mustBe cornerJson
      decode[Bet](cornerJson) mustBe Right(corner)
    }
    "encode and decode trio" in {
      trio.asJson.noSpaces mustBe trioJson
      decode[Bet](trioJson) mustBe Right(trio)
    }
    "encode and decode basket" in {
      println(basket.asJson.noSpaces)
      basket.asJson.noSpaces mustBe basketJson
      decode[Bet](basketJson) mustBe Right(basket)
    }

    "not parse invalid json string into bet" in {
      val invalidJson = """{"invalid-key":0,"betAmount":20,"betType":"Straight"}"""
      decode[Bet](invalidJson).isLeft mustBe true
    }

    "parse BetPosition into json number" in {
      BetPosition(0).asJson.noSpaces mustBe "0"
    }

    "parse json number into betPosition" in {
      decode[BetPosition]("20") mustBe Right(BetPosition(20))
    }

    "encode and decode Chips" in {
      chips.asJson.noSpaces mustBe chipsJson
      decode[Chips](chipsJson) mustBe Right(chips)
    }



  }
}

object BetTest {

  val betAmount: Chips = Chips(20)
  val position1: BetPosition = BetPosition(1)
  val straight: Bet = Straight(List(position1), betAmount)
  val straightJson = """{"positions":[1],"betAmount":20,"betType":"Straight"}"""

  val oddPositions: List[BetPosition] = (1 to 35 by 2).toList.map(BetPosition(_))
  val odd: Bet = Odd(oddPositions, betAmount)
  val oddJson = """{"positions":[1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35],"betAmount":20,"betType":"Odd"}"""

  val evenPositions: List[BetPosition] = (2 to 36 by 2).toList.map(BetPosition(_))
  val even: Bet = Even(oddPositions, betAmount)
  val evenJson = """{"positions":[1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35],"betAmount":20,"betType":"Even"}"""

  val highPositions: List[BetPosition] = (1 to 18).toList.map(BetPosition(_))
  val high: Bet = High(highPositions, betAmount)
  val highJson = """{"positions":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18],"betAmount":20,"betType":"High"}"""

  val lowPositions: List[BetPosition] = (19 to 36).toList.map(BetPosition(_))
  val low: Bet = Low(lowPositions, betAmount)
  val lowJson = """{"positions":[19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36],"betAmount":20,"betType":"Low"}"""

  val rowPositions: List[BetPosition] = (1 to 34 by 3).toList.map(BetPosition(_))
  val row: Bet = Row(rowPositions, betAmount)
  val rowJson = """{"positions":[1,4,7,10,13,16,19,22,25,28,31,34],"betAmount":20,"betType":"Row"}"""

  val dozenPositions: List[BetPosition] = (1 to 12).toList.map(BetPosition(_))
  val dozen: Bet = Dozen(dozenPositions, betAmount)
  val dozenJson = """{"positions":[1,2,3,4,5,6,7,8,9,10,11,12],"betAmount":20,"betType":"Dozen"}"""

  val redPositions: List[BetPosition] = List(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).map(BetPosition(_))
  val red: Bet = Red(redPositions, betAmount)
  val redJson = """{"positions":[1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36],"betAmount":20,"betType":"Red"}"""

  val blackPositions: List[BetPosition] =List(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35).map(BetPosition(_))
  val black: Bet = Black(blackPositions, betAmount)
  val blackJson = """{"positions":[2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35],"betAmount":20,"betType":"Black"}"""

  val splitPositions: List[BetPosition] = List(2, 1).map(BetPosition(_))
  val split: Bet = Split(splitPositions, betAmount)
  val splitJson = """{"positions":[2,1],"betAmount":20,"betType":"Split"}"""

  val streetPositions: List[BetPosition] = List(3, 2, 1).map(BetPosition(_))
  val street: Bet = Street(streetPositions, betAmount)
  val streetJson = """{"positions":[3,2,1],"betAmount":20,"betType":"Street"}"""

  val sixLinePositions: List[BetPosition] = List(3, 2, 1, 4, 5, 6).map(BetPosition(_))
  val sixLine: Bet = SixLine(sixLinePositions, betAmount)
  val sixLineJson = """{"positions":[3,2,1,4,5,6],"betAmount":20,"betType":"SixLine"}"""

  val cornerPositions: List[BetPosition] = List(2, 1, 4, 5).map(BetPosition(_))
  val corner: Bet = Corner(cornerPositions, betAmount)
  val cornerJson = """{"positions":[2,1,4,5],"betAmount":20,"betType":"Corner"}"""

  val trioPositions: List[BetPosition] = List(2,1,0).map(BetPosition(_))
  val trio: Bet = Trio(trioPositions, betAmount)
  val trioJson = """{"positions":[2,1,0],"betAmount":20,"betType":"Trio"}"""

  val basketPositions: List[BetPosition] = List(2,1,0, 3).map(BetPosition(_))
  val basket: Bet = Basket(basketPositions, betAmount)
  val basketJson = """{"positions":[2,1,0,3],"betAmount":20,"betType":"Basket"}"""

  val chips: Chips = Chips(40)
  val chipsJson = "40"
}
