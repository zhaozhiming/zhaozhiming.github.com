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
  
* 下载calamari工程
  
{% codeblock lang:shell %}
$ mkdir calamari-node
$ cd calamari-node
$ git clone https://github.com/ceph/calamari.git
$ git clone https://github.com/ceph/Diamond.git
{% endcodeblock %}     
  
* 使用vagrant生成server安装文件
  
{% codeblock lang:shell %}
$ cd calamari/vagrant/precise-build
$ vagrant up //首先要保证你的vagrant已经导入名字为precise的box
...
...
Copying salt minion config to vm.
Checking if salt-minion is installed
salt-minion was not found.
Checking if salt-call is installed
salt-call was not found.
Bootstrapping Salt... (this may take a while)
Salt successfully configured and installed!
run_overstate set to false. Not running state.overstate.
run_highstate set to false. Not running state.highstate.

$ vagrant ssh
$ sudo salt-call state.highstate
...
...
Summary
-------------
Succeeded: 11
Failed: 0
-------------
Total: 11
{% endcodeblock %}     
    
* 这里的虚拟机将我们创建的根目录`calamari-node`和虚拟机中的`/git`目录关联起来了，我们可以通过在查看这2个目录中的任意一个来查看安装文件是否已经生成。
  
{% codeblock lang:shell %}
# 查看`calamari-node`目录
$ cd calamari-node
$ ls -l
drwxr-xr-x  28 zhaozhiming  staff   952 Oct 20 16:16 Diamond
drwxr-xr-x  32 zhaozhiming  staff  1088 Oct 20 16:14 calamari
-rw-r--r--  1 zhaozhiming  staff  18883769 Oct 21 15:58 calamari-repo-precise.tar.gz
-rw-r--r--  1 zhaozhiming  staff  16417474 Oct 21 15:58 calamari-server_1.2.1-68-gfdeb0f7_amd64.deb
-rw-r--r--  1 zhaozhiming  staff    307478 Oct 21 15:58 diamond_3.4.67_all.deb

# 查看虚拟机的`/git`目录
$ cd calamari-node/calamari/vagrant/precise-build
$ vagrant ssh
$ cd /git
$ ls -l
drwxr-xr-x  28 zhaozhiming  staff   952 Oct 20 16:16 Diamond
drwxr-xr-x  32 zhaozhiming  staff  1088 Oct 20 16:14 calamari
-rw-r--r--  1 zhaozhiming  staff  18883769 Oct 21 15:58 calamari-repo-precise.tar.gz
-rw-r--r--  1 zhaozhiming  staff  16417474 Oct 21 15:58 calamari-server_1.2.1-68-gfdeb0f7_amd64.deb
-rw-r--r--  1 zhaozhiming  staff    307478 Oct 21 15:58 diamond_3.4.67_all.deb

# 从上面可以看到安装文件已经生成好了，2个deb文件分别是server和监控服务的安装文件，tar.gz文件是安装服务所需的依赖包安装文件集合，如果是连网安装的话，这个tar.gz文件不需要用到。  

{% endcodeblock %}   
  
### 生成client安装文件
  
* 下载calamari-client工程  

{% codeblock lang:shell %}
$ cd calamari-node
$ git clone https://github.com/ceph/calamari-clients.git
{% endcodeblock %}   
  
* 使用vagrant生成client安装文件

{% codeblock lang:shell %}
$ cd calamari-client/vagrant/precise-build/
$ vagrant up
...
...
Copying salt minion config to vm.
Checking if salt-minion is installed
salt-minion was not found.
Checking if salt-call is installed
salt-call was not found.
Bootstrapping Salt... (this may take a while)
Salt successfully configured and installed!
run_overstate set to false. Not running state.overstate.
run_highstate set to false. Not running state.highstate.

$ vagrant ssh
$ sudo salt-call state.highstate
...
...
Summary
-------------
Succeeded: 13
Failed: 0
-------------
Total: 13
{% endcodeblock %}   
  
* 查看生成的安装文件，可以看到有1个deb文件和一个tar.gz文件，ubuntu的话直接使用deb文件进行安装就可以了，tar.gz文件不需要。

