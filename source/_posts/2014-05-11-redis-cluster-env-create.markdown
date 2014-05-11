---
layout: post
title: "Redis集群环境安装指南"
date: 2014-05-11 20:50
description: Redis集群环境安装指南
keywords: redis
comments: true
categories: code
tags: redis
---
  
{% img /images/post/2014-5/redis-cluster.jpg %}  
  
### 环境
RHLinux-6.4-64-EN, 红帽6.4 64位，英文正式发布版。  
<!--more-->  
  
### Redis3.0.0
  
* redis2.x版本还不支持集群，3.0版本将会支持，现在3.0版本还在开发中，现在是beta-3版本(截止2014-5-8)，但功能是可用的。
* 下载Redis3.0.0 beta-3版本，点击[这里][url1]下载。
* Redis3的安装可以参照之前的[单机安装指南][url2]操作。 
  
### 创建Redis集群实例
* 创建集群节点的文件夹，先创建cluster-test文件夹，再以端口为名称创建子文件夹。这里我们要创建6个Redis实例，3个作为master，3个作为slave。
   
{% codeblock lang:bash %}
mkdir cluster-test
cd cluster-test
mkdir 7000 7001 7002 7003 7004 7005
{% endcodeblock %}  
  
* 在每个文件夹下面创建创建Redis配置文件，注意根据不同实例的端口号修改下面的配置文件，nodes.conf文件为Redis实例启动时自动生成。
  
{% codeblock lang:bash %}
port 7000
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
{% endcodeblock %}  
  
* 开6个terminal窗口，分别启动这6个Redis实例。
  
{% codeblock lang:bash %}
cd 7000
../redis-server ./redis.conf
{% endcodeblock %}  
  
* 当成功启动后，能看到每个terminal出现下面的字样，是因为node.conf文件不存在，所以给每个实例分配了一个新的ID。
  
{% codeblock lang:bash %}
[82462] 26 Nov 11:56:55.329 * No cluster configuration found, I'm 97a3a64667477371c4479320d683e4c8db5858b1
{% endcodeblock %}  
  
### 创建集群
* 现在Redis的6个实例都已经启动了，现在来开始创建集群。创建集群用到了一个ruby文件，放在redis3目录的src子目录下，找到后执行以下命令。这里的`-replicas 1`表示每个master配备1个slave，后面的参数就是6个实例的ip加端口，以空格隔开。  
  
{% codeblock lang:bash %}
cd redis-3.0.0-beta3/src
./redis-trib.rb create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 \
127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
{% endcodeblock %}  
  
* 执行命令后会提示你是否接受提示的配置信息，默认的是前3台作为master机器，后3台作为slave机器，输入`yes`，出现最后的信息表示集群已经创建好了。
  
{% codeblock lang:bash %}
Creating cluster
Connecting to node 127.0.0.1:7000: OK
Connecting to node 127.0.0.1:7001: OK
Connecting to node 127.0.0.1:7002: OK
Connecting to node 127.0.0.1:7003: OK
Connecting to node 127.0.0.1:7004: OK
Connecting to node 127.0.0.1:7005: OK >>>
Performing hash slots allocation on 6 nodes...
Using 3 masters: 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002
127.0.0.1:7000 replica #1 is 127.0.0.1:7003
127.0.0.1:7001 replica #1 is 127.0.0.1:7004
127.0.0.1:7002 replica #1 is 127.0.0.1:7005
M: 9991306f0e50640a5684f1958fd754b38fa034c9 127.0.0.1:7000 slots:0-5460 (5461 slots) master
M: e68e52cee0550f558b03b342f2f0354d2b8a083b 127.0.0.1:7001 slots:5461-10921 (5461 slots) master
M: 393c6df5eb4b4cec323f0e4ca961c8b256e3460a 127.0.0.1:7002 slots:10922-16383 (5462 slots) master
S: 48b728dbcedff6bf056231eb44990b7d1c35c3e0 127.0.0.1:7003
S: 345ede084ac784a5c030a0387f8aaa9edfc59af3 127.0.0.1:7004
S: 3375be2ccc321932e8853234ffa87ee9fde973ff 127.0.0.1:7005
Can I set the above configuration? (type 'yes' to accept): yes

Nodes configuration updated >>>
Sending CLUSTER MEET messages to join the cluster Waiting for the cluster to join... >>>
Performing Cluster Check (using node 127.0.0.1:7000)
M: ebbb890e899244e4a2a6303826a29543ebf9885d 127.0.0.1:7000
   slots:0-5460 (5461 slots) master
M: ebbb890e899244e4a2a6303826a29543ebf9885d 127.0.0.1:7001
   slots:5461-10922 (5462 slots) master
M: 73c22198cd1d0782ec24e3c9b03378030891c9a3 127.0.0.1:7002
   slots:10923-16383 (5461 slots) master
M: c785d85b95d7bdc28ec90384ab8a5885b289542c 127.0.0.1:7003
   slots: (0 slots) master
   replicates b949ea2d9c822ff069521a354b4ed48855ac6203
M: 570c472fe553ba3c9d0fb3ba16fcdb8579b4fc86 127.0.0.1:7004
   slots: (0 slots) master
   replicates ebbb890e899244e4a2a6303826a29543ebf9885d
M: f86667ec42cff41a5666162a912713173f5306d7 127.0.0.1:7005
   slots: (0 slots) master
   replicates 73c22198cd1d0782ec24e3c9b03378030891c9a3
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
{% endcodeblock %}  
  
PS: 安装过程还发现RedHat系统上没有安装ruby，需要先安装ruby，而且公司机器还不能上网下载安装，只能通过离线的方式安装ruby，这里有离线安装的文档可以参考一下:[https://rvm.io/rvm/offline][url3]。  
  
### 验证
可以使用Redis3.0的redis-cli客户端进行验证，需要加上`-c`参数，表示集群的意思。  
  
{% codeblock lang:bash %}
$ redis-cli -c -p 7000
redis 127.0.0.1:7000> set foo bar
-> Redirected to slot [12182] located at 127.0.0.1:7002
OK
redis 127.0.0.1:7002> set hello world
-> Redirected to slot [866] located at 127.0.0.1:7000
OK
redis 127.0.0.1:7000> get foo
-> Redirected to slot [12182] located at 127.0.0.1:7002
"bar"
redis 127.0.0.1:7000> get hello
-> Redirected to slot [866] located at 127.0.0.1:7000
"world"
{% endcodeblock %}  

更多的资料可以参考这里：[http://redis.io/topics/cluster-tutorial][url4]。
  
[url1]: https://github.com/antirez/redis/archive/3.0.0-beta3.tar.gz
[url2]: http://10.42.173.13/zhaozhiming003/redis-poc/blob/master/doc/redis_install.md
[url3]: https://rvm.io/rvm/offline
[url4]: http://redis.io/topics/cluster-tutorial

 

 
