# 一、安装之前
- 记录了在docker容器中安装HDP(amabari-server)的过程，并将整个HDP环境保存为Docker镜像，以方便以后使用或部署集群。
- 这只是最基本的docker容器和镜像的用法，没有做Dockerfile。只适合Docker和HDP入门，跟git上一些上完成度很高的作品没法比。
- Docker Hub上现在也有ambari官方制作的ambari-server和ambari-agent镜像，只是太久没更新（版本2.4）。git上也有第三方大佬制作ambari 2.6镜像。但是我觉得自己动手作一遍，对学习docker和hdp都会有帮助；
- 关于docker版本的选择，没有用yum源里的docker.io（这是docker的早期版本），而是从docker官网下载新版rpm，新版新增了一些命令（如网络命令），取消了配置文件（/etc/sysconfig/docker*），好像还取消了容器磁盘默认10G的限制（不太确定）。
- 开始之前，请确保你的宿主机linux系统已经安装好docker，并且可以访问外网（用于拉取docker镜像以及给容器安装几个应用程序）。
- 下面的操作有**宿主机系统**下的和**docker容器系统**下的，请看清楚再操作。
- 如果你对docker还是个麻瓜，那么这三个概念是必须理解的：
    - 什么是**宿主机系统**：在linux系统中安装了docker，那么这个linux系统就是docker容器的宿主机系统。
    - 什么是**基础镜像**：HDP需要依赖系统环境（比如centos7.4.1708）。所以先要从docker-hub上拉取一个centos镜像，然后用这个centos镜像创建一个容器，在容器里的centos之上安装HDP，进而制作自己的HDP镜像。所以这个centos镜像就是你的HDP镜像的**基础镜像**。
    - 什么是**容器**：如果说docker镜像是静态的文件，那么docker容器是通过镜像运行起来的进程。用命令“ docker run 镜像id“ 可以启动一个指定镜像的容器进程，用命令“ docker ps -a“ 可以查看当前全部的容器进程。用命令“docker stop 容器id“ 可以停止一个容器进程。用命令“docker start 容器id“ 可以启动一个容器进程。容器被创建之后也会生成容器文件占用磁盘空间，用命令“docker rm 容器id“ 可以删除一个容器的容器文件。

# 二、版本清单
- Docker 18.09.0
- jdk-8u144-linux-x64.tar.gz
- ambari-2.6.0.0-centos7.tar.gz
- HDP-2.6.3.0-centos7-rpm.tar.gz
- HDP-UTILS-1.1.0.21-centos7.tar.gz
- 基础镜像：centos:centos7.4.1708（只能选ambari支持的版本）
- 宿主机系统：任何支持Docker的linux发行版（参考docker官网，我用的centos7.4.1708）

# 三、开始安装
1. 下载好前面几个包，安装在宿主机系统下的只有Docker 18.09.0；
2. 宿主机：从docker-hub拉取基础镜像（需要外网）
    ```
    docker pull centos:centos7.4.1708
    docker images
    ```
3. 宿主机：解压到与容器共享的目录（把tar.gz复制进容器里面再解压也可以，解压到宿主集是方便后面继续创建ambari-agent的镜像）
    ```
    mkdir -p /home/sj/ambari/HDP-UTILS
    tar xzvf ambari-2.6.0.0-centos7.tar.gz -C /home/sj/ambari/
    tar xzvf HDP-2.6.3.0-centos7-rpm.tar.gz -C /home/sj/ambari/
    tar xzvf HDP-UTILS-1.1.0.21-centos7.tar.gz -C /home/sj/ambari/HDP-UTILS/
      
    tar xzvf jdk-8u144-linux-x64.tar.gz -C /root
    ```
4. 宿主机：创建容器，容器名三hdp1，容器主机名也是hdp1
    ```
    docker run -d -v /home/sj/ambari:/var/www/html/ambari -v /root/jdk1.8.0_144:/root/jdk1.8.0_144 --privileged --name hdp1 --hostname hdp1 centos:centos7.4.1708 /usr/sbin/init
    
    //查看创建的容器
    docker ps -a
    
    //进入容器centos的shell，可以在容器中看到共享的两个目录
    docker exec -ti hdp1 /bin/bash
    ```

