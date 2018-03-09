package io.crossref

import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import dispatch._
import org.json4s._
import scribe._
import org.json4s.jackson.Serialization._
import org.json4s.DefaultFormats

class CrossRef(private val contact: Option[String] = None) extends AutoCloseable {

  import JsonObjects._

  def this(contact: String) = this(Some(contact))

  implicit val formats = DefaultFormats

  private val params = HashMap.empty[String, String]
  contact.foreach(c => params += ("mailto" -> c))

  private val client = Http.withConfiguration { b =>
    b.setRequestTimeout(36000)
      .setCompressionEnforced(true)
      .setMaxConnectionsPerHost(10)
      .setMaxConnections(100)
  }

  private def convertFields: PartialFunction[JField, JField] = {
    case ("message-type", x)           => ("messageType", x)
    case ("message-version", x)        => ("messageVersion", x)
    case ("date-parts", x)             => ("dateParts", x)
    case ("date-time", x)              => ("dateTime", x)
    case ("reference-count", x)        => ("referenceCount", x)
    case ("content-domain", x)         => ("contentDomain", x)
    case ("crossmark-restriction", x)  => ("crossmarkRestriction", x)
    case ("is-referenced-by-count", x) => ("isReferencedByCount", x)
    case ("published-online", x)       => ("publishedOnline", x)
    case ("container-title", x)        => ("containerTitle", x)
    case ("content-type", x)           => ("contentType", x)
    case ("content-version", x)        => ("contentVersion", x)
    case ("intended-application", x)   => ("intendedApplication", x)
    case ("references-count", x)       => ("referencesCount", x)
    case ("start-index", x)            => ("startIndex", x)
    case ("search-terms", x)           => ("searchTerms", x)
    case ("total-results", x)          => ("totalResults", x)
    case ("items-per-page", x)         => ("itemsPerPage", x)
    case ("next-cursor", x)            => ("nextCursor", x)
  }

  private var averageTime: Double = 0.0
  private var averageSize: Double = 0.0

  import org.asynchttpclient.{Response => Resp}

  private def parseResponse(r: Resp, startTime: Long): JValue = {
    val endTime = Duration(System.currentTimeMillis() - startTime, MILLISECONDS).toSeconds
    info(s"Request took: $endTime seconds")
    info(s"Content Length: ${r.getHeader("content-length")}")
    as.json4s.Json(r)
  }

  def base(): Response[Items] = {
    val req = contact.map(c => works <<? Map("mailto" -> c)).getOrElse(works)
    info(s"URL: ${req.url}")
    val startTime = System.currentTimeMillis()
    val resp      = client(req OK (r => parseResponse(r, startTime)))

    resp().transformField(convertFields).extract[Response[Items]]
  }

  def close(): Unit = client.shutdown()
}

object CrossRef {
  def apply(contact: Option[String] = None): CrossRef = new CrossRef(contact)
  def apply(contact: String): CrossRef                = new CrossRef(contact)
}

object CR extends App {
  def time[R](block: => R): R = {
    val startTime = System.currentTimeMillis()
    val r         = block
    val endTime   = Duration(System.currentTimeMillis() - startTime, MILLISECONDS).toMillis
    println(s"Execution took: $endTime milliseconds")
    r
  }
  implicit val formats = DefaultFormats

  val r    = CrossRef("shcarman@gmail.com")
  val recs = r.base()

  info(s"Items: ${recs.message.items.length}")
  info(s"Cursor: ${recs.message.nextCursor.get}")

  val bytes = write(recs.message.items).getBytes()
  time {
    val raf = new RandomAccessFile("example.json", "rw").getChannel
    raf.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length).put(bytes)
    raf.close()
  }
  r.close()
}
