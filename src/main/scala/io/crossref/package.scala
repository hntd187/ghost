package io

import dispatch._

package object crossref {

  final val VERSION = "0.1"

  val base = host("api.crossref.org").secure

  val works = base / "works" <<? Map("rows" -> "1000")

}
