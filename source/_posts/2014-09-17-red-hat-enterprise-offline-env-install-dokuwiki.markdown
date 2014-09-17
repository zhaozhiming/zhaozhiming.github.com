---
layout: post
title: "Redhat企业版无网环境下安装Dokuwiki"
date: 2014-09-17 12:23
description: Redhat企业版无网环境下安装Dokuwiki
keywords: dokuwiki,redhat,offline
comments: true
categories: code
tags: [dokuwiki,redhat,offline]
---
  
{% img /images/post/2014-9/dokuwiki.png %}  
  
## [Dokuwiki][dokuwiki]介绍
Dokuwiki是一个轻量级，高可用，免费开源的wiki软件，它不需要DB数据库，wiki内容直接以文本文件形式存储在文件系统上。Dokuwiki语法简洁易懂，管理维护简单，支持扩展多种模板展示wiki网站，还可以使用很多开源的插件来增加wiki的功能，比一般的传统wiki更加强大，更容易使用。  
  
<!--more-->
## 环境与版本
  
* 操作系统: Redhat Enterprise Linux Server release 6.4 (Santiago)
* 网络环境: 无法连接网络
* Dokuwiki版本: 最新版
* Apache Http Server版本: 2.4.10
* PHP版本: 5.6.0
  
下面是Dokuwiki的系统要求:  

{% blockquote %}
DokuWiki System Requirements
1. Webserver supporting PHP
2. PHP version 5.2 or later
{% endblockquote %}
  
## 安装Apache Http Server(httpd)
没有网络安装Linux软件是比较苦逼的，需要先安装该软件依赖的软件，如果依赖层次较深，就需要先安装完很多依赖软件后才能安装该软件。如果有网络的情况下，直接使用`yum install`或`apt-get install`就可以把相关依赖的软件都一起安装了。  
  
先看一下httpd的安装要求:  
  
* APR和APR-Util
  
先确定系统已经安装了这2个软件，如果没有的话先到[Apache APR][apache-apr]下载源码包，下载完成后分别解压到httpd的`scrlib/apr`和`srclib/apr-util`(`srclib`在httpd的压缩文件解压后的目录里面)，解压后的目录结构如下。后面在安装httpd的时候使用`./configure`命令时加上`--with-included-apr`就可以了。  
  
{% codeblock lang:sh %}
httpd-2.4.10
--|scrlib
----|apr
------|apr.exp
------|...other apr files
----|apr-util
------|aprutil.dep
------|...other apr-util files
{% endcodeblock %}   
  
* Perl-Compatible Regular Expressions Library ([PCRE][pcre])
PCRE安装比较简单，去pcre的网站下载源码包后解压，cd到解压目录执行下面的命令。  
  
{% codeblock lang:sh %}
./configure
make
make install
{% endcodeblock %}   
  
安装完依赖的软件后，就可以开始安装httpd了，到Apache的网站下载最新版的[Apache Http Server软件][apache_http_server]，执行以下命令，最后一步是启动httpd服务，如果安装成功的话在浏览器输入`http://127.0.0.1`可以看到`It Works!`的字样。  
  
{% codeblock lang:sh %}
./configure --prefix=/usr/local/apache2 --with-included-apr
make
make install
/usr/local/apache2/bin/apachectl -k start
{% endcodeblock %}   
  
## 安装[PHP][php]
安装PHP也需要安装其他软件，需要先安装[libxml2][libxml2]，先到网站下载源码包，然后执行下面的命令安装。  
  
{% codeblock lang:sh %}
./configure --prefix=/usr/local/libxml2
make
make install
{% endcodeblock %}   
  
安装完libxml2后，到PHP网站下载最新版的源码，然后执行下面的命令安装，注意configure要带--with-apx2参数，指向apache2的apx2命令。  
  
{% codeblock lang:sh %}
./configure --prefix=/usr/local/php --with-apxs2=/usr/local/apache2/bin/apxs --with-libxml-dir=/usr/local/libxml2
make
make install
{% endcodeblock %}   
  
编辑httpd的配置文件即/usr/local/apache2/conf/httpd.conf，并添加以下内容:
  
{% codeblock lang:sh %}
AddType application/x-httpd-php .php
AddType application/x-httpd-php-source .phps
{% endcodeblock %}   
  
复制php.ini文件到PHP的安装目录，源码包里有2个php.ini文件，随便哪一个都可以。
  
{% codeblock lang:sh %}
cp ~/tools/php-5.6.0/php.ini-production /usr/local/php/php.ini
{% endcodeblock %}   
  
重启httpd。
   
{% codeblock lang:sh %}
/usr/local/apache2/bin/apachectl -k restart
{% endcodeblock %}   
   
建立test.php文件放在httpd目录(/usr/local/apache2/)下的htdocs下，内容如下，通过浏览器查看`http://127.0.0.1/test.php`，如果显示了内容就表示PHP安装成功了。  
  
{% codeblock lang:php %}
<?php
phpinfo();
?>
{% endcodeblock %}   
  
## 安装dokuwiki
到dokuwiki网站下载源码包，执行下面的命令进行安装。
  
{% codeblock lang:sh %}
mv 下载目录/dokuwiki-xxxx.tgz /usr/local/apache2/htdocs
cd /usr/local/apache2/htdocs
tar -xvf dokuwiki-xxxx.tgz
mv dokuwiki-xxxx dokuwiki
{% endcodeblock %}   
  
查询httpd的用户是什么，这样才可以将dokuwiki的文件夹授权给这个用户，执行下面命令可以看到httpd进程的用户，我查到的是daemon。
  
{% codeblock lang:sh %}
lsof -i | grep :http
chown -R daemon:daemon dokuwiki
{% endcodeblock %}   
  
在浏览器中输入`http://127.0.0.1/dokuwiki/install.php`可以看到安装向导页面，根据向导安装dokuwiki即可。

## ubuntu有网络情况下安装dokuwiki
看完一大篇没有网络的安装后，再来看有网络的情况下安装是多么的简单。  
  
安装系统是Ubuntu 14.04 LTS，执行完下面的命令，dokuwiki就安装完成了。 
  
{% codeblock lang:sh %}
sudo apt-get update
sudo apt-get install apache2
sudo apt-get install php5
cd /var/www
sudo cp ~/Download/dokuwiki-xxx.tgz .
sudo tar -zxvf dokuwiki-xxxx.tgz
sudo mv dokuwiki-xxxx dokuwiki
sudo chown -R www-data:www-data dokuwiki
{% endcodeblock %}   
  

[dokuwiki]: https://www.dokuwiki.org
[apache_http_server]: http://httpd.apache.org/download.cgi
[apache-apr]: http://apr.apache.org
[pcre]: http://www.pcre.org
[php]: http://php.net
[libxml2]: http://xmlsoft.org

