package com.shoppingcart.http

import cats.{ Applicative, MonadThrow }
import cats.effect.{ Async, IO }
import org.http4s.{ Response => Http4sResponse, _ }
import org.http4s.headers.`Content-Type`
import com.shoppingcart.service.Logger
import fs2.Chunk
import io.circe.{ Decoder, Encoder, Json }

trait HttpClient[F[_]] {
  import HttpClient.{ PriceUriPrefix, JsonExtension }

  def getPrice(item: String): F[Either[String, Response]] = {
    val priceUrl : Either[ParseFailure, Uri] = Uri.fromString(s"$PriceUriPrefix$item$JsonExtension")
    priceUrl match {
      case Right(url) => call(url)
      case Left(_)    => error(s"Bad item name: $item")
    }
  }

  def call(url: Uri): F[Either[String, Response]]

  def error(message: String): F[Either[String, Response]]
}

object HttpClient {
  import cats.implicits.toFunctorOps
  import org.http4s.ember.client._

  implicit def syncEntityJsonEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T] =
    EntityEncoder[F, Chunk[Byte]]
      .contramap[Json] { json =>
        val bytes = Http4sCirceInstances.printer.printToByteBuffer(json)
        Chunk.byteBuffer(bytes)
      }
      .withContentType(`Content-Type`(MediaType.application.json))
      .contramap(t => Encoder.apply[T].apply(t))

  implicit def asyncEntityJsonDecoder[F[_]: Async, T: Decoder]: EntityDecoder[F, T] =
    Http4sCirceInstances.circeInstances.jsonOf[F, T]

  val PriceUriPrefix = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/"
  val JsonExtension  = ".json"

  def apply[F[_]](implicit F: HttpClient[F]): HttpClient[F] = F

  def respHandler[F[_]: Async: MonadThrow: Logger](resp: Http4sResponse[F]): F[Either[String, Response]] =
    resp match {
      case Status.Successful(r) =>
        r.attemptAs[Response].leftMap(_.message).value
      case failure =>
        val errMsg = s"Failed. Status ${failure.status.code}"
        Logger[F].log(errMsg).map { _ => Left(errMsg) }
    }

  /**
   *  Implement HttpClient using EmberClientBuilder, which manages a connection pool of http connections
   *  and speaks HTTP 1.x
   */
  implicit val HttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri): IO[Either[String, Response]] =
      EmberClientBuilder.default[IO].build.use { httpClient =>
        // use `httpClient` here and return an `IO`. the httpClient will be acquired and released
        // automatically each time the `IO` is run.
        val req = Request[IO](method = Method.GET, uri = url)
        httpClient.run(req).use { resp =>
          respHandler(resp)
        }
        // `httpClient` is released and returned to the pool
      }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }
}
