package io.crossref

import scala.concurrent.Future

import com.softwaremill.sttp._

package object http {
  type Req[R]     = RequestT[Id, Either[DeserializationError[io.circe.Error], R], Nothing]
  type Resp[R]    = Future[NoFResp[R]]
  type NoFResp[R] = Response[Either[DeserializationError[io.circe.Error], R]]
}