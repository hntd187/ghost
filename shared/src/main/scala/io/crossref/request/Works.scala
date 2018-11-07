package io.crossref.request

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import io.circe._
import io.circe.generic.auto._
import io.crossref.JsonObjects
import io.crossref.http.HttpRequest

case class Works(doi: Option[String])(implicit d: Decoder[JsonObjects.ItemResponse],
                                      backend: SttpBackend[Future, Nothing],
                                      execution: ExecutionContext)
    extends HttpRequest[JsonObjects.ItemResponse] {

  lazy protected val reqUri: Uri = uri"$baseUri/works/$doi"

}

import scala.concurrent.ExecutionContext.Implicits.global
object te {
  def main(args: Array[String]) {

    implicit val backend: SttpBackend[Future, Nothing] = AsyncHttpClientFutureBackend()

    val w = Works(None)
    println(Await.ready(w(), 10 seconds))
//    println(parser.parse("[[1,2,3]]").right.get.as[List[List[Json]]].right.get(0)(0).getClass)
    backend.close()
  }
}
