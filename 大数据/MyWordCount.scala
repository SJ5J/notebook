package demo

import org.apache.spark.{SparkConf, SparkContext}

/* 提交任务的命令：
bin/spark-submit --master spark://bigdata111:7077 --class demo.MyWordCount /root/temp/mywordcount.jar hdfs://192.168.157.111:9000/input/data.txt hdfs://192.168.157.111:9000/output/0608/wc2
 */
object MyWordCount {
  def main(args: Array[String]): Unit = {
    //配置, 设置本地模式.setMaster("local")
    val conf = new SparkConf().setAppName("MyWordCount")  //.setMaster("local")

    //核心：创建SC (SparkContext)
    val sc = new SparkContext(conf)

    //使用sc对象执行任务
//本地运行
//    sc.textFile("hdfs://192.168.157.111:9000/input/data.txt")
//        .flatMap(_.split(" "))
//         .map((_,1))
//          .reduceByKey(_+_)
//            .saveAsTextFile("hdfs://bigdata111:9000/output/0608/wc1")
    //集群运行
    sc.textFile(args(0))       //args(0)输入值,
      .flatMap(_.split(" "))
      .map((_,1))
      .reduceByKey(_+_)
      .saveAsTextFile(args(1)) //args(1)输出位置

    //停止SparkContext对象
    sc.stop()
  }
}
