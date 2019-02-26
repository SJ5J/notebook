package day0611

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object MyWebLogDemo {
  def main(args: Array[String]): Unit = {
    //定义SparkContext对象
    val conf = new SparkConf().setAppName("MyWebLogDemo").setMaster("local")
    val sc = new SparkContext(conf)
    
    // 读入日志文件
    //rdd1结果 :(hadoop.jsp,1)
    val rdd1 = sc.textFile("D:\\temp\\localhost_access_log.2017-07-30.txt").map{
      //line: 相当于value1
      line => {
        //处理该行日志: 192.168.88.1 - - [30/Jul/2017:12:53:43 +0800] "GET /MyDemoWeb/head.jsp HTTP/1.1" 200 713
        //解析字符串，找到jsp的名字
        //第一步解析出：GET /MyDemoWeb/head.jsp HTTP/1.1
        val index1 = line.indexOf("\"")   //第一个双引号的位置
        val index2 = line.lastIndexOf("\"")  //第二个双引号的位置
        val str1 = line.substring(index1+1,index2)
        
        //第二步解析出：/MyDemoWeb/head.jsp
        val index3 = str1.indexOf(" ") 
        val index4 = str1.lastIndexOf(" ")
        val str2 = str1.substring(index3+1, index4)
        
        //第三步解析出: head.jsp
        val jspName = str2.substring(str2.lastIndexOf("/")+1)
        
        //返回 (hadoop.jsp,1)
        (jspName,1)
      }
    }

    
    //按照jspname进行累加：(hadoop.jsp,10)
    val rdd2 = rdd1.reduceByKey(_+_)
    
    //按照访问量排序，降序
    val rdd3 = rdd2.sortBy(_._2,false)
    
    //取出前两条
    println(rdd3.take(2).toBuffer)
    
    sc.stop()
    
  }
}
















