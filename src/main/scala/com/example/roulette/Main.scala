package com.example.roulette

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.example.roulette.RouletteServer.stream
import fs2.Stream
import fs2.concurrent.Topic


// TODO: implement multiple states (players, game status, maybe more)
case class State(messages: Int) // just a placeholder, will be replaced

case class FromClient(username: String, message: String)

case class ToClient(message: String)

object Main extends IOApp {

  def modifyState(fromClient: FromClient, ref: Ref[IO, State]): IO[ToClient] = {
    ref.modify { currentState =>
      val messages = currentState.messages
      val FromClient(username, message) = fromClient
      val nextState = State(messages + 1)
      val toClient = ToClient(s"[$messages][$username]: $message")
      (nextState, toClient)
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, Option[FromClient]]
      t <- Topic[IO, ToClient]
      ref <- IO.ref(State(0))

      exitCode <- {
        val messageStream = Stream
          .fromQueueNoneTerminated(q)
          .evalMap(fromClient => modifyState(fromClient, ref))
          .through(t.publish)  // if something in the queue, topic will publish message to all it's subscribers

        val combinedStream = Stream(messageStream, stream(q, t)).parJoinUnbounded // why unbounded?

        combinedStream.compile.drain.as(ExitCode.Success)
      }
    } yield exitCode
  }


}
