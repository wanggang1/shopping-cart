package com.shoppingcart.service

import cats.Monad
import com.shoppingcart.domain.StoreDomain.{ Quantity, ShoppingCart }
import com.shoppingcart.http.HttpClient
import com.shoppingcart.payment.{ Payment, PaymentSummary }

object ShoppingSession {
  import cats.implicits.{ catsSyntaxSemigroup, toFlatMapOps, toFunctorOps }

  def add[F[_]: Monad: HttpClient: Logger](item: String, quantity: Int, cart: ShoppingCart): F[ShoppingCart] = {
    val lItem = item.toLowerCase
    cart.items.get(lItem) match {
      case Some(q) =>
        Logger[F].log(s"Add $quantity × $lItem @ ${q.unitPrice} each").map { _ =>
          cart.copy(items = cart.items.updated(lItem, q |+| Quantity(quantity)))
        }
      case None =>
        HttpClient[F].getPrice(lItem).flatMap {
          case Right(resp) =>
            Logger[F].log(s"Add $quantity × $lItem @ ${resp.price} each").map { _ =>
              cart.copy(items = cart.items.updated(lItem, Quantity(quantity, resp.price)))
            }
          case Left(_) =>
            Logger[F].log(s"Item not in stock: $item").map{ _ => cart }
        }
    }
  }

  def remove[F[_]: Monad: Logger](item: String, quantity: Int, cart: ShoppingCart): F[ShoppingCart] = {
    val lItem = item.toLowerCase
    cart.items.get(lItem) match {
      case Some(q) =>
        if (q.quantity > quantity)
          Logger[F].log(s"Remove $quantity × $lItem from cart").map { _ =>
            cart.copy(items = cart.items.updated(lItem, Quantity(q.quantity - quantity, q.unitPrice)))
          }
        else
          Logger[F].log(s"Remove all $lItem from cart").map { _ =>
            cart.copy(items = cart.items - lItem)
          }
      case None =>
        Logger[F].log(s"Not in cart, $lItem").map { _ => cart }
    }
  }

  def checkout[F[_]: Payment](cart: ShoppingCart): F[PaymentSummary] = {
    val totalCost = cart.items.values.foldLeft(0.0){ (total, q) => total + q.quantity * q.unitPrice }
    Payment[F].pay(totalCost)
  }

}
