---
layout: post
title: "科学上网利器Shadowsocks"
date: 2015-11-07 10:31
description: 翻墙利器Shadowsocks
keywords: ss,vps 
comments: true
categories: vps
tags: [ss, vps]
---

{% img /images/post/2015-11/ss_vultr.jpg 400 350 %}  
  
前段时间AppStore出了一款神器[Surge][surge]，可以让iOS像其他平台使用[Shadowsocks][ss]（以下简称ss）一样地轻松科学上网，因为以前都是用买的SSH或VPN科学上网，所以对自建的VPN服务这一块没有太多关注，甚至错过了ss这种成熟的工具。最近试用了过后觉得非常方便，所以在这里记录一下。
  
<!--more-->  
  
## ss介绍
  
简单的说，ss就是帮助你翻过伟大的GFW的一个工具，只要你有一台可以访问国外网站的服务器，你就可以建立自己的VPN服务。它的方案十分完备，有服务端和各种操作系统的客户端，而且还内置了一个路由列表，在访问网站时会自动识别该网站是否需要科学上网，如果需要的则走国外线路，否则走本地线路，非常智能。  
  
## vps选择
要搭建自己的VPN，首先需要一台国外的服务器。老牌的vps有[Linode][linode]和[DigitalOcean][do]，都是不错的选择，后起之秀有[vultr][vultr]，我选择vultr是因为它最近在做活动，每个新用户送50美元，但是50美元需要在2个月内使用完，相当于头2个月可以选择25美元/月的服务器，这种价位的服务器性能是很高的。  
  
vultr50美元的活动链接是这个：[50美元活动][vultr_50dollor]，需点击页面中的`TRY VULTR CLOUD SERVERS FREE`按钮来注册用户，注册之后会要求绑定银行信用卡，绑定成功后vultr会在信用卡上预约扣款2.5美元，放心这个钱后面会退的。因为vultr需要24小时来审核你的账户，vultr要求每个人只能有一个账号，不允许一个人建立多个账号。
  
当过了2个月之后，还可以使用其他优惠码来对账户进行充值，优惠码链接请看这里：[vultr优惠码][vultr_youhui]。
  
## vps使用
  
vps就是一台云服务器，创建的时候要选择机房的物理位置、对应的操作系统和服务器配置，vultr最低的配置是5美元/月，对普通的搭搭博客和搭建VPN的需求是绰绰有余了。  
  
{% img /images/post/2015-11/vultr_size.png %}  
  
搭建完后就可以对机器进行相关的配置了，最常用的就是配置自己的访问电脑`SSH KEYS`，这样每次ssh登陆就可以不用输入密码了。  
  
{% img /images/post/2015-11/vultr_ssh.png %}  
  
## ss服务端
  
搭建ss服务器的说明请看这里：[ss服务器搭建][ss_server]，操作比较简单，先安装pip，然后再通过pip安装ss，pip是Python的下载包工具，现在的linux操作系统一般都默认安装了Python。  
  
然后是设置端口、密码和加密算法，建议用后台方式启动服务，我是用配置文件的方式启动ss服务，这种方式可以将配置信息放在文件中维护。  
  
如果连接有问题，可以通过查看日志文件来看是什么问题，文件路径：`/var/log/shadowsock.log`。  
  
## ss客户端——android
  
ss安卓客户端的下载地址在这里：[ss安卓客户端][ss_android]。App中文名字叫`影梭`，安装完成后打开配置界面，填写服务器ip，远程端口，本地端口，密码和加密方法，这些都是和服务端的配置文件内容一致的，完了点击连接，然后你的手机就可以科学上网了，赶紧把Fackbook, Twitter这些下下来玩一下吧。  
  
{% img /images/post/2015-11/ss_android.jpg 200 150 %}  
  
## ss客户端——mac
  
ss在Mac上的客户端在这里：[ssMac客户端][ss_mac]。下载后直接安装即可，安装完成后在右上角菜单栏有个箭头的Logo就是ss了。如下图所示，进入ss的Server来配置服务端的信息。  
  
{% img /images/post/2015-11/ss_mac.png 200 150 %}  
  
配置完成后，点击`Turn Shadowsocks On`就可以开启ss客户端了，然后在Chrome浏览器中安装SwitchySharp插件，配置信息如下，以后只要选择了这个代理就可以科学上网了。  
  
{% img /images/post/2015-11/chrome_proxy.png 400 350 %}  
  
## proxifier
  
在Mac上安装了ss的客户端之后，虽然可以通过浏览器科学上网了，但是如果想让其他软件，比如邮箱，terminal等也能科学上网的话，就需要将你的ss代理全局化了。这里介绍一个软件[proxifier][proxifier]，它可以很方便地将ss的服务设置为全局服务。  
  
* 首先创建代理  
  
{% img /images/post/2015-11/proxy_create.png 400 350 %}  
  
* 然后再创建规则，注意这时要将Mac上的ss客户端ShadowsocksX设为`直连`，其他应用设为走ss代理。  
  
{% img /images/post/2015-11/proxy_rule.png 400 350 %}  
  
* 最后观看proxifier的连接信息，就可以看到电脑上应用的网络连接信息了。  

[surge]: https://itunes.apple.com/cn/app/surge-web-developer-tool-proxy/id1040100637?ls=1&mt=8
[ss]: https://github.com/shadowsocks/shadowsocks/wiki
[linode]: http://www.linode.com
[do]: https://www.digitalocean.com
[vultr]: https://www.vultr.com/
[vultr_50dollor]: https://www.vultr.com/freetrial/
[vultr_youhui]: http://vultr.youhuima.cc/
[ss_server]: https://github.com/shadowsocks/shadowsocks/wiki/Shadowsocks-%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E
[ss_android]: https://github.com/shadowsocks/shadowsocks-android/releases
[ss_mac]: https://github.com/shadowsocks/shadowsocks-iOS/wiki/Shadowsocks-for-OSX-%E5%B8%AE%E5%8A%A9
[proxifier]: https://www.proxifier.com/
