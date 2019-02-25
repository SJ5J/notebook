

上次课：Storm和HBase的集成问题（演示）
注意一个问题：执行Storm的本地模式（权限不足）：需要使用管理员来启动Eclipse

------------------------------------------
Spark:大数据的计算引擎

第一部分：Scala编程语言
第二部分：Spark Core内核（最重要的内容）---> 概念RDD：相当于MapReduce
第三部分：Spark SQL：相当于Hive，也支持SQL语句 -----> 底层依赖Spark Core ----> 依赖RDD
第四部分：Spark Streaming：相当于Storm用于流式计算 - ----> 底层依赖Spark Core ----> 依赖RDD
                           注意：但是Spark Streaming不能做到实时性很高

==========================  第一部分：Scala编程语言 ============================
第一章：Scala基础
一、Scala简介：多范式的编程语言
	1、多范式：支持面向对象、支持函数式编程
	2、底层依赖JVM
	
二、安装配置Scala、常用的开发工具
	1、安装配置
		版本：2.11.8版本跟Spark的版本一致（spark-2.1.0-bin-hadoop2.7.tgz）
		      scala-2.11.8.zip（Windows）
		      scala-2.11.8.tgz（Linux）
			  
		以windows为例：类似JDK的安装
			（1）解压: C:\Java\scala-2.11.8
			（2）设置SCALA_HOME: C:\Java\scala-2.11.8
			（3）把%SCALA_HOME%/bin加入PATH路径
			（4）执行: scala -version
			
	2、常用开发工具
		（1）REPL：命令行
				   退出： :quit
		
		（2）IDEA: 默认没有Scala环境，安装插件SBT（需要联网）
		
		（3）Scala IDE：就是Eclipse

三、Scala的常用数据类型
	1、注意：在Scala中，任何数据都是对象。
		举例：数字 1 ----> 是一个对象，就有方法
		scala> 1.toString
		res0: String = 1     ----> 定义了新的变量 res0，类型String
		
	2、Scala定义变量的时候，可以不指定变量的类型，Scala会进行类型的自动推导
		举例：下面的语句是一样的
		      var a:Int = 10
			  var b = 10
			  
		如何定义常量？ val
		      val c = 10
		
	3、数据的类型
		（1）数值类型：复习
			（*）Byte：  8位的有符号   -128~127
			（*）Short：16位的有符号   -32768 ~ 32767
			（*）Int:   32位的有符号
			（*）Long： 64位的有符号
			（*）Float：浮点数
			（*）Double：双精度
		
		（2）字符串：Char、String
			 对于字符串，在Scala中可以进行插值操作
			 val s1 = "hello world"
			 
			 可以在另一个字符串中，引用s1的值
			 "My name is Tom and ${s1}"
		
		（3）Unit类型：相当于Java中的void 类型
				()代表一个函数：没有参数，也没有返回值
		
				scala> val f = ()
				f: Unit = ()
				
				val f = (a:Int)
		
		（4）Nothing类型：一般来说，表示在函数（方法）执行过程中产生了Exception
			举例： 定义函数 def
			       def myfunction = throw new Exception("some exception ....")
				   myfunction: Nothing
		
四、Scala的函数	
	1、内置函数：数学运算
		举例：求最大值
		max(1,2)
		
		包: import scala.math._
		https://www.scala-lang.org/files/archive/api/2.11.8/#package
		
		scala> max(1,2)
		res4: Int = 2  ----> 定义了一个新的变量来保存运算的结果
		
		var result:Int = max(1,2)
		var result = max(1,2)
	
	2、自定义函数：def
		
		
	3、Scala的条件表达式 if.. else
		//注意：scala中函数的最后一句话，就是函数分返回值
		//不写reture		
		
五、循环： for、while、do...while

六、Scala函数的参数：求值策略
	1、call by value：对函数的实参求值，并且仅求一次
		举例：def test1(x:Int,y:Int):Int = x+x  没有用到y	
	2、call by name：函数的实参每次在函数体内部被调用的时候，都会进行求值
		举例：def test2(x: => Int,y: =>Int):Int = x+x  没有用到y
		

	3、一个复杂点的例子
		x是call by value
		y是call by name
		
	   def test3(x:Int,y: =>Int):Int = 1
	   
	   再定义一个死循环的函数
	   def loop():Int = loop
	   
	   考虑下面的两个调用
	   test3(1,loop) ---> 正常
	   test3(loop,1) ---> 死循环
	   
	4、函数的参数：默认参数、代名参数、可变参数

