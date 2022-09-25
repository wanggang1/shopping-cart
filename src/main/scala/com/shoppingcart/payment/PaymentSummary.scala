package com.shoppingcart.payment

object PaymentSummary {
  import scala.math.BigDecimal

  val TAX: Double = 0.125

  def rounding(value: Double): Double =
    BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def tax(subtotal: Double): Double = rounding(subtotal * TAX)

  def compute(subtotal: Double): PaymentSummary = {
    val taxAmount = tax(subtotal)
    val rounded   = rounding(subtotal)
    new PaymentSummary(subtotal, taxAmount, rounded + taxAmount)
  }
}

class PaymentSummary private(val subtotal: Double, val tax: Double, val total: Double) {
  override def toString: String = {
    val formatTotal = f"$total%1.2f"
    s"""
      | subtotal: $subtotal
      | tax:      $tax
      | total:    $formatTotal
      |""".stripMargin
  }
}