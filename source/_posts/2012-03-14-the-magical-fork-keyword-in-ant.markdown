---
layout: post
title: "ant里面神奇的fork"
date: 2012-03-14 10:51
description: "ant里面神奇的fork"
keywords: ant, fork
comments: true
categories: code
tags: ant
---

最近两天一直在处理ant运行java程序的一个问题，用IDE直接运行类里面的main函数一切正常，但用ant跑该函数就报错误，错误的原因是运行ant任务时调用的是AntClasloader，而IDE里面调用的是jvm里面的classloader。  
  
<!--more-->  
如何使ant直接调用jvm的classloader呢？尝试过了很多办法都不行，最后在不经意间设置了fork=true这个属性，结果运行正常了。  

在网上查了下fork的资料，ant官方定义是：  
{% blockquote %}
if enabled triggers the class execution in another VM
{% endblockquote %}  
  
Ant默认行为是调用运行Ant本身的JVM，然而如果你想要单独地调用编译器，则需要设置fork属性为true。  
{% codeblock lang:xml %}
<java fork="true" classname="xxx" />
{% endcodeblock %}  
  
以后发现在IDE里面可以运行成功，但在ant里面不能跑成功的，可以加上fork这个属性，这样一般就都可以通过了：）  

