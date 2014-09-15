---
layout: post
title: "快速找到你想要的jar包"
date: 2014-09-14 17:41
description: 快速找到你想要的jar包
keywords: grepcode,maven-repository
comments: true
categories: code
tags: [grepcode,maven]
---
  
{% img /images/post/2014-9/class-not-found.jpeg %}  
  
在做Java开发时，经常遇到`Class not found`的错误，一般的做法就是在google上搜索class名字，然后再搜索这个class所在的jar包是哪个，最后才找到可以下载jar包的链接。过程比较繁琐，有没有更好的方法可以快速的找到缺少的class所在的jar包呢？答案是肯定的。  
  
<!--more-->

## [Grepcode][grepcode]
  
{% img /images/post/2014-9/grep-code-1.jpg %}  
  
这个网站可以通过class名称直接搜索到拥有这个class的所有的jar包，比如说我们运行Java程序时报下面的错误，发现`cn/com/starit/io/SystemDemo02`找不到。  
  
{% codeblock lang:sh %}
java.lang.NoClassDefFoundError: cn/com/starit/io/SystemDemo02
Caused by: java.lang.ClassNotFoundException: cn.com.starit.io.SystemDemo02
    at java.net.URLClassLoader$1.run(URLClassLoader.java:200)
    at java.security.AccessController.doPrivileged(Native Method)
    at java.net.URLClassLoader.findClass(URLClassLoader.java:188)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:307)
    at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:301)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:252)
    at java.lang.ClassLoader.loadClassInternal(ClassLoader.java:320)
Exception in thread "main" 
{% endcodeblock %}   
  
* 进入grepcode首页，输入`cn/com/starit/io/SystemDemo02`进行查询。
  
{% img /images/post/2014-9/grep-code-1.jpg %}  
  
* 可以看到查询结果涵盖了所有可能包含这个class的jar包，最前面的是相似度最接近的jar包名称，点开版本号还可以看到具体的源码。
  
{% img /images/post/2014-9/grep-code-2.jpg %}  
  
## [mvnrepository][mvn-repository]

知道Jar包的名称后，我们就可以通过mvnrepository这个网站来下载对应的jar包了，操作也很简单，输入jar名称就可以查询到相关的jar版本信息，里面还有maven，gradle等构建工具的XX。
  
{% img /images/post/2014-9/mvn-repository-1.jpg %}  
  
{% img /images/post/2014-9/mvn-repository-2.jpg %}  
  
{% img /images/post/2014-9/mvn-repository-3.jpg %}  
    

[grepcode]: http://grepcode.com/
[mvn-repository]: http://mvnrepository.com/
