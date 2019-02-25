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
    - 客户端：Driver Program，提交spark任务， **<u>核心：创建一个对象SC：SparkContext</u>**
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


# 三、提交Spark任务，执行Spark Demo程序
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

2. （用IDEA或者Scala IDE）开发Spark程序来执行任务：

    WordCount程序，处理HDFS数据

    1. Java版本

       ```java
       //
       
       
       
       ```

       

    2. Scala版本

       ```scala
       //
       ```

       

# 四、Spark执行原理分析
1. 分析WordCount程序处理过程

   ```
   //1. 单步运行WordCount （每一步都会产生一个新的RDD）
   bin/spark-shell --master spark://bigdata111:7077
   var rdd = sc.textFile("hdfs://bigdata111:9000/input/data.txt")
   
   //2. 
   
   
   
   
   
   
   ```

2. Spark提交任务的流程：类似Yarn调度任务的过程

# 五、Spark的算子（函数）
1. 重要：什么是RDD（类）
2. RDD的算子
    1. Transformation：不会触发计算
    2. Action：会触发计算
3. RDD的缓存机制
4. RDD的容错机制
5. RDD依赖关系

# 六、Spark Core编程案例
