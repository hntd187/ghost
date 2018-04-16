package io.crossref

import org.apache.spark.sql._

object SparkTests extends App {

  val spark = SparkSession.builder().appName("test").master("local[*]").getOrCreate()

  import spark.implicits._

  val filePath = ""
  val data = spark.read
    .json(filePath)
    .withColumnRenamed("abstract", "abs") // Spark does not allow abstract to be a column name.
    .as[Publication]

  data.show()

}
