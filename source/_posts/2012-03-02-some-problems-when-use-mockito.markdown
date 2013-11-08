---
layout: post
title: "使用Mockito时遇到的一些问题"
date: 2012-03-02 17:44
description: "使用Mockito时遇到的一些问题"
keywords: mockito, java, junit
comments: true
categories: code
tags: [mockito, java, junit]
---

最近在使用Mockito时遇到了几个比较tricking的问题，在这里记录一下。  

<!--more-->
1.如果方法的参数或者返回类型是泛型通配符相关的（如<?>，<? extends XXX>），不管你定义的对象类型是否正确匹配，用any(match)方法时都会编译出错。  
    
解决办法：修改方法的参数类型，去掉泛型通配符。我遇到的相关代码去掉通配符之后也是运行正常的，如果去掉后运行不正常，可能需要寻找其他解决办法，具体没有研究。  
    
2.mock对象的方法有多个参数，如果对第一个参数使用any()方法匹配，则后面的所有参数都需要使用any匹配，而不能使用真实对象作为参数。  
{% codeblock error info - test.java lang:java %}
This exception may occur if matchers are combined with raw values:
    //incorrect:
    someMethod(anyObject(), "raw String");
When using matchers, all arguments have to be provided by matchers.
For example:
    //correct:
    someMethod(anyObject(), eq("String by matcher"));
{% endcodeblock %}  
  
3.在setUp方法里面进行方法mock，直接在IDE里面运行junit成功，但用ant运行则会报错，其实这是另外一个测试类里面verify方法使用错误，如下： 
  
{% codeblock code - test.java lang:java %}
    //错误
    verify(packageBuildContext.getPackageBuilder());

    //正确
    verify(packageBuildContext).getPackageBuilder()
{% endcodeblock %}  
  
很奇怪，在用ant跑单元测试的时候，这个测试类的错误不是在这个类的测试报告里体现，而是出现在另外一个类的测试报告里，而且直接在IDE里面运行junit不会报错。  