七、lazy值：如果一个变量被lazy修饰了，他的初始化会被推迟到第一次使用的时候
	举例1
		scala> var x = 10
		x: Int = 10

		scala> lazy val y = x + 10
		y: Int = <lazy>

		scala> y
		res0: Int = 20	
		
	举例2：读文件（存在）
	       读文件（不存在）
			scala> val words = scala.io.Source.fromFile("d:\\temp\\a.txt").mkString
			words: String = I love Beijing

			scala> val words1 = scala.io.Source.fromFile("d:\\temp\\b.txt").mkString
			java.io.FileNotFoundException: d:\temp\b.txt (系统找不到指定的文件。)
			  at java.io.FileInputStream.open0(Native Method)
			  at java.io.FileInputStream.open(FileInputStream.java:195)
			  at java.io.FileInputStream.<init>(FileInputStream.java:138)
			  at scala.io.Source$.fromFile(Source.scala:91)
			  at scala.io.Source$.fromFile(Source.scala:76)
			  at scala.io.Source$.fromFile(Source.scala:54)
			  ... 32 elided

			scala> lazy val words1 = scala.io.Source.fromFile("d:\\temp\\b.txt").mkString
			words1: String = <lazy>

			scala> words1
			java.io.FileNotFoundException: d:\temp\b.txt (系统找不到指定的文件。)
			  at java.io.FileInputStream.open0(Native Method)
			  at java.io.FileInputStream.open(FileInputStream.java:195)
			  at java.io.FileInputStream.<init>(FileInputStream.java:138)
			  at scala.io.Source$.fromFile(Source.scala:91)
			  at scala.io.Source$.fromFile(Source.scala:76)
			  at scala.io.Source$.fromFile(Source.scala:54)
			  at .words1$lzycompute(<console>:11)
			  at .words1(<console>:11)
			  ... 32 elided

			scala>

八、异常:Exception
	补充：复习：异常处理的机制 ----> 向上处理异常机制

九、数组、映射、元组
	1、数组：定长数组  Array
	         变长数组  ArrayBuffer
			 
	2、映射Map、元组Tuple
		
第二章：Scala面向对象：类似Java
	一、复习：面向对象的基本概念
		（*）定义：把数据和操作数据的方法放到一起，作为一个整体（类 class）
		（*）面向对象的特质：
			（1）封装
			（2）继承
			（3）多态
	
	
	二、定义类class
	
	三、属性的get和set方法
	
	四、内部类（嵌套类）：在类的内部，又定义了一个类
		举例：创建类，保存学生和学生考试成绩（科目 成绩）
	
	
	五、类的构造器
		1、主构造器：跟类名写在一起，只能有一个主构造器
		2、辅助构造器：可以有个多个，关键字 this 
		
	六、Object对象（相当于Java中的static）
		1、在Object对象中的所有内容都是静态的
		2、举例（1）：实现单例模式：一个类只有一个对象
		   举例（2）：省略main方法，objec对象需要继承App父类

	七、apply方法：作用：使得程序简单，省略new关键字

	八、继承:Scala和Java一样，使用extends关键字扩展类。
		例子：参考讲义
	

	九、特质（trait）：当成Java中的抽象类，支持多重继承（像Java的接口）
		trait就是抽象类。trait跟抽象类最大的区别：trait支持多重继承

	十、包和包对象: 参考讲义


第三章：Scala函数式编程（最有特色）：核心概念---> 把一个函数作为另一个函数参数的值传递过去
	一、回顾：Scala函数：def关键字
	
	二、什么是匿名函数：没有名字的函数
	
	三、带函数参数的函数：也叫：高阶函数
						  把一个函数作为另一个函数参数的值传递过去
	                      一个函数的参数值是另一个函数
						  
	四、闭包、柯里化
	五、示例：常用的高阶函数
		1、map：对集合中的每个元素进行操作，返回一个结果
			val numbers = List(1,2,3,4,5,6)
			每个元素乘以2
			numbers.map((x:Int)=>x*2)
			简写方式
			numbers.map(_*2)
		
		
		2、foreach：对集合中的每个元素进行操作，不返回结果
			numbers.foreach(_*2)
		
		3、filter：过滤，选择满足条件的元素
			选择能够被2整除的元素
			numbers.filter((i:Int)=> i%2==0)
			
			完整
			numbers.filter((i:Int)=>{
				if(i%2 == 0){
					true
				}else{
				    false
				}
			})
		
		4、zip：把两个集合合并成一个
			List(1,2,3).zip(List(4,5,6))
			
			List(1,2,3).zip(List(4,5,6,7))
		
		5、partition: 分区，根据分区条件，把满足条件的分成一个区，不满足条件的分成另一个区
			val numbers = List(1,2,3,4,5,6)
			能够被2整除的分成一个区，不能整除的分成另一个区
			numbers.partition((i:Int)=> i%2==0)
			
			完整
			numbers.partition((i:Int)=>{
				if(i%2 == 0){
					true
				}else{
				    false
				}
			})		
				
		6、find: 查找第一个满足条件的元素
			能够被3整除的元素
			numbers.find(_%3 == 0)
		
		
		7、flatten：把一个嵌套的结果展开
			List(List(1,2,3),List(4,5,6)).flatten
				
		8、flatMap = flatten + map
			举例
			val myList = List(List(1,2,3),List(4,5,6))
			myList.flatMap(x=>x.map(_*2))
			
			第一步：将List(1,2,3)和List(4,5,6)展开 (1,2,3,4,5,6)
			第二步：(1,2,3,4,5,6)每个元素乘以2

