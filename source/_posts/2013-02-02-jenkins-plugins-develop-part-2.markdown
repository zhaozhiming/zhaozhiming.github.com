---
layout: post
title: "jenkins插件开发（二）-- HelloWorld"
date: 2013-02-02 18:35
description: "jenkins插件开发（二）-- HelloWorld"
keywords: jenkins插件,开发
comments: true
categories: code
tags: [jenkins plugins]
---
  
在上一篇blog中我们讲了如何搭建jenkins插件的开发环境，接下来介绍如何开发我们的插件。  

<!--more-->
###创建HelloWorld插件  

学习每门新语言的时候，我们都会写一个HelloWorld程序，这里介绍的是如何创建一个Jenkins的HelloWorld插件。  
  
#####1、首先修改Maven的配置，将Maven的连接库修改为jenkins plugins的资源库，方便相关pom组件从jenkins上下载，修改的是 %USERPROFILE%/.m2/settings.xml文件。  
  
{% codeblock settings.xml lang:xml %}
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>

  <profiles>
    <!-- Give access to Jenkins plugins -->
    <profile>
      <id>jenkins</id>
      <activation>
        <activeByDefault>true</activeByDefault> <!-- change this to false, if you don't like to have it on per default -->
      </activation>
      <repositories>
        <repository>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <mirrors>
    <mirror>
      <id>repo.jenkins-ci.org</id>
      <url>http://repo.jenkins-ci.org/public/</url>
      <mirrorOf>m.g.o-public</mirrorOf>
    </mirror>
  </mirrors>
</settings>
{% endcodeblock %}   
  
#####2、打开CMD，进入一个想要放插件工程的目录，执行以下命令。

{% codeblock lang:sh %}
mvn -U org.jenkins-ci.tools:maven-hpi-plugin:create -DgroupId={your.gound.id} -DartifactId={your.plugin.id}
{% endcodeblock %}   
  
命令中的 your.groud.id 和 your.plugin.id 填你插件的具体对应的值。  
  
your.group.id 会形成你的包结构，比如 your.group.id=abc.def.ghi，那工程下的src/main/java 目录下会产生abc.def.ghi 这样的目录。  
  
执行完命令后，该目录下会产生一个名称是{your.plugin.id}的目录，这个目录工程就是我们的HelloWorld插件工程。  
  
#####3、插件打包  
  
创建好工程之后，执行下面的命令可以在 target 子目录下产生一个 hpi 文件，该文件就是我们的插件文件，可以直接上传到 jenkins 的服务器。  
  
{% codeblock lang:sh %}
mvn package
{% endcodeblock %}  
  
#####4、启动本地服务  
  
在第三步产生的 hpi 文件要上传到一个部署好的jenkins服务器才可以看到效果，我们也可以通过以下命令启动一个包含插件的本地jenkins服务。  
  
{% codeblock lang:sh %}
mvn hpi:run
{% endcodeblock %}  
  
执行完命令之后，在浏览器打开localhost:8080，就可以访问本地的Jenkins服务了，这里默认用的是 jetty6的 web 容器。  
  
#####5、运行HelloWorld插件  
  
在Job的配置页面，其中的构建子项中Add Build Step按钮，点击后下拉框中会有一个Say Hello World的选项，这个就是我们的HelloWorld 插件，选中后保存配置，进行Job构建，在构建日志中就可以看到插件的打印日志。  
  
这里介绍的是新建一个插件，对已有插件进行扩展不在本次讨论范围内，更多信息可以参考[这里][url1]。    


---
  
* [jenkins插件开发（一）-- 环境搭建](http://zhaozhiming.github.io/blog/2013/01/31/jenkins-plugins-develop-part-1/)
* [jenkins插件开发（三）-- 插件编写](http://zhaozhiming.github.io/blog/2013/02/03/jenkins-plugins-develop-part-3/)
* [jenkins插件开发（四）-- 插件发布](http://zhaozhiming.github.io/blog/2013/02/04/jenkins-plugins-develop-part-4/)


[url1]: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial
  
