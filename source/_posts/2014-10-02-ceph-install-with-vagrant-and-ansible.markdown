---
layout: post
title: 使用Vagrant和Ansible搭建Ceph环境
date: 2014-10-2 22:03
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
  
## [Ansible][ansible]
  
{% img /images/post/2014-9/ansible.jpg %}  
  
Ansible是一个开源的远程机器管理软件，可以批量操作多台远程服务器。`PS: Ansible只适合操作Linux和Unix机器，如果是Windows系统是不可以的。`  
  
#### 安装
要安装Ansible需要先安装Python2.6/7，然后可以通过easy_install或pip进行下载安装。
  
{% codeblock lang:sh %}
sudo esay_install ansible
# or
sudo pip install ansible
{% endcodeblock %}   
  
#### 使用示例
创建一个文件夹，在文件夹里面创建一个hosts文件，hosts格式如下:   
  
{% codeblock lang:sh %}
# hosts
[ceph]
192.168.42.2
192.168.42.101
192.168.42.201
{% endcodeblock %}   
    
可以看到hosts文件里面有几个远程机器的ip(这里是虚拟机)，远程机器可以分组，通过中括号里面的组名来划分。  
  
然后执行下面的命令执行简单的命令。  
  
{% codeblock lang:sh %}
$ ansible all -a 'who'
ceph-mon0 | success | rc=0 >>
ceph     pts/0        2014-10-02 08:54 (192.168.42.60)

ceph-osd0 | success | rc=0 >>
ceph     pts/0        2014-10-02 08:54 (192.168.42.60)

ceph-osd1 | success | rc=0 >>
ceph     pts/0        2014-10-02 08:54 (192.168.42.60)
{% endcodeblock %}   
      
从输出信息上可以看到这几台远程机器都成功执行了`who`命令，不过如果要成功执行上面的执行，还需要先在执行机和远程机上面设置无密码ssh连接。  
  
#### 无密码ssh连接
假设有2台机器，机器A和机器B，现在想让机器A`ssh`机器B的时候不需要输入用户和密码，操作如下。  
  
* 在机器B上创建一个用户，并配置好，下面命令的`username`指自己要创建的用户名。
  
{% codeblock lang:sh %}
$ sudo useradd -d /home/{username} -m {username}
$ sudo passwd {username}
# 输入密码
$ echo "{username} ALL = (root) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/{username}
$ sudo chmod 0440 /etc/sudoers.d/{username}
{% endcodeblock %}   
  
* 在机器A上生成密钥，并发送给机器B。
  
{% codeblock lang:sh %}
ssh-keygen

Generating public/private key pair.
Enter file in which to save the key (/ceph-admin/.ssh/id_rsa):
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
Your identification has been saved in /ceph-admin/.ssh/id_rsa.
Your public key has been saved in /ceph-admin/.ssh/id_rsa.pub.

$ ssh-copy-id {username}@{机器B}
{% endcodeblock %}   
  
* 到这里就可以不用输入密码进行ssh了，如果想连用户名也不想输入的话，需要机器A在`.ssh`文件下创建一个`config`文件，在里面添加如下内容。
  
{% codeblock lang:sh %}
Host 机器B
   Hostname 机器B
   User {username}
{% endcodeblock %}   
  
#### playbook
ansible还可以通过一个playbook脚本进行远程机器的操作。playbook的示例如下:   
  
{% codeblock playbook.yml lang:yaml %}
# playbook.yml
---
- hosts: all 
  remote_user: ceph 
  tasks:
    - name: whoami
      shell: 'whoami > whoami.rst'
{% endcodeblock %}   
    
完了执行如下命令可以看到执行结果。  
  
{% codeblock lang:sh %}
$ ansible-playbook playbook.yml 

PLAY [all] ******************************************************************** 

GATHERING FACTS *************************************************************** 
ok: [ceph-mon0]
ok: [ceph-osd1]
ok: [ceph-osd0]

TASK: [whoami] **************************************************************** 
changed: [ceph-mon0]
changed: [ceph-osd0]
changed: [ceph-osd1]

PLAY RECAP ******************************************************************** 
ceph-mon0                  : ok=2    changed=1    unreachable=0    failed=0   
ceph-osd0                  : ok=2    changed=1    unreachable=0    failed=0   
ceph-osd1                  : ok=2    changed=1    unreachable=0    failed=0   
{% endcodeblock %}   
    
这时可以在远程机器的用户目录上可以看到新产生了一个`whoami.rst`的文件。  
  
关于ansible就介绍到这里，想要了解更多信息可以查看[ansible的文档][ansible-doc]。  

## [Ceph-ansible][ceph-ansible]
这个github项目主要是利用了上面介绍的2个工具，使用vagrant来创建ceph需要的服务器vm，然后将ceph的环境搭建通过ansible的playbook脚本执行。  
  
#### 执行步骤

* 下载ceph-ansible项目;
  
{% codeblock lang:sh %}
$ git clone https://github.com/ceph/ceph-ansible.git
{% endcodeblock %}   
  
* 一行命令就可以完成环境搭建，完成后ceph的环境是: 3个mon，3个osd，1个rgw;
  
{% codeblock lang:sh %}
$ vagrant up
{% endcodeblock %}   
  

[ceph]: http://ceph.com/
[vagrant]: https://www.vagrantup.com/
[vagrant-box]: https://vagrantcloud.com/discover/featured
[vagrant-license]: https://www.vagrantup.com/vmware
[ansible]: http://www.ansible.com/home
[ansible-doc]: http://docs.ansible.com/
[ceph-ansible]: https://github.com/ceph/ceph-ansible