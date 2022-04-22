package com.example.roulette.router.rest

import cats.effect.kernel.Concurrent
import cats.implicits._
import com.example.roulette.player.PlayersCache
import com.example.roulette.request.HttpRequestProcessor
import com.example.roulette.request.Request.{ RegisterPlayer, RemovePlayer }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class RestRoutes[F[_]: Concurrent](playersCache: PlayersCache[F]) extends Http4sDsl[F] {

  val httpRequestProcessor: HttpRequestProcessor[F] = HttpRequestProcessor.make()

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "register" =>
      for {
        reqBody  <- req.as[RegisterPlayer]
        response <- httpRequestProcessor.handleRequest(playersCache)(reqBody)
      } yield response

    case req @ POST -> Root / "remove" =>
      for {
        reqBody  <- req.as[RemovePlayer]
        response <- httpRequestProcessor.handleRequest(playersCache)(reqBody)
      } yield response
  }
}

object RestRoutes {
  def make[F[_]: Concurrent](playersCache: PlayersCache[F]): HttpRoutes[F] = new RestRoutes(playersCache).routes
}
