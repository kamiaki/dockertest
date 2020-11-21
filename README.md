# 7分钟学会docker

## 在线安装

```markdown
# 安装yum工具包，不装的话没法远程安装
yum install -y yum-utils
# 设置代理镜像，不用国外的慢
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
# 更新包信息，提高下载速度
yum makecache
# 如果是centos8，安装依赖，特别重要
yum install https://download.docker.com/linux/fedora/30/x86_64/stable/Packages/containerd.io-1.2.6-3.3.fc30.x86_64.rpm
# 安装docker，分别是docker社区版，docker客户端，和容器运行器三个东西
yum install docker-ce docker-ce-cli containerd.io
# 添加阿里云加速
# 阿里云，控制台，找到容器镜像服务
mkdir -p /etc/docker
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://ifxzmdbu.mirror.aliyuncs.com"]
}
EOF
systemctl daemon-reload
# 运行
systemctl stop firewalld
systemctl start docker
# 卸载
yum remove docker-ce docker-ce-cli containerd.io
rm -rf /var/lib/docker
```

## 离线安装

```markdown
# 下载
https://download.docker.com/linux/static/stable/x86_64/docker-19.03.11.tgz
# 解压复制安装
tar xzvf docker-19.03.11.tgz
cp docker/* /usr/bin/

# 写配置文件
vim /usr/lib/systemd/system/docker.service

# 在文件里写如下内容，保存
[Unit]
Description=Docker Application Container Engine
Documentation=https://docs.docker.com
After=network-online.target firewalld.service
Wants=network-online.target
[Service]
Type=notify
ExecStart=/usr/bin/dockerd
ExecReload=/bin/kill -s HUP $MAINPID
LimitNOFILE=infinity
LimitNPROC=infinity
TimeoutStartSec=0
Delegate=yes
KillMode=process
Restart=on-failure
StartLimitBurst=3
StartLimitInterval=60s
[Install]
WantedBy=multi-user.target

# 从新加载
systemctl daemon-reload

# 运行
systemctl stop firewalld
systemctl start docker
```



## 创建docker应用

一瞬间，搭建泰拉瑞亚游戏服务器

```markdown
# 搜索泰拉瑞亚
docker search terraria
# 拉取泰拉瑞亚
docker pull ryshe/terraria
# 创建一个泰拉瑞亚
docker run -d -it --rm -p 7777:7777 -v /docker/terraria/world:/root/.local/share/Terraria/Worlds ryshe/terraria:latest -world /root/.local/share/Terraria/Worlds/akiworld.wld -autocreate 1
# 回到一个已有的泰拉瑞亚
docker run  --rm -p 7777:7777 -v /docker/terraria/world:/root/.local/share/Terraria/Worlds ryshe/terraria:latest -world /root/.local/share/Terraria/Worlds/akiworld.wld

# 玩
```

一瞬间，搭建mysql数据库

```markdown
# mysql
docker search mysql
# 拉mysql
docker pull mysql:5.6
# 运行
docker run --name mysql56 -v /docker/mysql:/var/lib/mysql -p 6666:3306 -e MYSQL_ROOT_PASSWORD=666666 -d mysql:5.6 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
# 进入容器 可以不用
docker exec -it mysql56 /bin/bash
# 改权限 可以不用
mysql -u root -p
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '666666' WITH GRANT OPTION;
flush privileges;
exit;
```



## 创建自己的镜像

写dockerfile，dockerfile就是自己创建的镜像的图纸，docker会根据这个图纸创建镜像

```markdown
# 写Dockerfile
FROM java:8
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
COPY *.jar /app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app.jar","--server.port=8888"]


# build镜像
docker build -f Dockerfile -t dockerlearn .

# 运行镜像
docker run -d -p 80:8888 --name myapp dockerlearn

# 查看日志
docker logs -f myapp 


FROM
#基础镜镜像，—切从这里开始构建
MATNTAINER
#镜像是谁写的，姓名+邮箱
RUN
#镜像构建的时候需要运行的命令
ADD
#步骤:tomcat境像，这个tomcat压缩包!添加内容
WORKDIR
#镜像的工作目录
VOLUME
#挂载的目录
EXPOSE
#保留端口配置
CMD
#一般用做参数 比如 java -jar xxx.jar --server.port=8080
#--server.port=8080就是 CMD要写的 会被替换
ENTRYPOINT
#一般用做不变的指令 比如 java -jar xxx.jar 
ONBUILD
#当构建一个被继承DockerFile这个时候就会运行 ONBUILD的指令。触发指令。
COPY
#类似ADD ，将我们文件拷贝到镜像中
ENV
#构建的时候设置环境变量!
```

commit

```
//必须是将容器生成镜像
//docker commit -a "作者" -m "备注信息" 容器id 新镜像名:版本
docker commit -a "aki" -m "测试commit" 4a17138d446e kamiaki/pushtest:2.0

//push如果版本一致就会覆盖
docker push kamiaki/pushtest:latest

//指令说明
-a :提交的镜像作者；
-c :使用Dockerfile指令来创建镜像；
-m :提交时的说明文字；
-p :在commit时，将容器暂停。

```



