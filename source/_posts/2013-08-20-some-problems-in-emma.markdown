---
layout: post
title: "使用emma时遇到的一些问题"
date: 2012-03-27 22:05
description: "使用emma时遇到的一些问题"
keywords: emma,问题
comments: true
categories: code
tags: [ant, jenkins, emma]
---
今天在用使用emma的过程中遇到了几个问题，记录一下.  
  
**1.跑junit过程中没办法产生coverage data文件，导致最后没办法出emma报告，上官网查了一下原因如下：**  
<!--more-->  
{% blockquote %}
I have instrumented my classes but am not getting any coverage data...   
This question has been asked several times and in all cases the users forgot to package or deploy the instrumented versions of their classes. Please check that your build places EMMA-instrumented classes ahead of the normal application classpath at runtime.
{% endblockquote %} 
意思是emma会根据工程里面的class文件产生自己的instrumented class，junit在跑单元测试的时候，classpath要把定义emma的class文件路径放在项目class文件路径前面，才能产生coverage data文件。  
  
{% codeblock lang:xml %}
<junit ...>
  <classpath location="${emma.classes.dir}"/>
  <classpath location="${project.classes.dir}"/>
...
</junit>
{% endcodeblock %}  

**2.emma的示例代码里面，report的sourcepath属性只有一个src路径，如果项目里面有多个src路径同时要进行覆盖率检查的话，则需要这样写：**  

{% codeblock lang:xml %}
<report sourcepath="${src1};${src2};${src3}">
   ...
</report>
{% endcodeblock %}  
不同的src路径用操作系统的classpath分隔符或逗号隔开，比如window用分号隔开，官网是这样解释的：  
{% blockquote %}
 sourcepath : An optional source path to use for report generation (a path-like structure). It is interpreted as a list of directories (separated by the OS-specific classpath separator or comma) containing .java source files. The local path names within each directory should reflect class package names. (Currently, only the HTML report generator uses this data, and only atmethod report depth.)
{% endblockquote %}  
  
**3.jenkins的emma插件可以显示覆盖率的趋势，已经包，类，方法的覆盖率情况，但不支持链接显示代码源文件，看了下jenkins的官网，有人已经对插件提了这样一个需求，但回复说无法实现，说明如下：**    
{% blockquote %}
 This was an obvious feature and so I tried to do this, but the problem is that the HTML file names and anchors that EMMA puts are just random numbers. So Hudson cannot link to the appropriate portion of those HTML files.

I also thought about generating those reports by myself in Hudson, but EMMA doesn't leave the line-by-line coverage information in XML file either.

So that leaves me no choice but to parse emma's data file directly. So this is bit involving.　　
{% endblockquote %}  
  
意思是说emma的html源文件是用随机数字命名，所以在xml文件中无法链接这些html源文件，emma没有修复这个问题，插件作者也无法完成这个需求:(  
  
我的解决方案是用doc link插件在jenkins页面上直接链接coverage.html文件。


