---
layout: post
title: "在junit中添加fail--有test失败即build failed"
date: 2012-02-24 11:28
description: "在junit中添加fail--有test失败即build failed"
keywords: junit,ant
comments: true
categories: code
tags: [ant, junit]
---
  
项目使用jenkins做持续集成，ant来构建，发现在跑junit单元测试的时候，如果有test case失败了，ci的状态是黄色的unstable，而不是红色的failed，看起来很不爽。个人觉得build只有两种状态最好，绿色stable和红色failed，黄色让人看起来很困惑，是要fix好呢还是不fix也可以呢？  
  
<!--more-->  
  在网上查到解决方案，就是在ant的build文件里面，加上这样一段：  
  
{% codeblock demo - build.xml lang:xml %}
　　<target name="unitTest" depends="runCompileTest">
　　　　<junit printsummary="on" failureproperty="junit.failure">
　　　　　　<!-- some code here-->
　　　　</junit>

　　　　<fail message="Some tests failed - Build failed" status="2">
　　　　    <condition>
　　　　        <isset property="junit.failure" />
　　　　    </condition>
　　　　</fail>
    </target>
{% endcodeblock %}  