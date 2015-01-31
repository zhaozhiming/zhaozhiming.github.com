---
layout: post
title: "OSGi的简单代码示例"
date: 2015-01-25 20:35
description: OSGI的简答代码示例
keywords: osgi,felix
comments: true
categories: code
tags: [osgi,felix]
---

{% img /images/post/2015-1/osgi.gif %}  
  
OSGi(Open Service Gateway Initiative)是面向Java的动态模型系统，使用OSGi可以进行模块的动态加载，无需停止重启服务器，而模块就是我们下面要开发的Bundle。OSGi在电信或其他大型企业里面用的比较多，Eclipse现在也是用osgi的方式来添加插件。  
  
<!--more-->  
  
## IDEA的OSGi环境搭建

* 我们使用[Felix][felix]这个OSGi框架来进行OSGi代码的开发，首先我们下载最新版本的Felix包并解压

{% img /images/post/2015-1/felix_download.png %}  
  
* 接着在IDEA进行OSGi的设置

{% img /images/post/2015-1/felix_idea_setting_1.png %}  

{% img /images/post/2015-1/felix_idea_setting_2.png %}  

* 

[felix]: http://felix.apache.org/  

