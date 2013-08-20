---
layout: post
title: "安装win7和ubuntu双系统"
date: 2012-04-16 16:53
comments: true
categories: code
tags: [windows, ubuntu]
---

最近买了新的笔记本电脑，发现新买的电脑上面安装的是win7用户版，在网上查了一下这个版本的win7是功能最少的。。。另外又发现偌大的500G硬盘居然只给分成2个区，每个250。。。各种不爽，于是决定格式化硬盘重新安装系统。  

**1.安装win7**  
  
在网上找了下win7的iso映像，有各种版本，说是旗舰版的win7功能是比较多比较好的。另外目前的cpu都是64位的，所以我打算安装64位的系统，64位系统和32位最大区别是可以支持4G以上的内存，刚好我买电脑的时候加了根内存条，现在总内存是6G，所以64位旗舰版的win7最满足我的需求了：）  
  
我下载的是青苹果的一个iso镜像，下载下来之后刻录成启动光盘，然后用光盘启动系统，发现里面只有winPE选项有用，其他选项都是废的。。。进入PE后，用里面的工具重新分了下区，然后用ghost安装了win7。这个版本的镜像里面没有offic软件，没办法只好上网找了office2007，找到一个石油学院的office iso镜像，兼容win7 64位，又免序列号的。安装成功之后将C盘系统用ghost克隆了一个备份保存，win7安装完成。  

**2.安装ubuntu**  

开始我安装的是ubuntu11，用的是以前的安装盘，这里纠结了一下是用wubi装还是正规安装，网上说是wubi安装属体验系统，功能可能不全，最后用ubuntu的启动光盘正常安装了ubuntu11。  
  
安装ubuntu时，开始有3个选项，一是跟win7一起安装，一个是替代wiin7，一个是自定义。看了网上攻略我选择了自定义，选择自定义时要自己划分ubuntu分区。我划分了3个，一个是主分区，这个分区相当与windows的系统C盘，大小10G，挂载点“/”。第二个是交换空间，相当是win7的虚拟内存空间，我划分了4G。最后一个是home区，这个相当与windows系统的其他分区（比如D盘，E盘。。。)，我划分了20个G，挂载点是“/home"。  

安装完后重启系统，就可以看到ubuntu的启动引导菜单了。如果你想将win7放在菜单首位，可以进入ubuntu系统修改/boot/grub/grub.cfg文件，将里面的win7菜单移到ubuntu菜单前面就可以了。  
  
**3.重装ubuntu**  

后来发现ubuntu11上网网速极慢，查了很多都不知道是哪里出问题，就重新下载了ubuntu12，安装过程与上面一样。安装完成之后重启电脑发现ubuntu引导目录不见了，要重新做ubuntu的菜单引导。  
  
先用ubuntu的启动光盘进入试用版ubuntu，然后打开终端，输入下面命令查询ubuntu的安装区，我的是sda9。  
  
{% codeblock lang:sh %}
sudo fdisk -l
{% endcodeblock %}    

然后创建了一个临时目录，这个目录是为了后面mount ubuntu启动分区用的，命令如下：  
  
{% codeblock lang:sh %}
mkdir /media/tmpdir
mount  /dev/sda8  /media/tmpdir
{% endcodeblock %}    

接下来是最重要的一步，输入命令下面命令：  
  
{% codeblock lang:sh %}
sudo grub-install --root-directory=/media/tmpdir /dev/sda 
{% endcodeblock %}    
  
注意，这里就是sda，后面不要写成sda9，如果显示no error report，则表示基本成功了。然后保存重启系统，会发现ubuntu的菜单已经有了，但是看起来比较乱，版本信息也不是安装的系统版本信息，所以接下来进入ubuntu系统，打开终端，输入：  
  
{% codeblock lang:sh %}
sudo update-grub2
{% endcodeblock %}    

完了之后再次重启系统就会发现ubuntu正常的启动引导菜单了。如果想把win放在前面，就照之前说的方法改下ubuntu的配置文件就OK了。  
  
就这样折腾了一个星期，最后终于把系统都装好了，好累。。。不过也学到了很多系统知识。下次再装系统就轻车熟路了：）  
