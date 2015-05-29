---
layout: post
title: "jenkins插件开发（一）--环境搭建"
date: 2013-01-31 17:42
description: "jenkins插件开发（一）--环境搭建"
keywords: jenkins插件,开发
comments: true
categories: code
tags: jenkins plugins
---
  
最近写了一个jenkins插件，功能比较简单，时间主要是花在对jenkins插件框架和Maven的熟悉上。jenkins插件虽然以前也接触过一点，不过现在都忘得差不多了，这个笔记权当知识点记录，顺带介绍介绍经验。  
  
<!--more-->  
首先是环境搭建，这里列一下jenkins插件开发所需工具：  
  
* JDK6.0或更高
* 构建工具--Maven
* IDE--IDEA或eclipse
* web服务器：jetty或tomcat（可选）
  
###Maven  
  
jenkins插件需要用到Maven这个构建工具，大家可以去Maven的官网下载：[这里][url1]  
[url1]: http://maven.apache.org/download.cgi  
  
如何安装Maven？以windows环境为例：  
  
1. 解压下载的zip包（比如：apache-maven-3.0.4-bin.zip）到一个指定的目录（比如：D:\apache-maven-3.0.4）；
2. 添加M2_HOME环境变量，指就是我们刚刚解压的目录（如何设置环境变量JAVA开发应该都懂得）；
3. 添加M2这个环境变量，值是%M2_HOME%/bin，并在PATH这个环境变量的值后面追加 %M2%；
4. 环境变量中确保有JAVA_HOME这个环境变量，有的话在cmd窗口可以直接用java -version打印JDK版本信息；
5. 配置完所有环境变量后，在cmd窗口打印命名mvn -v，会打印出Maven和JDK的信息，这样就表示安装Maven成功了；
  
更详细的安装说明请看[这里][url2]。  
[url2]: http://maven.apache.org/download.cgi  
  
###IDE  

我以前的blog还介绍过IDEA，相对于eclipse我还是比较喜欢用IDEA来开发。因为IDEA已经集成了Maven的插件，所以可以直接通过项目中的pom文件打开整个项目。在IDEA主界面右边有个maven projects的区域，打开可以看到工程的各个maven命令（图1）。  
  
使用IDEA还需要添加一个针对jelly页面的插件，这个插件可以对jelly文件进行语法提示和高亮显示，插件地址点击[这里][url3]，效果见图2。  
[url3]: http://plugins.intellij.net/plugin/?id=1885  
  
图1:  
{% img /images/post/29152926-97e25c38b1754deca8843030a113cb3e.png 图1 %}
  
图2:  
{% img /images/post/29152950-1f91f25eac354a75a44e34287f75cbba.png 图2 %}
   
###Jetty(可选)  
  
其实运行插件工程的hpi:run就可以通过maven启动一个自带的Jetty6服务器，不需要额外再安装jetty服务器。但为了让我们的插件测试更接近真实环境，我们可以将做好的插件放到另外一个的Jetty服务器中，来测试插件的运行效果。  
  
1. 去Jetty官网下载最新的Jetty包；
2. 将包解压到本地目录；
3. 将jenkins的war包放到解压目录中的webapps子文件夹中；
4. 在解压目录的contexts子目录中增加一个xml文件，文件名是jenkins.xml（内容如下）;
5. 打开CMD进入解压目录，执行java -jar start.jar命令，即可启动Jetty服务器；
6. 打开浏览器，进入localhost:8080/jenkins进入jenkins主页；
  
{% codeblock jenkins.xml lang:xml %}
<Configure class="org.eclipse.jetty.webapp.WebAppContext">
  <Set name="contextPath">/jenkins</Set>
  <Set name="war"><SystemProperty name="jetty.home" default="."/>/webapps/jenkins.war</Set>
  <Get name="securityHandler">
    <Set name="loginService">
      <New class="org.eclipse.jetty.security.HashLoginService">
        <Set name="name">Jenkins Realm</Set>
        <Set name="config"><SystemProperty name="jetty.home" default="."/>/etc/realm.properties</Set>
      </New>
    </Set>
  </Get>
</Configure>
{% endcodeblock %}   
  
注意：如果用hpi:run启动的服务，jenkins地址是：localhost:8080，而使用真实Jetty启动的服务，地址是：localhost:8080/jenkins。  
到这里你的jenkins插件开发环境已经搭建好了，我们在下一篇blog再继续介绍具体的插件开发，谢谢！  


---
  
* [jenkins插件开发（二）-- HelloWorld](http://zhaozhiming.github.io/blog/2013/02/02/jenkins-plugins-develop-part-2/)
* [jenkins插件开发（三）-- 插件编写](http://zhaozhiming.github.io/blog/2013/02/03/jenkins-plugins-develop-part-3/)
* [jenkins插件开发（四）-- 插件发布](http://zhaozhiming.github.io/blog/2013/02/04/jenkins-plugins-develop-part-4/)
  

