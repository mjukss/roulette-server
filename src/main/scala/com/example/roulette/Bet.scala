package com.example.roulette

case class Bet(betType: BetType, chips: Int, position: Option[Int])
