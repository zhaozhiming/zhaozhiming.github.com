---
layout: post
title: "Ceph认证原理"
date: 2014-09-13 09:11
description: Ceph认证原理
keywords: ceph,auth
comments: true
categories: code
tags: [ceph,auth]
---
  
{% img /images/post/2014-9/rados-arch.png %}  
  
为识别用户和防范攻击，ceph提供了cephx来进行用户和后台进程认证。  
<!--more-->

## 原理介绍

Cephx使用共享密钥的方式进行认证，意思是客户端和monitor集群都有一份客户端的密钥。它提供了一个相互认证的机制，意味着集群确定用户拥有密钥，而用户确定集群拥有密钥的备份。  
  
Ceph不提供统一的认证接口给对象存储，所以客户端必须直接跟OSD交互。为了保护数据，ceph提供了cephx认证系统，用来对用户在客户端的操作进行认证。Cephx认证协议与Kerberos相类似。  
  
用户通过客户端与monitor进行交互。跟Kerberos不同，每个monitor都可以进行认证，所以没有单点和性能瓶颈的问题。Monitor返回一个类似Kerberos的数据结构，包含了一个session key来访问ceph服务。Session key是通过加密用户自己的密钥来生成的，所以只有该用户能请求monitor服务。然后客户端使用session key想monitor发起请求，然后monitor提供给客户端一个ticket来使客户端和OSD进行认证。Monitor和OSD共享密钥，所以客户端可以使用Monitor提供的ticket来和OSD进行交互。跟Kerberos一样，cephx的ticket会过期，所以攻击者不能使用过期的ticket或session key来做不正当的事情。这种认证形式将阻止攻击者通过修改用户已泄露信息的方式，或者伪造消息进行通讯访问的方式来进行攻击，只要用户的密钥不要在失效前泄露就没有什么问题。  
  
使用cephx时，管理员需要先设置user。在下面的图表中，client.admin用户调用ceph auth get-or-create-key的命令来生成用户名和密钥，ceph的auth子系统来生成用户名和密钥，保存一份密钥在monitor并将用户的密钥传回给client.admin用户。这使得客户端和monitor共享了一份密钥。  
  
{% img /images/post/2014-9/rados-auth-1.png %}  
  
为了能通过monitor的认证，客户端将用户名传给monitor，然后monitor产生一个session key并且通过密钥将其加密，并使之与用户名关联。然后monitor将加密session key回传给客户端，客户端再通过共享密钥进行解密，从而获取到seesion key。session key标示当前用户的当前回话。然后客户端再发起请求要求一个代表用户会话密钥的ticket，monitor产生ticket并通过用户密钥进行加密，并将其回传给客户端。客户端对ticket进行解密，以后客户端向OSD或MDS发起请求时，就用它来对请求进行签名。
  
{% img /images/post/2014-9/rados-auth-2.png %}  
  
cephx协议验证客户端和Ceph服务器之间的通信。在初始化认证之后，在客户端和服务器间的每一条信息，都会使用ticket进行签名，这样monitor，OSD和MDS服务就可以使用他们的共享密钥进行验证。
  
{% img /images/post/2014-9/rados-auth-3.png %}  
  
认证提供的保护是在ceph客户端和服务器之间。认证不能超过客户端，比如用户从一台远程服务器上访问cpeh客户端，ceph的认证就不能适用于远程主机和客户端了。  
  
## 生成存储集群的keyring  

* 生成client.admin的key

{% codeblock lang:sh %}
ceph auth get-or-create client.admin mon 'allow *' mds 'allow *' osd 'allow *' -o /etc/ceph/ceph.client.admin.keyring
{% endcodeblock %}   
  
注意：这个操作会覆盖原有的ceph.client.admin.keyring文件，请谨慎操作。
 
* 创建monitor的keyring
  
{% codeblock lang:sh %}
ceph-authtool --create-keyring /tmp/ceph.mon.keyring --gen-key -n mon. --cap mon 'allow *'
{% endcodeblock %}   
  
* 复制monitor的keyring文件到每个monitor的data目录
  
{% codeblock lang:sh %}
cp /tmp/ceph.mon.keyring /var/lib/ceph/mon/ceph-a/keyring
{% endcodeblock %}   
  
* 为每个OSD产生keyring，id是OSD的编号
  
{% codeblock lang:sh %}
ceph auth get-or-create osd.{$id} mon 'allow rwx' osd 'allow *' -o /var/lib/ceph/osd/ceph-{$id}/keyring
{% endcodeblock %}   
  
* 为每个MDS产生keyring，id是MDS编号
  
{% codeblock lang:sh %}
ceph auth get-or-create mds.{$id} mon 'allow rwx' osd 'allow *' mds 'allow *' -o /var/lib/ceph/mds/ceph-{$id}/keyring
{% endcodeblock %}   
  
* 在ceph.conf的global中增加认证开关
  
{% codeblock lang:properties %}
auth cluster required = cephx
auth service required = cephx
auth client required = cephx
{% endcodeblock %}   
  
* 重启存储服务
        
## 认证配置
  
* auth cluster required：[cephx | none]:如果打开，表示存储集群（mon,osd,mds）相互之间需要通过keyring认证。
* auth service required：[cephx | none]:如果打开，表示客户端（比如gateway）到存储集群（mon,osd,mds）需要通过keyring认证。
* auth client required：[cephx | none]:如果打开，表示存储集群（mon,osd,mds）到客户端（比如gateway）需要通过keyring认证。
  
## Rados gateway创建keyring
需要使用一台管理节点的机器来生成keyring文件，管理节点是使用ceph-deploy才有的机器，我理解没有管理节点的话，使用mon或osd的机器可以创建keyring。  

* 创建文件并增加权限
  
{% codeblock lang:sh %}
sudo ceph-authtool --create-keyring /etc/ceph/ceph.client.radosgw.keyring
sudo chmod +r /etc/ceph/ceph.client.radosgw.keyring
{% endcodeblock %}   
  
* 使用ceph-authtool在keyring文件中生成随机密码 
  
{% codeblock lang:sh %}
sudo ceph-authtool /etc/ceph/ceph.client.radosgw.keyring -n client.radosgw.gateway --gen-key
{% endcodeblock %}   
  
* 在keyring中增加存储集群的操作权限；

{% codeblock lang:sh %}
sudo ceph-authtool -n client.radosgw.gateway --cap osd 'allow rwx' --cap mon 'allow rwx' /etc/ceph/ceph.client.radosgw.keyring
{% endcodeblock %}   
  
* 将gateway的key添加到存储集群（-k不知道是什么参数）
  
{% codeblock lang:sh %}
sudo ceph -k /etc/ceph/ceph.client.admin.keyring auth add client.radosgw.gateway -i /etc/ceph/ceph.client.radosgw.keyring
{% endcodeblock %}   
  
* 将生成的keyring文件上传到gateway的机器
  
{% codeblock lang:sh %}
sudo scp /etc/ceph/ceph.client.radosgw.keyring  ceph@{hostname}:/home/ceph
ssh {hostname}
sudo mv ceph.client.radosgw.keyring /etc/ceph/ceph.client.radosgw.keyring
{% endcodeblock %}   
  
* 在ceph.conf中配置gateway的keyring文件路径
  
{% codeblock lang:properties %}
[client.radosgw.{instance-name}]
host = {host-name}
keyring = /etc/ceph/ceph.client.radosgw.keyring
rgw socket path = /var/run/ceph/ceph.radosgw.{instance-name}.fastcgi.sock
log file = /var/log/ceph/client.radosgw.{instance-name}.log
{% endcodeblock %}   

