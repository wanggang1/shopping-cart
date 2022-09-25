package com.shoppingcart

import cats.effect.IOApp
import cats.effect.IO
import com.shoppingcart.domain.StoreDomain.ShoppingCart
import com.shoppingcart.service.{ Logger, ShoppingSession }

object Main extends IOApp.Simple {
  import ShoppingSession._

  val purchase1: IO[Unit] =
    for {
      cart1 <- add[IO]("cornflakes", 2, ShoppingCart())
      cart2 <- add[IO]("weetabix", 1, cart1)
      ps    <- checkout(cart2)
      unit  <- Logger[IO].log(ps.toString)
    } yield unit

  val purchase2: IO[Unit] =
    for {
      cart1 <- add[IO]("cornflakes", 2, ShoppingCart())
      cart2 <- add[IO]("weetabix", 1, cart1)
      cart3 <- add[IO]("Cheerios", 3, cart2)
      cart4 <- add[IO]("candy", 1, cart3)
      cart5 <- add[IO]("weetabix", 1, cart4)
      ps    <- checkout(cart5)
      unit  <- Logger[IO].log(ps.toString)
    } yield unit

  def run: IO[Unit] = purchase1

}
