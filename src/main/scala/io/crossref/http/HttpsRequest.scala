package io.crossref.http

import dispatch._
import org.json4s._
import scribe._

import scala.concurrent.ExecutionContext.Implicits.global

trait HttpsRequest {

  private implicit val formats: Formats = DefaultFormats
  protected val request: Req

  def apply[R: Manifest](): Future[R] = {
    for (response <- get[R](request))
      yield response
  }

  private def toJson[R: Manifest](resp: JValue): R = {
    info(s"$resp")
    resp.extract[R]
  }

  protected def get[R: Manifest](req: Req): Future[R] = {
    info(s"Request made for URL: ${req.url}")
    for {
      resp <- Http.default(req OK as.json4s.Json)
      data = toJson[R](resp)
    } yield data
  }
}
