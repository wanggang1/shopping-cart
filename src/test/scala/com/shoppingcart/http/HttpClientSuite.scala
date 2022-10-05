package com.shoppingcart.http

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.{ Status, Uri, Response => Http4sResponse }

object HttpClientSuite {
  val successResp = Response("Cornflakes", 1.99)
  implicit val SuccessHttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri): IO[Either[String, Response]] = IO { Right(successResp) }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }

  val errorMsg = "404 not found"
  implicit val FailedHttpClientIO: HttpClient[IO] = new HttpClient[IO] {
    def call(url: Uri): IO[Either[String, Response]] = IO { Left(errorMsg) }

    def error(message: String): IO[Either[String, Response]] = IO { Left(message) }
  }
}

class HttpClientSuite extends CatsEffectSuite {
  import HttpClient.syncEntityJsonEncoder

  test("response handler") {
    val response = Response("bread", 1.99)
    val httpResp = Http4sResponse[IO](Status.Ok).withEntity(response)
    HttpClient.respHandler[IO](httpResp).map {
      result => assertEquals(result, Right(response))
    }
  }

  test("response handler for empty") {
    val emptyResp = Http4sResponse[IO]()
    HttpClient.respHandler[IO](emptyResp).map {
      result => assertEquals(result, Left("Malformed message body: Invalid JSON: empty body"))
    }
  }

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
