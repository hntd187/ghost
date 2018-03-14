package io.crossref

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

  val batchSize = 100
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
