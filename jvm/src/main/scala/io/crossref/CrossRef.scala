package io.crossref

import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset._
import java.nio.file._

import scala.collection.JavaConverters._
import scala.collection.mutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util._

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.circe._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.crossref.JsonObjects._
import org.asynchttpclient.{DefaultAsyncHttpClientConfig, Response => Resp}
import org.xerial.snappy.Snappy
import scribe._

class CrossRef(private val config: Config) extends AutoCloseable {

  private val encoding: Charset                 = StandardCharsets.UTF_8
  private var part: Int                         = 0
  private var numReqs: Int                      = 0
  private var averageTime: Double               = 0.0
  private var averageSize: Double               = 0.0
  private var prevCursor: String                = ""
  private var writeHandle: Option[Future[Unit]] = None
  private var recordBuffer: Queue[Publication]  = Queue.empty[Publication]
  private var cursorBuffer: Queue[String]       = Queue.empty[String]

  private val clientCfg = new DefaultAsyncHttpClientConfig.Builder()
    .setRequestTimeout(36000)
    .setCompressionEnforced(true)
    .setMaxConnectionsPerHost(10)
    .setMaxConnections(100)
    .build()

  private implicit val client = AsyncHttpClientFutureBackend.usingConfig(clientCfg)
  private val outputDir       = Paths.get(config.outputDir)

  if (Files.notExists(outputDir)) {
    info(s"Output Dir: $outputDir does not exist...creating...")
    Files.createDirectory(outputDir)
  }

  private def parseResponse(r: Resp, startTime: Long): Json = {
    val endTime = System.currentTimeMillis() - startTime
    numReqs += 1
    averageTime += endTime
    averageSize += r.getHeader("content-length").toDouble
    io.circe.parser.parse(r.getResponseBody).right.getOrElse(Json.Null)
  }

  def base(): Unit = {
    if (config.resume) {
      resume()
    } else {
      val req = uri"$works/?mailto=${config.contact}&cursor=*"
      request(req)
    }
  }

  def next(): Unit = {
    if (prevCursor.isEmpty) return
    val req = uri"$works/?mailto=${config.contact}&cursor=$prevCursor"
    request(req)
  }

  def resume(): Unit = {
    val checkpoint = Paths.get(config.outputDir, ".checkpoint")
    require(Files.exists(checkpoint), "Can't resume because checkpoint file does not exist.")

    resumePart()

    val cursor = new String(Files.readAllBytes(checkpoint)).trim
    info(s"Resuming from cursor position: $cursor")
    val req = uri"$works/?mailto=${config.contact}&cursor=$cursor"
    request(req, update = false)
  }

  def request(r: Uri, update: Boolean = true): Unit = {
    val startTime = System.currentTimeMillis()
    val resp      = sttp.get(r).response(asJson[ItemResponse]).send()
    val items     = Await.result(resp, Duration.Inf).unsafeBody.right.get
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
      Snappy.compress(records.asJson.noSpaces.getBytes(encoding))
    } else {
      records.asJson.noSpaces.getBytes(encoding)
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
      h
    }.wait()
    client.close()
  }
}

object CrossRef {
  def apply(config: Config): CrossRef = new CrossRef(config)
}
