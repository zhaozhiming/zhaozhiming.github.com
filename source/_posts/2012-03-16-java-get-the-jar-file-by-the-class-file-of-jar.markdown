---
layout: post
title: "Java通过class文件得到所在jar包"
date: 2012-03-16 18:04
description: "Java通过class文件得到所在jar包"
keywords: java,class,jar
comments: true
categories: code
tags: [ant, java]
---
  
今天遇到一个问题，需要通过知道的class文件得到该文件所在的jar包，试过很多办法都不行，最后在网上找到了一个解决办法，如下：  
<!--more-->
{% codeblock demo.java lang:java %}
String path = XXX.class.getProtectionDomain().getCodeSource().getLocation().getFile();
File jarFile = new File(path);
{% endcodeblock %}  
  
其中的XXX指已经知道的类名，然后通过后面的方法可以直接获取到JAR包，具体这些方法是干嘛的，下来研究后再补充。  

    