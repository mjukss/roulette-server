package com.example.roulette.client

import com.example.roulette.player.Player.Username
import com.example.roulette.request.Request

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class ClientConnection(username: Username,
                                  requests: List[Request],
                                  msgLimit: Long, delay: FiniteDuration = 0.seconds,
                                  stayConnected: FiniteDuration = 0.seconds
                                 )