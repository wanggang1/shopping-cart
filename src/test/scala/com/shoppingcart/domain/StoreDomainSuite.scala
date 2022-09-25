package com.shoppingcart.domain

import munit.CatsEffectSuite

class StoreDomainSuite extends CatsEffectSuite {
  import StoreDomain._

  test("combine quantities properly") {
    import cats.Monoid
    import cats.syntax.monoid._

    assertEquals(Monoid[Quantity].empty, Quantity(0))

    val item1 = Quantity(1, 1.99)
    val item3 = Quantity(3, 1.99)
    assertEquals(item1 |+| item3, Quantity(4, 1.99))
    assertEquals(item3 |+| item1, Quantity(4, 1.99))

    assertEquals(Monoid[Quantity].empty |+| item3, item3)
    assertEquals(item3 |+| Monoid[Quantity].empty, item3)
  }

}
