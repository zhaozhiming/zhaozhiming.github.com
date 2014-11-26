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
An open source project to pack, ship and run any application as a lightweight container.
{% endblockquote %}
  
现在是这样的:  
  
{% blockquote %}
An open platform for distributed applications for developers and sysadmins.
{% endblockquote %}
  
但不管怎样，Docker是一个很好的东西，可以让我们快速创建自己的开发环境，真正做到`Build once, Run anywhere`。  
  
## Docker命令介绍
如果有过Vagrant使用经验的话，Docker使用起来非常简单，无非就是把Vagrant的box换成image，把Vagrant的VM换成container就可以了。如果没有Vagrant使用经验也没有关系，试试Docker的[TryIt][docker-tryit]，里面有一个教程可以让你快速掌握Docker的一些基本命令。  
  
比较常用的Docker命令:  

* docker version: 查看Docker版本。
* docker pull [image name]: 下载一个docker的镜像，类似git拉代码的命令，不过这个是拉docker镜像。
* docker images: 列出所有的镜像。
* docker run [image name] [command]: 运行一个镜像的某个命令，这样会产生一个container。
* docker ps: 列出container。
* docker start [container name]: 启动一个container。
* docker stop [container name]: 停止一个container。
* docker rm [container name]: 删除一个container。
* docker rmi [image name]: 删除一个image镜像。  
  
## Docker镜像
在Docker官网上可以看到已经有很多做好的镜像，比如Ubuntu，CentOS，MySql等，而且每天都有一些新的镜像不断在上传，因为上传一个镜像就像github上传代码一样简单。  
  
{% img /images/post/2014-11/docker-images.png %}  
  
但是Docker在下载镜像的时候会发现速度很慢，有时候甚至连不上，这时候需要翻墙(可以看看我之前的文章，[让路由飞越长城(一)][fanqiang1]，[让路由飞越长城(二)][fanqiang2])，或者找一下国内的镜像(可以看下[这里][docker-cn])，虽然国内的镜像没有Docker官网的那么全，但基本的镜像还是有的。  
  
## Docker & Vagrant
在没有Docker之前，我使用Vagrant来创建自己的开发环境，Docker和Vagrant都有一个很好的特点，就是通过虚拟化环境来创建开发环境，这样的好处是不会影响本机的环境配置。  
  
试想一下，如果在你需要安装Mysql，Ruby，Apache等服务，在本机上就需要做各种配置，像修改环境变量等，遇到版本升级还需要删除本地配置，然后再更新，久而久之本地环境就会被"污染"了，这时候想安装其他服务可能就会报各种莫名其妙的错误。  
  
如果我们有虚拟化环境就不会存在这种问题了，在虚拟环境安装各种服务，不需要的话销毁环境重新创建一个即可，简单又方便。  
  
{% img /images/post/2014-11/docker_vagrant_small.png %}  
  
在Docker推出之后，网上就有各种比较Docker和Vagrant的文章，可以看看StackOverFlow上面的[这篇文章][docker-vs-vagrant]，连Docker的作者也来回答这个问题。  

其实Docker和Vagrant不一定是竞争的关系，也可以是相辅相成的关系，比如在本地安装Docker，还是需要修改一些本地的配置，以后遇到版本升级还是会遇到修改配置的问题，如果是下载一个Docker的Vagrant box(已经有人制作了一个，见[这里][vagrant-docker-box])，再使用VM安装其他docker镜像就不会有这种问题了。  
  
在网上有人做过Docker的性能评估，分别对比了原生OS，OS安装Docker，OS系统安装虚拟机，虚拟机安装Docker这4种情况的性能情况，最后的结论是在虚拟机上运行Docker性能比较差，建议如果是生产环境还是使用原生的Docker比较好，性能比较文章请见[这里][docker-performance]。  
  
## Steve Wozniak is not boring
在研究Docker的过程中，发现如果创建contrainer的时候不指定containrer名称的话，系统会自动帮你创建名称，而名称是随机生成的。名称随机有2部分组成，左半部分是形容词，右半部分是人名，是一些影响计算机发展的IT名人，在源码中可以看到这些名字的说明。  
  
有趣的是，当名字随机到`boring_wozniak`的时候，程序会跳过生成这个名字，而继续生成下个随机名字，旁边有段注释是`Steve Wozniak is not boring`，相当好玩。  
  
{% img /images/post/2014-11/docker-humorous.png %}  
  


[vagrant-ceph]: http://zhaozhiming.github.io/blog/2014/10/02/ceph-install-with-vagrant-and-ansible/
[docker]: https://www.docker.com/
[docker-tryit]: https://www.docker.com/tryit/
[fanqiang1]: http://zhaozhiming.github.io/blog/2014/08/29/let-the-router-cross-great-wall-part-1/
[fanqiang2]: http://zhaozhiming.github.io/blog/2014/08/30/let-the-router-cross-great-wall-part-2/
[docker-cn]: https://docker.cn/
[docker-vs-vagrant]: http://stackoverflow.com/questions/16647069/should-i-use-vagrant-or-docker-io-for-creating-an-isolated-environment
[vagrant-docker-box]: https://github.com/mitchellh/boot2docker-vagrant-box
[docker-performance]: http://blogs.vmware.com/performance/2014/10/docker-containers-performance-vmware-vsphere.html