package com.shoppingcart.service

import cats.effect.IO
import com.shoppingcart.domain.StoreDomain.{ Quantity, ShoppingCart}
import com.shoppingcart.http.HttpClientSuite
import munit.CatsEffectSuite

class ShoppingSessionSuite extends CatsEffectSuite {

  test("add items to shopping cart") {
    import HttpClientSuite.SuccessHttpClientIO

    for {
      cart1 <- ShoppingSession.add[IO]("Cornflakes", 2, ShoppingCart())
      cart2 <- ShoppingSession.add[IO]("Cornflakes", 1, cart1)
    } yield {
      val result = cart2.items.get("cornflakes")
      assertEquals(result, Some(Quantity(3, 1.99)))
    }
  }

  test("remove items from shopping cart") {
    import HttpClientSuite.SuccessHttpClientIO

    for {
      cart1 <- ShoppingSession.add[IO]("Cornflakes", 2, ShoppingCart())
      cart2 <- ShoppingSession.add[IO]("Cornflakes", 1, cart1)
      cart3 <- ShoppingSession.remove[IO]("Cornflakes", 2, cart2)
    } yield {
      val result = cart3.items.get("cornflakes")
      assertEquals(result, Some(Quantity(1, 1.99)))
    }
  }

  test("remove all items from shopping cart") {
    import HttpClientSuite.SuccessHttpClientIO

    for {
      cart1 <- ShoppingSession.add[IO]("Cornflakes", 2, ShoppingCart())
      cart2 <- ShoppingSession.add[IO]("Cornflakes", 1, cart1)
      cart3 <- ShoppingSession.remove[IO]("Cornflakes", 4, cart2)
    } yield {
      val result = cart3.items.get("cornflakes")
      assertEquals(result, None)
    }
  }

  test("remove items not in shopping cart") {
    import HttpClientSuite.SuccessHttpClientIO

    for {
      cart1 <- ShoppingSession.add[IO]("Cornflakes", 2, ShoppingCart())
      cart2 <- ShoppingSession.add[IO]("Cornflakes", 1, cart1)
      cart3 <- ShoppingSession.remove[IO]("Bread", 4, cart2)
    } yield {
      val result = cart3.items.get("cornflakes")
      assertEquals(result, Some(Quantity(3, 1.99)))
      val result2 = cart3.items.get("bread")
      assertEquals(result2, None)
    }
  }


  test("not add items to shopping cart when item can't be found") {
    import HttpClientSuite.FailedHttpClientIO

    ShoppingSession.add[IO]("Candy", 2, ShoppingCart()).map { result =>
      assertEquals(result, ShoppingCart(Map.empty))
    }
  }

  test("pay shopping items") {
    val cart = ShoppingCart(
      Map(
        "cornflakes" -> Quantity(2, 2.52),
        "weetabix"   ->  Quantity(1, 9.98)
      )
    )

    ShoppingSession.checkout[IO](cart).map { result =>
      assertEquals(result.subtotal, 15.02)
      assertEquals(result.tax, 1.88)
      assertEquals(result.total, 16.90)
    }
  }

}