第四章：Scala常用集合
	一、可变集合、不可变集合
	
	二、列表
	
	三、序列
	
	四、Set：不重复元素的集合，默认是：HashSet
	
	五、模式匹配：就相当于switch ... case 语句
	
	六、样本类：case class，支持模式匹配，就相当于支持switch ... case 语句   相当于 instanceof


第五章：Scala的高级内容：泛型
	一、泛型类：定义类的时候，接收一个泛型参数
	
	二、泛型函数：定义函数的时候，接收一个泛型参数
		举例：
			（1）定义函数：创建一个Int类型的数组
			    def mkIntArray(elem:Int*) = Array[Int](elem:_*)
				mkIntArray(1,2,3)
				mkIntArray(1,2,3,4,5)
				
			（2）定义函数：创建一个String类型的数组
				def mkStringArray(elem:String*) = Array[String](elem:_*)
				mkStringArray("Tom","Mary")
				
			（3）在函数中使用泛型，取代上面的两个函数
				import scala.reflect.ClassTag
				def mkArray[T:ClassTag](elem:T*)=Array[T](elem:_*)
				
				说明：ClassTag表示Scala在运行时候的状态信息，这里表示调用时候的数据类型
				
				调用
				mkArray(1,2,3,4,5)
				mkArray("Tom","Mary")
	
	三、泛型的上界和泛型的下界：规定泛型的取值范围
	    1、普通的数据类型为例
		          10 <= x:Int <=100  ----> 规定了x的范围(10,100)
				  
		2、规定：类型的取值范围 ---> 下界  上界
				定义几个类（继承关系）  Class A ---> Class B ---> Class C  ---> Class D
				
				定义泛型
				    D  <: T 泛型类型 <: B    ---->  泛型T的取值：B、C、D
		
		3、概念
			上界：定义 S <: T 表示S的类型必须是T的子类
			下界：定义 U >: T 表示U的类型必须是T的父类
			
		4、举例：上界为例
		5、举例：拼加字符，接收类型必须是String或者String的子类
		         def addTwoString[T<:String](x:T,y:T) = {println(x+"*****"+y)}
				 
				 调用
				   addTwoString("abc","xyz")  ---> abc*****xyz
				   addTwoString(100,200)      ---> 期望：100*****200
					错误
			<console>:13: error: inferred type arguments [Int] do not conform to method addTwoString's type parameter bounds [T <
				   addTwoString(100,200)
				   ^
			<console>:13: error: type mismatch;
			 found   : Int(100)
			 required: T
				   addTwoString(100,200)
								^
			<console>:13: error: type mismatch;
			 found   : Int(200)
			 required: T
				   addTwoString(100,200)					

				实际上：addTwoString(100,200) 
						1、首先 100和200 转换成字符串 "100" "200"
						2、再拼加 100*****200
		
								
	四、视图界定： 扩展了上界和下界的范围，表示方式：<% 
		1、可以接收：（1）上界和下界的类型
		             （2）允许接收通过隐式转换过去的类型
					 
				实际上：addTwoString(100,200) 
						1、首先 100和200 转换（隐式转换）成字符串 "100" "200"
						2、再拼加 100*****200
						
		2、改写上面的例子		 
			（*）定义订转换规则：隐式转换函数
					implicit def intToString(n:Int):String = {n.toString}
					
			（*）使用视图界定改写 addTwoString
					def addTwoString[T <% String](x:T,y:T) = {println(x+"*****"+y)}
					表示：T可以是String和String的子类
					        也可以是能够转换成String的其他类型
							
							
			（*）分析一下执行的过程
					addTwoString(100,200)
					（1）检查参数的类型：Int类型
					     接收的是String的类型
						 
					（2）在当前的会话中，查找有没有一个隐式转换函数：Int ---> String
					（3）如果找到，先调用这个隐式转换函数
					     如果没有找到，出错
						 
					（4）再调用函数
		
	五、协变和逆变
		概念
		1、协变：泛型变量的值可以是本身或者其子类的类型
		
		
		2、逆变：泛型变量的值可以是本身或者其父类的类型
	
	
	六、隐式转换函数：使用关键字implicit
		1、什么是隐式转换函数？ 使用关键字implicit，由Scala自动调用
			implicit def intToString(n:Int):String = {n.toString}
			
			
		2、举例：定义一个隐式转换函数，针对class
	
	七、隐式参数：是在参数的前面加上implicit
				用途：实现隐式转换
	
	八、隐式类：是在类的前面加上implicit
				用途：增强类的功能（类似：包装设计模式，动态代理对象）

	
（一两年前：Scala的Actor编程：已经废弃）














