package com.shoppingcart.http

import cats.effect.IO
import com.shoppingcart.service.Logger
import munit.CatsEffectSuite
import org.http4s.Uri

object HttpClientSuite {
  val successResp = Response("Cornflakes", 1.99)
  implicit val SuccessHttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri)(implicit logger: Logger[IO]): IO[Either[String, Response]] = IO { Right(successResp) }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }

  val errorMsg = "404 not found"
  implicit val FailedHttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri)(implicit logger: Logger[IO]): IO[Either[String, Response]] = IO { Left(errorMsg) }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }
}

class HttpClientSuite extends CatsEffectSuite {

  test("get price successfully") {
    import HttpClientSuite.SuccessHttpClientIO

    HttpClient[IO].getPrice("cornflakes").map {
      result =>  assertEquals(result, Right(HttpClientSuite.successResp))
    }
  }

  test("get price failed") {
    import HttpClientSuite.FailedHttpClientIO

    HttpClient[IO].getPrice("candy").map {
      result =>  assertEquals(result, Left("404 not found"))
    }
  }

  test("bad item name") {
    import HttpClientSuite.SuccessHttpClientIO

    HttpClient[IO].getPrice("中").map {
      result =>  assertEquals(result, Left("Bad item name: 中"))
    }
  }

}
