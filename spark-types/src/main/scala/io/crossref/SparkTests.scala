package io.crossref

import org.apache.spark.sql._

object SparkTests extends App {

  val spark = SparkSession.builder().appName("test").master("local[*]").getOrCreate()

  import spark.implicits._

  val data = spark.read.json("cursors\\part-0.json").withColumnRenamed("abstract", "abs").as[Publication]

  data.printSchema()

}
