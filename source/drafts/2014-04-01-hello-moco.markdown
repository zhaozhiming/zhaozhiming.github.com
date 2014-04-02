---
layout: post
title: "moco使用初探"
date: 2014-04-01 20:50
description: moco
keywords: moco
comments: true
categories: code
tags: test
---

{% img /images/post/2014-4/moco.png %}  

最近在写一个SDK包的单元测试，团队开始使用Mockito加写Stub类的方式来mock Http请求，发现这样写每次要mock一个http请求都要写一个Stub类，这样以后测试代码多了会比较难维护。想到之前[郑大大][url1]的[Moco][url2]，决定拿来试用一下。  
<!--more-->  
