package io.crossref
package http

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import io.circe.Decoder
import scribe.Logging

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}

case class Error(code: Int, message: String)
case class ErrorCase(error: Error)

@implicitNotFound("Cannot find Spotify client, did you create one?")
private[crossref] abstract class HttpRequest[R](implicit d: Decoder[R], backend: SttpBackend[Future, Nothing], val execution: ExecutionContext) extends Logging {

  protected val reqUri: Uri
  protected val request: Req[R] = sttp.get(reqUri).response(asJson[R])

  def apply(): Future[R] = {
    logger.info(s"Request made for URL: ${request.uri}")
    get(request)
  }

  private def toJson(resp: NoFResp[R]): Either[ErrorCase, R] = {
    resp.body match {
      case Right(Right(v)) => Right(v)
      case Right(Left(e))  => Left(ErrorCase(Error(resp.code, e.message)))
      case Left(e)         => Left(ErrorCase(Error(resp.code, s"Non-200 Response code ${resp.code}, ${resp.statusText}\n $e")))
    }
  }

  protected def get(req: Req[R]): Future[R] = {
     req.send().map { r =>
        toJson(r) match {
          case Right(v) => v
          case Left(e)  => throw new Exception(s"${e.error.message}\n")
        }
      }
    }
}