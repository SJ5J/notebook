package demo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

/*
 * 集群模式的提交命令
 * bin/spark-submit --master spark://bigdata111:7077 --class demo.JavaWordCount /root/temp/sparkwc.jar hdfs://bigdata111:9000/input/data.txt
 */
public class JavaWordCount {

	public static void main(String[] args) {
		//指定任务名，可显示在spark webconsle上
		SparkConf conf = new SparkConf();
		conf.setAppName("MyJavaWordCount");
		//设置本地模式运行
		//conf.setMaster("local"); 
		
		//创建java版本的SC 上下文
		JavaSparkContext context = new JavaSparkContext(conf);
				
		//读数据，返回java版本的RDD集合，集合里每个元素是一句话
		JavaRDD<String> lines = context.textFile(args[0]);
		//JavaRDD<String> lines = context.textFile("hdfs://bigdata111:9000/input/data.txt");
		
		/*
		 * 分词
		 * 定义匿名内部类FlatMapFunction<String, U>: String: 表示源数据的类型
		 *                                          U: 返回值的类型 String
		 */
		JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {

			@Override
			public Iterator<String> call(String data) throws Exception {
				//数据data : I love Beijing
				return Arrays.asList(data.split(" ")).iterator();
			}
		});
		
		/*
		 * 每个单词记一次数
		 * 返回: (Beijing,1)
		 * mapToPair: 相当于MapReduce中的Map的输出类型
		 * PairFunction<String, K2, V2>  String 每个单词  K2, V2 相当于Map的输出
		 */
		JavaPairRDD<String, Integer> mapOutput = words.mapToPair(new PairFunction<String, String, Integer>() {

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				// Beijing ---> (Beijing,1)
				return new Tuple2<String, Integer>(word,1);
			}
		});
		
		// Reduce操作（分组  累加）     Function2泛型表示： a     b     结果   
		JavaPairRDD<String, Integer> reduceOutput = mapOutput.reduceByKey(new Function2<Integer, Integer, Integer>() {
			
			@Override
			public Integer call(Integer a, Integer b) throws Exception {
				return a+b;
			}
		});
		
		//触发一个计算，输出到屏幕
		List<Tuple2<String, Integer>> finalResult = reduceOutput.collect();
        
		//输出到文件
		//reduceOutput.saveAsTextFile(path);
		
		//打印结果
		for(Tuple2<String, Integer> r: finalResult){
			System.out.println(r._1  + "\t" + r._2); 
		}
		
		//停止context
		context.stop();
	}

}

