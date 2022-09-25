package com.shoppingcart.service

trait Logger[F[_]] {
  def log(line: String): F[Unit]
}

object Logger {
  def apply[F[_]](implicit F: Logger[F]): Logger[F] = F

  import cats.effect.IO
  // Implement a simple console logger
  implicit val ConsoleLoggerIO: Logger[IO] = (line: String) => IO { println(s" $line") }
}