## dockercompose

```markdown
# 安装
sudo curl -L "https://github.com/docker/compose/releases/download/1.27.3/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
# 授权
sudo chmod +x /usr/local/bin/docker-compose
# 软连接
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
# 测试
docker-compose --version


# 写docker-compose.yml
version: '2'
services:
  mydockerlearn:
    image: dockerlearn
    container_name: "mydockerlearn"
    ports:
      - "80:8888"
    networks:
      - my-docker-net
    depends_on:
      - mysql56
  mysql56:
    image: mysql:5.6
    container_name: "mysql56"
    ports:
      - "6666:3306"
    volumes:
      - /docker/mysql:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: 666666
    networks:
      - my-docker-net
networks:
  # 配置docker network
  my-docker-net:
    driver: bridge
    ipam:
      config:
        # 子网络
        - subnet: 10.10.0.0/16
          gateway: 10.10.0.1


# 运行
docker-compose up
docker-compose up -d 后台运行
docker-compose stop
docker-compose start 重启之后就在后台运行了

docker-compose logs -f -t --tail 300 查看日志
```







## 基本命令

### 所有命令文档

<https://docs.docker.com/engine/reference/commandline/>



### 帮助命令

```shell
docker version 	版本
docker info 	运行信息
docker 	万能命令 help
```



### 启动 关闭重启docker

```shell
systemctl start docker
systemctl stop docker
systemctl restart docker
```



### 镜像相关命令

#### 搜索镜像

可以去网站搜索，<https://hub.docker.com/> 

```shell
docker search mysql 	搜索mysql
docker search mysql --filter=stars=3000		搜索星星不小于3000的
```

#### 下载镜像

```shell
docker pull mysql
docker pull mysql:5.7	指定版本下载
docker pull centos
```

#### 查看镜像

```shell
docker images 查看所有镜像
docker images -q 只显示id
```

#### 删除镜像

```shell
docker rmi -f a4fdfd462add  		删除的是id
docker rmi $(docker images -aq）		批量删除
```

#### 镜像的导入和导出

```shell
    1.导出镜像：
    docker save kamiaki/soilapp | gzip > soilapp.tar.gz
    docker save kamiaki/soilmysql  | gzip > soilmysql.tar.gz

    2.导入镜像：
    gunzip -c  soilapp.tar.gz | docker load
    gunzip -c  soilmysql.tar.gz | docker load
```

#### docker commit 提交

```shell
//必须是将容器生成镜像
//docker commit -a "作者" -m "备注信息" 容器id 新镜像名:版本
docker commit -a "aki" -m "测试commit" 4a17138d446e kamiaki/pushtest:2.0

//push如果版本一致就会覆盖
docker push kamiaki/pushtest:latest

//指令说明
-a :提交的镜像作者；
-c :使用Dockerfile指令来创建镜像；
-m :提交时的说明文字；
-p :在commit时，将容器暂停。
```





### 容器相关命令

#### 容器命令

```shell
docker run image
//参数
--name="名字"		容器名字
-d				后台运行
-it				交互方式运行，进入容器查看内容，进入容器
-p				指定端口 -p 8080:8080 -p 主机端口:容器端口 -p 容器端口
-P				随机指定端口
-v    			数据卷挂载       -v /home/aki/mysql:/var/lib/mysql  -v 本地:容器
--net host		//解决容器没有网的问题

//启动并进入容器
docker run -it centos /bin/bash
//退出容器
exit
//容器不停止退出
ctrl + p + q

//查看运行的容器
docker ps

//删除容器
docker rm id
docker rm -f id  //强制删除 关闭停止后删除
docker rm -f $(docker ps -aq)  //批量删除

//启动和停止容器
docker start id
docker restart id
docker stop id		//与kill相同，但优雅 注意看stop是优雅的停止，kill是野蛮的停止，
docker kill id

//查看匿名挂载
docker volume  ls 
```

#### docker --link

```shell
//docker run -d -P --name 容器名 --link 需要连接的容器名 容器镜像
docker run -d -P --name webapp --link mysql kamiaki/webapp

//这样就可以ping通了，两个容器 直接ping通 反向无法ping通
docker exec -it webapp ping mysql

//link 过去的容器名 就是 ip，比如mysql地址写法如下:
docker-mysql-name: soilmysql
url: jdbc:mysql://${docker-mysql-name}:3306/soil?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
```





### 常用命令

```shell
//后台运行
docker run -d centos 如果后台运行，就必须有一个前台服务。

//日志
docker logs -tf --tail 10 id

//查看进程
docker top id

//查看镜像元数据
docker inspect id

//进入当前容器 bash是命令行工具  开启一个新的终端
docker exec -it id /bin/bash

//进入容器2 进入容器命令行  进入正在运行的终端 不推荐 因为退出会关闭容器
docker attach id

///拷贝文件 从容器里拷贝文件出来
docker cp 容器id:/aaa.txt /xxxxxxx

///拷贝文件 拷贝进去
docker cp /opt/test.js testtomcat:/usr/local/tomcat/webapps/test/js

//杀所有 删所有
docker kill $(docker ps -a -q)
```

