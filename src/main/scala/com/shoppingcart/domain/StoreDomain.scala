package com.shoppingcart.domain

object StoreDomain {

  object Quantity {
    import cats.Monoid

    private val DefaultUnitPrice = 0.0

    implicit val monoidStoreItem: Monoid[Quantity] =
      new Monoid[Quantity] {
        def combine(x: Quantity, y: Quantity): Quantity =
          x.unitPrice match {
            case DefaultUnitPrice => y.copy(quantity = x.quantity + y.quantity)
            case _                => x.copy(quantity = x.quantity + y.quantity)
          }

        def empty: Quantity = Quantity(0)
      }
  }
  case class Quantity(quantity: Int, unitPrice: Double = Quantity.DefaultUnitPrice)

  case class ShoppingCart(items: Map[String, Quantity] = Map.empty) extends AnyVal
}