5. 容器中：centos7系统环境配置
    ```
    //设置root用户密码
    echo "root:111111" | chpasswd
    
    //添加JAVA环境变量
    vi /etc/bashrc
    JAVA_HOME=/root/jdk1.8.0_144    
    export JAVA_HOME   
    
    PATH=$JAVA_HOME/bin:$PATH   
    export PATH
    
    //使JAVA_HOME立即生效
    source /etc/bashrc
    java -version
    
    //查看主机名为hdp1
    hostname -f
    
    //docker centos镜像没有firewalld和selinux，不需要关闭
    
    //查看本地IP与主机名映射，在添加hdp节点后，注意在这添加映射
    vi /etc/hosts
    172.17.0.2 hdp1
    ```
    
6. 容器中：HDP依赖的服务安装和配置
    ```
    //安装ssh，apache，ntp, jdbc驱动，mariadb
    yum makecache
    yum -y install net-tools openssh-server openssh-clients httpd ntp mysql-connector-java mariadb-server mariadb-bench mariadb-embedded

    //查看到容器本地IP为 172.17.0.2
    ifconifg
    
    //把四个服务起起来
    systemctl enable sshd.service
    systemctl start sshd.service
    
    systemctl enable httpd
    systemctl start httpd
    
    systemctl enable mariadb
    systemctl start mariadb
    
    systemctl enable ntpd
    systemctl start ntpd
    
    //一、配置ssh免密登陆
    //1. 生成秘钥对（不要给秘钥设置密码！）
    ssh-keygen -t rsa
    回车
    回车
    回车
    
    //2. 依次把本机公钥发给本机和别的HDP节点
    //第一次要输入系统root用户密码，即前面配的 111111
    ssh-copy-id -i ~/.ssh/id_rsa.pub root@hdp1
    
    //3. 用ssh登录到本机验证免密
    ssh hdp1
    exit
    
    //二、配置数据库
    //1. 
    mysql_secure_installation
    回车
    Y
    设置密码 Welcome_1
    设置密码 Welcome_1
    回车
    回车
    回车
    回车
    
    //2. 注意一行一行执行，不能全部粘贴一把梭
    mysql -uroot -pWelcome_1
    
    create database ambari;
    CREATE USER 'ambari'@'%'IDENTIFIED BY 'Welcome_1';
    GRANT ALL PRIVILEGES ON *.* TO 'ambari'@'%';
    FLUSH PRIVILEGES;
    
    
    create database hive;
    CREATE USER 'hive'@'%'IDENTIFIED BY 'Welcome_1';
    GRANT ALL PRIVILEGES ON *.* TO 'hive'@'%';
    FLUSH PRIVILEGES;
    
    
    create database oozie;
    CREATE USER 'oozie'@'%'IDENTIFIED BY 'Welcome_1';
    GRANT ALL PRIVILEGES ON *.* TO 'oozie'@'%';
    FLUSH PRIVILEGES;
    exit
    
    
    //三、配置ntp服务，解决ntpdate -d hdp1 错误 
    vi /etc/ntp.conf
    # Hosts on local network are less restricted.
    # 允许内网其他机器同步时间，IP是容器本地的网段
    restrict 172.17.0.0 mask 255.255.255.0 nomodify notrap
    
    # 外部时间服务器不可用时，以本地时间作为时间服务
    # 127.127.1.0是固的不能变
    server  127.127.1.0     # local clock
    fudge   127.127.1.0 stratum 10
    
    //重启ntp服务并验证
    systemctl restart ntpd
    ntpdate -d hdp1
    ```
