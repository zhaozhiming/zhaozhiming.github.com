---
layout: post
title: "jenkins插件开发（四）-- 插件发布"
date: 2013-02-04 20:47
description: "jenkins插件开发（四）-- 插件发布"
keywords: jenkins插件,开发
comments: true
categories: code
tags: jenkins plugins
---
  
上一篇blog介绍了插件开发中要注意的一些问题， 我们再来介绍插件开发完成后，如何上传到jenkins的插件中心（这里假设你的代码是放在github上的，使用svn或其他版本管理工具的请参考其他文章）。  
<!--more-->
###组织授权  

首先去到google group（被伟大的GFW和谐了，只能翻墙访问，如何翻墙不在这里讨论）的jenkins开发列表（jenkinsci-dev@googlegroups.com）里发帖告诉组织者，你开发的插件id是什么，插件功能是做什么的，还有你的github用户名是什么。组织者如果觉得没问题，就会在jenkins 的github上给你创建一个以你插件名称命名的代码库，然后在github上将你拉入jenkins的组织，这样就可以在这个代码库上添加你的插件代码了。如果你在github上已经有了自己插件的代码库，也可以将地址告诉组织者，这样他会fork你的代码库，如果是这样的话你需要在更新了你原仓库的代码后，再将jenkins仓库上的代码同步。  
  
###修改POM文件  
  
再来是修改项目的pom文件，需要增加一些信息，这样发布的时候才可以正确显示你的插件内容。首先是你的源码控制管理配置，增加配置如下：  
  
{% codeblock pom.xml lang:xml %}
  <scm>
    <connection>scm:git:ssh://github.com/jenkinsci/MYPLLUGINNAME.git</connection>
    <developerConnection>scm:git:ssh://git@github.com/jenkinsci/MYPLUGINNAME.git</developerConnection>
    <url>https://github.com/jenkinsci/MYPLUGINNAME</url>
  </scm>
{% endcodeblock %}   
  
插件的WIKI页面（后面会说明如何添加WIKI）：

{% codeblock pom.xml lang:xml %}
<project>
  ...
  <url>http://wiki.jenkins-ci.org/display/JENKINS/My+Plugin</url>
</project>
{% endcodeblock %}  
  
还有维护人员信息：  

{% codeblock pom.xml lang:xml %}
<project>
  ...
  <developers>
    <developer>
      <id>devguy</id>
      <name>Developer Guy</name>
      <email>devguy@developerguy.blah</email>
    </developer>
  </developers>
</project>
{% endcodeblock %}  

###发布插件  
  
最简单的方式就是执行以下命令：  
  
{% codeblock lang:sh %}
 mvn release:prepare release:perform
{% endcodeblock %}  
  
如果发布成功（窗口提示BUILD SUCCESS 字样）就表示你的插件已经发布到jenkins的更新中心了，不过一般要等个一到半天更新中心才会更新。  
  
一般第一次发布都会有一些问题，没有那么容易成功，这里再介绍一下发布过程中容易出现的问题。  
  
**插件版本没有以-SNAPSHOT 结尾。**可能有些同学会将-SNAPSHOT结尾的版本号给改成一个正式的版本号，比如1.0，如果是这样的话，发布的时候就会报错。因为jenkins 插件发布的过程中会将你的预设版本号（比如 1.0-SNAPSHOT）改成一个正式的版本号 1.0，然后提交到代码仓库，在检查插件版本号的时候，如果发现不是预设版本号（以-SNAPSHOT结尾）就会报错，所以大家不用担心自己的插件版本号命名问题，在插件发布的时候，会自动帮你修正为正式版本号的。  
  
**发布出错回滚。**如果在插件发布的过程中出错，重新再执行上面的命令是不行的，会报版本已存在的错误，需要先执行一下下面的命令来清除出错的发布信息。  
  
{% codeblock lang:sh %}
 mvn release:clean
{% endcodeblock %}  
  
**Github 无法push。**发布的过程中会使用git将你的代码push到github上，有时候会报权限不允许的问题（Permission denied），那可能是你的github配置有问题，可以参考[这里][url1]来设置你的SSH或者[这里][url2]看看是否其他问题。  
[url1]: https://help.github.com/articles/generating-ssh-keys  
[url2]: https://help.github.com/articles/error-permission-denied-publickey    

###WIKI页面  
  
发布好了你的插件之后呢，我们需要在jenkins的官网上添加关于你插件的WIKI，以便让使用你插件的用户知道插件的信息。  
  
首先要在jenkins官网上申请一个帐号，申请成功之后你就可以在插件主页上添加你的插件页面了。去到插件主页面（[https://wiki.jenkins-ci.org/display/JENKINS/Plugins][url3]），点击右上角的Add链接，选择page选项，就可以进入页面编写了。  
[url3]: https://wiki.jenkins-ci.org/display/JENKINS/Plugins  
  
在WIKI页面中写上你的插件名称，内容要加上下面的语句：  
  
{% codeblock lang:html %}
 {jenkins-plugin-info:pluginId=your-artifact} 
{excerpt}  your plugin description  {excerpt} 
{% endcodeblock %}  
  
your-artifact要写你的插件id，这样页面就会自动去加载插件的相关信息。excerpt里面的是你插件描述，会显示在插件主页上。  
  
最后是在WIKI label上加上插件的分类，比如是UI相关的插件就写plugin-UI，报告类相关的就写plugin-report，这样在插件主页上会将你的插件归到某类插件下。  
  
这里介绍编写WIKI的一个小窍门，可以先进入其他插件页面，然后点击右上角的Edit链接，这样就进入了页面的编辑页面，在这里就可以看到其他插件是如何编写的，参考一下再来编写自己的WIKI页面吧。  
  
###持续集成  
  
在上传你插件代码的时候，可能你会想使用持续集成来跑你的测试案例，看看上传的代码是否有破坏原有的功能。没有问题，可以使用BuildHive@CloudBees来为你Github上的代码做持续集成。  
  
使用很简单，先进入这个网址[https://buildhive.cloudbees.com/job/jenkinsci/][url3]，然后使用你的github帐号登录，接着勾选你想要做持续集成的github项目。这样该项目每次提交代码之后，BuildHive@CloudBees就会为你做持续集成，如果有问题的话会发邮件通知你。    
[url3]: https://buildhive.cloudbees.com/job/jenkinsci/    
     
到这里，jenkins插件开发的所有介绍已经全部结束了，之前做插件开发的时候查到一些中文资料，都是一些比较入门的内容，所以自己就想写一个比较全面的介绍，希望这一系列的文章可以帮到你，谢谢。



