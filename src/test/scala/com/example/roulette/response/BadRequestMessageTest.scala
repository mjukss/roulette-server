package com.example.roulette.response

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BadRequestMessageTest extends AnyWordSpec with Matchers {
  import BadRequestMessageTest._

  "BadRequestMessage json validation" should {
    "encode and decode UsernameTaken" in {
      usernameTaken.asJson.noSpaces mustBe usernameTakenJson
      decode[BadRequestMessage](usernameTakenJson) mustBe Right(usernameTaken)
    }
    "encode and decode UsernameDoesNotExist" in {
      usernameDoesNotExist.asJson.noSpaces mustBe usernameDoesNotExistJson
      decode[BadRequestMessage](usernameDoesNotExistJson) mustBe Right(usernameDoesNotExist)
    }
    "encode and decode InsufficientFunds" in {
      insufficientFunds.asJson.noSpaces mustBe insufficientFundsJson
      decode[BadRequestMessage](insufficientFundsJson) mustBe Right(insufficientFunds)
    }
    "encode and decode CustomBadRequestMessage" in {
      customBadRequestMessage.asJson.noSpaces mustBe customBadRequestMessageJson
      decode[BadRequestMessage](customBadRequestMessageJson) mustBe Right(customBadRequestMessage)
    }
    "encode and decode RequestIsInvalid" in {
      requestIsInvalid.asJson.noSpaces mustBe requestIsInvalidJson
      decode[BadRequestMessage](requestIsInvalidJson) mustBe Right(requestIsInvalid)
    }
    "encode and decode WrongPassword" in {
      wrongPassword.asJson.noSpaces mustBe wrongPasswordJson
      decode[BadRequestMessage](wrongPasswordJson) mustBe Right(wrongPassword)
    }
    "encode and decode UserAlreadyPlaying" in {
      userAlreadyPlaying.asJson.noSpaces mustBe userAlreadyPlayingJson
      decode[BadRequestMessage](userAlreadyPlayingJson) mustBe Right(userAlreadyPlaying)
    }
    "encode and decode CanNotPlaceBetInThisGamePhase" in {
      canNotPlaceBetInThisGamePhase.asJson.noSpaces mustBe canNotPlaceBetInThisGamePhaseJson
      decode[BadRequestMessage](canNotPlaceBetInThisGamePhaseJson) mustBe Right(canNotPlaceBetInThisGamePhase)
    }
    "encode and decode InvalidBet" in {
      invalidBet.asJson.noSpaces mustBe invalidBetJson
      decode[BadRequestMessage](invalidBetJson) mustBe Right(invalidBet)
    }
  }

}

object BadRequestMessageTest {
  import BadRequestMessage._
  val usernameTaken: BadRequestMessage = UsernameTaken
  val usernameTakenJson = """"UsernameTaken""""

  val usernameDoesNotExist: BadRequestMessage = UsernameDoesNotExist
  val usernameDoesNotExistJson = """"UsernameDoesNotExist""""

  val insufficientFunds: BadRequestMessage = InsufficientFunds
  val insufficientFundsJson = """"InsufficientFunds""""

  val customBadRequestMessage: BadRequestMessage = CustomBadRequestMessage("error")
  val customBadRequestMessageJson = """"error""""

  val requestIsInvalid: BadRequestMessage = RequestIsInvalid
  val requestIsInvalidJson = """"RequestIsInvalid""""

  val wrongPassword: BadRequestMessage = WrongPassword
  val wrongPasswordJson = """"WrongPassword""""

  val userAlreadyPlaying: BadRequestMessage = UserAlreadyPlaying
  val userAlreadyPlayingJson = """"UserAlreadyPlaying""""

  val canNotPlaceBetInThisGamePhase: BadRequestMessage = CanNotPlaceBetInThisGamePhase
  val canNotPlaceBetInThisGamePhaseJson = """"CanNotPlaceBetInThisGamePhase""""

  val invalidBet: BadRequestMessage = InvalidBet
  val invalidBetJson = """"InvalidBet""""
}