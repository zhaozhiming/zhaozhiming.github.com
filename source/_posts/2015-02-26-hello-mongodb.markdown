---
layout: post
title: "MongoDB简介"
date: 2015-02-26 16:42
description: MongoDB简介
keywords: mongodb
comments: true
categories: code
tags: mongodb
---
  
{% img /images/post/2015-2/mongodb.jpeg %}  
  
[MongoDB][mongodb]是一个[NoSQL][nosql](Not only SQL)数据库，使用C++语言编写，当前最新版本为3.0(beta)。  
  
<!--more-->  
  
## 安装

在MongoDB官网上有各个OS的安装指导，但在docker横行的时代，使用docker来安装无疑是最方便的，这里是[MongoDB的docker镜像地址][docker_mongodb]，使用非常简单，执行以下命令，docker就会自动下载镜像并启动MongoDB容器了。  
  
{% codeblock lang:sh %}
docker run --name somename -d -p 27017:27017 mongo:tag
{% endcodeblock %} 
  
## 客户端
  
对比了几个MongoDB的GUI客户端，发现一个比较好用的客户端[Robomongo][robomongo]，而且是跨平台的，安装完成后点击添加连接，输入ip和端口号就可以连接到你的MongoDB服务器了。  
  
{% img /images/post/2015-2/robomongo.png %}  
  
PS: 因为我是OS系统，用boot2docker来启动docker的，所以我的ip不是`localhost`，而是`192.168.59.103`。  
  
## sql查询

MongoDB是用表达式语言进行数据库操作的，这里[有一篇blog][mongodb_operation]介绍了MongoDB的一些简单操作，并有SQL语句与之对应，下面简单介绍几个命令。  
  
#### 关系型数据库 vs NoSQL
  
在介绍命令之前，需要先理解与关系型数据库两者概念上的区别。  
  
* 表：table vs collection
* 行：view/row(s) vs json document
* 索引：index vs index
  
#### 简单命令
  
{% codeblock lang:sh %}
# 创建一个聚集集合（table）
db.createCollection(“collName”, {size: 20, capped: 5, max: 100});

# 查询集合所有记录
db.userInfo.find();
相当于: select * from userInfo;

# 查询age = 22的记录
db.userInfo.find({"age": 22});
相当于: select * from userInfo where age = 22;

# 查询name中包含 mongo的数据
db.userInfo.find({name: /mongo/});
相当于: select * from userInfo where name like ‘%mongo%’;

# 查询name = zhangsan, age = 22的数据
db.userInfo.find({name: 'zhangsan', age: 22});
相当于: select * from userInfo where name = ‘zhangsan’ and age = ‘22’;

# 更新记录
db.users.update({age: 25}, {$set: {name: 'changeName'}}, false, true);
相当于: update users set name = ‘changeName’ where age = 25;

# 删除
db.users.remove({age: 132});
相当于: delete from users where age = 132;
{% endcodeblock %} 
  
## Java示例
  
使用Java来操作MongoDB也比较简单，首先要下载Java驱动，在Maven库上可以查询到。  
  
{% codeblock lang:sh %}
org.mongodb:mongo-java-driver:3.0.0-beta2
{% endcodeblock %} 
  
驱动最新的版本是3.0，语法上跟2.x有一些差别，具体示例可以参考[MongoDB官网的Java示例][mongodb_java]。  
  
#### 简单示例
  
{% codeblock MyMongoDB.java lang:java %}
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MyMongoDb {

    public static void main(String[] args) {
        MongoClient mongo = new MongoClient("192.168.59.103", 27017);

        // 根据名称查找数据库
        MongoDatabase mydb = mongo.getDatabase("mydb");
        // 根据名称查找集合
        MongoCollection<Document> collection = mydb.getCollection("collectName");

        // 查询该集合的所有记录
        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            System.out.println(document.toString());
        }

        // 插入一条记录
        Document t = new Document("name", "time")
                .append("age", 100)
                .append("sex", true)
                .append("address", new BasicDBObject("name", "china").append("province", "Sichuan"));
        collection.insertOne(t);

        // 更新一条记录
        collection.updateOne(new Document("name", "time"), new Document("$set", new Document("age", 101)));
    }
{% endcodeblock %} 


## 数据库设计原则

  
[mongodb]: http://www.mongodb.org/
[nosql]: http://en.wikipedia.org/wiki/NoSQL
[docker_mongodb]: https://registry.hub.docker.com/_/mongo/
[robomongo]: http://robomongo.org/
[mongodb_operation]: http://www.cnblogs.com/hoojo/archive/2011/06/01/2066426.html
[mongodb_java]: http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-3.0-java-driver/