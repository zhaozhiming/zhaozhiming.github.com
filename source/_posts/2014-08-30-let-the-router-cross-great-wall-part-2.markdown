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
  
配置好了高级的路由器，安装了dd-wrt版本的固件，现在还不忙着让路由器翻墙，先在一台机器上测试能通过新路由器能正常上网和翻墙，可以了之后我们再来配置路由器的翻墙功能。
<!--more-->
 
### VPN
翻墙基本是两种方式:SSH和VPN。SSH是在浏览器上连接国外的服务器从而可以浏览被和谐的网站，而VPN是让整个设备的网络都连上国外的服务器，包括浏览器和其他任何软件。VPN的好处是可以让其他软件更快速地连接国外服务器，比如要下载android的sdk，国内网络经常半天没有反应，翻墙后下载速度就快多了，但缺点也比较明显，就是访问国内网站的时候会比较慢，使用国内软件比如QQ的时候时不时会掉线。  
  
这里有个比较好的VPN推荐一下[Strong VPN][strong-vpn](需要翻墙才能访问)，这个是一个美国比较好的VPN，连接稳定，访问快速，24小时技术支持，价格也比较合适，有个55美刀/年的套餐，支持PPTP,L2TP和SSTP连接。国内的VPN和SSH我用了几个都不大满意，不是有些网站不能上，就是经常掉线连不上。如果有米的话，还可以去租VPS(虚拟专用服务器)，自己搭VPN服务，那速度绝对比直接买的VPN快，甚至还可以自己卖VPN账号。  
  
不管怎样，我们需要一个VPN账号，而且可以支持PPTP连接的，下面会用到。
  
{% img /images/post/2014-8/strong-vpn.png %}  
  
### [autoddvpn][autoddvpn](需要翻墙才能访问)
autoddvpn是一个解决方案，主要有下面几大功能:  

* 让你的dd-wrt路由器可以自由翻墙，路由下的所有设备仿佛在墙外一样，完全感觉不到墙的存在；
* 智能路由，可以自动判断访问的网站是墙内还是墙外的，如果是墙内的就不走VPN，如果是墙外的走VPN；
* 守护进程，监控你的VPN服务是否正常，如果VPN断了可以随时重连。
  
autoddvpn有2种设置模式:传统模式(classicMode)和优雅模式(graceMode)，这里我们主要介绍[graceMode][autoddvpn-gracemode]。在gracemode中，我们主要看PPTP这一块的内容，因为我们的VPN账号只支持PPTP，不支持OpenVPN(Strong VPN支持PPTP,L2TP,SSTP连接，但dd-wrt不支持L2TP和SSTP)。  
  
* 首先开启路由器的[JFFS][autoddvpn-jffs]，这样可以将我们的脚本放到jffs里面(jffs我理解是一块可以挂载的小硬盘)；
* 接着按照文档设置DNS([PPTP设置][autoddvpn-gracemode-pptp])；
* ssh或telnet到你的路由器，登录名和密码是你刷了dd-wrt版本后进入web页面设置的用户名和密码；

{% codeblock lang:sh %}
$ telnet 192.168.11.1
Trying 192.168.11.1...
Connected to 192.168.1.1.
Escape character is '^]'.

DD-WRT v24-sp2 std (c) 2014 NewMedia-NET GmbH
Release: 06/23/14 (SVN revision: 24461)

DD-WRT login: root
Password: 
==========================================================
 
 ____  ___    __        ______ _____         ____  _  _ 
 | _ \| _ \   \ \      / /  _ \_   _| __   _|___ \| || | 
 || | || ||____\ \ /\ / /| |_) || |   \ \ / / __) | || |_ 
 ||_| ||_||_____\ V  V / |  _ < | |    \ V / / __/|__   _| 
 |___/|___/      \_/\_/  |_| \_\|_|     \_/ |_____|  |_| 
 
                       DD-WRT v24-sp2
                   http://www.dd-wrt.com
 
==========================================================


BusyBox v1.22.1 (2014-06-23 03:23:41 CEST) built-in shell (ash)
Enter 'help' for a list of built-in commands.

root@DD-WRT:~# 
{% endcodeblock %} 

* 登陆后执行下面的命令；  

{% codeblock lang:sh %}
# mkdir /jffs/pptp
# cd /jffs/pptp
# wget http://autoddvpn.googlecode.com/svn/trunk/grace.d/vpnup.sh
# wget http://autoddvpn.googlecode.com/svn/trunk/grace.d/vpndown.sh
# wget http://autoddvpn.googlecode.com/svn/trunk/pptp/jffs/run.sh
# chmod a+x *.sh
# nvram set rc_startup='/jffs/pptp/run.sh'
# nvram commit
{% endcodeblock %} 

一般执行上面的wget命令都是不行的，因为autoddvpn网站要翻墙才能访问，所以可以在pptp目录下创建那3个脚本文件，然后将网站上脚本的内容copy到脚本文件里面，命令更新如下。  

{% codeblock lang:sh %}
# mkdir /jffs/pptp
# cd /jffs/pptp
# touch vpnup.sh
# touch vpndown.sh
# touch run.sh
# ...直接从网站上复制脚本内容，再拷贝到文件里面
# chmod a+x *.sh
# nvram set rc_startup='/jffs/pptp/run.sh'
# nvram commit
{% endcodeblock %}  

* 参考[文档][autoddvpn-pptp-client]的`设置PPTP client`部分进行dd-wrt的PPTP客户端设置;
* 重启dd-wrt路由器，启动后会产生一个Log文件`/tmp/autoddvpn.log 這是autoddvpn的log`;
* 最后关于守护进程的脚本，autoddvpn没有提供，其实就是定时检查一下PPTP连接是否通的，不通的话就重连，下面给一个openvpn的检查脚本，pptp的上网自己查一下；
  
{% codeblock /jffs/pptp/openvpnDaemon.sh lang:sh %}
#!/bin/sh
ISRUN=`ps|grep "openvpn"|wc -l`
if [[ $ISRUN -lt 4 ]]
then
echo "Not running, start!"
openvpn --config /jffs/openvpn/openvpn.conf --daemon
else
echo "Openvpn is already running."
exit
fi
{% endcodeblock %}  
  
然后在dd-wrt的web界面的“管理”->“管理”下启用Cron，并且在附加任务中输入：

{% codeblock lang:sh %}
*/2 * * * * /jffs/openvpn/openvpnDaemon.sh
{% endcodeblock %}  

### 悬而未决的问题——PPTP无法连接
可能有的人使用VPN进行PPTP连接时会发现连接不上，在windows下是报一个`806`的异常，这种可能是你的ISP(网络服务提供商——电信或联通)没有提供这个服务，这种情况下还不知道怎么解决。在网上查过不少资料，说什么设置路由器的PPTP穿透功能，配置1723端口开启等，这些都没有什么效果，希望有高手告知这种情况要怎么解决。谢谢


[strong-vpn]: http://www.strongvpn.com/
[autoddvpn]: https://code.google.com/p/autoddvpn/
[autoddvpn-gracemode]: https://code.google.com/p/autoddvpn/wiki/graceMode
[autoddvpn-gracemode-pptp]: https://code.google.com/p/autoddvpn/wiki/graceMode#設置方式(以PPTP為例)
[autoddvpn-jffs]: https://code.google.com/p/autoddvpn/wiki/jffs
[autoddvpn-pptp-client]: https://code.google.com/p/autoddvpn/wiki/HOWTO#設置PPTP_client


