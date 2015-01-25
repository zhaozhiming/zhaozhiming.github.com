---
layout: post
title: "Apache Thrift的简单代码示例"
date: 2015-01-25 19:07
description: Apache Thrift的简单代码示例
keywords: thrift
comments: true
categories: code
tags: thrift
---

{% img /images/post/2015-1/thrift.jpeg %}  
  
[Thrift][thrift]是Fackbook推出的一个跨语言通讯框架，相比常用的以json为载体的rest http方式来说，性能上更加优越。另外Thrift集成了RPC（Remote Procedure Call Protocol）的实现，比同类型的产品[Protobuf][protobuf]功能要更全面。  
  
<!--more-->  
  
## 安装和镜像源
  
* 以Ubuntu14.04为例，首先安装Thrift所需要的软件。  
  
{% codeblock lang:sh %}
sudo apt-get install libboost-dev libboost-test-dev libboost-program-options-dev libboost-system-dev libboost-filesystem-dev libevent-dev automake libtool flex bison pkg-config g++ libssl-dev
{% endcodeblock %} 
  
* 接着安装对应语言的编译器，以Java为例，需要安装JDK和Ant，顺便把Maven也装上。  
  
{% codeblock lang:sh %}
sudo apt-get install openjdk-7-jdk ant maven
{% endcodeblock %} 
  
* 下载Thrift的包，最新的版本是0.9.2，但不知道什么原因，0.9.2版本安装会有问题，退一个版本就OK了，我们安装0.9.1版本。  
  
{% codeblock lang:sh %}
wget http://apache.fayea.com/thrift/0.9.1/thrift-0.9.1.tar.gz
tar -zxvf thrift-0.9.1.tar.gz 
cd thrift-0.9.1
sudo ./configure --without-tests
sudo make
sudo make install
{% endcodeblock %} 
  
* 最后跑一下thrift的命令测试一下是否安装成功
  
{% codeblock lang:sh %}
$ thrift --version
Thrift version 0.9.1
{% endcodeblock %} 
  
* 如果嫌安装比较麻烦，也可以使用下面Vagrant或者Docker的Thrift镜像。  
  * [docker镜像][docker-source]
  * [vagrant镜像][vagrant-source]

## 接口定义
  
安装完成Thrift之后，我们来编写一个thrift文件，定义服务端的接口。  
  
{% codeblock hello.thrift lang:java %}
 namespace java service.demo 
 service Hello{ 
  string helloString(1:string para) 
  i32 helloInt(1:i32 para) 
  bool helloBoolean(1:bool para) 
  void helloVoid() 
  string helloNull() 
 }
{% endcodeblock %} 
    
可以看到我们定义了5个不同的接口，接着使用Thrift对文件进行编译，产生对应语言的程序文件，下面以Java为例。  
  
{% codeblock lang:sh %}
thrift —gen java hello.thrift
{% endcodeblock %} 
  
命令执行完成后，会生成一个Hello.java的文件。  
  
## 接口实现

前面只是定义了接口的签名，现在我们来对接口进行实现，实现类需要实现Hello.Iface，代码如下:  
  
{% codeblock HelloServiceImpl lang:java %}
package service.demo;

import org.apache.thrift.TException;

public class HelloServiceImpl implements Hello.Iface {
    @Override
    public boolean helloBoolean(boolean para) throws TException {
        System.out.printf("hello true/false");
        return para;
    }

    @Override
    public int helloInt(int para) throws TException {
        System.out.println("hello times: " + para);
        return para;
    }

    @Override
    public String helloNull() throws TException {
        System.out.println("hello null");
        return null;
    }

    @Override
    public String helloString(String para) throws TException {
        System.out.println("hello " + para);
        return para;
    }

    @Override
    public void helloVoid() throws TException {
        System.out.println("Hello World");
    }
}
{% endcodeblock %} 
  
## 服务端代码的实现

接着我们来编写我们的服务器代码:  
  
{% codeblock HelloServiceImpl lang:java %}
package service.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import service.demo.Hello;
import service.demo.HelloServiceImpl;

public class HelloServiceServer {
    public static void main(String[] args) {
        try {
            // 设置服务端口为 9527 
            TServerSocket serverTransport = new TServerSocket(9527);
            // 关联处理器与 Hello 服务的实现
            TProcessor processor = new Hello.Processor(new HelloServiceImpl());
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Start server on port 9527...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
{% endcodeblock %} 
  
## 客户端代码的实现

最后来完成我们的客户端代码，客户端我们也是用Java来实现。  
  
{% codeblock HelloServiceImpl lang:java %}
package service.client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import service.demo.Hello;

public class HelloServiceClient {
    public static void main(String[] args) {
        try {
            // 设置调用的服务地址为本地，端口为 9527
            TTransport transport = new TSocket("localhost", 9527);
            transport.open();
            // 设置传输协议为 TBinaryProtocol
            TProtocol protocol = new TBinaryProtocol(transport);
            Hello.Client client = new Hello.Client(protocol);
            // 调用服务的 helloVoid 方法
            client.helloVoid();

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
{% endcodeblock %} 
  
代码完成后，可以先运行HelloServiceServer的main方式，然后再运行HelloServiceClient的main方法，可以在HelloServiceServer的输出结果中看到客户端打印的`Hello World`字样。  
  
## 部署
参考了一些资料后，决定使用Jar包来启动thrift服务，并使用[Supervisor][supervisor]来进行进程的监控和自动重启，下面是supervisor的配置文件，里面配置了我们的thrift程序。  
  
{% codeblock /etc/supervisord.conf lang:sh %}
[program:thrift]
command=/usr/bin/java -jar /home/vagrant/thrift-hello/demo.jar
stderr_logfile=/home/vagrant/logs/err.log
stdout_logfile=/home/vagrant/logs/out.log
autostart=true

[unix_http_server]
file = /tmp/supervisor.sock
chmod = 0777
chown= nobody:nogroup

[inet_http_server]
port = 127.0.0.1:9001

[supervisord]
logfile = /tmp/supervisord.log
logfile_maxbytes = 50MB
logfile_backups=10
loglevel = info
pidfile = /tmp/supervisord.pid
nodaemon = false

[supervisorctl]
serverurl = unix:///tmp/supervisor.sock

[rpcinterface:supervisor]
supervisor.rpcinterface_factory = supervisor.rpcinterface:make_main_rpcinterface
{% endcodeblock %}  
  
参考资料: http://stackoverflow.com/questions/17639442/deploy-and-serve-a-thrift-server    
  


[thrift]: https://thrift.apache.org/ 
[protobuf]: https://code.google.com/p/protobuf/
[docker-source]: https://registry.hub.docker.com/u/evarga/thrift/
[vagrant-source]: https://github.com/apache/thrift/blob/master/contrib/Vagrantfile
[supervisor]: http://supervisord.org/