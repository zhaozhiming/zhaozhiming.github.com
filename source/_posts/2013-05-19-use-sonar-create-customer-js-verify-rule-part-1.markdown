---
layout: post
title: "利用Sonar定制自定义JS扫描规则（一）——sonar环境搭建"
date: 2013-05-19 11:20
comments: true
categories: code
tags: [js, sonar]
---
  
接触sonar已经有段时间了，最早是在一个项目组里面听到这个框架，后来在公司持续集成活动的推动下，也逐渐学习了sonar的具体功能。sonar集成了findbugs，pmd，checkstyle各种静态代码检查工具的功能，并且可以将各种扫描结果组合成一张张漂亮的报告，功能很是强大。下面介绍一下如何使用sonar来自定义javascrpit的扫描规则。  
  
<!--more-->
虽然使用过很多次sonar，但都是在别人搭好的环境上使用的，自己还没有真正搭建过，这里首先介绍一下sonar环境的搭建。

### sonar环境搭建  
  
首先到sonar官网上下载其最新版本，我下的是3.5.1。下下来是一个zip包，直接将其解压到你想要安装的目录就好了。  

#### 使用H2内存数据库  
  
sonar服务的启动是要有数据库来支持的，sonar本身自带了H2内存数据库，可以直接使用。在解压目录下的conf目录下，有一个sonar.properties的配置文件是来保存sonar的配置，如果你使用的是H2数据库的话，将配置文件中的下面部分代码注释去掉即可。  
  
{% codeblock sonar.properties lang:properties %}
#sonar.jdbc.url:                            jdbc:h2:tcp://localhost:9092/sonar
#sonar.jdbc.driverClassName:                org.h2.Driver
{% endcodeblock %}  
  
在解压目录下的extras\database\mysql目录，有2个mysql的sql文件，其中1个是创建sonar数据库和用户的脚本，可以参照这个脚本来创建H2的sonar相关数据库和用户。  
  
#### 使用外部数据库（以mysql为例）  
  
sonar官网强烈建议，如果你的产品只是学习或者练习用的demo，可以使用内存数据库，否则请使用外部数据库。sonar支持大部分主流的数据库，比如mysql，oracle，postgresql，Microsoft SQLServer。  
  
我们以mysql为例，首先安装mysql（安装mysql的过程就不再详细描述，网上有很多），这里要注意将数据库的字符集设置为UTF-8。安装完成之后，同样修改conf目录下的sonar.properties文件，将mysql部分的配置项打开。然后在mysql中将上面提到的那个创建sonar数据库和用户脚本施行一遍。  
  
{% codeblock sonar.properties lang:properties %}
#----- MySQL 5.x
# Comment the embedded database and uncomment the following line to use MySQL
#sonar.jdbc.url:                            jdbc:mysql://localhost:3306/sonar?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true
{% endcodeblock %}  
  
#### 启动服务  

数据库配置完成之后，就可以来启动web服务了，去到解压目录下的bin目录，找到自己相关系统的目录，以64位windows系统为例，目录为：bin\windows-x86-64，运行该目录下的Startsonar.bat文件，如果启动没有报错的话，就可以在浏览器中输入：`http://localhost:9000`，查看sonar的主界面了。  
  
### 安装Sonar Runner  

上面搭建的只是sonar的服务平台，如果想用sonar来分析自己项目代码的话，可以有多种方式，比如ant、maven等，而sonar官网上推荐的是使用Sonar Runner来执行分析项目的操作。  
  
首先去到sonar官网下载Sonar Runner的压缩包，和sonar的压缩包一样，先解压到你要安装的目录。然后增加SONAR_RUNNER_HOME这个环境变量，变量值为你runner的解压目录，然后将$SONAR_RUNNER_HOME$/bin的加到PATH变量后面（配置过java环境变量的同学都懂的）。这样就安装完成了，打开cmd验证一下，打出sonar-runner -h，如果出现下面的提示信息，即表示你的runner安装成功了。  
  
{% codeblock lang:sh %}
usage: sonar-runner [options]
 
Options:
 -h,--help             Display help information
 -X,--debug            Produce execution debug output
 -D,--define <arg>     Define property
{% endcodeblock %}  
  
安装完runner以后，在你的项目根目录下，增加sonar-project.properties文件，内容如下：  
  
{% codeblock sonar.properties lang:properties %}
# required metadata
sonar.projectKey=my:project
sonar.projectName=My project
sonar.projectVersion=1.0
 
# optional description
sonar.projectDescription=Fake description
 
# path to source directories (required)
sonar.sources=srcDir1,srcDir2
 
# path to test source directories (optional)
sonar.tests=testDir1,testDir2
 
# path to project binaries (optional), for example directory of Java bytecode
sonar.binaries=binDir
 
# optional comma-separated list of paths to libraries. Only path to JAR file is supported.
sonar.libraries=path/to/library/*.jar,path/to/specific/library/myLibrary.jar,parent/*/*.jar
 
# The value of the property must be the key of the language.
sonar.language=java
 
# Additional parameters
sonar.my.property=value
{% endcodeblock %}  
  
上面的配置项根据名字和说明大概知道是做什么用的了，填上自己项目中对应的值即可。然后打开cmd窗口，去到你的项目根目录下，执行sonar-runner命令，这样sonar就开始分析你的项目代码了，分析完成之后，在浏览器中刚才那个9000端口地址的网页中就可以看到你的项目了。  
  
### 安装Javascript插件  
  
使用管理员的角色登录sonar，根据这个路径进入更新中心：Settings > System > Update Center。在Available Plugins窗口找到javascript插件，点击其中的install按钮进行安装（如下图所示）。安装完后重启sonar服务器即可。  
  
{% img /images/post/01202231-c4477ac31ac3411686f1a4b98ae3ece1.png %}  
  

