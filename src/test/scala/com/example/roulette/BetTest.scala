package com.example.roulette

import com.example.roulette.Bet.{BetPosition, Chips}
import com.example.roulette.BetTest.{bets, chips, chipsJson, jsonBets}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BetTest extends AnyWordSpec with Matchers {
  "Bet json validation" should {
    "parse bets into json strings" in {
     val betEncodingMatchJson = bets.zip(jsonBets).forall {
       case (bet, betJson) => (bet: Bet).asJson.noSpaces == betJson
     }
      betEncodingMatchJson.mustBe(true)
    }
    "parse json string into bet" in {
      val jsonDecodingMatchBet = bets.zip(jsonBets).forall {
        case (bet, betJson) => decode[Bet](betJson) == Right(bet)
      }
      jsonDecodingMatchBet.mustBe(true)
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

  import Bet._

  val chips: Chips = Chips(40)
  val chipsJson = "40"

  val straightBet: Straight = Straight(BetPosition(0), Chips(20))
  val oddBet: Odd = Odd(Chips(20))
  val evenBet: Even = Even(Chips(20))
  val highBet: High = High(Chips(30))
  val lowBet: Low = Low(Chips(50))
  val rowBet: Row = Row(BetPosition(1), Chips(10))
  val dozen: Dozen = Dozen(BetPosition(3), Chips(5))
  val red: Red = Red(Chips(40))
  val black: Black = Black(Chips(2))
  val split: Split = Split(List(BetPosition(1), BetPosition(2)), Chips(5))
  val street: Street = Street(BetPosition(1), Chips(15))
  val sixLine: SixLine = SixLine(BetPosition(1), Chips(15))
  val corner: Corner = Corner(List(BetPosition(1), BetPosition(2), BetPosition(4), BetPosition(5)), Chips(15))
  val trio: Trio = Trio(BetPosition(1), Chips(4))
  val basket: Basket = Basket(Chips(4))

  val jsonBets = List(
    """{"position":0,"betAmount":20,"betType":"Straight"}""",
    """{"betAmount":20,"betType":"Odd"}""",
    """{"betAmount":20,"betType":"Even"}""",
    """{"betAmount":30,"betType":"High"}""",
    """{"betAmount":50,"betType":"Low"}""",
    """{"position":1,"betAmount":10,"betType":"Row"}""",
    """{"position":3,"betAmount":5,"betType":"Dozen"}""",
    """{"betAmount":40,"betType":"Red"}""",
    """{"betAmount":2,"betType":"Black"}""",
    """{"positions":[1,2],"betAmount":5,"betType":"Split"}""",
    """{"position":1,"betAmount":15,"betType":"Street"}""",
    """{"position":1,"betAmount":15,"betType":"SixLine"}""",
    """{"positions":[1,2,4,5],"betAmount":15,"betType":"Corner"}""",
    """{"position":1,"betAmount":4,"betType":"Trio"}""",
    """{"betAmount":4,"betType":"Basket"}"""

  )

  val bets: List[Bet] = List(
    straightBet,
    oddBet,
    evenBet,
    highBet,
    lowBet,
    rowBet,
    dozen,
    red,
    black,
    split,
    street,
    sixLine,
    corner,
    trio,
    basket
  )
}
