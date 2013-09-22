---
layout: post
title: "sonar在ant工程中读取单元测试和覆盖率报告"
date: 2012-06-17 08:30
comments: true
categories: code
tags: [ant, sonar]
---
  
虽然sonar支持ant工程的构建，但目前最大的不足是无法在分析过程中产生单元测试和覆盖率报告，这样在sonar面板上覆盖率板块就始终没有数据。但幸运的是，sonar可以读取已经生成好的报告，让报告的内容显示在sonar的覆盖率面板上。  
  
<!--more-->  
首先需要配置sonar.dynamicAnalysis属性，这个属性有3个值，分别是true, false和reuseReports，默认值是true，即进行动态分析，但只对maven工程有效，要想读取外部的报告，需要将值设置为reuseReports。  
  
然后是单元测试报告的路径设置，对应的属性是sonar.surefire.reportsPath，value为junit报告的文件夹路径，这个路径下有junit生成的那些TEST-\*.xml文件。  
  
最后是覆盖率报告的路径设置，这个要看是用什么覆盖率工具。sonar有3种覆盖率工具的设置属性，分别是*jacoco，cobertura和clover*。像我们工程用的是cobertura，对应的属性名为*sonar.cobertura.reportPath*，其他2种分别是*sonar.jacoco.reportPath*和*sonar.clover.reportPath*。属性值和单元测试的略为不同，单元测试属性是指向文件夹，覆盖率是指向具体的覆盖率文件，比如cobertura的覆盖率文件是coverage.xml，则value应该写成“yourReportDir/coverage.xml"。  
  
配置完后重新进行sonar分析即可看到覆盖率报告成产生了。下面是具体代码：  

{% codeblock build.xml lang:xml %}
<property name="sonar.dynamicAnalysis" value="reuseReports" />
<!--unnit test-->
<property name="sonar.surefire.reportsPath" value="[baseDir]/myReports/unitTestReportDir" />
<!--coverage-->
<property name="sonar.cobertura.reportPath" value="[baseDir]/myReports/coverage.xml" />
{% endcodeblock %}  



