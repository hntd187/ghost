package io.crossref

import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset._
import java.nio.file._
import scala.collection.JavaConverters._
import scala.collection.mutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

import dispatch._
import org.asynchttpclient.{Response => Resp}
import org.json4s._
import org.json4s.jackson.Serialization._
import org.xerial.snappy.Snappy
import scribe._

class CrossRef(private val config: Config) extends AutoCloseable {

  private implicit val formats: DefaultFormats.type = DefaultFormats
  private val encoding: Charset                     = StandardCharsets.UTF_8

  private var part: Int                         = 0
  private var numReqs: Int                      = 0
  private var averageTime: Double               = 0.0
  private var averageSize: Double               = 0.0
  private var prevCursor: String                = ""
  private var writeHandle: Option[Future[Unit]] = None
  private var recordBuffer: Queue[Publication]  = Queue.empty[Publication]
  private var cursorBuffer: Queue[String]       = Queue.empty[String]

  private val client: Http = Http.withConfiguration { b =>
    b.setRequestTimeout(36000)
      .setCompressionEnforced(true)
      .setMaxConnectionsPerHost(10)
      .setMaxConnections(100)
  }

  private val outputDir = Paths.get(config.outputDir)
  if (Files.notExists(outputDir)) {
    info(s"Output Dir: $outputDir does not exist...creating...")
    Files.createDirectory(outputDir)
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

  private def parseResponse(r: Resp, startTime: Long): JValue = {
    val endTime = System.currentTimeMillis() - startTime
    numReqs += 1
    averageTime += endTime
    averageSize += r.getHeader("content-length").toDouble
    dispatch.as.json4s.Json(r)
  }

  def base(): Unit = {
    if (config.resume) {
      resume()
    } else {
      val req = works <<? Map("mailto" -> config.contact, "cursor" -> "*")
      request(req)
    }
  }

  def next(): Unit = {
    if (prevCursor.isEmpty) return
    val req = works <<? Map("mailto" -> config.contact, "cursor" -> prevCursor)
    request(req)
  }

  def resume(): Unit = {
    val checkpoint = Paths.get(config.outputDir, ".checkpoint")
    require(Files.exists(checkpoint), "Can't resume because checkpoint file does not exist.")

    resumePart()

    val cursor = new String(Files.readAllBytes(checkpoint)).trim
    info(s"Resuming from cursor position: $cursor")
    val req = works <<? Map("mailto" -> config.contact, "cursor" -> cursor)
    request(req, update = false)
  }

  def request(r: Req, update: Boolean = true): Unit = {
    val startTime = System.currentTimeMillis()
    val resp      = client(r OK (r => parseResponse(r, startTime)))
    val items     = resp().transformField(convertFields).extract[Response[Items]]
    if (update) {
      recordBuffer ++= items.message.items
      cursorBuffer ++= items.message.`next-cursor`
    }
    prevCursor = items.message.`next-cursor`.getOrElse("")
  }

  def writeFile(): Unit = {
    val atPart = part
    val prev   = prevCursor
    val write  = Future(writeFile(recordBuffer, cursorBuffer, prev))

    recordBuffer.clear()
    cursorBuffer.clear()

    recordBuffer = Queue.empty[Publication]
    cursorBuffer = Queue.empty[String]

    write.onComplete {
      case Success(_) => info(s"Wrote Part $atPart..."); writeHandle = None
      case Failure(e) => throw e
    }
    writeHandle = Some(write)
  }

  private def resumePart(): Unit = {
    val parts = Files
      .list(outputDir)
      .iterator()
      .asScala
      .filter(p => !Files.isDirectory(p) && !p.endsWith(".checkpoint"))

    if (parts.nonEmpty) {
      val fileName = parts
        .maxBy(p => p.toFile.lastModified())
        .getFileName
        .toString
        .split("-")

      part = fileName(1).toInt + 1

      info(s"Part set to $part...")
    }
  }

  private def writeFile(records: Queue[Publication], cursors: Queue[String], checkpoint: String): Unit = {
    val bytes: Array[Byte] = if (config.compression) {
      Snappy.compress(write(records).getBytes(encoding))
    } else {
      write(records).getBytes(encoding)
    }
    val cursorBytes: Array[Byte] = cursors.mkString("\n").getBytes(encoding)
    val dataChan: FileChannel    = new RandomAccessFile(s"${config.outputDir}/part-$part.json.snappy", "rw").getChannel
    val cursorChan: FileChannel  = new RandomAccessFile(s"${config.outputDir}/part-$part-cursors.txt", "rw").getChannel

    val mappedData: MappedByteBuffer = dataChan.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length)
    mappedData.put(bytes)

    val mappedCursors = cursorChan.map(FileChannel.MapMode.READ_WRITE, 0, cursorBytes.length)
    mappedCursors.put(cursorBytes)

    dataChan.close()
    cursorChan.close()
    part += 1
    writeCheckpoint(checkpoint)
  }

  private def writeCheckpoint(cursor: String): Unit = {
    val checkpoint = Paths.get(config.outputDir, ".checkpoint")
    Files.write(checkpoint, cursor.getBytes(encoding), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

  def close(): Unit = {
    writeHandle.foreach { h =>
      info("Waiting on writes to finish...")
      h()
    }
    client.shutdown()
  }
}

object CrossRef {
  def apply(config: Config): CrossRef = new CrossRef(config)
}
