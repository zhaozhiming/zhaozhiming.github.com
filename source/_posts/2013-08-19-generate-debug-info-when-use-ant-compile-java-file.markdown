---
layout: post
title: "在ant编译java文件时产生debug信息"
date: 2012-03-06 17:41
comments: true
categories: code
---

使用ant编译java文件时，如果没有设置debug属性，则不会产生编译信息，ant的默认设置是不打印编译信息。  
  
如果想在编译过程中显示编译信息，需设置debug属性为true，并且设置debugLevel，如下代码所示：  

{% codeblock ant file - build.xml lang:xml %}
<javac ... debug="true" debuglevel="lines, vars, source">
{% endcodeblock %}  
  
ant的官方文档：  

 debug       |  Indicates whether source should be compiled with debug information; defaults to off. If set to off, -g:none will be passed on the command line for compilers that support it (for other compilers, no command line argument will be used). If set to true, the value of the debuglevel attribute determines the command line argument.  | No  
--------------- |  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| ---------  
debuglevel|  Keyword list to be appended to the -g command-line switch. This will be ignored by all implementations except modern, classic(ver >= 1.2) and jikes. Legal values are none or a comma-separated list of the following keywords: lines, vars, and source. If debuglevel is not specified, by default, nothing will be appended to -g. If debug is not turned on, this attribute will be ignored. |  
  
  
这里要注意的是如果设置debug属性为true，但是没有设置debuglevel属性，编译时还是不会打印信息，因为debuglevel的默认值是none，只有单独设置为lines, vars, source或其组合才会打印出信息。  
