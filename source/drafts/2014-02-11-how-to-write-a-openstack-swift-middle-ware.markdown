---
layout: post
title: "openstack swift中间件编写"
date: 2014-02-11 20:33
published: false
description: 
keywords: openstack,swift,middleware
comments: true
categories: code
tags: [openstack, swift, middleware]
---

{% img https://s.yunio.com/Orkc2v %}

[OpenStack][url1]是一个美国国家航空航天局和Rackspace合作研发的云端运算‎软件，以Apache许可证授权，并且是一个自由软件和开放源代码项目。Swift 最初是由 Rackspace 公司开发的高可用分布式对象存储服务，并于 2010 年贡献给 OpenStack 开源社区作为其最初的核心子项目之一，为其 Nova 子项目提供虚机镜像存储服务。Swift 构筑在比较便宜的标准硬件存储基础设施之上，无需采用 RAID（磁盘冗余阵列），通过在软件层面引入一致性散列技术和数据冗余性，牺牲一定程度的数据一致性来达到高可用性和可伸缩性，支持多租户模式、容器和对象读写操作，适合解决互联网的应用场景下非结构化数据存储问题。此项目是基于 Python 开发的，采用 Apache 2.0 许可协议，可用来开发商用系统。



[url1]: http://zh.wikipedia.org/wiki/OpenStack