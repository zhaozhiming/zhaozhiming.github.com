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
{% img /images/post/2014-8/intellijidea_android.gif %}  

一、Rados gateway 创建 keyring，使用一台管理节点的机器来生成keyring文件。
（PS:管理节点是使用ceph-deploy才有的机器，我理解没有管理节点的话，使用mon或osd的机器可以创建keyring）
1、创建文件并增加权限
sudo ceph-authtool --create-keyring /etc/ceph/ceph.client.radosgw.keyring
sudo chmod +r /etc/ceph/ceph.client.radosgw.keyring
 
2、使用ceph-authtool在keyring文件中生成随机密码
sudo ceph-authtool /etc/ceph/ceph.client.radosgw.keyring -n client.radosgw.gateway --gen-key
 
3、在keyring中增加存储集群的操作权限；
sudo ceph-authtool -n client.radosgw.gateway --cap osd 'allow rwx' --cap mon 'allow rwx' /etc/ceph/ceph.client.radosgw.keyring
 
4、将gateway的key添加到存储集群（-k不知道是什么参数）
Once you have created a keyring and key to enable the Ceph Object Gateway with access to the Ceph Storage Cluster, add the key to your Ceph Storage Cluster. For example:
sudo ceph -k /etc/ceph/ceph.client.admin.keyring auth add client.radosgw.gateway -i /etc/ceph/ceph.client.radosgw.keyring
 
5、将生产的keyring文件上传到gateway的机器
sudo scp /etc/ceph/ceph.client.radosgw.keyring  ceph@{hostname}:/home/ceph
ssh {hostname}
sudo mv ceph.client.radosgw.keyring /etc/ceph/ceph.client.radosgw.keyring
 
6、在ceph.conf中配置gateway的keyring文件路径
[client.radosgw.{instance-name}]
host = {host-name}
keyring = /etc/ceph/ceph.client.radosgw.keyring
rgw socket path = /var/run/ceph/ceph.radosgw.{instance-name}.fastcgi.sock
log file = /var/log/ceph/client.radosgw.{instance-name}.log
 
 
二、cephx认证原理
为识别用户和防范攻击，ceph提供了cephx来进行用户和后台进程认证。
 
Cephx使用共享密钥的方式进行认证，意思是客户端和monitor集群都有一份客户端的密钥。它提供了一个相互认证的机制，意味着集群确定用户拥有密钥，而用户确定集群拥有密钥的备份。
 
Ceph不提供统一的认证接口给对象存储，所以客户端必须直接跟OSD交互。为了保护数据，ceph提供了cephx认证系统，用来对用户在客户端的操作进行认证。Cephx认证协议与Kerberos相类似。
 
用户通过客户端与monitor进行交互。跟Kerberos不同，每个monitor都可以进行认证，所以没有单点和性能瓶颈的问题。Monitor返回一个类似Kerberos的数据结构，包含了一个session key来访问ceph服务。Session key是通过加密用户自己的密钥来生成的，所以只有该用户能请求monitor服务。然后客户端使用session key想monitor发起请求，然后monitor提供给客户端一个ticket来使客户端和OSD进行认证。Monitor和OSD共享密钥，所以客户端可以使用Monitor提供的ticket来和OSD进行交互。跟Kerberos一样，cephx的ticket会过期，所以攻击者不能使用过期的ticket或session key来做不正当的事情。这种认证形式将阻止攻击者通过修改用户已泄露信息的方式，或者伪造消息进行通讯访问的方式来进行攻击，只要用户的密钥不要在失效前泄露就没有什么问题。
 
使用cephx时，管理员需要先设置user。在下面的图表中，client.admin用户调用ceph auth get-or-create-key的命令来生成用户名和密钥，ceph的auth子系统来生成用户名和密钥，保存一份密钥在monitor并将用户的密钥传回给client.admin用户。这使得客户端和monitor共享了一份密钥。
 
<image001.png>

为了能通过monitor的认证，客户端将用户名传给monitor，然后monitor产生一个session key 
 
 To authenticate with the monitor, the client passes in the user name to the monitor, and the monitor generates a session key and encrypts it with the secret key associated to the user name. Then, the monitor transmits the encrypted ticket back to the client. The client then decrypts the payload with the shared secret key to retrieve the session key. The session key identifies the user for the current session. The client then requests a ticket on behalf of the user signed by the session key. The monitor generates a ticket, encrypts it with the user’s secret key and transmits it back to the client. The client decrypts the ticket and uses it to sign requests to OSDs and metadata servers throughout the cluster.
 
<image002.png>

The cephx protocol authenticates ongoing communications between the client machine and the Ceph servers. Each message sent between a client and server, subsequent to the initial authentication, is signed using a ticket that the monitors, OSDs and metadata servers can verify with their shared secret.

<image003.png>

The protection offered by this authentication is between the Ceph client and the Ceph server hosts. The authentication is not extended beyond the Ceph client. If the user accesses the Ceph client from a remote host, Ceph authentication is not applied to the connection between the user’s host and the client host.

For configuration details, see Cephx Config Guide. For user management details, see User Management.


 
1、生成存储集群的keyring
         1）生成client.admin的key
ceph auth get-or-create client.admin mon 'allow *' mds 'allow *' osd 'allow *' -o /etc/ceph/ceph.client.admin.keyring
         注意：这个操作会覆盖原有的ceph.client.admin.keyring文件，请谨慎操作。
 
         2）创建monitor的keyring
ceph-authtool --create-keyring /tmp/ceph.mon.keyring --gen-key -n mon. --cap mon 'allow *'
 
         3）复制monitor的keyring文件到每个monitor的data目录
cp /tmp/ceph.mon.keyring /var/lib/ceph/mon/ceph-a/keyring
 
         4）为每个OSD产生keyring，id是OSD的编号
         ceph auth get-or-create osd.{$id} mon 'allow rwx' osd 'allow *' -o /var/lib/ceph/osd/ceph-{$id}/keyring
 
         5）为每个MDS产生keyring，id是MDS编号
ceph auth get-or-create mds.{$id} mon 'allow rwx' osd 'allow *' mds 'allow *' -o /var/lib/ceph/mds/ceph-{$id}/keyring
 
         6）在ceph.conf的global中增加认证开关
auth cluster required = cephx
auth service required = cephx
auth client required = cephx
 
         7）重启存储服务
        
2、认证配置
         1）auth cluster required：[cephx | none]
         如果打开，表示存储集群（mon,osd,mds）相互之间需要通过keyring认证。
 
         2）auth service required：[cephx | none]
         如果打开，表示客户端（比如gateway）到存储集群（mon,osd,mds）需要通过keyring认证。
 
         3）auth client required：[cephx | none]
         如果打开，表示存储集群（mon,osd,mds）到客户端（比如gateway）需要通过keyring认证。
 


<!--more-->
