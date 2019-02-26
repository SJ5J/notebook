# Spark 概述
1.  Spark core：
    - 执行引擎，相当于mapreduce，都是离线计算
    - 核心是RDD 弹性分布式数据集，由分区组成
2.  Spark SQL
    - 相当于HIVE，PIG： 
    - SQL和DSL语句 --> spark任务（RDD）--> 提交到SPARK集群运行
3. Spark Streaming
    - 相当于Storm
    - 流失计算： 连续的数据 --> 不连续的数据（Dstream离散流，其本质是RDD）

# 一、 什么是Spark
1.  为什么要学习Spark ： MapReduce的不足
    - MapReduce的缺点不足：核心Shuffle---> 产生大量I/O
    - 什么是Spark？http://spark.apache.org/     
    Apache Spark™ is a unified analytics engine for large-scale data processing. 

2. Spark的特点(参考官网)：基于内存
    1. 快
    2. 易用
    3. 通用
    4. 兼容性


# 二、Spark的体系结构与部署（重点）
1. 体系结构：主从结构（单点故障）
    - 官网提供了一张图  http://spark.apache.org/docs/latest/cluster-overview.html
    - **Spark没有对内存进行管理，内存的管理交给应用程序**；
    - 客户端：
      - Driver Program (spark-shell,  spark-submit)
      - 提交spark任务(如WordCount)， 
      - **<u>核心：创建一个对象SC：SparkContext</u>**
    - 服务器端： 主节点- Cluster Manager
      - 职责：相当于ResourceManager
      - 管理调度集群的资源和任务
      - 接收客户端任务请求
      - 部署方式：standalone(master)、Yarn、Mesos等（Hive on Spark部署文档）
    - 服务器端： 从节点- Worker Node  [Cache,  Task]
      - Worker：从节点上的资源和任务管理者
      - **Spark耗费内存的原因：Worker的默认行为是占用本节点的所有资源**
      - Worker启动多个Executor线程来执行Spark任务
2. 安装部署

    1. 伪分布 standalone

    ```shell
    //1. 安装包 spark-2.1.0-bin-hadoop2.7.tgz
    tar -zxvf spark-2.1.0-bin-hadoop2.7.tgz -C /root/
    
    //注意： 由于spark脚本命令和hadoop由冲突，只能设置一个
    
    //2. 配置文件 conf/spark-env.sh
    cd /root/spark-2.1.0-bin-hadoop2.7/conf/
    cp spark-env.sh.template spark-env.sh
    
    //3. 环境变量 JAVA_HOME SPARK_MASTER_DIR SPARK_MASTER_PORT
    vim spark-env.sh
    export JAVA_HOME=/root/jdk1.8.0_144
    export SPARK_MASTER_HOST=bigdata111
    export SPARK_MASTER_PORT=7077
    
    //4. 修改从节点配置 (把localhost删掉，填本机IP或主机名)
    cp slaves.template slaves
    vim slaves
    bigdata111
    
    //5. 启动spark
    /root/spark-2.1.0-bin-hadoop2.7/sbin/start-all.sh
    
    //6. Spark Web Console (内置Tomcat： 8080)
    http://ip:8080
    ```
    2. 全分布（三台）

       1. master：bigdata112 

       2. worker：bigdata113 bigdata114

    ```shell
    //前3步与前面伪分布一样
    
    //4. 修改从节点配置 (把localhost删掉)
    cp slaves.template slaves
    vim slaves
    bigdata113
    bigdata114
    
    //5. 把配好的主节点spark文件夹复制到从节点
    scp -r /root/spark-2.1.0-bin-hadoop2.7/ root@bigdata113:/root/
    
    scp -r /root/spark-2.1.0-bin-hadoop2.7/ root@bigdata114:/root/
    
    //6. 启动spark
    /root/spark-2.1.0-bin-hadoop2.7/sbin/start-all.sh
    
    //6. Spark Web Console (内置Tomcat： 8080)
    http://ip:8080
    ```

