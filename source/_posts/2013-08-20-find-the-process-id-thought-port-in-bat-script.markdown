---
layout: post
title: "bat脚本:通过端口号查找进程号"
date: 2012-03-26 22:51
comments: true
categories: code
tags: [jenkins, auto-deploy, bat]
---

最近在用jenkins做自动化web部署，web服务器是tomcat。  
  
遇到了这样一个问题：在服务器上执行tomcat的shutdown.bat命令可以正常关机，但用jenkins执行shutdown.bat命令时却经常关闭不了，从而导致重启时报错。后来想到了杀进程的办法来关闭tomcat服务器，但是要怎样才能查到tomcat进程并杀掉呢？  
  
下面是通过端口号查找进程的命令：  
{% codeblock lang:bat %}
netstat -ano | findstr 8888 //我的tomcat端口给我改成8888了。
{% endcodeblock %}  
  
查出来的结果好像这样：TCP    0.0.0.0:8888           0.0.0.0:0              LISTENING       1844  
其中最后一项是进程号（PID），要怎么取到PID并杀掉进程呢？  
  
可以使用for语句来操作，for默认是以空格或tab为分割，即上面的结果会分割成5个部分，而pid正好是第5部分的内容，所以命令可以这样写：  
{% codeblock lang:bat %}
for /f "tokens=5" %%i in ("netstat -ano | findstr 8888") do taskkill /f /pid %%i /t
{% endcodeblock %}  
  
taskkill命令是杀进程的，/f表示强制进行，/t表示将其子进程一并杀掉。  
  
这样就不怕tomcat服务关不掉了，重启也正常了，自动化部署成功。  
