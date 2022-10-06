package com.shoppingcart.http

import io.circe.{ Decoder, Encoder, HCursor, Json }

object Response {

  implicit val encodeFoo: Encoder[Response] = new Encoder[Response] {
    final def apply(a: Response): Json = Json.obj(
      ("title", Json.fromString(a.title)),
      ("price", Json.fromDoubleOrNull(a.price))
    )
  }

  implicit val decoderInstance: Decoder[Response] = new Decoder[Response] {
    final def apply(c: HCursor): Decoder.Result[Response] =
      for {
        title <- c.downField("title").as[String]
        price <- c.downField("price").as[Double]
      } yield {
        new Response(title, price)
      }
  }

}

case class Response(title: String, price: Double)
