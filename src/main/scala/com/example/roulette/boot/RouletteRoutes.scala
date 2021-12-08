package com.example.roulette.boot

import cats.Monad
import cats.data.OptionT
import cats.effect.std.Queue
import com.example.roulette.player.Player.Username
import com.example.roulette.player.PlayersCache
import com.example.roulette.request.Request
import com.example.roulette.request.Request.{RemovePlayer, RequestOrError}
import com.example.roulette.response.BadRequestMessage.UsernameTaken
import com.example.roulette.response.{Response, ResponseProcessor}
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object RouletteRoutes {
  def gameRoutes[F[_] : Monad](
                                     playersCache: PlayersCache[F],
                                     q: Queue[F, Option[(Username, RequestOrError)]],
                                     t: Topic[F, Response])(wsb: WebSocketBuilder2[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / username =>
        val toClient: Stream[F, WebSocketFrame] = t.subscribe(1000)
          .collect(ResponseProcessor.toWebSocketText(Username(username)))

        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.collect {
          case Text(text, _) => (Username(username), Request.fromString(text))
          case Close(_) => (Username(username), Right(RemovePlayer))
        }.enqueueNoneTerminated(q)

        val usernameTakenJson = (Response.BadRequest(Username(username), UsernameTaken): Response).asJson.noSpaces
        val usernameTakenStream: Stream[F, WebSocketFrame] = Stream.emit(Text(usernameTakenJson)).take(1)

        (for {
          _ <- OptionT(playersCache.readOne(Username(username)))
          forbidden <- OptionT.liftF(wsb.build(usernameTakenStream, fromClient))
        } yield forbidden) getOrElseF wsb.build(toClient, fromClient)

    }
  }
}
