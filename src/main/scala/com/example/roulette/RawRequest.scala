package com.example.roulette

import com.example.roulette.Player.Username
import io.circe

final case class RawRequest(username: Username, eitherRequest: Either[circe.Error, Request])


