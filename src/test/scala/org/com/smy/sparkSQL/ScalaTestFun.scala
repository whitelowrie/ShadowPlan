package org.com.smy.sparkSQL

import org.scalatest.FunSuite

import scala.xml.XML

/**
  * Created by bigdata on 2017/3/20.
  */
class ScalaTestFun extends FunSuite{
  test("XML"){
    import scala.xml._
    import scala.xml.transform._
    val colmns =
      """
        |cust_no	earlist_deal_date	latest_deal_date	cur_overdue_status	cur_overdue_max_days	his_overdued_max_days	his_prepaid_max_days	total_period_no	total_due_period	overdue_period	due_paid_period	due_prepaid_period	due_prepaid_t_days	due_overduepaid_period	due_overduepaid_t_days	due_normalpaid_period	min_overdue_p	cur_overdue_p	total_undue_period	total_undue_paid_period	total_amt	total_due_amt	total_undue_amt	total_p3_amt	total_p6_amt	total_p12_amt	overdue_amt	total_deals	total_deals_p3	total_deals_p6	total_deals_p12	facility_amt	std_total_t_facility	average_amt	month_after_facility	average_trans_usage_rate	facility_date_format
      """.stripMargin
    val items = new NodeBuffer
    val split = colmns.split("\t")

    val clomns = split.foreach(x => items += <column>{x}</column>)

    val root = <columns type ="deal"> </columns>

    val xmls = root.copy(child = root.child ++ items)

    XML.save("/Users/bigdata/Documents/MyData/columns.xml", xmls, "utf-8",true)

  }

  test("XMLLoad"){
      val load = XML.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("columns.xml"))

      print(load)

  }
}
