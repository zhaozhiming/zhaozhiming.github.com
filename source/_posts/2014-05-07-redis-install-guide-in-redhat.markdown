---
layout: post
title: "Red Hat系统安装Redis"
date: 2014-05-07 19:53
description: Red Hat系统安装Redis
keywords: redis
comments: true
categories: code
tags: redis
---

{% img /images/post/2014-5/redis.jpg %}    
<!--more-->  

## 环境
RHLinux-6.4-64-EN, 红帽6.4 64位，英文正式发布版  
  
## 安装
安装很简单，先下载redis的压缩包，下载地址见[这里][url3]，然后拷贝到你的linux机器，接着执行下面的命令。

{% codeblock lang:bash %}
$ tar xzf redis-2.6.14.tar.gz
$ cd redis-2.6.14
$ make
{% endcodeblock %}  
  
## 启动
编译完后增加了src目录，执行src下面的redis-server脚本即可启动redis服务。

{% codeblock lang:bash %}
$ src/redis-server
{% endcodeblock %}  

## 调试
执行src目录下的redis-cli脚本，这个是redis的客户端。

{% codeblock lang:bash %}
$ src/redis-cli
redis> set foo bar
OK
redis> get foo
"bar"
{% endcodeblock %}  

## java调用
1. Redis比较出名的java客户端是jedis，先下载jedis的jar包，可以去maven库搜下jedis就可以下载到，源码是放在github上：[https://github.com/xetorthio/jedis][url1]。
2. 简单写个main方法就可以调用。
{% codeblock lang:java %}
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.20.8.39"); //redis服务器的ip，端口默认6379
        jedis.set("foo", "bar");
        String value = jedis.get("foo");
        System.out.println(value);
    }
{% endcodeblock %}  
  
## 后台进程
之前的启动方式不是后台进程方式的，终端关了服务也就停了，可以使用下面的命令将Redis作为后台进程启动，并添加到系统启动命名中。
  
{% codeblock lang:bash %}
$ cd redis-2.6.14/utils
$./install_server
{% endcodeblock %}  

执行命令后，会提示你回答几个问题，可以一路回车过去，选择默认设置。  
  
{% codeblock lang:bash %}
Please select the redis port for this instance: [6379]
Selecting default: 6379
Please select the redis config file name [/etc/redis/6379.conf]
Selected default - /etc/redis/6379.conf
Please select the redis log file name [/var/log/redis_6379.log]
Selected default - /var/log/redis_6379.log
Please select the data directory for this instance [/var/lib/redis/6379]
Selected default - /var/lib/redis/6379
Please select the redis executable path [/usr/local/bin/redis-server]
...
{% endcodeblock %}  
  
后续可以通过下面的方式启停服务。  
1. /etc/init.d/redis_6379 start --启动  
2. /etc/init.d/redis_6379 stop --服务  
当然也可在/usr/local/bin目录下使用redis-server来启动。  
  
**PS: 我在执行install_server脚本后，发现服务启动不起来，查看/etc/init.d/redis_6379这个文件发现里面的换行符号被替换成了`/n`符号，手动将这些符号替换成换行就可以了。**
  
## 更多资料
更多资料可以看这里：[http://redis.io/][url2]

[url1]: https://github.com/xetorthio/jedis
[url2]: http://redis.io/
[url3]: http://redis.io/download

