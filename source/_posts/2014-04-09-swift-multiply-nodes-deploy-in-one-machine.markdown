---
layout: post
title: "swift单节点多dev环境部署"
date: 2014-04-09 20:45
description: 
keywords: swift
comments: true
categories: code
tags: swift
---

## 环境目标
* 同一台机器
* 1台proxy server
* 5个storage node
* 每个storage node有2个dev

<!--more-->  

安装过程可以按照[swift all in one][url1]文档进行搭建，在操作过程中需要修改以下的地方。 

## Using a loopback device for storage 
1. Create the file for the loopback device:  
`(这里的标题和编号是与saio的保持一致，这样方便大家按照saio的编号进行修改，后面的步骤与此相同，不再做说明)`
  
修改前：  
{% codeblock lang:bash %}
sudo truncate -s 1GB /srv/swift-disk
{% endcodeblock %} 
  
修改后：  
{% codeblock lang:bash %}
sudo truncate -s 500GB /srv/swift-disk
{% endcodeblock %}
  
将xfs文件系统的大小改为500GB，原来的1GB太小不适合做测试。  
  
3. Create the mount point and the individualized links:   
将原来的脚本修改为：  
{% codeblock lang:bash %}
sudo mkdir /mnt/sdb1
sudo mount /mnt/sdb1
sudo mkdir /mnt/sdb1/1 /mnt/sdb1/2 /mnt/sdb1/3 /mnt/sdb1/4 /mnt/sdb1/5
sudo chown ${USER}:${USER} /mnt/sdb1/*
for x in {1..5}; do sudo ln -s /mnt/sdb1/$x /srv/$x; done
sudo mkdir -p /srv/1/node/dev1 /srv/1/node/dev2 /srv/2/node/dev3 /srv/2/node/dev4 /srv/3/node/dev5 /srv/3/node/dev6 /srv/4/node/dev7 /srv/4/node/dev8 /srv/5/node/dev9 /srv/5/node/dev10 /var/run/swift
sudo chown -R ${USER}:${USER} /var/run/swift
# **Make sure to include the trailing slash after /srv/$x/**
for x in {1..5}; do sudo chown -R ${USER}:${USER} /srv/$x/; done
{% endcodeblock %} 
  
创建属于5个节点的文件夹，在每个节点文件夹下创建2个dev文件夹，表示1个节点有2个dev，其中region1有6台dev(1~6)，region2有4台dev(7~10)。  
  
## Common Post-Device Setup
Add the following lines to /etc/rc.local (before the exit 0):   
修改前：  
{% codeblock lang:bash %}
mkdir -p /var/cache/swift /var/cache/swift2 /var/cache/swift3 /var/cache/swift4
{% endcodeblock %} 
  
修改后：  
{% codeblock lang:bash %}
mkdir -p /var/cache/swift /var/cache/swift2 /var/cache/swift3 /var/cache/swift4 /var/cache/swift5
{% endcodeblock %}
因为有5个节点，所以增加了1个新节点的缓存文件夹。  
  
## Setting up rsync
Here is the default rsyncd.conf file contents maintained in the repo that is copied and fixed up above:  
  
在/etc/rsyncd.conf文件追加以下内容：  
  
{% codeblock lang:bash %}
[account6052]
max connections = 25
path = /srv/5/node/
read only = false
lock file = /var/lock/account6052.lock

[container6051]
max connections = 25
path = /srv/5/node/
read only = false
lock file = /var/lock/container6051.lock

[object6050]
max connections = 25
path = /srv/5/node/
read only = false
lock file = /var/lock/object6050.lock
{% endcodeblock %}
  
增加了新节点的account, container, object服务的同步配置。  
  
You should see the following output from the above command: 
  
{% codeblock lang:bash %}
account6012
account6022
account6032
account6042
account6052
container6011
container6021
container6031
container6041
container6051
object6010
object6020
object6030
object6040
object6050
{% endcodeblock %}
  
验证rsync可以看到新增的account, container, object信息。  
  
## Optional: Setting up rsyslog for individual logging
  
将/etc/rsyslog.d/10-swift.conf文件内容修改为：  
{% codeblock lang:bash %}
# Uncomment the following to have a log containing all logs together
#local1,local2,local3,local4,local5.*   /var/log/swift/all.log

# Uncomment the following to have hourly proxy logs for stats processing
#$template HourlyProxyLog,"/var/log/swift/hourly/%$YEAR%%$MONTH%%$DAY%%$HOUR%"
#local1.*;local1.!notice ?HourlyProxyLog

local1.*;local1.!notice /var/log/swift/proxy.log
local1.notice           /var/log/swift/proxy.error
local1.*                ~ 

local2.*;local2.!notice /var/log/swift/storage1.log
local2.notice           /var/log/swift/storage1.error
local2.*                ~

local3.*;local3.!notice /var/log/swift/storage2.log
local3.notice           /var/log/swift/storage2.error
local3.*                ~

local4.*;local4.!notice /var/log/swift/storage3.log
local4.notice           /var/log/swift/storage3.error
local4.*                ~

local5.*;local5.!notice /var/log/swift/storage4.log
local5.notice           /var/log/swift/storage4.error
local5.*                ~

local6.*;local6.!notice /var/log/swift/storage5.log
local6.notice           /var/log/swift/storage5.error
local6.*                ~

local7.*;local7.!notice /var/log/swift/expirer.log
local7.notice           /var/log/swift/expirer.error
local7.*                ~
{% endcodeblock %}
  
修改日志配置，将原来的local6指向storage node 5, 原来的expirer用local7来记录日志（`注意：后面在修改各个节点的服务配置文件时需要知道这些日志配置信息`）。 
  
## Configuring each node
  
3. /etc/swift/object-expirer.conf 
  
修改前：  
{% codeblock lang:bash %}
log_facility = LOG_LOCAL6     
{% endcodeblock %}

修改后：  
{% codeblock lang:bash %}
log_facility = LOG_LOCAL7
{% endcodeblock %}

* 新增account5的配置文件 /etc/swift/account-server/5.conf：  
  
{% codeblock lang:bash %}
[DEFAULT]
devices = /srv/5/node
mount_check = false
disable_fallocate = true
bind_port = 6052
workers = 1
user = swift
log_facility = LOG_LOCAL6
recon_cache_path = /var/cache/swift5
eventlet_debug = true

[pipeline:main]
pipeline = recon account-server

[app:account-server]
use = egg:swift#account

[filter:recon]
use = egg:swift#recon

[account-replicator]
vm_test_mode = yes

[account-auditor]

[account-reaper]
{% endcodeblock %}
  
* 新增container5的配置文件/etc/swift/container-server/5.conf：  
  
{% codeblock lang:bash %}
[DEFAULT]
devices = /srv/5/node
mount_check = false
disable_fallocate = true
bind_port = 6051
workers = 1
user = swift
log_facility = LOG_LOCAL6
recon_cache_path = /var/cache/swift5
eventlet_debug = true
allow_versions = true

[pipeline:main]
pipeline = recon container-server

[app:container-server]
use = egg:swift#container

[filter:recon]
use = egg:swift#recon

[container-replicator]
vm_test_mode = yes

[container-updater]

[container-auditor]

[container-sync]
{% endcodeblock %}
  
* 新增object5的配置文件/etc/swift/object-server/5.conf：  
  
{% codeblock lang:bash %}
[DEFAULT]
devices = /srv/5/node
mount_check = false
disable_fallocate = true
bind_port = 6050
workers = 1
user = swift
log_facility = LOG_LOCAL6
recon_cache_path = /var/cache/swift5
eventlet_debug = true

[pipeline:main]
pipeline = recon object-server

[app:object-server]
use = egg:swift#object

[filter:recon]
use = egg:swift#recon

[object-replicator]
vm_test_mode = yes

[object-updater]

[object-auditor]
{% endcodeblock %}
  
## Setting up scripts for running Swift
  
7. Construct the initial rings using the provided script:   
  
先修改bin/remakerings文件，在执行remakerings命令：  
  
{% codeblock lang:bash %}
#!/bin/bash

cd /etc/swift

rm -f *.builder *.ring.gz backups/*.builder backups/*.ring.gz

swift-ring-builder object.builder create 19 6 1
swift-ring-builder object.builder add r1z1-127.0.0.1:6010/dev1 1
swift-ring-builder object.builder add r1z1-127.0.0.1:6010/dev2 1
swift-ring-builder object.builder add r1z2-127.0.0.1:6020/dev3 1
swift-ring-builder object.builder add r1z2-127.0.0.1:6020/dev4 1
swift-ring-builder object.builder add r1z3-127.0.0.1:6030/dev5 1
swift-ring-builder object.builder add r1z3-127.0.0.1:6030/dev6 1
swift-ring-builder object.builder add r2z1-127.0.0.1:6040/dev7 1
swift-ring-builder object.builder add r2z1-127.0.0.1:6040/dev8 1
swift-ring-builder object.builder add r2z2-127.0.0.1:6050/dev9 1
swift-ring-builder object.builder add r2z2-127.0.0.1:6050/dev10 1
swift-ring-builder object.builder rebalance
swift-ring-builder container.builder create 19 6 1
swift-ring-builder container.builder add r1z1-127.0.0.1:6011/dev1 1
swift-ring-builder container.builder add r1z1-127.0.0.1:6011/dev2 1
swift-ring-builder container.builder add r1z2-127.0.0.1:6021/dev3 1
swift-ring-builder container.builder add r1z2-127.0.0.1:6021/dev4 1
swift-ring-builder container.builder add r1z3-127.0.0.1:6031/dev5 1
swift-ring-builder container.builder add r1z3-127.0.0.1:6031/dev6 1
swift-ring-builder container.builder add r2z1-127.0.0.1:6041/dev7 1
swift-ring-builder container.builder add r2z1-127.0.0.1:6041/dev8 1
swift-ring-builder container.builder add r2z2-127.0.0.1:6051/dev9 1
swift-ring-builder container.builder add r2z2-127.0.0.1:6051/dev10 1
swift-ring-builder container.builder rebalance
swift-ring-builder account.builder create 19 6 1
swift-ring-builder account.builder add r1z1-127.0.0.1:6012/dev1 1
swift-ring-builder account.builder add r1z1-127.0.0.1:6012/dev2 1
swift-ring-builder account.builder add r1z2-127.0.0.1:6022/dev3 1
swift-ring-builder account.builder add r1z2-127.0.0.1:6022/dev4 1
swift-ring-builder account.builder add r1z3-127.0.0.1:6032/dev5 1
swift-ring-builder account.builder add r1z3-127.0.0.1:6032/dev6 1
swift-ring-builder account.builder add r2z1-127.0.0.1:6042/dev7 1
swift-ring-builder account.builder add r2z1-127.0.0.1:6042/dev8 1
swift-ring-builder account.builder add r2z2-127.0.0.1:6052/dev9 1
swift-ring-builder account.builder add r2z2-127.0.0.1:6052/dev10 1
swift-ring-builder account.builder rebalance
{% endcodeblock %}
  
新ring环有2的19次方，6个副本，修改后重新生成ring环即可。

[url1]: http://docs.openstack.org/developer/swift/development_saio.html