{% codeblock lang:shell %}
$ cd calamari-node
$ ls -l
drwxr-xr-x  28 zhaozhiming  staff   952 Oct 20 16:16 Diamond
drwxr-xr-x  32 zhaozhiming  staff  1088 Oct 20 16:14 calamari
drwxr-xr-x  22 zhaozhiming  staff      748 Oct 20 16:46 calamari-clients
-rw-r--r--  1 zhaozhiming  staff  18883769 Oct 21 15:58 calamari-repo-precise.tar.gz
-rw-r--r--  1 zhaozhiming  staff  16417474 Oct 21 15:58 calamari-server_1.2.1-68-gfdeb0f7_amd64.deb
-rw-r--r--  1 zhaozhiming  staff    307478 Oct 21 15:58 diamond_3.4.67_all.deb
-rw-r--r--   1 zhaozhiming  staff  1711253 Oct 21 12:38 calamari-clients-build-output.tar.gz
-rw-r--r--   1 zhaozhiming  staff  1705364 Oct 21 12:38 calamari-clients_1.2.1.1-29-g3790c24_all.deb
{% endcodeblock %}   
  
## 安装Calamari
  
* 创建一个ubuntu的虚拟机来安装calamari，首先在根目录下创建一个Vagrantfile文件。
  
{% codeblock lang:shell %}
$ cd calamari-node
$ touch Vagrantfile
{% endcodeblock %}   
  
* Vagrantfile文件内容如下，注意要使用绑定好的IP。
  
{% codeblock lang:ruby %}
# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "precise64"

  config.vm.define "manager" do |manager|
    manager.vm.hostname = "manager-env"
    manager.vm.network :private_network, ip: "192.168.26.10"
    manager.vm.provider :virtualbox do |vb|
      vb.memory = 512
    end
  end
end
{% endcodeblock %}   
  
* 启动虚拟机并登陆
  
{% codeblock lang:shell %}
$ vagrant up
$ vagrant ssh 
{% endcodeblock %}   
  
* 在虚拟机上安装salt
  
{% codeblock lang:shell %}
$ sudo apt-get install python-software-properties
$ sudo add-apt-repository ppa:saltstack/salt
$ sudo apt-get update
$ sudo apt-get install salt-master
$ sudo apt-get install salt-minion
{% endcodeblock %}   
  
* 在虚拟机上安装所需依赖包
  
{% codeblock lang:shell %}
$ sudo apt-get update && sudo apt-get install -y apache2 libapache2-mod-wsgi libcairo2 supervisor python-cairo libpq5 postgresql
{% endcodeblock %}   
  
* 安装calamari
  
{% codeblock lang:shell %}
$ cd /vagrant
$ sudo dpkg -i calamari-server*.deb calamari-clients*.deb
{% endcodeblock %}   

* 初始化calamari服务，这里会要求你输入用户名、邮箱、密码，这个用户名密码是在浏览器访问calamari服务需要的。
  
{% codeblock lang:shell %}
$ sudo calamari-ctl initialize
[INFO] Loading configuration..
[INFO] Starting/enabling salt...
[INFO] Starting/enabling postgres...
[INFO] Initializing database...
[INFO] Initializing web interface...
[INFO] You will now be prompted for login details for the administrative user account. This is the account you will use to log into the web interface once setup is complete.
Username (leave blank to use 'root'):
Email address: karan.singh@csc.fi
Password:
Password (again):
Superuser created successfully.
[INFO] Starting/enabling services...
[INFO] Restarting services... - See more at: http://ceph.com/category/calamari/#sthash.qUtbU0mX.dpuf
{% endcodeblock %}   
  
