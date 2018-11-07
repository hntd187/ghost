package io
import com.softwaremill.sttp._

package object crossref {
  final val VERSION     = "0.1"
  final val MAX_RESULTS = 1000
  val base              = uri"api.crossref.org"
  val works             = uri"$base/works/?rows=$MAX_RESULTS"
}
