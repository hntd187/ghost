package io.crossref

import java.nio.file.{Files, Paths}
import java.util.concurrent.{ConcurrentLinkedQueue, Executors}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import dispatch._
import scopt._
import scribe._

case class Config(compression: Boolean = true, resume: Boolean = false, outputDir: String = "results", contact: String = "")

object CR extends App {
  val optionParser = new OptionParser[Config]("CrossRef") {
    head("CrossRef Miner", VERSION)

    override def errorOnUnknownArgument: Boolean = true
    override def showUsageOnError: Boolean       = true

    opt[String]('o', "output").optional().action((v, c) => c.copy(outputDir = v))
    opt[Unit]('r', "resume").optional().action((_, c) => c.copy(resume = true))
    opt[Unit]('n', "no-compression").optional().action((_, c) => c.copy(compression = false))
    opt[String]('c', "contact").required().action((v, c) => c.copy(contact = v))
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

import scala.collection.JavaConverters._
import scala.collection.Seq

object Testing extends App {

  implicit val enc: JsonValueCodec[ItemResponse] = JsonCodecMaker.make[ItemResponse](CodecMakerConfig())
  val pubEnc: JsonValueCodec[Seq[Publication]]   = JsonCodecMaker.make[Seq[Publication]](CodecMakerConfig())

  val records       = new ConcurrentLinkedQueue[Publication]()
  val futures       = Seq.empty[Future[Seq[Publication]]]
  val threadPool    = Executors.newFixedThreadPool(8)
  implicit val pool = ExecutionContext.fromExecutor(threadPool)

  val file = Files.readAllLines(Paths.get("/Users/steve_carman/Desktop/crossref/part-0-cursors.txt")).asScala.toList
  val u    = url("https://api.crossref.org/works")

  println(s"Keys: ${file.length}")

  val req = file.map { l =>
    Thread.sleep(100)
    val params = Map("mailto" -> "shcarman%40gmail.com", "rows" -> "1000", "cursor" -> l)
    Http.default(u <<? params OK as.IterJson[ItemResponse]).map(r => r.message.items)
  }

  val fs = Future.foldLeft(req)(Seq.empty[Publication])(_ ++ _)

  val result = Await.result(fs, Duration.Inf)

  println(result.length)
  val wo = WriterConfig(2, false, 32768)

  Files.write(Paths.get("test.json"), writeToArray(result, wo)(pubEnc))
  println("Done...")
}
