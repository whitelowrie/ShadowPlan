package org.smy.kmeans

//import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.xml.XML
import org.apache.spark.sql.types.DateType

/**
  * Created by bigdata on 2017/3/17.
  */
object KMeansTest {
  def main(args: Array[String]): Unit = {

//
    val conf = new SparkConf().setMaster("local[*]").setAppName("k-means")

    val filePath = "/Users/bigdata/Desktop/画像_80655/交易数据(20170316).csv"

//
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



    val columnsArray = selectFrame.columns

//    println(selectFrame.dtypes.mkString(","))
//
//    val col1 = new VectorAssembler().setInputCols(Array("cur_overdue_max_days")).setOutputCol("features")
//
//    println(columnsArray.mkString(","))
////    for(column <- columnsArray){
//      val select = selectFrame.select($"cur_overdue_max_days")
//
//      val transform = col1.transform(select)
//
//      val fit = new KMeans().setK(4).setSeed(1L).fit(transform)
//
//
//      println(fit.clusterCenters.mkString(","))
////    }
val select = selectFrame.select($"cur_overdue_max_days")

    val train = KMeans.train(select.rdd.map(x => Vectors.dense(x.getDouble(0))), 4, Int.MaxValue)

    println(train.clusterCenters.mkString(","))

//   selectFrame.select("earlist_deal_date").show(100)

//      val centers = fit.clusterCenters

//   println(select.rdd.take(100).map(_.getAs[String](0)).mkString(","))



//    selectFrame.show(10)


//    new KMeans().setK(3).fit(sql)

  }

  def getDoRDDKMeans(): Unit ={

  }

  def getFields(): List[String] ={
    val load = XML.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("columns.xml"))
    val map = load.child.map(_.text.trim).filter( x => x != null && x.length > 0)
    map.toList
  }



}

//case class CUST(custNo:String,) extends Serializable
