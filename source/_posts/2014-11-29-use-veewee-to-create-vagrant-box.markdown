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
  
虽然[Vagrant][vagrant]官网上已经有了很多OS的box，比如Ubuntu，CentOS，Debian等，但像RHEL这种不免费的OS，Vagrant上面就没有它的box，如果需要用到RHEL的box，我们就需要自己来制作。下面讲下从iso文件到box文件的一个制作过程。    
  
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
  
* 命令执行完后，会在当前目录下产生一个`definitions`的文件夹，这时我们需要修改下面的一些文件。
  
{% codeblock lang:sh %}
$ vi definitions/rhel65-64bit/definition.rb
{% endcodeblock %}   
  
`definition.rb`是Veewee的创建脚本，我们将其中的`iso-file`的值修改为iso的文件名，比如`rhel-server-6.5-x86_64-dvd.iso`，其他iso开头的选项可以不修改或删除。  
  
{% codeblock definition.rb lang:ruby %}
Veewee::Session.declare({
  ...
  ...
  :iso_file => "rhel-server-6.5-x86_64-dvd.iso",
  ...
  ...
  })
{% endcodeblock %}   
  
* 然后下载rhel6.5的iso文件，上网查一下资源还是比较多的，这里就不贴iso文件的链接了，怕链接以后会失效，请自行搜索。
* 在当前目录下创建iso的子文件夹，将下载的iso文件放到这个文件夹中。  
  
{% codeblock lang:sh %}
$ mkdir iso
$ mv /your/iso/path/rhel-server-6.5-x86_64-dvd.iso iso
{% endcodeblock %}   
  
* 执行命令创建Box，然后去喝杯咖啡，等一会儿回来看看RHEL6.5的VM应该就创建好了。
  
{% codeblock lang:sh %}
$ veewee vbox build 'rhel65-64bit'
{% endcodeblock %}   
  
* 进到VirtualBox的虚拟机目录(比如在OSX下是`~/VirtualBox VMs`)，进行vagrant创建box文件操作。
  
{% codeblock lang:sh %}
$ cd ~/VirtualBox\ VMs/rhel65-64bit/
$ vagrant package --base rhel65-64bit --output rhel65-64bit.box
{% endcodeblock %}   
  
* 最后使用vagrant启动vm，正常启动证明我们的box已经正确创建了。
  
{% codeblock lang:sh %}
$ vagrant box add --name rhel65-64bit rhel65-64bit.box
$ vagrant init rhel65-64bit
$ vagrant up
{% endcodeblock %}   
  
## 手动创建Box
如果想手动创建Box也是可以的，不过比较麻烦，下面是手动创建Box的一些注意事项，注意以下命令都是在你的VM进行操作，所以首先要能ssh到VM，没有ssh的话需要先安装ssh。  
  
* 安装一些基本的软件，比如ssh, wget, curl等。
* 设置root用户密码为`vagrant`。
* 新增用户`vagrant`，密码也是设置为`vagrant`。
* 修改visudo的配置，让vagrant用户使用sudo时不需要输入密码。  
  
{% codeblock lang:sh %}
$ visudo
{% endcodeblock %}   
  
在最后一行增加以下内容:  
 
{% codeblock visudo lang:sh %}
vagrant ALL=(ALL) NOPASSWD:ALL
{% endcodeblock %}   
  
* 安装Guset Additions，这个是为了可以使用vagrant来创建共享文件夹。
  
{% codeblock lang:sh %}
$ wget http://download.virtualbox.org/virtualbox/4.3.18/VBoxGuestAdditions_4.3.18.iso 
$ sudo mkdir -p /media/VBoxGuestAdditions
$ sudo mount -o loop,ro VBoxGuestAdditions_4.3.8.iso /media/VBoxGuestAdditions
$ sudo /media/VBoxGuestAdditions/VBoxLinuxAdditions.run
{% endcodeblock %}   
  
* 修改ssh配置，让vagrant可以无密码ssh登陆VM。
  
{% codeblock lang:sh %}
$ cd /home/vagrant
$ mkdir .ssh
$ wget https://raw.githubusercontent.com/mitchellh/vagrant/master/keys/vagrant.pub -O .ssh/authorized_keys
$ chmod 700 .ssh
$ chmod 600 .ssh/authorized_keys
$ chown -R vagrant:vagrant .ssh
{% endcodeblock %}   
  
上面这些做好以后，就可以退出VM，后面的步骤就跟Veewee创建Box一样了，就是使用vagrant来生成box文件，添加box，启动VM。
  
[vagrant]: https://www.vagrantup.com/
[virtualbox]: https://www.virtualbox.org/
[veewee]: https://github.com/jedi4ever/veewee