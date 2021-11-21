package com.example.roulette

import cats.effect.Async
import cats.effect.std.Queue
import cats.syntax.all._
import com.example.roulette.Player.Username
import com.example.roulette.Request.{RemovePlayer, RequestOrError}
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object RouletteRoutes {
  def gameRoutes[F[_] : Async](q: Queue[F, Option[RequestOrError]], t: Topic[F, Response])(wsb: WebSocketBuilder2[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "game" / username =>
        val toClient: Stream[F, WebSocketFrame] = t.subscribe(1000).map { response =>
          Text((response : Response).asJson.deepDropNullValues.noSpaces)
        }
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.collect {
          case Text(text, _) => Request.fromString(text)
          case Close(_) => Right(RemovePlayer(Username(username)))
        }.enqueueNoneTerminated(q)

        for {
          wsResponse <- wsb.build(toClient, fromClient)
        } yield wsResponse
    }
  }

}
