package io.crossref

import java.io.RandomAccessFile
import java.nio.channels.FileChannel

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

import dispatch._
import org.asynchttpclient.{Response => Resp}
import org.json4s._
import org.json4s.jackson.Serialization._
import scribe._

class CrossRef(private val contact: Option[String] = None) extends AutoCloseable {

  import JsonObjects._

  def this(contact: String) = this(Some(contact))

  private implicit val formats: DefaultFormats.type = DefaultFormats

  var part                        = 0
  private var numReqs: Int        = 0
  private var averageTime: Double = 0.0
  private var averageSize: Double = 0.0
  private val client: Http = Http.withConfiguration { b =>
    b.setRequestTimeout(36000)
      .setCompressionEnforced(true)
      .setMaxConnectionsPerHost(10)
      .setMaxConnections(100)
  }

  private val recordBuffer = mutable.Queue.empty[Publication]
  private val cursorBuffer = mutable.Queue.empty[String]
  var prevCursor           = ""

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

  private def parseResponse(r: Resp, startTime: Long): JValue = {
    val endTime = System.currentTimeMillis() - startTime
    numReqs += 1
    averageTime += endTime
    averageSize += r.getHeader("content-length").toDouble
    //info(s"Response Time: $endTime, Response Size: ${r.getHeader("content-length")}")
    as.json4s.Json(r)
  }

  def base(): Unit = {
    val req = contact.map(c => works <<? Map("mailto" -> c, "cursor" -> "*")).getOrElse(works)
    //info(s"URL: ${req.url}")
    request(req)
  }

  def next(): Unit = {
    if (prevCursor.isEmpty) return
    //info(s"Average Response Time: ${averageTime / numReqs}, Average Response Size: ${averageSize / numReqs}")
    val req = contact.map(c => works <<? Map("mailto" -> c, "cursor" -> prevCursor)).getOrElse(works)
    request(req)
  }

  def request(r: Req): Unit = {
    val startTime = System.currentTimeMillis()
    val resp      = client(r OK (r => parseResponse(r, startTime)))
    val items     = resp().transformField(convertFields).extract[Response[Items]]
    recordBuffer ++= items.message.items
    cursorBuffer ++= items.message.nextCursor
    prevCursor = items.message.nextCursor.getOrElse("")
  }

  def writeFile(): Unit = {
    import org.xerial.snappy.Snappy

    val bytes       = Snappy.compress(write(recordBuffer).getBytes())
    val cursorBytes = cursorBuffer.mkString("\n").getBytes()
    val raf         = new RandomAccessFile(s"part-$part.json.snappy", "rw").getChannel
    val cursors     = new RandomAccessFile(s"part-$part-cursors.txt", "rw").getChannel

    raf.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length).put(bytes)
    cursors.map(FileChannel.MapMode.READ_WRITE, 0, cursorBytes.length).put(cursorBytes)
    raf.close()
    cursors.close()
  }

  def close(): Unit = client.shutdown()
}

object CrossRef {
  def apply(contact: Option[String] = None): CrossRef = new CrossRef(contact)
  def apply(contact: String): CrossRef                = new CrossRef(contact)
}

object CR extends App {

  implicit val formats = DefaultFormats

  val r = CrossRef("shcarman@gmail.com")
  r.base()

  for (i <- 1 to 50) {
    info(s"--> ($i / 50)...")
    r.next()
  }
  r.writeFile()

  r.close()
}
