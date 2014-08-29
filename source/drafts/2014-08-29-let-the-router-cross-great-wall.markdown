---
layout: post
title: "让路由飞越长城"
date: 2014-08-29 21:13
description: 让路由飞越长城
keywords: ddwrt,buffalo,autoddvpn
comments: true
categories: code
tags: [ddwrt,buffalo,autoddvpn]
---
  
{% img /images/post/2014-8/greatefw.jpeg %}  
  

大纲：
* 打造翻墙路由器的原因
* 路由器的选型——buffalo
* 路由器固件的升级
    * 升级到路由器厂商的ddwrt固件版本
    * 升级到ddwrt官网的固件版本
* vpn的选择——pptp，l2tp
* autoddvpn的使用
* 悬而未决的问题求解

最近GFW貌似升级了，现在连Google也无法访问，开发用百度实在搜不到什么东西，用bing也各种不爽(用了几天自己都快病了...)。虽说可以用SSH，VPN翻墙，但家里的电子设备如果比较多，要一个一个设置起来比较麻烦，如果能在路由器翻墙就好了，这样只要设备只需要接入路由器的WIFI就可以自动翻墙，无需任何设置。  
<!--more-->
  
### 可以刷固件的路由器
要实现路由器翻墙，首先需要一个高级的路由器，这种路由器可以刷一些路由器固件（固件我理解就是路由器的操作系统，一般是基于Linux的），我们后面需要在固件上通过脚本配置实现翻墙。后面我们会基于DD-WRT这个固件讲解脚本如何配置，所以请选择可以刷DD-WRT固件的路由器，这里有[网页][dd-wrt-devs]可以参考.  

我在网上参考了一些资料，觉得Buffalo(巴法洛)的路由器比较好，大部分实现了路由器翻墙的介绍文章都是使用Buffalo的，所以向大家也推荐这个牌子，我用的型号是WZR-HP-G300NH2，淘宝上买400多。
  
{% img /images/post/2014-8/WZR-HP-G300NH2.jpg %}  
  
### [DD-WRT][dd-wrt-index]
路由器的固件其实不只DD-WRT，还有[tomato][tomato-index]也是一个比较流行的路由器固件，两种固件各有优劣，网上有很多它们的比较，这里就不说了。



[dd-wrt-devs]: http://www.dd-wrt.com/wiki/index.php/Supported_Devices
[dd-wrt-index]: http://www.dd-wrt.com
[tomato-index]: http://www.polarcloud.com/tomato

