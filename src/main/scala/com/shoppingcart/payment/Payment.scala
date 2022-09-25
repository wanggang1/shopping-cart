package com.shoppingcart.payment

trait Payment[F[_]] {
  def pay(amount: Double): F[PaymentSummary]
}

object Payment {
  def apply[F[_]](implicit F: Payment[F]): Payment[F] = F

  import cats.effect.IO
  // The real Payment will submit to the payment system, but here just simply compute the payment summary
  implicit val PaymentIO: Payment[IO] = (amount: Double) => IO { PaymentSummary.compute(amount) }
}
