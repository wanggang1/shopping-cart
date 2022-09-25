package com.shoppingcart.http

import org.http4s.{ EntityDecoder, Method, ParseFailure, Request, Status, Uri }
import com.shoppingcart.service.Logger

trait HttpClient[F[_]] {
  import HttpClient.{ PriceUriPrefix, JsonExtension }

  def getPrice(item: String)(implicit logger: Logger[F]): F[Either[String, Response]] = {
    val priceUrl : Either[ParseFailure, Uri] = Uri.fromString(s"$PriceUriPrefix$item$JsonExtension")
    priceUrl match {
      case Right(url) => call(url)
      case Left(_)    => error(s"Bad item name: $item")
    }
  }

  def call(url: Uri)(implicit logger: Logger[F]): F[Either[String, Response]]

  def error(message: String): F[Either[String, Response]]
}

object HttpClient {
  import cats.effect.IO
  import org.http4s.ember.client._
  import org.http4s.circe._
  import io.circe.generic.auto._

  implicit val authResponseEntityDecoder: EntityDecoder[IO, Response] = jsonOf

  val PriceUriPrefix = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/"
  val JsonExtension  = ".json"

  def apply[F[_]](implicit F: HttpClient[F]): HttpClient[F] = F

  // Implement HttpClient using EmberClientBuilder, which manages a connection pool of http connections
  implicit val HttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri)(implicit logger: Logger[IO]): IO[Either[String, Response]] =
      EmberClientBuilder.default[IO].build.use { httpClient =>
        val req = Request[IO](method = Method.GET, uri = url)
        httpClient.run(req).use {
            case Status.Successful(r) =>
              r.attemptAs[Response].leftMap(_.message).value
            case failure =>
              failure.as[String].map { b =>
                val errMsg = s"Failed Status ${failure.status.code}, Response: $b"
                logger.log(errMsg)
                Left(errMsg)
              }
        }
      }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }
}
