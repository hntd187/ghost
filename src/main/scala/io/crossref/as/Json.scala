package io.crossref.as

import com.github.plokhotnyuk.jsoniter_scala.core._

import org.asynchttpclient.Response

object IterJson extends {
  def apply[T](r: Response)(implicit codec: JsonValueCodec[T]): T = {
    readFromArray(r.getResponseBodyAsBytes)(codec)
  }
}
