package io.crossref.as

import java.nio.file._

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.asynchttpclient.Response
import scribe._

object IterJson extends {
  def apply[T: JsonValueCodec](r: Response): T =
    try {
      readFromArray(r.getResponseBody.replace("[[null]]", "[[]]").getBytes())
    } catch {
      case e: JsonParseException =>
        info(e.getMessage)
        info(s"From URL: ${r.getUri}")
        Files.write(Paths.get("error.json"), r.getResponseBodyAsBytes)
        sys.exit(1)
    }

  def read[T: JsonValueCodec](bytes: Array[Byte]): T = readFromArray(bytes)

  def write[T: JsonValueCodec](v: T, cfg: WriterConfig = WriterConfig()): Array[Byte] = writeToArray(v, cfg)
}
