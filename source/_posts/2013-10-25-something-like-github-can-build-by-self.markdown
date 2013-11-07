---
layout: post
title: "类似github的框架"
date: 2013-10-25 07:36
description: 类似github的框架
keywords: 类似github的框架
comments: true
categories: code
tags: [github,git]
---

github是程序员经常上的网站，但如果是在一家苦逼不能访问外网的公司，那不能把自己的代码托管在github上绝对是一件非常痛苦的事情。如果想要在公司内网也可以用github托管自己的代码，那就要自己搭建类似github的服务器，好在类似github的框架有很多，基本上都是基于git的，可以无缝衔接github而无需额外学习其他技术。  
  
<!--more-->
##github企业版
[github enterprise][url1]，如果你的基金充足，github企业版绝对是你的首选。它基本上包涵了现有github网站上面的一切功能，你使用它甚至在视觉上都不会发生任何变化，界面都是和现有github一样的。而且安装十分方便，安装文件就是一个虚拟机镜像文件，只要用虚拟机加载就可以了。但刚才说了“如果你的基金充足”，说明它是要收费的，而且价格不菲，1个20人团队的license每年要5000美刀。国外有用到github企业版的公司有Bizzard（玩游戏的都知道），国内用的很少，我了解到的只有豆瓣在用，而且只用在其中一个或几个项目组（PS:豆瓣好像有自己的代码托管工具叫豆瓣Code，克隆github且增加了自己的特性）。  
  
##gitlab
[gitlab][url2]，是用Ruby On Rails开发的一款开源代码托管框架，界面也是仿照github设计的，github用户可以快速上手，最重要的是它是一个开源的软件，这意味着你可以免费获取到软件，并将它部署到自己的机器上。但是gitlab的安装过程比较复杂，安装需要依赖以下几个工具：  
  
- ruby 1.9.3+
- git 1.7.10+
- redis 2.0+
- MySQL or PostgreSQL

另外gitlab的markdown文件显示功能不是很好，比如markdown中的table和嵌入html在github上面是可以正常显示，但是在gitlab上面就不行。  
  
##GitBucket
[GitBucket][url3]，是一个用scala写的高仿github的代码托管框架。这个框架是今年才开发的，目前功能还不是很齐全，数据库也是用的内存数据库，但是github上面的基本功能都有。它的一个最重要的特点是**安装非常方便**，只需要下载gitbucket最新版本的war包，然后放到随便哪个web容器（Jetty，Tomcat）就可以启起来了，想尝尝鲜的同学可以试用一下。  
  
##其他框架
下面列举一些其他类似的框架，没怎么用过不好做评价，下面的括号表示用哪种语言写的。  
  
- [Gitorious][url4](Ruby)
- [InDefero][url5](PHP)
- [Girocco][url6](Perl)
- [Gitosis][url7](Python)
- [Gitolite][url8](Perl)
  
这里也有一篇文章是介绍类似github框架的框架，看[这里][url9]。  
  

[url1]: https://enterprise.github.com/
[url2]: https://github.com/gitlabhq/gitlabhq
[url3]: https://github.com/takezoe/gitbucket
[url4]: http://gitorious.org/
[url5]: http://www.indefero.net
[url6]: http://repo.or.cz
[url7]: http://eagain.net/gitweb/?p=gitosis.git
[url8]: http://github.com/sitaramc/gitolite
[url9]: http://blacka.com/david/2010/09/28/hosting-your-own-git-repositories/