7. 容器中：配置hdp和ambari本地源
    ```
    //1. 修改ambari本地源文件
    //hdp1 也可替换成server本地的IP，如172.17.0.2
    vi  /var/www/html/ambari/ambari/centos7/2.6.0.0-267/ambari.repo
    
    #VERSION_NUMBER=2.6.0.0-267
    [ambari-2.6.0.0]
    name=ambari Version - ambari-2.6.0.0
    baseurl=http://hdp1/ambari/ambari/centos7/2.6.0.0-267
    gpgcheck=1
    gpgkey=http://hdp1/ambari/ambari/centos7/2.6.0.0-267/RPM-GPG-KEY/RPM-GPG-KEY-Jenkins
    enabled=1
    priority=1
    
    //2. 修改HDP和HDP-UTILS本地源文件
    vi /var/www/html/ambari/HDP/centos7/2.6.3.0-235/hdp.repo
    #VERSION_NUMBER=2.6.3.0-235
    [HDP-2.6.3.0]
    name=HDP Version - HDP-2.6.3.0
    baseurl=http://hdp1/ambari/HDP/centos7/2.6.3.0-235
    gpgcheck=1
    gpgkey=http://hdp1/ambari/HDP/centos7/2.6.3.0-235/RPM-GPG-KEY/RPM-GPG-KEY-Jenkins
    enabled=1
    priority=1
    
    [HDP-UTILS-1.1.0.21]
    name=HDP Utils Version - HDP-UTILS-1.1.0.21
    baseurl=http://hdp1/ambari/HDP-UTILS
    gpgcheck=1
    gpgkey=http://hdp1/ambari/HDP-UTILS/RPM-GPG-KEY/RPM-GPG-KEY-Jenkins
    enabled=1
    priority=1
    
    //3. 将ambari.repo和hdp.repo拷贝到/etc/yum.repos.d/目录下
    cp /var/www/html/ambari/HDP/centos7/2.6.3.0-235/hdp.repo /var/www/html/ambari/ambari/centos7/2.6.0.0-267/ambari.repo /etc/yum.repos.d/
    
    //4. 删HDP下四个 .html文件
    rm -f /var/www/html/ambari/HDP/centos7/2.6.3.0-235/*.html
    
    //5. 用本地源安装ambari-server
    yum install -y ambari-server
    
    //6. 配置ambari-server 
    ambari-server setup
    y
    回车(root作为启动ambari后台进程的用户)
    3
    /root/jdk1.8.0_144
    y
    3
    回车
    回车
    回车
    回车
    Welcome_1
    Welcome_1
    回车
    
    //7. 将ambari需要的表导入数据库
    mysql -uambari -pWelcome_1
    use ambari
    source /var/lib/ambari-server/resources/Ambari-DDL-MySQL-CREATE.sql
    show tables;
    exit
    
    ```
8.  宿主机：到了这里可以暂停备份一下，将当前配置的容器生成新的镜像
    ```
    docker commit -m "new ambari-server setup" hdp1 centos:ambari.server.hdp1
    docker images
    
    //再稳妥一点，把镜像导出备份成文件
    docker save centos:ambari.server.hdp1 | gzip > docker-centos-ambari-server.tgz
    
    //如果要恢复镜像，只需
    docker load < docker-centos-ambari-server.tgz
    ```

9.  容器中：现在可以放心启动ambari了
    ```
    ambari-server start
    ```
    
