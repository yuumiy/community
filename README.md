# 基于SpringBoot的学习社区

#### 1、项目环境

SpringBoot 2.1.5.RELEASE

Maven 3.5.2

Tomcat 8

jdk1.8

#### 2、技术栈

技术栈：Spring+Springmvc+Mybatis+SpringBoot+Mysql+Redis+Thymeleaf+Kafka+ElasticSearch+Quartz+Caffine

#### 3、项目启动方式

配置mysql、七牛云等信息。

打开zookeeper、kafka、elasticsearch、redis。



F:\JavaTools\redis-2.8.9>redis-server.exe redis.windows.conf

F:\JavaTools\kafka_2.12-2.3.0>bin\windows\zookeeper-server-start.bat config\zookeeper.properties

F:\JavaTools\kafka_2.12-2.3.0>bin\windows\kafka-server-start.bat config\server.properties

打开es的bin目录，打开es.bat

开发环境使用application-dev，生产环境使用application-pro，生产环境需要重新配置文件目录地址

#### 4、提供的账号
普通用户：aaa  　密码:123456　　　无权限

版主：bbb　　　密码:123456　　　置顶、加精权限

管理员：ccc 　　密码:123456　　　置顶、加精、删帖权限