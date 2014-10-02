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
  
以前使用VM(虚拟机)情况是这样的:   
  
* 下载操作系统的iso镜像
* 通过VM管理工具(VMWare，VirtualBox等)将iso镜像转换为VM
* 登陆到VM进行操作
  
使用了Vagrant之后就非常方便了，一个命令就可以搞定VM的安装，ssh到VM也无需输入用户名密码，还可以查看所有VM的状态等。  
  
#### Box
vagrant通过box来生成VM，box可以理解是一个制作好的VM，这意味着你搭建完自己的开发环境后，也可以将其制作成一个box，供团队其他成员使用。  
  
box的容量非常小，比如Ubuntu12.04的一个iso镜像一般要500多M，制作成VM可能要10G左右，而一个ubuntu12.04的box只有300多M。Vagrant的box可以在[这里][vagrant-box]下载，除了有各种OS(ubuntu, windosw, CentOS等)的VM外，还有Virtualbox和VMWare这两种虚拟器软件对应的box，不过要使用VMWare的box
需要安装插件和到购买相关的[License][vagrant-license]，毕竟VMWare不是免费的软件。  
  
下载了box后，执行下面命令就可以添加box了，如果直接输入box名称并发现本地没有box的话，会自动下载box文件。`PS: Vagrant默认使用Virtualbox作为虚拟器软件，所以在安装Vagrant还需要先安装Virtualbox。`  
  
{% codeblock lang:sh %}
//添加本地box文件
$ vagrant box add /your/box/path/xxx.box
//添加指定名称的box，没有的话会自动下载box文件
$ vagrant box add hashicorp/precise32
//列出所有的box
$ vagrant box list
{% endcodeblock %}   
  
#### Vagrant基本操作
Vagrant的操作非常简单，现在介绍几个常用的操作指令。`PS: 下面的大部分命令后面可以跟vm名称，不跟的话是对所有的vm进行操作。`  
  
* vagrant status: 展示vm的信息。
* vagrant up: 启动vm。
* vagrant ssh [vm]: ssh到某个vm上，无需输入用户名和密码。
* vagrant halt: 关闭vm。
* vagrant destroy: 销毁vm，如果你的vm被你玩残了，销毁它然后重新启动一个就可以了，很方便。  
#### Vagrant共享
使用`vagrant ssh`到vm后，可以看到根目录下有个`/vagrant`文件夹，这个是vm和工程间的共享目录，在这个文件夹里面存放东西，可以在存放Vagrantfile的目录里面看到，反之亦然，在vm里面也可以读取到工程下的文件。  
  
#### Vagrantfile
初始化vagrant工程后可以看到一个`Vagrantfile`的文件，这个是配置vm的文件，可以看下面的例子:
  
  
{% codeblock lang:ruby %}
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "hashicorp/precise64"

  config.vm.define :rgw do |rgw|
    rgw.vm.network :private_network, ip: "192.168.42.2"
    rgw.vm.host_name = "ceph-rgw"
    rgw.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "192"]
    end
    rgw.vm.provider :vmware_fusion do |v|
      v.vmx['memsize'] = '192'
    end
  end
end
{% endcodeblock %}   
  
这个Vagrantfile指定了box的名称，然后创建了一个名称为`rgw`的vm，指定了vm的ip、hostname、内存大小。  
  
关于vagrant就介绍到这里，想要了解更多信息可以查看[vagrant官网][vagrant]。

## Ansible
  
{% img /images/post/2014-9/ansible.jpg %}  
  

## Ceph-ansible


[ceph]: http://ceph.com/
[vagrant]: https://www.vagrantup.com/
[vagrant-box]: https://vagrantcloud.com/discover/featured
[vagrant-license]: https://www.vagrantup.com/vmware