* 登陆浏览器，输入虚拟机的ip(比如http://192.168.26.10)，可以看到如下页面。
  
{% img /images/post/2014-10/calamari-login.png %}  
  
* 输入刚才设置的用户名密码后，可以看到calamari提示你进行ceph集群配置。
  
{% img /images/post/2014-10/calamari-first.png %}  
  
## Ceph集群配置监控服务
  
### 配置ceph集群  

* 登陆其中一台ceph集群机器(这里假设ceph集群都是ubuntu环境)，安装监控服务。
  
{% codeblock lang:shell %}
$ sudo dpkg -i diamond_3.4.67_all.deb #deb文件是之前生成server安装文件时一起生成的，需要将其先考到ceph集群机器上
{% endcodeblock %}   
  
* 创建默认的监控配置文件
  
{% codeblock lang:shell %}
$ sudo mv /etc/diamond/diamond.conf.example /etc/diamond/diamond.conf
{% endcodeblock %}   
  
* 安装salt-minion服务
  
{% codeblock lang:shell %}
$ sudo apt-get install python-software-properties
$ sudo add-apt-repository ppa:saltstack/salt
$ sudo apt-get update
$ sudo apt-get install salt-minion
{% endcodeblock %}   
    
* 在`/etc/hosts`文件中增加calamari服务器的映射关系
  
{% codeblock /etc/hosts lang:shell %}
...
...
192.168.26.10 ceph-calamari
{% endcodeblock %}   
  
* 修改salt-minion的配置文件`/etc/salt/minion`，将master指向calamari服务器
  
{% codeblock /etc/salt/minion lang:shell %}
...
master: ceph-calamari
...
{% endcodeblock %}   
  
* 重启服务
  
{% codeblock lang:shell %}
$ sudo service salt-minion restart  
$ sudo service diamond restart
{% endcodeblock %}   
  
* 在所有的ceph集群机器上重复以上的步骤。  

### 在calamari服务上添加ceph集群机器
  
* 查看salt-key
  
{% codeblock lang:shell %}
$ sudo salt-key -L
Accepted Keys:
Unaccepted Keys:
ceph-mon0
ceph-mon1
ceph-mon2
ceph-osd0
ceph-osd1
ceph-osd2
Rejected Keys:
{% endcodeblock %}   
  
* 添加ceph集群机器到calamari
  
{% codeblock lang:shell %}
$ sudo salt-key -A
The following keys are going to be accepted:
Unaccepted Keys:
ceph-mon0
ceph-mon1
ceph-mon2
ceph-osd0
ceph-osd1
ceph-osd2
Proceed? [n/Y] y
Key for minion ceph-mon0 accepted.
Key for minion ceph-mon1 accepted.
Key for minion ceph-mon2 accepted.
Key for minion ceph-osd0 accepted.
Key for minion ceph-osd1 accepted.
Key for minion ceph-osd2 accepted.
{% endcodeblock %}   

* 再次查看salt-key，可以看到所有节点都已添加。
  
{% codeblock lang:shell %}
$ sudo salt-key -L
Accepted Keys:
ceph-mon0
ceph-mon1
ceph-mon2
ceph-osd0
ceph-osd1
ceph-osd2
Unaccepted Keys:
Rejected Keys:
{% endcodeblock %}   
      
* 在浏览器中再次登陆calamari服务，可以看到已经能监控ceph集群的信息。
  
{% img /images/post/2014-10/calamari.png %}  
  
{% img /images/post/2014-10/calamari-page1.png %}  
  
{% img /images/post/2014-10/calamari-page2.png %}  
  
{% img /images/post/2014-10/calamari-page3.png %}  
  
## 参考资料

文档1：[http://calamari.readthedocs.org/en/latest/operations/index.html][calamari-doc1]
文档2：[http://ceph.com/category/calamari/][calamari-doc2]


[ceph]: http://ceph.com/
[django]: https://www.djangoproject.com/
[nodejs]: http://nodejs.org/
[angularjs]: https://angularjs.org/
[bootstrap]: http://getbootstrap.com/
[vagrant_blog]: http://zhaozhiming.github.io/blog/2014/10/02/ceph-install-with-vagrant-and-ansible
[vagrant_box]: https://vagrantcloud.com/discover/featured
[calamari-doc1]: http://calamari.readthedocs.org/en/latest/operations/index.html
[calamari-doc2]: http://calamari.readthedocs.org/en/latest/operations/index.html