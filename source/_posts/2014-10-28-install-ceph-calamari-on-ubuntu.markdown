---
layout: post
title: "在Ubuntu12.04上安装Ceph Calamari"
date: 2014-10-28 15:56
description: 在Ubuntu12.04上安装Ceph Calamari
keywords: calamari,ceph,ubuntu
comments: true
categories: code
tags: [calamari,ceph,ubuntu]
---
  
{% img /images/post/2014-10/calamari.png %}  
  
Calamari是[Ceph][ceph]的一个监控和管理工具，它提供了一些定义好的REST API。Calamari包括服务端和客户端，服务端是使用Python的web框架[Django][django]开发的，提供了供客户端调用的REST API接口。客户端使用了[NodeJS][nodejs],[AngularJS][angularjs],[Bootstrap3][bootstrap]，每个模块可以独立部署更新，其界面十分简洁清晰，几乎涵盖了所有监控要求。  
  
下面介绍一下Calamari在Ubuntu上面的安装过程。  
  
<!--more-->
  
## 环境准备

* 安装VitrualBox和Vagrant，vagrant的使用可以参照我之前的blog——[使用Vagrant和Ansible搭建Ceph环境][vagrant_blog]。
* 下载ubuntu12.04的box文件——[box文件下载地址][vagrant_box]，我们在虚拟机中安装calamari，不污染我们的本机环境。
  
## 生成Calamari安装文件  
### 生成server安装文件
  
{% codeblock lang:shell %}
$ mkdir calamari-server	
{% endcodeblock %}     
  

## 安装Calamari

## Ceph集群配置监控服务



[ceph]: http://ceph.com/
[django]: https://www.djangoproject.com/
[nodejs]: http://nodejs.org/
[angularjs]: https://angularjs.org/
[bootstrap]: http://getbootstrap.com/
[vagrant_blog]: http://zhaozhiming.github.io/blog/2014/10/02/ceph-install-with-vagrant-and-ansible
[vagrant_box]: https://vagrantcloud.com/discover/featured