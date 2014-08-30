---
layout: post
title: "让路由飞越长城(一)"
date: 2014-08-29 21:13
description: 让路由飞越长城
keywords: ddwrt,buffalo,autoddvpn
comments: true
categories: code
tags: [ddwrt,buffalo,autoddvpn]
---
  
{% img /images/post/2014-8/greatefw.jpeg %}  
  
最近GFW貌似升级了，现在连Google也无法访问，开发用百度实在搜不到什么东西，用bing也各种不爽(用了几天自己都快病了)。虽说可以用SSH，VPN翻墙，但家里的电子设备如果比较多，要一个一个设置起来就会比较麻烦，如果能在路由器上翻墙就好了，这样只要设备只需要接入路由器的WIFI就可以自动翻墙，无需任何设置。  
<!--more-->
  
### 可以刷固件的路由器
要实现路由器翻墙，首先需要一个高级的路由器，这种路由器可以刷一些路由器固件（固件我理解就是路由器的操作系统，一般是基于Linux的），我们后面需要在固件上通过脚本配置实现翻墙。我是基于DD-WRT这个固件进行路由翻墙的，所以我选择可以刷DD-WRT固件的路由器，这里有[网页][dd-wrt-devs]可以参考.  

我在网上参考了一些资料，觉得Buffalo(巴法洛)的路由器比较好，大部分实现了路由器翻墙的介绍文章都是使用Buffalo的，所以向大家也推荐这个牌子，我用的型号是WZR-HP-G300NH2，淘宝上卖400多。  

使用了Buffalo的路由器后，感觉要比以前用的那些路由快很多，同样是打开百度（百度最大的功能，测网络联通）速度明显感觉不一样。  
  
{% img /images/post/2014-8/WZR-HP-G300NH2.jpg %}  
  
### [DD-WRT][dd-wrt-index]
路由器的固件其实不只dd-wrt一种，还有[Tomato][tomato-index]也是一种比较流行的路由器固件，两种固件各有优劣，网上有很多它们的比较，这里就不说了。  
  
dd-wrt对应不同品牌的路由器有不同的固件版本，每个品牌的不同型号也会对应不同的固件版本，要在这么多版本中查找到自己想要的固件版本，需要登录到dd-wrt的官网进行选择。  
  
进入官网首页后，点击Router Database模块，然后在输入框中输入路由器型号，就可以看到对应的dd-wrt版本记录。  

{% img /images/post/2014-8/dd-wrt-1.png %}  
  
点击查询出来的版本记录，会出现固件版本的详细说明和下载链接。
  
{% img /images/post/2014-8/dd-wrt-2.png %}  
  
这里有2个固件可供选择(不同型号的固件名称会有不同，这里是以我的路由器型号举例):  
  
* buffalo_to_ddwrt_webflash-MULTI.bin是指你的路由器固件不是dd-wrt的，需要把固件升级成dd-wrt。
* wzr-hp-g300nh2-dd-wrt-webupgrade-MULTI.bin是说你的固件已经是dd-wrt了，但固件版本太老了要升级。
  
用上面的方法查出来的固件版本其实不是最新的，这里有个dd-wrt版本的[ftp链接][dd-wrt-ftp]，打开连接后，可以看到不同年份的目录，进入某个目录后可以看到不同品牌的路由器的目录，比如`ftp://ftp.dd-wrt.com/betas/2014/06-23-2014-r24461/buffalo_wzr-hp-g300nh2/`，这里就是最新的固件版本了。  
  
### 升级路由器固件
其实Buffalo也有自己官方的dd-wrt版本，但看网上介绍buffalo官方的dd-wrt版本功能不行，所以建议还是刷正式的dd-wrt版本。在dd-wrt官网上可以看到路由器固件的[安装介绍][dd-wrt-install](这里是WZR-HP-G300NH2的[安装介绍][dd-wrt-install-special]），但个人感觉里面的说明太复杂，什么30/30/30硬复位大法，TFTP刷新法等看不大明白，同时也怕太复杂导致失误操作让路由器变砖头。  
  
最简单的方式是在你的路由器的web管理页面直接升级（我只试过Buffalo的，其他的路由器我没试过）:

* 打开浏览器进入`192.168.1.1`，如果你的路由器是连接电信路由器网口的，可能ip地址不是这个；
* 输入用户名密码后，进入管理配置页面，点击其中的更新，选择dd-wrt的固件文件打开。
* 点击**更新固件**按钮，然后大概等个5~6分钟，等看到100%完成，路由器会自动重启；
* 重新进入`192.168.1.1`，可以看到变成dd-wrt的管理页面了，首次登陆需要输入用户名和密码。
  
{% img /images/post/2014-8/dd-wrt-3.png %}  
  

[dd-wrt-devs]: http://www.dd-wrt.com/wiki/index.php/Supported_Devices
[dd-wrt-index]: http://www.dd-wrt.com
[tomato-index]: http://www.polarcloud.com/tomato
[dd-wrt-ftp]: ftp://ftp.dd-wrt.com/betas/
[dd-wrt-install]: http://www.dd-wrt.com/wiki/index.php/Installation
[dd-wrt-install-special]: http://dd-wrt.com/wiki/index.php/Buffalo_WZR-HP-G300NH

