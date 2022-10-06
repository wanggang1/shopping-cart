package com.shoppingcart.http

import io.circe.Printer
import org.http4s.circe.CirceInstances

object Http4sCirceInstances {
  val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  val circeInstances: CirceInstances =
    CirceInstances.withPrinter(printer).build
}
