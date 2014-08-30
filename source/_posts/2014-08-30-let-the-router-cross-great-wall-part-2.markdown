---
layout: post
title: "让路由飞越长城(二)"
date: 2014-08-30 19:30
description: 让路由飞越长城
keywords: ddwrt,buffalo,autoddvpn
comments: true
categories: code
tags: [ddwrt,buffalo,autoddvpn]
---
  
{% img /images/post/2014-8/greatefw.jpg %}  
  
大纲：
* 打造翻墙路由器的原因
* 路由器的选型——buffalo
* 路由器固件的升级
    * 升级到路由器厂商的ddwrt固件版本
    * 升级到ddwrt官网的固件版本
* vpn的选择——strong vpn
* autoddvpn的使用
* 悬而未决的问题求解


配置好了高级的路由器，安装了dd-wrt版本的固件，现在还不忙着让路由器翻墙，先在一台机器上测试能通过新路由器能正常上网和翻墙，可以了之后我们再来配置路由器的翻墙功能。
<!--more-->
 
### VPN
翻墙基本是两种方式:SSH和VPN。SSH是在浏览器上连接国外的服务器从而可以浏览被和谐的网站，而VPN是让整个设备的网络都连上国外的服务器，包括浏览器和其他任何软件。整个设备网络翻墙的好处是可以让其他软件更快速地连接国外服务器，比如要下载android的sdk，国内经常半天没有反应，翻墙后下载速度就快多了，但缺点也比较明显，就是访问国内网站的时候会比较慢，国内的软件比如QQ时不时会掉线什么的。  
  
这里有个比较好的VPN推荐一下[Strong VPN][strong-vpn]，这个是美国比较好的一个VPN，稳定速度快，24小时技术支持，价格也比较合适，有个55美刀/年的套餐，支持PPTP,L2TP和SSTP连接。国内的VPN和SSH我用了几个都不大满意，不是有些网站不能上，就是经常掉线连不上。如果有米的话，还可以去租VPS(虚拟专用服务器)，自己搭VPN服务，那速度绝对比直接买的VPN快，甚至还可以自己卖VPN账号。  
  
不管怎样，我们需要一个VPN账号，而且可以支持PPTP连接的。
  
{% img /images/post/2014-8/strong-vpn.jpg %}  
  
### [autoddvpn][autoddvpn]
autoddvpn是一个解决方案，主要有下面几大功能:  

* 让你的dd-wrt路由器可以自由翻墙，路由下的所有设备仿佛在牆外一般，完全感覺不到牆的存在；
* 智能路由，可以自动判断访问的网站是墙内还是墙外的，如果是墙内的就不走VPN，如果是墙外的走VPN；
* 守护进程，监控你的VPN服务是否正常，如果VPN断了可以随时重连。


[strong-vpn]: http://www.strongvpn.com/
[autoddvpn]: https://code.google.com/p/autoddvpn/


