package io

import dispatch._

package object crossref {

  val base = host("api.crossref.org").secure

  val works = base / "works" <<? Map("rows" -> "1000")

}
