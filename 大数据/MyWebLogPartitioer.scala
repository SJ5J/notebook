package day0611

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.Partitioner
import scala.collection.mutable.HashMap

object MyWebLogPartitioer {
  def main(args: Array[String]): Unit = {
    //注意：这一句
    System.setProperty("hadoop.home.dir", "D:\\temp\\hadoop-2.4.1\\hadoop-2.4.1")
    
    //定义SparkContext对象
    val conf = new SparkConf().setAppName("MyWebLogPartitioer").setMaster("local")
    val sc = new SparkContext(conf)
    
    // 读入日志文件
    //rdd1结果 :(hadoop.jsp,对应的日志)
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
        
        //返回 (hadoop.jsp,对应的日志)
        (jspName,line)
      }
    }
    
    //根据jsp的名字建立分区，得到jsp名字个数
    //得到所有不重复的jsp的名字 ---> String
    val rdd2 = rdd1.map(_._1).distinct().collect()
    
    //根据jsp的名字建立分区，创建分区规则
    val mypartitioner = new MyPartitioner(rdd2)
    
    //注意：rdd1是一个PairRDDFunctions
    //执行分区
    val result = rdd1.partitionBy(mypartitioner)
    
    //输出
    result.saveAsTextFile("d:\\temp\\partitioner")
    
    sc.stop()
    
  }
}

//创建自己的分区规则：根据所有不重复的jsp的名字
class MyPartitioner(allJSPName:Array[String]) extends Partitioner{
  //定义一个集合保存分区的条件
  //String表示是jsp名字       Int分区号
  val partitionMap = new HashMap[String,Int]()
  
  //建立分区规则
  var partID = 0 //分区号
  for(name <- allJSPName){
    //有一个jsp，建立一个分区
    partitionMap.put(name, partID)
    
    partID += 1
  }
  
  //返回分区的个数
  override def numPartitions: Int  = partitionMap.size
  
  //根据jsp的名字获取分区号
  override def getPartition(key: Any): Int = {
    partitionMap.getOrElse(key.toString, 0)
  }
  
}


























