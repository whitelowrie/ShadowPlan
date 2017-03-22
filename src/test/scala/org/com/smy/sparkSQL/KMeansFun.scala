package org.com.smy.sparkSQL

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DateType
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FunSuite

import scala.xml.XML

/**
  * Created by bigdata on 2017/3/20.
  */
class KMeansFun extends FunSuite{
  val conf = new SparkConf().setMaster("local[*]").setAppName("k-means")
  val filePath = "/Users/bigdata/Desktop/画像_80655/交易数据(20170316).csv"

  test("MLKMeans"){

    val columnName = "due_prepaid_period"

    val k = 8

    val data = getData().select(columnName).where(col(columnName) !== 0)

    data.show(100)

    val col1 = new VectorAssembler().setInputCols(Array(columnName)).setOutputCol("features")

    val transform = col1.transform(data)

    val fit = new org.apache.spark.ml.clustering.KMeans().setK(k).setSeed(1L).fit(transform)

    println(fit.clusterCenters.sortBy(_.argmax).mkString(","))

    val WSSSE = fit.computeCost(transform)


    println("Within Set Sum of Squared Errors = " + WSSSE)

  }

  test("MLLibKMeans"){
    val columnName = "his_prepaid_max_days"

    val k = 4

    val data = getData().select(columnName).map(x => Vectors.dense(x.getDouble(0)))

    val train = org.apache.spark.mllib.clustering.KMeans.train(data, k, Int.MaxValue)


    println(train.clusterCenters.mkString(","))

    val WSSSE = train.computeCost(data)

    println("Within Set Sum of Squared Errors = " + WSSSE)

  }

//  test("MLLibKMeans++"){
//    val columnName = "his_prepaid_max_days"
//
//    val k = 4
//
//    val data = getData().select(columnName).map(x => Vectors.dense(x.getDouble(0)))
//
//    val train = org.apache.spark.mllib.clustering.BisectingKMeans //KMeans.train(data, k, Int.MaxValue)
//
//
//    println(train.clusterCenters.mkString(","))
//
//    val WSSSE = train.computeCost(data)
//
//    println("Within Set Sum of Squared Errors = " + WSSSE)
//
//  }

  def getData(): DataFrame ={
    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    val dealFrame = sqlContext
      .read
      .format("com.databricks.spark.csv")
      .option("header", true.toString)
      .option("inferSchema", true.toString)
      .load(filePath)

    var columns = getFields().map(x => rint(when($"$x" === "None", 0).otherwise($"$x")).as(x))


    val selectFrame = dealFrame.select(columns:_*)
      //添加借款时间
      .withColumn("firstdeal_afterfacility", datediff($"facility_date_format".cast(DateType),$"earlist_deal_date".cast(DateType)))
      //添加月均借款
      .withColumn("deals_per_month", $"total_deals" / when($"month_after_facility" === 0, 1).otherwise($"month_after_facility"))

      .drop($"earlist_deal_date")
      .drop($"facility_date_format")
      .drop($"month_after_facility")

    selectFrame
  }

  def getFields(): List[String] = {
    val load = XML.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("columns.xml"))
    val map = load.child.map(_.text.trim).filter( x => x != null && x.length > 0)
    map.toList
  }
}
