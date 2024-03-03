---
layout: post
title: "在ubunut下使用pycharm和eclipse进行python远程调试"
date: 2014-04-13 15:54
description: 在ubunut下使用pycharm和eclipse进行python远程调试
keywords: python,remote debug,pycharm,eclipse,ubuntu
comments: true
categories: code
tags: [python,remote debug,pycharm,eclipse,ubuntu]
---

{% img /images/post/2014-4/pycharm_and_eclipse.jpg %}  

我比较喜欢Pycharm，因为这个是JetBrains公司出的python IDE工具，该公司下的java IDE工具——IDEA，无论从界面还是操作上都甩eclipse几条街，但项目组里有些人使用eclipse比较久了，一时让他们转pycharm比较困难，所以还是两边的设置都介绍一下吧。  
<!--more-->  

## pycharm远程调试
pycharmd的远程调试比eclipse的简单多了，而且调试程序也比较稳定，不像eclipse那样容易出一些莫名其妙的问题，步骤如下。  
  
1. 将pycharm安装目录下的pycharm-debug.egg文件拷贝到远程机器上（如果是python3的话就拷贝pycharm-debug-py3k.egg文件）。
2. 在远程机器上安装pycharm-debug.egg，安装命令： `easy_install pycharm-debug.egg`。
3. 在pycharm中设置断点监听配置。  
{% img /images/post/2014-4/pycharm-debug/pycharm-1.png %}  
{% img /images/post/2014-4/pycharm-debug/pycharm-2.png %}  
{% img /images/post/2014-4/pycharm-debug/pycharm-3.png %}  
  
4. 启动调试监听程序。  
{% img /images/post/2014-4/pycharm-debug/pycharm-4.png %}  
{% img /images/post/2014-4/pycharm-debug/pycharm-5.png %} 
  
5. 在远程机器上启动服务并发起http请求。  
{% img /images/post/2014-4/eclipse-debug/eclipse-10.png %}  
  
6. pycharm监听到请求会进入断点。
{% img /images/post/2014-4/pycharm-debug/pycharm-6.png %} 
  
更多关于pycharm远程调试的信息可以参阅[这里][url1]。

## eclipse远程调试  
使用eclipse进行python远程调试，需要先在ubuntu下面安装eclipse和eclipse的python插件PyDev。  

* Eclipse安装比较简单，直接解压下载后的eclipse包即可使用。
* PyDev可以按照[官网的向导][url2]进行安装。  
安装完后按照下面的步骤进行远程调试。  
  
* 在window->Preperences->PyDev->Debug中，设置debug端口，比如12306。  
{% img /images/post/2014-4/eclipse-debug/eclipse-1.png %}  
  
* 新增PyDev视图。  
{% img /images/post/2014-4/eclipse-debug/eclipse-2.png %}  
{% img /images/post/2014-4/eclipse-debug/eclipse-3.png %}  
   
* 在Pydev试图中增加remote debug菜单。  
{% img /images/post/2014-4/eclipse-debug/eclipse-4.png %}  
{% img /images/post/2014-4/eclipse-debug/eclipse-5.png %}  
  
* 在远程机器的源码里面添加调试代码和断点。  
{% img /images/post/2014-4/eclipse-debug/eclipse-6.png %}  
  
* 断点设置语句：  
`pydevd.settrace('192.168.8.128', port=12306, stdoutToServer=True, stderrToServer=True)`  
注意：这里的ip是指设置了调试监听的机器ip。  
{% img /images/post/2014-4/eclipse-debug/eclipse-7.png %}  
  
* 通过Pydev菜单打开调试监听。  
{% img /images/post/2014-4/eclipse-debug/eclipse-8.png %}  
开始监听显示信息如下：  
{% img /images/post/2014-4/eclipse-debug/eclipse-9.png %}  
  
* 启动远程服务并发起http请求。
{% img /images/post/2014-4/eclipse-debug/eclipse-10.png %}  
  
* Pydev监听到请求后就会进入断点。
{% img /images/post/2014-4/eclipse-debug/eclipse-11.png %}  

更多远程调试的内容可以参照Pydev的[官网步骤][url3]。
 
[url1]: http://www.jetbrains.com/pycharm/webhelp/remote-debugging.html
[url2]: http://pydev.org/manual_101_install.html
[url3]: http://pydev.org/manual_adv_remote_debugger.html