3. Spark HA：两种方式
    1. 基于文件目录(参考讲义)： 用于开发测试环境（伪分布，单机）

       1. 将spark Application和worker的状态信息写入一个目录。
       2. 一旦Master发生故障，就可以从该目录恢复sparkApplication和worker。只需重新启动Master进程（sbin/start-master.sh）。

    ```shell
    //停止spark
    // mkdir  /root/spark-2.1.0-bin-hadoop2.7/sbin/recovery
    cd /root/spark-2.1.0-bin-hadoop2.7
    sbin/stop-all.sh
    jsp
    mkdir recovery && cd recovery
    
    //修改配置文件
    vi spark-env.sh
    export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=FILESYSTEM -Dspark.deploy.recoveryDirectory=/root/spark-2.1.0-bin-hadoop2.7/recovery"
    
    //重新启动
    sbin/start-all.sh
    
    //查看recovery目录下生成worker状态信息
    
    //连接spark
    bin/spark-shell --master spark://bigdata111:7077
    
    //停掉master，spark-shell 看到断开连接了
    sbin/stop-master.sh
    
    //重新启动spark
    sbin/start-master.sh
    
    // spark-shell 回车后恢复连接了 
    
    :quit
    ```

    2. 基于ZooKeeper：用于生产环境

       1. Master点： bigdata112  bigdata113
       2. Worker节点：bigdata113 bigdata114

    ```
    //1. 提前安装好Zookeeper，并在每台节点上启动Zookeeper
    zkServer.sh start
    
    //2. 查看状态
    zkSserver.sh status
    
    //3. 修改spark-env.sh(每台都一样)
    export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=bigdata112:2181,bigdata113:2181,bigdata114:2181 -Dspark.deploy.zookeeper.dir=/spark"
    
    //4. 注释掉这两行(每台都一样)，改由ZK维护主节点信息
    #export SPARK_MASTER_DIR=bigdata111
    #export SPARK_MASTER_PORT=7077
    
    //5. 去主节点 bigdata112 启动spark
    which start-all.sh
    /root/spark-2.1.0-bin-hadoop2.7/sbin/start-all.sh
    
    // jps
    //主节点bigdata112： QuorumPeerMain   Master
    //从节点bigdata113： QuorumPeerMain   Worker
    //从节点bigdata114： QuorumPeerMain   Worker
    
    //6. 在bigdata113上单独启动一个master
    /root/spark-2.1.0-bin-hadoop2.7/sbin/start-master.sh
    
    // bigdata113上 jps
    // QuorumPeerMain   Master  Worker
    
    //此时112和 113都是master，但一个是ALIVE，一个是STANDBY
    
    //6. Spark Web Console http://ip:8080
    ```


