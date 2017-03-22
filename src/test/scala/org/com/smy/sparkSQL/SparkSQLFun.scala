package org.com.smy.sparkSQL

import breeze.linalg.max
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.random.RandomRDDs
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FunSuite

import scala.reflect.internal.util.TableDef.Column

/**
  * Created by bigdata on 2017/3/19.
  */
class SparkSQLFun extends FunSuite{

  val conf = new SparkConf().setMaster("local").setAppName("Test")

  val sc = new SparkContext(conf)

  test("read csv"){

    val filePath = "/Users/bigdata/Desktop/画像_80655/交易数据(20170316).csv"
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read
                      .format("com.databricks.spark.csv")
                      .option("header", true.toString)
                      .option("inferSchema", true.toString)
                      .load(filePath)


    df.show(10)
  }

  test("readMySte"){
    val hc = new HiveContext(sc)

    import hc.implicits._

    val custTable = hc.table("v_bd_hwh_comm_info_v2")

    val channelRecord = hc.table("v_ams_channel_cost_record")

    //其中有很多操作 === ,!==, startWith
    val where = custTable.where($"regist_datetime" > "20170307")



    val join = channelRecord.join(where, $"cost_datetime" === $"regist_date")

    val by = join.groupBy($"regist_datetime")
                 .agg(Map("cust_no" -> "count",
                          "marital_status_datetime" -> "count"))

    val by1 = by.orderBy("regist_datetime")

    by1.show(10)
  }

  test("hive"){
    val hc = new HiveContext(sc)
    import hc.implicits._
    val names = hc.tables().where($"tableName" contains("channel"))

    names.foreach(row => {
      println(row.get(0))
    })
  }

  test("xml"){
    import scala.xml._
    val colmns =
      """
        |cust_no	earlist_deal_date	latest_deal_date	cur_overdue_status	cur_overdue_max_days	his_overdued_max_days	his_prepaid_max_days	total_period_no	total_due_period	overdue_period	due_paid_period	due_prepaid_period	due_prepaid_t_days	due_overduepaid_period	due_overduepaid_t_days	due_normalpaid_period	min_overdue_p	cur_overdue_p	total_undue_period	total_undue_paid_period	total_amt	total_due_amt	total_undue_amt	total_p3_amt	total_p6_amt	total_p12_amt	overdue_amt	total_deals	total_deals_p3	total_deals_p6	total_deals_p12	facility_amt	std_total_t_facility	average_amt	month_after_facility	average_trans_usage_rate	facility_date_format
      """.stripMargin
    val split = colmns.split("\t")
    print(split)
  }

}
