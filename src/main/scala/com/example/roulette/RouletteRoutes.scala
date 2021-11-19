package com.example.roulette

import cats.effect.Async
import cats.effect.std.Queue
import cats.syntax.all._
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

object RouletteRoutes {
  def gameRoutes[F[_] : Async](q: Queue[F, Option[FromClient]], t: Topic[F, ToClient])(wsb: WebSocketBuilder2[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "game" / username =>
        val toClient: Stream[F, WebSocketFrame] = t.subscribe(1000).map(tc => WebSocketFrame.Text(tc.message))
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.collect {
          // TODO: need better way to match when user connected
          case WebSocketFrame.Text("start", _) => FromClient(username, "joined the game") // TODO: add user to state
          case WebSocketFrame.Text(text, _) => FromClient(username, text) // TODO: bet request that adds bet to the state
          case WebSocketFrame.Close(_) => FromClient(username, s"$username gone") // TODO: remove user from the state
        }.enqueueNoneTerminated(q)

        for {
          wsResponse <- wsb.build(toClient, fromClient)
        } yield wsResponse

    }
  }

}