# 三、提交并执行Spark Demo程序
1. 提交Spark任务的工具
    1. spark-submit:  用于提交Spark任务（jar文件 ）， 相当于 hadoop jar 命令 （提交MapReduce任务）；​

       ```
       // spark提供的例子jar包： 
       /root/spark-2.1.0-bin-hadoop2.7/examples/jars/spark-examples_2.11-2.1.0.jar
       
       examples/
       |
       |--jars (例子可执行jar包)
       |--src
           |
           |--main/
               |
               |--java
               |--R
               |--python
               |--scala/org/apache/spark/examples
               |
               |--resources/ (测试数据：txt json avro parquet)
                      |
                      |--txt
                      |--json
                      |--avro （阿福罗：序列化框架）
                      |--parquet（列式存储文件）
           
       //jars目录 提供例子可执行jar包
       
       //src/main目录下提供各种语言的例子源码（java，python，scala等）
       
       // src/main/resources 提供测试数据， 格式：
       // txt json avro parquet
       
       
       //执行scala例子：
       //蒙特卡罗求pi思想：落在圆里的飞镖与圆外的飞镖的比例
       //查看例子源码里的包名和类名
       // vi src/main/scala/org/apache/spark/examples/SparkPi.scala
       // 200是扔飞镖次数
       bin/spark-submit --master spark://bigdata111:7077 \
       --class org.apache.spark.exaples.SparkPi \
       examples/jars/spark-examples_2.11-2.1.0.jar 200
       
       //浏览器访问webconsle 查看任务执行状态
       http://bigdata111:7077
       ```

       

    2. spark-shell：交互式命令行工具，类似Scala的REPL命令行，类似Oracle的SQL*PLUS。

       1. spark-shell 本身也是作为spark任务（application）运行的，可以在webconsle看到。scala程序运行在这个application里。

       2. 有两种运行模式

       3. 本地模式： bin/spark-shell

          不连接到集群，再本地执行任务，类似storm的本地模式。

          spark-shell的启动信息：'sc' (master = local[*],

          开发程序： setMaster("local")

          ```shell
          bin/spark-shell spark://bigdata111:7077
          ```

       4. 集群模式：bin/spark-shell --master 

          连接到集群，在集群执行任务，类似storm的集群模式。

          spark-shell的启动信息： 'sc' (master = spark://***

          开发程序： 没有这句 setMaster("local")

          ```scala
          //启动hdfs
          start-dfs.sh
          
          //查看dfs数据
          hdfs dfs -cat /input/data.txt
          I love Beijing ....
          
          //进入spark-shell集群模式
          bin/spark-shell --master spark://bigdata111:7077
          
          //textFile可读取hdfs和本地文件
          //sc.textFile("/root/input/data.txt")
          //sc是SparkContext对象，该对象时提交spark程序的入口
          //textFile("hdfs://192.168.88.111:9000/data/data.txt")是hdfs中读取数据
          //flatMap(_.split(" "))先map在压平
          //map((_,1))将单词和1构成元组
          //reduceByKey(_+_)按照key进行reduce，并将value累加
          //.collect是把结果打印在屏幕上
          //.saveAsTextFile是保存结果到文件
          sc.textFile("hdfs://bigdata111:9000/input/data.txt").flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).collect
          
          //保存结果到hdfs中
          sc.textFile("hdfs://bigdata111:9000/input/data.txt").flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).saveAsTextFile("hdfs://bigdata111:9000/output/spark")
          
          //查看结果，产生两个结果文件，说明有两个分区
          hdfs dfs -ls /output/spark/
          
          //spark结果只产生一个分区
          sc.textFile("hdfs://bigdata111:9000/input/data.txt").flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).repartition(1).saveAsTextFile("hdfs://bigdata111:9000/output/spark111")
          
          
          ```

          

       5. spark-shell中单步运行WordCount （每一步都会产生一个新的RDD集合）

           ```scala
           bin/spark-shell --master spark://bigdata111:7077
           
           //延时读取数据（不会执行）  _ 代表前一句的每一句话
           var rdd1 = sc.textFile("hdfs://bigdata111:9000/input/data.txt")
           
           //将每句话进行分词，再合并到一个集合（Array）中
           var rdd2 = rdd1.flatMap(_.split(" "))
           
           //每个单词记一次数，完整： rdd2.map(word=>(word,1))
           var rdd3 = rdd2.map((_,1))
           
           //把相同key的value累加。 
           //reduceByKey(_+_) 完整：reduceByKey((a,b)=>a+b)
           //Array((Tom,1),(Tom,2),(Mary,3),(Tom,4))
           //第一步：分组 (Tom,(1,2,4)) (Mary,(3))
           //第二步：每组value求和 
           var rdd4 = rdd3.reduceByKey(_+_)
           
           // 打印结果
           rdd2.collect
           rdd3.collect
           ```

           

2. （用IDEA或者Scala IDE）开发Spark程序来执行任务：

    WordCount程序，处理HDFS数据

    1. Java版本

       ```java
       // eclips环境
       //1. 下载spark jar包 /root/spark-2.1.0-bin-hadoop2.7/jars/
       
       //2. new java 工程，导入jar包（或者用maven），复制jar包，粘贴到工程lib下
       // 然后在lib下全选jar包上右键->Build Path-> Add to Build Path (变成奶瓶)
       
       //3. src右键-> new package (demo)-> demo 右键-> new class (wordCount) 
       
       //4. 代码 JavaWordCount.java
       
       //5. 执行
       //a. 本地执行，需要设置 conf.setMaster("local"); 
       //context.textFile输入数据的参数 "hdfs://bigdata111:9000/input/data.txt"
       //代码区右键 Run As-> Java Application -> eclips输出打印结果
       
       
       //b. 集群执行, 注释掉这个  conf.setMaster("local"); 
       //context.textFile输入数据的参数用 args[0], 即提交jar时指定数据位置
       //打成jar包
       在package （demo）上右键 -> Export -> JAR file -> 选择保存路径文件名/root/sparkwc.jar -> 两次next -> 指定Main class （JavaWordCount）-> Finish
       
       //提交到集群执行
       bin/spark-submit --master spark://bigdata111:7077 --class demo.JavaWordCount /root/sparkwc.jar hdfs://bigdata111:9000/input/data.txt
       
       //终端打印出执行结果，也可以把结果保存到文件 reduceOutput.saveAsTextFile(path)
       ```

       

    2. Scala版本(用eclips的scalaIDE插件，或IDEA的SBT插件)

       ```scala
       // IDEA 环境
       //创建工程 
       new project -> Scala -> SBT -> 工程名 SparkDemo -> 选低的SBT版本 1.0.4 -> 选scala版本（与spark一致：看spark-shell）-> finish
       
       //导入jar包 (或者用maven)
       工程名右键 -> new Directory:lib -> cp jars files to lib -> 全选lib下的jar -> 右键Add as Library -> 
       
       //src-> main -> scala -> new Package(demo) -> new Scala Class (Object: MyWordCont)
       
       //代码 MyWordCount.scala
       
       //执行
       //1. 本地执行，数据和结果存在hdfs上
       start-dfs.sh
       代码区右键 -> Run MyWordCount
       //查看结果
       hdfs dfs -ls /output/0608/wc1
       
       //2. 用spark-submit提交到集群运行
       //配置jar包(Artifacts)
       File -> ProjectStructure -> Artifacts -> 点 "+" -> JAR -> Empty -> Name:MyWordCOunt 
       -> OutputLayout标签下 点"+" -> ModuleOutput(把源码编译后的 .class 文件添加到jar包) -> 
       选中 SparkDemo 工程 -> OutputLayout标签下 点MyWordCOunt.jar -> 点下面Create Manifest ->
       OK -> Main Class: 点... 填MyWordCount -> apply
       
       //生成jar包(Artifacts)
       菜单栏Build -> Build Artifects -> Build
       
       //生成的jar包在工程下的 out目录下
       cp out/MyWordCount.jar /root/
       
       //提交jar包到集群
       bin/spark-submit --master spark://bigdata111:7077 --class demo.MyWordCount /root/MyWordCount.jar hdfs://bigdata111:9000/input/data.txt hdfs://bigdata111:9000/output/0608/wc2
       
       hdfs dfs -ls /output/0608/wc2
       ```

       

# 四、Spark执行原理分析
## 1. 分析WordCount程序处理过程
1. sc.textFile算子读取数据
    1. 返回HadoopRDD<k1,v1>：偏移量，数据
    2. 执行map(pair->pair._2.toString) 即取出v1(文件内容)
    返回类型：MapPartitionsRDD[String]
2. .flatMap(_.split(" "))
    1. 分词： _.split(" ")，每句话返回一个Array，如 Arrya(I,love,Beijing)
    2. 压平： flatMap，即合并成一个Array 
3. .map((_,1)) 对上一步RDD中每个单词计一次数，返回一个元组： <单词,1>。相当于map阶段的结束。
4. .reduceByKey(_+_) 
    1. 分组/聚合： 将上一步的 <k2,v2> （即<单词,1>）按照key分组，
    得到<k3,v3> （如 {love,(1,1)}）
    2. 累加：_+_ 把聚合后的value进行累加，得到 {love,2}等

## 2. Spark提交任务的流程：类似Yarn调度任务的过程

1. 通过SparkContext对象提交任务请求给ClusterManager（Master）
2. ClusterManager将任务信息和资源分配给Worker
3. 各个节点的Worker就会启动Executor线程
4. 客户端提交jar包给worker执行
    1. Executor执行任务时，按阶段（stage）执行
    2. Spark任务会被划分成多个阶段。每个阶段可能会产生新的RDD，后一个RDD会依赖前一个RDD。而Stage的划分是根据RDD的依赖关系（宽、窄）。

# 五、Spark的RDD和算子
## 1. 什么是RDD
- Resident Distributed DataSet 弹性分布式数据集
- Spark中数据的基本抽象（类）
- Spark源码中关于RDD的说明
    - IDEA 搜索 类/文件名 (ctrl+shift+N): RDD.scala
    - RDD的五个主要特性
    - RDD的分区：
       - 源数据集合 即是 RDD，如集合(1,2,3,4,5,6,7,8,9,10)
       - RDD包含多个分区，每个分区可能给不同worker节点处理
          - 创建RDD时可指定分区数，如3个分区 (1,2,3,4) (5,6,7) (8,9,10)
          - 不指定的话，会用数据量和从节点数来计算默认分区数）
       - 所以，RDD是逻辑概念，分区是物理概念
       - Spark SQL 创建的 表（DataFrame）、Spark Streaming的核心:DStream输入流 ， 本质都是RDD
    ```
    * Internally, each RDD is characterized by five main properties:
    *  - A list of partitions                  一组分区, 即RDD由分区组成
    *  - A function for computing each split   函数，用于计算RDD中数据分片
    *  - A list of dependencies on other RDDs  RDD之间的依赖关系：宽、窄
    *  - Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)
    *  - Optionally, a list of preferred locations to compute each split on (e.g. block locations for an HDFS file)			 
    ```

- 如何创建RDD
   ```
   1. 登录spark-shell
   bin/spark-shell --master spark://bigdata111:7077
   
   2. 使用SparkContex创建RDD, 分区数为3 :
   val rdd1 = sc.parallelize(Array(1,2,3,4,5,6,7,8,9,10),3)
   
   3. 通过读取外部的数据源，直接创建RDD
   val rdd2 = sc.textFile("hdfs://bigdata111:9000/input/data.txt")
   val rdd3 = sc.textFile("/root/temp/input/data.txt")
   ```

## 2. RDD的算子
即RDD类的函数、方法，用于操作RDD集合中的数据

1. Transformation：不会触发计算，延时计算
    - map(func)：对原来的RDD中的每个元素，执行func的操作，并且返回一个新的RDD
    map(word=>(word,1))
    - filter： 过滤，选择满足条件的元素
    - flatMap = flatten + map
    - mapPartitions(func):  对原来的RDD中的每个分区，执行func的操作，并且返回一个新的RDD
    - mapPartitionsWithIndex(func):  跟mapPartitions一样，每个分区带有下标
    - union 并集
    - intersection 交集
    - distinct 去重
    - groupByKey： 分组
    reduceByKey： 分组，会有一个本地操作（相当于有一个Combiner）

2. Action：会触发计算
    - collect : 触发计算
    - count 求个数
    - first：求第一个元素

## 3. RDD的缓存机制
- 提高效率
- 默认：缓存内存中StorageLevel.MEMORY_ONLY
- 调用算子：persist或者cache
- 查看RDD.scala的源码
    ```
    /**
    * Persist this RDD with the default storage level (`MEMORY_ONLY`).
    */
    def persist(): this.type = persist(StorageLevel.MEMORY_ONLY)

    /**
    * Persist this RDD with the default storage level (`MEMORY_ONLY`).
    */
    def cache(): this.type = persist()			

    (*)缓存的位置： 查看StorageLevel.scala源码
    val NONE = new StorageLevel(false, false, false, false)
    val DISK_ONLY = new StorageLevel(true, false, false, false)
    val DISK_ONLY_2 = new StorageLevel(true, false, false, false, 2)
    val MEMORY_ONLY = new StorageLevel(false, true, false, true)
    val MEMORY_ONLY_2 = new StorageLevel(false, true, false, true, 2)
    val MEMORY_ONLY_SER = new StorageLevel(false, true, false, false)
    val MEMORY_ONLY_SER_2 = new StorageLevel(false, true, false, false, 2)
    val MEMORY_AND_DISK = new StorageLevel(true, true, false, true)
    val MEMORY_AND_DISK_2 = new StorageLevel(true, true, false, true, 2)
    val MEMORY_AND_DISK_SER = new StorageLevel(true, true, false, false)
    val MEMORY_AND_DISK_SER_2 = new StorageLevel(true, true, false, false, 2)
    val OFF_HEAP = new StorageLevel(true, true, true, false, 1)	
    ```
（*）举例：测试数据 sales文件29.3M：Oracle数据库中的订单表（大概92万条）
   （1）读取数据
     val rdd1 = sc.textFile("hdfs://bigdata111:9000/oracle/sales")
				 
   （2）计算:执行count操作
       rdd1.count   ---> Action操作，触发计算，这一次没有缓存
       rdd1.cache   ---> 缓存数据，但不会触发计算，不会立即缓存，cache是Transformation算子
       rdd1.count   --->  触发计算，同时把结果缓存
       rdd1.count   ---> 最后这一次，直接从缓存中读取数据
       通过检查Spark Web Console判断执行的时间
## 4. RDD的容错机制
（*）通过检查点checkpoint来实现，缺点：产生I/O
（*）复习：（1）HDFS的检查点，由SecondaryNameNode来进行日志合并		           			
（2）Oracle中，Oracle数据库中也有检查点，如果发生检查点，会以最高优先级唤醒数据库写进程（DBWn）把内存中的脏数据写到数据文件上（持久化）

（*）RDD的容错机制：由于RDD存在依赖关系，可能造成血统（LineAge：任务的执行的生命周期）越长
如果lineage越长，容易出错

（*）检查点可以把中间的计算结果保存起来
两种
（1）本地目录：参考讲义

（2）HDFS的目录（生产环境）
注意：这种模式，需要将spark-shell运行在集群模式上
hdfs dfs -mkdir /checkpoint0611

设置Spark的检查点目录
sc.setCheckpointDir("hdfs://bigdata111:9000/checkpoint0611")
val rdd1 = sc.textFile("hdfs://bigdata111:9000/oracle/sales")
rdd1.checkpoint  ----> 表示该RDD可以执行检查点
rdd1.count       ----> 计算

## 5. RDD依赖关系
用来划分任务的Stage（阶段）
（*）窄依赖：独生子女
每一个父RDD的分区最多被一个子RDD的分区引用

（*）宽依赖：超生
每一个父RDD的分区被多个子RDD的分区引用
参考讲义
# 六、 RDD的高级算子
1、mapPartitionsWithIndex：对RDD中的每个分区进行操作，带有分区号
		def mapPartitionsWithIndex[U](f: (Int, Iterator[T]) ⇒ Iterator[U], preservesPartitioning: Boolean = false)(implicit arg0: ClassTag[U]): RDD[U] 
	
		举例：val rdd1 = sc.parallelize(List(1,2,3,4,5,6,7,8,9), 2)
					
				定义函数，把每个分区中的元素打印出来
				接收一个函数参数：
					第一个参数：分区号
					第二个参数：分区中的元素				
				def func1(index:Int,iter:Iterator[Int]):Iterator[String]={
				   //iter代表一个分区中的所有元素
				   //x 代表该分区中的一个元素
					iter.toList.map(x=>"Partition ID:" + index +",value="+x).iterator
				}	
				
				调用：rdd1.mapPartitionsWithIndex(func1).collect
				结果：(Partition ID:0,value=1, 
				       Partition ID:0,value=2, 
					   Partition ID:0,value=3, 
					   Partition ID:0,value=4, 
				       Partition ID:1,value=5, 
					   Partition ID:1,value=6, 
					   Partition ID:1,value=7, 
					   Partition ID:1,value=8, 
					   Partition ID:1,value=9)
	
	2、aggregate：聚合操作
		存在两次聚合：（1）局部操作
		              （2）全局操作
		API:  def aggregate[U](zeroValue: U)(seqOp: (U, T)=> U, combOp: (U, U) => U)(implicit arg0: ClassTag[U]): U 
			参数: zeroValue: U：初始值  ----> 同时作用于：局部操作和全局操作
			      seqOp: (U, T)=> U  局部操作
			      combOp: (U, U) => U 全局操作 
			
		举例：val rdd1 = sc.parallelize(List(1,2,3,4,5), 2)
		      调用func1查看每个分区中的元素:rdd1.mapPartitionsWithIndex(func1).collect
			  Partition ID:0,value=1, Partition ID:0,value=2, 
			  Partition ID:1,value=3, Partition ID:1,value=4, Partition ID:1,value=5
			  
			  调用：aggregate
			   rdd1.aggregate(0)(math.max(_,_),_+_)   ----->  7
			  
			  课堂作业：rdd1.aggregate(0)(_+_,_+_)             结果：15
			            rdd1.aggregate(10)(math.max(_,_),_+_)  结果：30
		
			  课后作业：P57：“更复杂一点的例子”
		
		一个字符串的例子：
			val rdd2 = sc.parallelize(List("a","b","c","d","e","f"),2)
			
			修改一下刚才的查看分区元素的函数
			
			def func2(index: Int, iter: Iterator[(String)]) : Iterator[String] = {
			iter.toList.map(x => "[partID:" +  index + ", val: " + x + "]").iterator
			}
			
			两个分区中的元素：
			[partID:0, val: a], [partID:0, val: b], [partID:0, val: c], 
			[partID:1, val: d], [partID:1, val: e], [partID:1, val: f]

	3、aggregateByKey：针对的是<key,value>的数据类型，先对局部进行操作，再对全局进行操作
		（*）测试数据：
		val pairRDD=sc.parallelize(List( ("cat",2), ("cat", 5), ("mouse", 4),("cat", 12), ("dog", 12), ("mouse", 2)), 2)
		
		（*）查看每个笼子（分区）中的动物
			def func3(index: Int, iter: Iterator[(String,Int)]) : Iterator[String] = {
				iter.toList.map(x => "[partID:" +  index + ", val: " + x + "]").iterator
			}	
			
			pairRDD.mapPartitionsWithIndex(func3).collect
			
			结果：
			[partID:0, val: (cat,2)], [partID:0, val: (cat,5)], [partID:0, val: (mouse,4)], 
			[partID:1, val: (cat,12)], [partID:1, val: (dog,12)], [partID:1, val: (mouse,2)]
				
	4、coalesce与repartition：重分区
		（*）都是重分区
		（*）区别：coalesce 默认不会进行shuffle(false)
		           repartition 就会进行shuffle
				   
		（*）举例：
		     val rdd1 = sc.parallelize(List(1,2,3,4,5,6,7,8,9), 2)
			 查看分区个数：rdd1.partitions.length
			 重新分区: val rdd2 = rdd1.repartition(3)
			           val rdd3 = rdd1.coalesce(3,false)  --->  分区数：2
					   val rdd4 = rdd1.coalesce(3,true)   --->  分区数：3
		    
	文档：http://homepage.cs.latrobe.edu.au/zhe/ZhenHeSparkRDDAPIExamples.html


# 七、Spark Core编程案例
案例一：分析tomcat的访问日志，求访问量最高的两个网页
		1、对每个jps的访问量求和
		2、排序
		3、取前两条记录
		结果：ArrayBuffer((oracle.jsp,9), (hadoop.jsp,9))
	
	案例二：分析tomcat的访问日志，根据网页的名字进行分区（类似MapReduce中的自定义分区）
		结果： 网页的名字    访问日志
		       oracle.jsp    192.168.88.1 - - [30/Jul/2017:12:54:37 +0800] "GET /MyDemoWeb/oracle.jsp HTTP/1.1" 200 242
               oracle.jsp    192.168.88.1 - - [30/Jul/2017:12:54:53 +0800] "GET /MyDemoWeb/oracle.jsp HTTP/1.1" 200 242

			   
	案例三：把上面分析的结果，保存到Oracle中（知识点：在哪里建立Connection？）: 对于非序列化的对象，如何处理？



