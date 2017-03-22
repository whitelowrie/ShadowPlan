package org.com.smy.sparkSQL

import org.apache.avro.generic.GenericData.StringType
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types._
import org.scalatest.FunSuite

/**
  * Created by bigdata on 2017/3/19.
  */
class DataFrameFun extends FunSuite with LocalSparkContext{

  val filePath = "/Users/bigdata/Desktop/画像_80655/交易数据(20170316).csv"
  test("UseRDDs"){
    withSpark{ sc =>
      val rdd = sc.textFile(filePath).map(x => {
        val fields = x.split(",")
        if(fields(7).matches("\\d+")){
          (fields(1), (fields(7).toInt, 1))
        }else
          (fields(1), (0,1))
      })
      val key = rdd.reduceByKey { case (v1, v2) => {
        (v1._1 + v2._1, v1._2 + v2._2)
      }}

      val map = key.map(x => (x._1, x._2._1/ x._2._2)).sortBy(_._1.replace("-","")).collect()

      print(map.mkString("\n"))
    }
  }

  test("dataFrame"){
    withSpark{sc => {
      val sqlContext = new SQLContext(sc)

      import sqlContext.implicits._
      import sqlContext._

      val rdd = sc.textFile(filePath).map(x => {
        val fields = x.split(",")
          List(fields(1), fields(7))
      })

      val bis = sc.parallelize(Array(List("earlist_deal_date","total_period_no")))

      val rowRDD = rdd.subtract(bis).map(x => Row.fromSeq(x))


      val map = rdd.first().map(StructField(_, org.apache.spark.sql.types.StringType, true))

      val structType = StructType(map)

      val createDataFrame = sqlContext.createDataFrame(rowRDD, structType)


      val agg = createDataFrame.groupBy("earlist_deal_date").agg(Map("total_period_no" -> "avg"))

      agg.show(10)

    }}
  }

  test("dataFrameCSV"){withSpark{sc =>
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", true.toString)
      .option("inferSchema", true.toString)
      .load(filePath)

    val select = df.select("earlist_deal_date","total_period_no")

    select.registerTempTable("temp_cust")

    val sql = sqlContext.sql(
      """SELECT earlist_deal_date, avg(total_period_no)
          from temp_cust GROUP BY earlist_deal_date
      """.stripMargin)


    sql.show(10)
  }}

  test("dataFrameCSV01"){withSpark{sc =>
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", true.toString)
      .option("inferSchema", true.toString)
      .load(filePath)

    val select = df.select("earlist_deal_date","total_period_no")

    select.registerTempTable("temp_cust")

    val sql = sqlContext.sql(
      """SELECT earlist_deal_date, avg(total_period_no)
          from temp_cust GROUP BY earlist_deal_date
      """.stripMargin)


    sql.show(10)
  }}


  test("SparkMatrix"){
    withSpark{sc =>
      sc.parallelize((Seq.fill(10)(10)))
    }
  }
}
