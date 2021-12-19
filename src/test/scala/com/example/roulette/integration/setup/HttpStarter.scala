package com.example.roulette.integration.setup

import cats.effect.{IO, Resource}
import cats.implicits.toTraverseOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.dsl.io.POST
import org.http4s.jdkhttpclient._
import org.http4s.{Request, Uri}

import java.net.http.HttpClient

object HttpStarter  {
  type ClientBody = com.example.roulette.request.Request

  private def buildHttpClient(uri: Uri, body: ClientBody, client: Client[IO]) = {
    val request = Request[IO](method = POST, uri).withEntity(body)
    client.expect[String](request).attempt.map(_.toOption)
  }

  def run(connection: List[(Uri, ClientBody)]) = {
    val httpClient = Resource.eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkHttpClient[IO](_))

    httpClient.use { client =>
      connection.map {
        case (uri, body) => buildHttpClient(uri, body, client)
      }.sequence
    }
  }
}
