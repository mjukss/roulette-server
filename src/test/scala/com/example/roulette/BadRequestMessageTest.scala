package com.example.roulette

import com.example.roulette.BadRequestMessageTest._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BadRequestMessageTest extends AnyWordSpec with Matchers {

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
}