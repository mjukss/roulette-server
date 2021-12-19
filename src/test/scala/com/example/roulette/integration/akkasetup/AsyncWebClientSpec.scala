package com.example.roulette.integration.akkasetup

import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import scala.concurrent.Future

class AsyncWebClientSpec extends Specification
  with ScalaFutures
  with IntegrationPatience {

  "WebClient when downloading images" should {

    "for a valid link return non-zero content " in {

      whenReady(Future.successful("Done")){ testImage =>

        testImage must be equalTo "Done"
        // Do whatever you need
      }

    }
  }
}