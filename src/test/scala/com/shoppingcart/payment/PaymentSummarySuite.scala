package com.shoppingcart.payment

import munit.CatsEffectSuite

class PaymentSummarySuite extends CatsEffectSuite {

  test("round payment amount") {
    assertEquals(PaymentSummary.rounding(1.234), 1.23)
    assertEquals(PaymentSummary.rounding(1.235), 1.24)
    assertEquals(PaymentSummary.rounding(1.0), 1.0)
  }

  test("compute payment tax") {
    assertEquals(PaymentSummary.tax(10.11), 1.26)
    assertEquals(PaymentSummary.tax(0.11), 0.01)
    assertEquals(PaymentSummary.tax(0.01), 0.0)
  }

  test("compute payment") {
    val payment = PaymentSummary.compute(33.999)
    assertEquals(payment.subtotal, 33.999)
    assertEquals(payment.tax, 4.25)
    assertEquals(payment.total, 38.25)
  }
}
