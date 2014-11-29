---
layout: post
title: "如何用iso文件创建Vagrant的Box"
date: 2014-11-29 08:06
description: 如何用iso文件创建Vagrant的Box
keywords: veewee,vagrant
comments: true
categories: code
tags: [veewee,vagrant]
---
  
{% img /images/post/2014-11/iso_to_box.jpg %}  
  
虽然[Vagrant][vagrant]官网上已经有了很多OS的box，比如Ubuntu，CentOS，Debian等，但像RHEL操作系统这种不免费的OS，Vagrant上面就没有它的box，如果需要用到RHEL的box，我们就需要自己来制作。下面讲下从iso文件到box文件的一个制作过程。    
  
<!--more-->

## 准备
在制作box文件之前，我们需要安装下面的软件:  

* [Vagrant][vagrant]: 这个没必要说了，我们就是要制作它的box文件。  
* [VirtualBox][virtualbox]: 跟VMWare一样的虚拟机软件，不过它是免费的。  
* [Veewee][veewee]: 这是一款可以轻松创建Vagrant的box文件的工具，它还可以创建KVM和其他虚拟机镜像。  
  
这些软件的安装我就不介绍了，请到软件网站自行了解。  

## 使用Veewee创建Box
  
* 首先查找Veewee下面有哪些VirtualBox的模板，下面的vbox表示VirtualBox，当然你也可以换成其他的虚拟机工具。  
  
{% codeblock lang:sh %}
$ veewee vbox templates
{% endcodeblock %}   
  
* 命令会列出veewee可以用的模板，如果我们要制作RHEL6.5的Box，可以找CentOS6.5的模板来制作。
  
{% codeblock lang:sh %}
$ veewee vbox define rhel65-64bit 'CentOS-6.5-x86_64-netboot'
{% endcodeblock %}   
  
* 命令执行完后，会在当前目录下产生一个`definitions`的文件夹，这时我们需要修改下面的一些文件，。
  
{% codeblock lang:sh %}
$ vi definitions/rhel65-64bit/definition.rb
{% endcodeblock %}   
  
`definition.rb`是Veewee的创建脚本，我们将其中的`iso-file`的值修改为iso的文件名，比如`rhel-server-6.5-x86_64-dvd.iso`，


[vagrant]: https://www.vagrantup.com/
[virtualbox]: https://www.virtualbox.org/
[veewee]: https://github.com/jedi4ever/veewee