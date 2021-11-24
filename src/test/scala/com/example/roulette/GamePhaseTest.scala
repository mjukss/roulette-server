package com.example.roulette

import com.example.roulette.GamePhaseTest.{betsClosed, betsClosedJson, betsOpen, betsOpenJson}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GamePhaseTest extends AnyWordSpec with Matchers {

  "GamePhase json validation" should {
    "encode and decode BetsOpen" in {
      betsOpen.asJson.noSpaces mustBe betsOpenJson
      decode[GamePhase](betsOpenJson) mustBe Right(betsOpen)
    }
    "encode and decode BetsClosed" in {
      betsClosed.asJson.noSpaces mustBe betsClosedJson
      decode[GamePhase](betsClosedJson) mustBe Right(betsClosed)
    }
  }

}

object GamePhaseTest {

  import GamePhase._

  val betsOpen: GamePhase = BetsOpen
  val betsOpenJson = """"BetsOpen""""

  val betsClosed: GamePhase = BetsClosed
  val betsClosedJson = """"BetsClosed""""
}