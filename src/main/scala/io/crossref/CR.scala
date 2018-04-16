package io.crossref

import java.io._
import java.nio.file._
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import dispatch._
import org.xerial.snappy.SnappyHadoopCompatibleOutputStream
import scopt._
import scribe._

case class Config(compression: Boolean = true, resume: Boolean = false, outputDir: String = "results", contact: String = "")

object CR extends App {
  val optionParser = new OptionParser[Config]("CrossRef") {
    head("CrossRef Miner", VERSION)

    override def errorOnUnknownArgument: Boolean = true
    override def showUsageOnError: Boolean       = true

    opt[String]('o', "output").optional().action((v, c) => c.copy(outputDir         = v))
    opt[Unit]('r', "resume").optional().action((_, c) => c.copy(resume              = true))
    opt[Unit]('n', "no-compression").optional().action((_, c) => c.copy(compression = false))
    opt[String]('c', "contact").required().action((v, c) => c.copy(contact          = v))
    help("help").text("Displays Help Text")
  }

  val options = optionParser.parse(args, Config()) match {
    case Some(o) => o
    case None    => sys.exit(1)
  }

  val r = CrossRef(options)
  r.base()

  val batchSize = 20
  val batches   = 10
  for (x <- 1 to batches) {
    for (i <- 1 to batchSize) {
      info(s"--> ($i / $batchSize) $x of $batches...")
      r.next()
    }
    r.writeFile()
  }
  r.close()

}

object Testing extends App {

  implicit val enc: JsonValueCodec[ItemResponse] = JsonCodecMaker.make[ItemResponse](CodecMakerConfig())
  val pubEnc: JsonValueCodec[Seq[Publication]]   = JsonCodecMaker.make[Seq[Publication]](CodecMakerConfig())
  val singlePubEnc: JsonValueCodec[Publication]  = JsonCodecMaker.make[Publication](CodecMakerConfig())

  val futures: Seq[Future[Seq[Publication]]] = Seq.empty
  val base_url: Req                          = url("https://api.crossref.org/works")
  val wo: WriterConfig                       = WriterConfig(0, false, 32768)
  val client: Http = Http.withConfiguration { c =>
    c.setUseNativeTransport(false)
    c.setCompressionEnforced(true)
    c.setMaxConnections(500)
    c.setMaxConnectionsPerHost(200)
    c.setPooledConnectionIdleTimeout(100)
    c.setConnectionTtl(100)
    c.setIoThreadsCount(24)
  }

  val base      = "/Users/steve_carman/Desktop"
  val cursorDir = Paths.get(base, "crossref")
  val pm        = FileSystems.getDefault.getPathMatcher("glob:**/*.txt")
  val files     = Files.list(cursorDir).filter(p => pm.matches(p)).iterator().asScala.toSeq
  val total     = files.length
  val start = if (args.nonEmpty) {
    args.head.toInt
  } else {
    0
  }

  files.slice(start, total - 1).zipWithIndex.foreach {
    case (p, i) =>
      info(s"Starting: ${p.getFileName}")
      val file   = readLines(p)
      val tokens = p.getFileName.toString.split("-").slice(0, 2).mkString("-")
      val output = s"$base/crossref2/$tokens.json.snappy"
      collectCursors(output, file)
      val pct = ((i.toDouble + start) / total.toDouble) * 100.0
      info(f"Finished: $output (${i + start}/$total) ($pct%3.2f%%)")
  }

  if (!client.client.isClosed) {
    client.client.close()
    client.shutdown()
  }

  @inline
  def writeJson(pubs: Seq[Publication], output: String): Unit = {
    val hos = new SnappyHadoopCompatibleOutputStream(new FileOutputStream(new File(output)))
    pubs.foreach { p =>
      writeToStream(p, hos, wo)(singlePubEnc)
      hos.write(Array[Byte]('\n'))
    }
    hos.close()
  }

  def collectCursors(output: String, cursors: immutable.Seq[String]): Unit = {
    val req = cursors.map { l =>
      Thread.sleep(100)
      val params = Map("mailto" -> "shcarman%40gmail.com", "rows" -> "1000", "cursor" -> l)
      client(base_url <<? params OK as.IterJson[ItemResponse]).map(r => r.message.items)
    }
    val fs     = Future.foldLeft(req)(Seq.empty[Publication])(_ ++ _)
    val result = Await.result(fs, Duration.Inf)
    Future(writeJson(result, output))
  }

  @inline
  def readLines(p: Path): immutable.Seq[String] = Files.readAllLines(p).asScala.toList
}
