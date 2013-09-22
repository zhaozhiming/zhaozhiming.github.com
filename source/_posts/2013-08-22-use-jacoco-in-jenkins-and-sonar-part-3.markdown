---
layout: post
title: "在jenkins和sonar中集成jacoco(三)--使用jacoco收集集成测试的覆盖率"
date: 2012-12-01 14:45
comments: true
categories: code
tags: [jacoco, jenkins, sonar]
---
  
我们系统使用weblogic做服务器，集成测试框架使用的是junit+selenium。  
  
首先，要把jacoco的jacocoagent.jar包放到部署应用的服务器上，接着在系统服务的JAVA_OPTIONS 后面添加如下参数：  

<!--more-->  
{% codeblock lang:sh %}
-javaagent:[yourpath/]jacocoagent.jar=includes=com.xxx.xxx.*,output=tcpserver,address=xxx.xxx.xxx.xxx,port=xxxxx
{% endcodeblock %}  
  
参数解释：  
  
1. 前面的yourpath是放jacocoagent.jar文件的目录路径；
2. includes是指要收集哪些类（注意不要关写包名，最后要写.\*)，不写的话默认是\*，会收集应用服务上所有的类，包括服务器和其他中间件的类，一般要过滤；
3. output有4个值，分别是file,tcpserver,tcpclient,mbean，默认是file。使用file的方式只有在停掉应用服务的时候才能产生覆盖率文件，而使用tcpserver的方式可以在不停止应用服务的情况下下载覆盖率文件，后面会介绍如何使用dump方法来得到覆盖率文件。
4. address是ip， port是端口，这是使用tcpserver方式需要的2个参数，也是后面dump方法要用到的。（这里的address我只能使用服务器的ip，如果使用其他ip，服务启动时会报错。）  
  
更多参数可以参考[java agent][url1]。  
[url1]: http://www.eclemma.org/jacoco/trunk/doc/agent.html
  
配置完应用服务的JAVA_OPTIONS之后，启动服务器，然后可以开始跑你的集成测试，跑完之后，实际上jacocoagent已经将覆盖率数据记录下来了，我们可以使用下面的ant任务来dump出覆盖率文件：  

{% codeblock build.xml lang:xml %}
　 <target name="downloadUatCoverageData">
        <jacoco:dump address="xxx.xxx.xxx.xxx" port="xxxx" reset="true" destfile="${basedir}/uat.exec" append="false"/>
    </target>
{% endcodeblock %}  
  
这里的address和port是刚才在JAVA_OPTIONS里面写的address和port的值，destfile是指生成的覆盖率文件路径。  

**注意，这里虽然得到了集成测试的覆盖率文件，但是需要应用服务器上的类文件才能产出相应的覆盖率报告，如果类文件是其他JVM编译的，产出的报告覆盖率是0%。**  
  
有2种方法可以得到覆盖率文件所需的class文件：  
  
1. 将应用服务部署的包（ear或war或jar）包下载下来之后解压，即可得到对应的class文件；  
2. 在前面做单元测试之后，可以将class文件打成一个zip包，然后上传到服务器，最后在需要的时候去服务器上取。  
  
得到集成测试的覆盖率文件之后，结合之前取到的单元测试覆盖率文件，我们可以将2个文件合并，得到综合的覆盖率文件，命令如下：  

{% codeblock build.xml lang:xml %}
　<target name="mergeCoverage">
        <jacoco:merge destfile="merged.exec">
            <fileset dir="${basedir}" includes="*.exec"/>
        </jacoco:merge>
   </target>
{% endcodeblock %}  

