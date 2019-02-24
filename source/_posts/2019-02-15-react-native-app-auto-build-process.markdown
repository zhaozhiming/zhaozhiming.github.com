---
layout: post
title: "React Navtive App 自动打包实践指南"
date: 2019-02-15 13:29
description: React Navtive App 自动打包实践指南
keywords: react-native,auto-build
comments: true
categories: react-native
tags: [react-native,auto-build]
---

{% img /images/post/2019/02/auto-build.jpg 400 300 %}

React Native 创建项目有 2 种方式，一种是通过 [Expo](https://expo.io/) 框架创建，这种项目可以通过 Expo 提供的命令进行打包；另外一种是通过 React Native 原生命令生成的项目，需要使用 [Gradle](https://gradle.org/) 和 [Xcode](https://developer.apple.com/xcode/) 这些原生工具打包，虽然麻烦了一些，但是灵活性更高，今天讨论的自动打包主要是基于这种方式创建的项目。

<!--more-->

## 打包流程

{% img /images/post/2019/02/process.png 873 300 %}

这是自动打包程序的流程图，概述了从我们提交代码到工作群中收到打包完成的通知这一过程。

* 提交代码：将要发布的代码提交到代码库，这里我们假设使用 [gitlab](https://about.gitlab.com/) 来做为我们代码管理的平台。
* 持续集成：将新代码合并到代码库后执行的一系列操作，多用来做新代码的静态检查、单元测试等，可以尽早发现集成过程中的问题，这里我们用持续集成来自动打包。
* 上传 App：将打好的 App 文件上传到分发平台，这里我们选择[蒲公英](https://www.pgyer.com/)，它是国内提供手机应用内侧服务较好的供应商。
* 消息通知：当所有东西都完成后，我们需要及时通知到相关人员，结合我们平时工作用的聊天工具钉钉，我们可以使用钉钉的自定义机器人来发送消息。

PS：在持续集成的过程中，我们使用了不同的策略进行打包，Android 使用 Docker + Gradle 的方式，而 iOS 因为强依赖于 Mac 的系统和环境，所以需要单独一台 Mac 机器来做为打包的机器，详细的打包过程将在下面介绍。

### 工具集

打包过程涉及的工具简单介绍一下：

* gitlab：代码托管平台，类似 [github](https://github.com/)，但可以支持免费内部部署服务，是大部分公司使用的内部代码托管平台。
* gitlab-ci：市面上有很多持续集成的框架，但如果是用 gitlab 来做代码管理的话，使用 gitlab-ci 无疑是最简单的持续集成方式。
* Docker：容器管理工具，可以和大部分持续集成工具集成，使用 Docker 来做持续集成是一种趋势。
* 蒲公英：国内做的比较好的手机内测服务平台，在上面上传 App 后用户就可以进行 App 测试，它对应的竞品是 [Fir.im](https://fir.im/)。
* Mac 机器：这里需要借助 Mac 的机器来做 iOS App 的打包程序，可以是任意苹果公司的机器，比如 Mac mini、MacBook air/pro 等。
* 钉钉：阿里推出的企业聊天工具，它支持自定义机器人的功能，方便我们定制自动发送消息功能。

## Android 打包

React Native 工程下面有个`android`目录，下面放置的是Android的原生代码，手动打包的话只需要在该目录下执行`gradlew assembleRelease`命令即可。

### Docker 镜像

### gitlab-ci 配置

### 打包

### 上传 apk

### 打包成功通知

## iOS 打包

### 环境安装

### 打包

### 上传 apk

### 打包成功通知

## 总结


