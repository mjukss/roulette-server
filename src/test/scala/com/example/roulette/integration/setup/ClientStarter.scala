package com.example.roulette.integration.setup

import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxParallelSequence1
import com.example.roulette.response.Response
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.http4s.Uri
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.jdkhttpclient.{JdkWSClient, WSClient, WSFrame, WSRequest}

import java.net.http.HttpClient

object ClientStarter {

  private def buildWSClient(client: PlayerConnection, wsClient: WSClient[IO]) = {
    val domain = uri"ws://localhost:8080/"
    val uri = Uri.fromString(s"$domain${client.username.value}").getOrElse(domain)

    wsClient.connectHighLevel(WSRequest(uri))
      .use { conn =>
        val receive = conn.receiveStream.collect {
          case WSFrame.Text(s, _) => decode[Response](s).toOption
        }

        val send = conn.sendMany(client.requests.map(request => WSFrame.Text(request.asJson.noSpaces)))

        for {
          _ <- send
          received <- receive.take(client.msgLimit).compile.toList.map(_.flatten)
          _ <- IO.sleep(client.stayConnected)
        } yield received

      }
  }

  def connectToServer(clients: List[PlayerConnection]): IO[List[Response]] = {
    val webSocket = Resource.eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkWSClient[IO](_))

    webSocket.use { wsClient =>
      clients.map(buildWSClient(_, wsClient))

      for {
        responses <- clients
          .map(client => IO.sleep(client.delay) *> buildWSClient(client, wsClient))
          .parSequence
      } yield responses.flatten

    }
  }
}
