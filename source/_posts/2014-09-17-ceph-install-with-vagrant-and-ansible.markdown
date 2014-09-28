---
layout: post
title: 使用Vagrant和Ansible搭建Ceph环境
date: 2014-09-17 22:03
description: 使用Vagrant和Ansible部署Ceph环境
keywords: vagrant,ansible,ceph
comments: true
categories: code
tags: [vagrant,ansible,ceph]
---
  
{% img /images/post/2014-9/ceph-install.jpg %}  
  
## [Ceph][ceph]简介  
Ceph是一个高性能，高可用，高扩展的分布式对象存储框架和文件系统，而且是一个免费开源的项目。  
  
但是Ceph的环境搭建起来比较麻烦，最简单的环境也需要2台虚拟机(1台做mon和osd，另外一台做gateway)，而且按照官方文档上面的指南进行安装，经常报各种莫名其妙的问题，现在给大家介绍一个简单的方法来进行Ceph环境的搭建。下面先介绍几个要用到的工具。  
  
<!--more-->
## [Vagrant][vagrant]
  
{% img /images/post/2014-9/vagrant.png %}  
  
以前在使用VM(虚拟机)的情况是这样:   

* 下载操作系统的iso镜像
* 通过VM管理工具(VMWare，VirtualBox等)将iso镜像转换为VM
* 登陆到VM进行操作
  
使用了Vagrant之后就非常方便了，一个命令就可以搞定VM的安装，ssh到VM也无需输入用户名密码，还可以查看所有VM的状态等。

#### Box
vagrant通过box来生成VM，box可以理解是一个制作好的VM，这意味着你搭建完自己的开发环境后，也可以将其制作成一个box，供团队其他成员使用。  
  
box的容量非常小，比如Ubuntu12。04的一个iso镜像一般要500多M，制作成VM可能要10G，而一个ubuntu12.04的box只有300多M。Vagrant的box可以在[这里][vagrant-box]下载



## Ansible
  
{% img /images/post/2014-9/ansible.jpg %}  
  

## Ceph-ansible


[ceph]: http://ceph.com/
[vagrant]: https://www.vagrantup.com/
[vagrant-box]: https://vagrantcloud.com/discover/featured