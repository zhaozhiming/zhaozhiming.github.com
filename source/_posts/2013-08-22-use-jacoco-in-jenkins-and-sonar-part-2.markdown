---
layout: post
title: "在jenkins和sonar中集成jacoco(二)--在jenkins中生成jacoco覆盖率报告"
date: 2012-11-30 14:45
comments: true
categories: code
tags: [jacoco, jenkins, sonar]
---
  
先要在jenkins上安装jacoco的插件，安装完成之后在job的配置项中可以增加这个选项：  

{% img /images/2012112911555041.png %}  
{% img /images/2012112911561356.png %}
  
第一个录入框是你的覆盖率文件（exec），第二个是class文件目录，第三个是源代码文件目录。  
  
配置好了之后进行构建，构建完成之后job首页就会出现覆盖率的趋势图，鼠标点击趋势图可以看到覆盖率详情，包括具体覆盖率数据和源码的覆盖率情况：  

趋势图  
{% img /images/2012112911373757.png 趋势图 %}
  
覆盖率详情    
{% img /images/2012112911394918.png 覆盖率详情 %}
  
