package com.example.roulette.wheel

import scala.annotation.tailrec
import scala.util.Try

final case class WheelRange(from: Int = 0, to: Int = 37)

object WheelRange {

  @tailrec
  def fromCommandLineArgs(args: List[String]): WheelRange = args match {
    case "lucky-number" :: num :: _ => Try {
      WheelRange(num.toInt, num.toInt + 1)
    } getOrElse WheelRange()
    case Nil => WheelRange()
    case _ :: xs => fromCommandLineArgs(xs)
  }
}
