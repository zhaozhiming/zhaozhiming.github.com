---
layout: post
title: "openstack swift all in one 详细介绍（一）"
date: 2013-11-26 07:13
published: false
description: openstack swift all in one 详细介绍
keywords: openstack swift all in one 详细介绍
comments: true
categories: code
tags: [openstack,swift]
---

###swift介绍
最近开始研究openstack的swift，openstack是一个开源的Iaas云框架，有云计算，云存储等功能，swift是其中基于对象存储的云存储组件，下面是对swift官网all in one部署流程的一个详细介绍。swift是一个分布式的存储系统框架，在正式部署时可以分很多台机器进行部署，这里all in one是指swift所有的服务都搭建在同一台机器上的意思。  
  
###环境准备
- 操作系统：Ubuntu12.04LTS
- 在ubuntu系统上创建swift用户，并有root权限
- 使用回环设备做存储
  


