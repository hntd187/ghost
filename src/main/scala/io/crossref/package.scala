package io

import dispatch._

package object crossref {
  final val VERSION     = "0.1"
  final val MAX_RESULTS = 1000
  val base              = host("api.crossref.org").secure
  val works             = base / "works" <<? Map("rows" -> s"$MAX_RESULTS")
}
