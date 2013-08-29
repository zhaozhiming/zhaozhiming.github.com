---
layout: post
title: "jenkins的2个问题"
date: 2012-05-21 15:52
description: "jenkins的2个问题"
keywords: jenkins,问题
comments: true
categories: code
tags: jenkins
---
  
最近CI服务器从老版本的hudson升级为jenkins，遇到了2个问题，记录一下：  
  
**1.升级为jenkins后，junit report里面显示的test case数量为原来的两倍，每个test case跑了2遍。**  
  
在job设置的junit文件路径提示这样写：'myproject/target/test-reports/\*.xml'，老的hudson版本只会读取TEST-\*.xml文件，而新的jenkins不仅会读取所有TEST-\*.xml，还会读取TESTS-TestSuites.xml文件，而这个文件是包含了前面的Test-*.xml里面的test case的，所以会显示为两倍。  
  
解决方案是:junit文件路径这样写：'myproject/target/test-reports/TEST-*.xml'，这样就不会包含TESTS-TestSuites.xml文件了。  
  
官网上已经有人建议job配置的提示要改一下，但不知道jenkins以后会在哪个版本改（也有可能不会改:(）。  
  
**2.jenkins没有在build之前清空工作区的选项。**  
  
老的hudson版本的job设置里Advanced Project Options下有个选项是Clean workspace before build，即在build之前清空workspace里面的内容，而jenkins里面没有这个选项。  
  
那如何实现在build前情况workspace呢？  
可以在Source Code Management里面进行配置，一般的版本管理工具（git, svn）都可以配置Check-out Strategy，例如SVN， 选择Always check out a fresh copy，这个策略的意思是:  
  
{% blockquote %}
Delete everything first, then perform "svn checkout". While this takes time to execute, it ensures that the workspace is in the pristine state.
{% endblockquote %}  
  
这样就实现了build前清空workspace的效果了。  
