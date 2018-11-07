package io.crossref.benchmarks

import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util.concurrent.TimeUnit

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import io.crossref._
import io.crossref.as.IterJson
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import JsonObjects._

object DataSetup {
  @State(Scope.Benchmark)
  class Data {
    val wo: WriterConfig                         = WriterConfig(0, false, 32768)
    val pubEnc: JsonValueCodec[Seq[Publication]] = JsonCodecMaker.make[Seq[Publication]](CodecMakerConfig())
    val data: Array[Byte]                        = Files.readAllBytes(Paths.get("C:\\Users\\hntd\\IdeaProjects\\ghost\\bench\\src\\main\\resources\\part-0.json"))
    val strData: String                          = new String(data, StandardCharsets.UTF_8)
    val pubs: Seq[Publication]                   = IterJson.read(data)(pubEnc)
    val format: DefaultFormats.type              = org.json4s.DefaultFormats
  }
}

class JsonBenchmarks {

  import DataSetup._

  @Benchmark @BenchmarkMode(Array(Mode.AverageTime, Mode.SampleTime)) @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def inter_read(state: Data, bh: Blackhole): Unit = {
    bh.consume(IterJson.read(state.data)(state.pubEnc))
  }
  @Benchmark @BenchmarkMode(Array(Mode.AverageTime, Mode.SampleTime)) @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def inter_write(state: Data, bh: Blackhole): Unit = {
    bh.consume(IterJson.write(state.pubs, state.wo)(state.pubEnc))
  }
  @Benchmark @BenchmarkMode(Array(Mode.AverageTime, Mode.SampleTime)) @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def json4s_read(state: Data, bh: Blackhole): Unit = {
    implicit val f = state.format
    bh.consume(parse(state.strData).extract[Seq[Publication]])
  }
  @Benchmark @BenchmarkMode(Array(Mode.AverageTime, Mode.SampleTime)) @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def json4s_write(state: Data, bh: Blackhole): Unit = {
    bh.consume(write(state.pubs)(state.format))
  }
}
