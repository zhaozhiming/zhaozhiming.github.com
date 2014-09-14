---
layout: post
title: "让你的安卓开发更容易(二)——Genymotion"
date: 2014-08-31 17:29
description: 让你的安卓开发更容易
keywords: android,genymotion
comments: true
categories: code
tags: [android,genymotion]
---
  
{% img /images/post/2014-8/genymotion-logo.jpg %}  
  
以前听过一个笑话，说是一个App好不容易拿到100万的融资，但是没几天就花完了，问创始人这钱怎么花的？创始人说:没什么，就是每个型号的Android手机各买了一个来做测试，钱就花完了。  
  
Android开发需要有强大的模拟器来避免这种尴尬，[Genymotion][genymotion-index]是一个Android模拟器，比起Google官方的AVD(Android Virtual Devices)，它有着启动快速，安装方便，简单上手的特点。
<!--more-->

## 注册安装
* 进入官网首页，点击`GET GENYMOTION`按钮（官网需要翻墙访问，不过有genymotion的[中文网][genymotion-ch]也可以访问）;
  
{% img /images/post/2014-9/genymotion-install-2.png %}  
  
* 有3个套餐让你选择，我们当然选择免费的先试用一下，点击download按钮;
  
{% img /images/post/2014-9/genymotion-install-3.png %}  
  
* 下载要求你先注册一个账号，注册完成后需要到注册邮箱接收邮件，激活你的genymotion账号;
  
{% img /images/post/2014-9/genymotion-install-1.png %}  
  
* 激活账号后，可以看到网站提示你可以下载了;
  
{% img /images/post/2014-9/genymotion-install-4.png %}  
  
* 下载页面中，可以看到最上面的genymotion版本包含了Oracle VirtualBox4.2.12这个虚拟机工具，如果是选择下面的genymotion，则需要先下载[VirtualBox][virtualbox]并安装;
  
{% img /images/post/2014-9/genymotion-install-5.png %}  
  
* 下载页面的下方，还有流行的Java IDE——Intellij IDEA和Eclipse的插件，看你用的IDE是哪个就下载哪个插件，这个后面会用到;
  
{% img /images/post/2014-9/genymotion-install-6.png %}  
  
* 下载genymotion的安装文件后，安装安装提示进行安装即可。

## 使用说明

* 点击安装完成后的genymotion图标，下图的中间那个图标；  
  
{% img /images/post/2014-9/genymotion-use-1.png %}  
  
* 启动后是genymotion客户端的主界面；  
  
{% img /images/post/2014-9/genymotion-use-2.jpg %}  
  
* 初次启动会提示你没有虚拟设备，是否添加一个？选择yes；  
  
{% img /images/post/2014-9/genymotion-use-3.jpg %}  
  
* 这时会弹出一个登陆框，输入注册的用户名（或邮箱）和密码，点击Connect按钮（这一步需要用VPN翻墙）；  
  
{% img /images/post/2014-9/genymotion-use-4.jpg %}  
  
* 验证通过后就可以添加虚拟设备了，下图是虚拟设备列表，可以选择Android版本和设备型号进行过滤查询（有些比较老的手机型号会查不到），选择你需要的虚拟设备，点击Next；  
  
{% img /images/post/2014-9/genymotion-use-6.png 150 250 %}  
{% img /images/post/2014-9/genymotion-use-7.png 150 250 %} 
{% img /images/post/2014-9/genymotion-use-5.png %}   
  
* genymotion会显示虚拟设备的详细信息，你确定无误后点击Next就会进行下载；  
  
{% img /images/post/2014-9/genymotion-use-8.png %}  
  
* 虚拟机下载中；  
  
{% img /images/post/2014-9/genymotion-use-9.png %}  
  
* 下载完成后回到主窗口，选择下载后的虚拟设备，点击Play按钮；  
  
{% img /images/post/2014-9/genymotion-use-10.png %}  
  
* 你马上就可以看到你的虚拟设备已经启动，速度很快。  
  
{% img /images/post/2014-9/genymotion-use-11.png 300 500 %}  
  
## 与Intellij IDEA集成
现在我们来看看如何在IDE里面启动虚拟设备，这里以Intellij IDEA为例。  
  
* 刚才我们下载了genymotion的IDE插件，在IDEA中打开插件管理设置界面，选择`install plugin from disk...`进行安装，安装完后重启IDEA；

{% img /images/post/2014-9/genymotion-idea-1.png %}  
  
* 重启IDEA后看到工具栏里面多了一个红色手机状的图标，图中工具栏的最后面一个图标；

{% img /images/post/2014-9/genymotion-idea-2.png %}  
  
* 点击图标出现genymotion设备列表窗口，可以看到现在设备的状态都是关机的(off)；

{% img /images/post/2014-9/genymotion-idea-3.png %}  
  
* 选择一个设备点击Start按钮，设备和在genymotion客户端一样启动了；

{% img /images/post/2014-9/genymotion-idea-4.png %}  
  
* 关掉IDEA中的设备列表窗口，运行你的App，会提示你是否需要在刚才启动的设备里面运行，选择OK；

{% img /images/post/2014-9/genymotion-idea-5.png %}  
  
* 可以看到你的App已经在genymotion的虚拟设备中运行了；

{% img /images/post/2014-9/genymotion-idea-6.png 300 500 %}  
  


[genymotion-index]: http://www.genymotion.com/
[genymotion-ch]: http://www.genymotion.cn/
[virtualbox]: https://www.virtualbox.org/

