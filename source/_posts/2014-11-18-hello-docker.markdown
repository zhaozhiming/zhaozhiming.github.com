---
layout: post
title: "使用Docker打造自己的开发环境"
date: 2014-11-18 21:45
description: 使用Docker打造自己的开发环境
keywords: docker
comments: true
categories: code
tags: docker
---
  
{% img /images/post/2014-11/docker.png %}  
  
作为这个时代的程序员真的很幸福，每天不但有一些改变世界的产品出现，而且提高程序员开发效率的工具也是层出不穷。之前介绍过如何使用[Vagrant来创建Ceph环境][vagrant-ceph]，使用Vagrant可以很方便的管理我们的虚拟机，同时来定制开发团队的开发环境，现在又出现了Docker，让我们有了一个更好的选择。  
  
<!--more-->

## [Docker][docker]介绍
  
之前有幸参加了OSChina在成都举办的源创会，听到了Docker中国社区创始人马全一老师介绍Docker，他们团队在去年就开始研究Docker，但不知道为什么Docker今年开始火了起来，原因可能是Docker今年搞了一次宣传会，IT界各种大佬公司都去捧场了，有Google，RedHat等，然后媒体争相报道，于是Docker就火了。  
  
Docker的介绍也是不断在变，以前官方的Docker的概括是这样的:  
  
{% blockquote %}
An open platform for distributed applications for developers and sysadmins.
{% endblockquote %}
  
现在是这样的:  
  
{% blockquote %}
An open source project to pack, ship and run any application as a lightweight container.
{% endblockquote %}
  
但不管怎样，Docker是一个很好的东西，可以让我们快速创建自己的开发环境，真正做到`Build once, Run anywhere`。  
  
## Docker命令介绍
如果有过Vagrant使用经验的话，Docker使用起来非常简单，无非就是把Vagrant的box换成image，把Vagrant的VM换成container就可以了。如果没有Vagrant使用经验也没有关系，试试Docker的[TryIt][docker-tryit]，里面有一个教程让你学会Docker的一些基本命令。  
  
比较常用的Docker命令:  
* docker version: 查看Docker版本。
* docker pull [image name]: 下载一个docker的镜像，类似git拉代码的命令，不过这个是拉docker镜像。
* docker images: 列出所有的镜像。
* docker run [image name] command: 运行一个镜像的某个命令，这样会产生一个container。
* docker ps: 列出container。
* docker start [container name]: 启动一个container。
* docker stop [container name]: 停止一个container。
* docker rm [container name]: 删除一个container。
* docker rmi [image name]: 删除一个image镜像。
  
## Docker镜像
在Docker官网上可以看到已经有很多做好的镜像，比如Ubuntu，CentOS，MySql等，而且每天都有一些新的镜像不断在上传，因为上传一个镜像就像github上传代码一样简单。  
  
{% img /images/post/2014-11/docker-images.png %}  
  
但是Docker在下载镜像的时候会发现速度很慢，有时候甚至连不上，这时候需要翻墙(可以看看我之前的文章，[让路由飞越长城(一)][fanqiang1]，[让路由飞越长城(二)][fangqiang2])，或者找一下国内的镜像(可以看下[这里][docker-cn])，不过国内的镜像没有Docker官网的那么全。





## Steve Wozniak is not boring


[vagrant-ceph]: http://zhaozhiming.github.io/blog/2014/10/02/ceph-install-with-vagrant-and-ansible/
[docker]: https://www.docker.com/
[docker-tryit]: https://www.docker.com/tryit/
[fanqiang1]: http://zhaozhiming.github.io/blog/2014/08/29/let-the-router-cross-great-wall-part-1/
[fanqiang2]: http://zhaozhiming.github.io/blog/2014/08/30/let-the-router-cross-great-wall-part-2/
[docker-cn]: https://docker.cn/