10. 宿主机：浏览器访问docker镜像中的http 80服务和ambari 8080服务
    ```
    //将容器主机名hdp1映射到容器本地IP 172.17.0.2
    vi /etc/hosts
    172.17.0.2 hdp1
    
    //1. 先确认宿主机可以访问到容器，浏览器应该可以看到共享目录下的文件
    http://hdp1:80/ambari
    
    //如果无法从宿主机访问容器内部的服务，你可能在用旧版docker，
    //旧版默认的NAT模式需要在docker run命令中加上 -p 80:80 -p 8080:8080
    //来作转发，或者配置docker的桥接网卡。
    //我用的新版docker貌似可以不用这些配置，直接访问到容器里的服务端口。
    
    
    //2. 浏览器访问 http://hdp1:8080，输入用户名/密码
    admin
    admin
    
    //3. 选Launch Install Wizard
    Next->
    
    //4. 填写集群名： mycluster  
    Next->
    
    //5. 下面的Repositories只保留redhat7，并修改仓库源路径（这两个路径可以在浏览器中访问）
    http://hdp1/ambari/HDP/centos7/2.6.3.0-235
    http://hdp1/ambari/HDP-UTILS
    Next->
    
    //6. 域名填hdp1，私钥在容器中查看cat /root/.ssh/id_rsa
    填入hdp1
    填入复制的私钥
    Next->
    
    //7. 会提示域名不是完整FQDN域名（类似hdp1.org），无视
    Next->
    
    //8. 自动检查HDP必须的服务是否可访问，全绿表示环境服务都正常
    //会有一个ntp和chronyd的warning，可能是因为宿主机到容器ntp服务访问有问题，也可能是需要安装chronyd服务，没有验证，我选择无视这个告警。
    
    //9. 只勾选HDFS 和 YARN + MapReduce2
    Next->
    OK
    OK
    Next->
    Next->
    
    //10. 在容器中创建目录
    mkdir -p /root/hdp/namenode
    mkdir -p /root/hdp/data
    mkdir -p /root/hdp/yarn/local
    mkdir -p /root/hdp/yarn/log
    
    //然后在标签中填写这些目录
    //HDFS标签: NameNode 填 
    /root/hdp/namenode
    
    //HDFS标签: DataNode 填 
    /root/hdp/data
    
    //YARN->Advanced标签: yarn.nodemanager.local-dirs填
    /root/hdp/yarn/local
    
    //YARN->Advanced标签: yarn.nodemanager.log-dirs填
    /root/hdp/yarn/log
    
    //Ambari Metrics和SmartSense标签分别填写密码：111111 （随意）
    Next->
    Deploy->
    
    //11. 配置完成，开始自动安装服务，结果全绿表示安装成功
    ```
11. 容器中：还要修改一个重启后会有主机检测不到心跳的问题，估计跟SSL库升级有关
    ```
    //在容器中
    vi /etc/ambari-agent/conf/ambari-agent.ini
    //在[security]段添加一行
    force_https_protocol=PROTOCOL_TLSv1_2
    ambari-agent restart
    ```
12. 现在重启容器和ambari以验证稳定性，如果重启后可正常开启服务，就可以把容器制作成镜像以备日后使用
    ```
    //1. 宿主机：重启并进入容器
    docker stop hdp1
    docker start hdp1
    docker exec -ti hdp1 /bin/bash
    
    //2. 容器中：启动ambari
    ambari-server start
    ambari-agent start
    
    //3. 宿主机：浏览器访问 http://hdp1:8080，然后Start all
    //如果可正常启动服务，就可以作成镜像了
    //宿主机：提交容器的修改到镜像
    docker commit -m "ambari-server installed" hdp1 centos:ambari.server.ok
    docker images
    
    //4. 也可把镜像导出备份成文件
    docker save centos:ambari.server.ok | gzip > docker-centos-ambari-server.tgz
    ```
13. 以后要启用hdp主机时，只需几个命令
    ```
    //1. 创建hdp容器
    docker run -d -v /home/sj/ambari:/var/www/html/ambari -v /root/jdk1.8.0_144:/root/jdk1.8.0_144 --privileged --name hdp1 --hostname hdp1 centos:ambari.server.ok /usr/sbin/init
    
    //2. 进入容器
    docker exec -ti hdp1 /bin/bash
    
    //3. 容器中：启动ambari
    ambari-server start
    ambari-agent start
    
    //4. 浏览器访问 http://hdp1:8080，并Start all
    ```
14. 以上是ambari-server的安装配置，ambari-agent我还不会搭；
15. 最后，如果有任何建议或发现任何问题，请联系我的QQ 119647515 ，另外转载请注明出处。

