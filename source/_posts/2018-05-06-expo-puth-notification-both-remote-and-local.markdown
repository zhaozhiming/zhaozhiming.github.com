---
layout: post
title: "基于 Expo 的 React Native 消息推送"
date: 2018-05-06 14:49
description: 基于 Expo 的 React Native 消息推送
keywords: expo,react native,push notification,local notification
comments: true
categories: React Native
tags: [expo,react native,push notification,local notification]
---

{% img /images/post/2018/05/notification.jpg 400 300 %}

[Expo](https://expo.io/) 是 [React Native](https://facebook.github.io/react-native/) 开发的一个神器，正如 Expo 官网上所说，Expo 之于 React Native 就像 Rails 之于 Ruby，它提供了很多超越原生 React Native API 的功能，包括二维码扫描、存储、内部浏览器等，甚至还可以使用 Expo 进行 APP 的打包，完全不需要使用 XCode 和 Android Studio。

而消息推送则是 APP 应用非常常见的一个功能，今天就来介绍一下基于 Expo 的 React Native 消息推送功能是如何开发的吧。

<!--more-->

APP 的消息推送一般有两种形式，一种是本地消息，比如 APP 中有个定时执行的任务，完成后给用户发送一个"任务执行完成"的通知；另外一种是远程消息，比如说某个电商做活动，会向用户推送一些活动相关的消息。

本地和远程消息两者本质的区别就是：本地消息是由 APP 本身发送的，而远程消息是由 APP 的后端（服务端）发送的。

## 本地消息推送

本地消息不需要和服务端交互，相对比较简单，让我们先来看看 Expo 的本地消息是如何发送的。

### 消息对象

一般我们手机上收到的推送消息大概是下面这样子，有 APP 的 Icon、标题、摘要信息以及推送的时间。

{% img /images/post/2018/05/notification_demo.png %}

所以我们需要先构造一个本地消息对象，它主要包含以下属性：

* title：字符串类型，必填属性，消息的标题，会显示在手机的通知栏上面
* body：字符串类型，必填属性，消息的摘要信息，会显示在手机的通知栏上面
* data：对象类型，可选属性，附加在消息上的一个数据对象，不会显示在通知栏上，但可在 APP 内部使用该对象。

{% codeblock lang:js %}
const localNotification = {
  title: 'Test',
  body: 'This is a Test',
  data: {
    foo: 'foo'
  },
};
{% endcodeblock %}

### 消息发送

创建好了消息对象后，我们就可以来尝试将它发送，Expo 提供了两种发送方式：立即发送和定时发送。

#### 立即发送

{% codeblock lang:js %}
import { Notifications } from 'expo';

Notifications.presentLocalNotificationAsync(localNotification);
{% endcodeblock %}

调用这个方法后 APP 会马上发送一条消息，然后在手机通知栏上就可以看到。

####  定时发送

定时发送除了要求消息对象外，还需要时间调度的对象。

{% codeblock lang:js %}
import { Notifications } from 'expo';

const schedulingOptions = {
  time: Date.now() + 1000 // 表示一秒后发送消息
};
Notifications.scheduleLocalNotificationAsync(localNotification, schedulingOptions);
{% endcodeblock %}

上面的示例中会在 1 秒后发送消息，`schedulingOptions` 的有如下属性：

* time：可以是 Date 对象，也可以是时间毫秒数，表示什么时候开始发送消息
* repeat：字符串，可以填的值有`'minute'、'hour'、'day'、'week'、'month'、'year'`，表示是否重复发送消息

## 远程消息推送

远程消息的发送稍微复杂一些，需要搭建自己的后台服务，流程图如下：

{% img /images/post/2018/05/push-notification.png %}

* 手机通过后台服务提供的 API 进行设备登记
* 后台服务调用 Expo 后台服务的 API
* Expo 后台服务往所有登记过的设备上推送消息

下面我们来详细看下每一步的具体操作。

### 后台服务

我们需要搭建自己的后台服务来提供 API，API 要做的事情就是接收请求中的设备号并保存起来，下面我们用`Node.js`来写个简单的 API 示例：

{% codeblock lang:js %}
// 使用 Set 来保存手机设备号，这样保证每个设备号只有一个
// 这里只是简单示例，更好的方案是用数据库保存设备号
const PUSH_TOKENS = new Set();
// 这里使用的是 [Hapi](https://hapijs.com/) 框架
exports.register = (server, options, next) => {
  server.route([
    {
      method: 'POST',
      path: '/users/push-token',
      config: {
        handler: (request, reply) => {
          // token 为请求参数中的设备号
          const { token } = request.payload;
          // 添加到 Set 中
          pushTokens.add(token);
          return reply({ message: `The tokens is ${token}` });
        },
      },
    },
  ]);
  next();
};
{% endcodeblock %}

例子非常简单，这里提供了一个方法为`POST`，url 为`/users/push-token`的 API，请求中必须带有 token 参数。

### 登记设备

准备好了 API 之后，我们就可以在设备上调用 API 进行设备登记了。

Expo 在`Notifications`模块中提供了相应的方法来让获取设备号，我们来看一下示例代码：

{% codeblock lang:js %}
import { Permissions, Notifications } from 'expo';

const getToken = async () => {
  // 首先要获取手机允许接收消息的权限
  const { status: existingStatus } = await Permissions.getAsync(
    Permissions.NOTIFICATIONS
  );
  let finalStatus = existingStatus;

  // 如果没有授权则发请求获取
  if (existingStatus !== 'granted') {
    const { status } = await Permissions.askAsync(Permissions.NOTIFICATIONS);
    finalStatus = status;
  }

  // 用户不允许则返回空
  if (finalStatus !== 'granted') return null;

  // 调用 Expo API 来获取设备号
  const token = await Notifications.getExpoPushTokenAsync();
  return token;
}
{% endcodeblock %}

拿到设备号后，我们就可以将设备号发送到我们的后台服务了。

{% codeblock lang:js %}
const PUSH_ENDPOINT = 'https://your-server.com/users/push-token';
const pushToken = async () => {
  try {
    // 通过 POST 请求将设备号发送到后台服务
    await fetch(PUSH_ENDPOINT, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ token }),
    });
  } catch (e) {
    console.error(`发送设备号异常：${e.message}`);
  }
}
{% endcodeblock %}

### 发送消息

最后一步是让 Expo 的后台服务将消息推送给登记过的所有设备，那我们要怎么发送请求给 Expo 的后台服务呢？

其实 Expo 提供了 sdk 包来让我们的后台服务可以与 Expo 的服务器进行通信，sdk 包支持多种语言，方便与各种 Web 服务集成，支持的语言包括`Node.js`、`Python`、`Ruby`、`PHP`、`Golang`等，我们以`Node.js`为例介绍一下我们的后台服务是如何与 Expo 后台服务进行通信的：

* 首先是要添加 Expo 的 sdk 包

{% codeblock lang:sh %}
yarn add expo-server-sdk
{% endcodeblock %}

* 接着调用 Expo 的 API 进行消息推送，下面是示例代码，可以看注释理解相应的代码：

{% codeblock lang:js %}
import Expo from 'expo-server-sdk';

// 创建 Expo 客户端实例
let expo = new Expo();

// 创建要发送的消息
let messages = [];
// 往所有设备推送消息
for (let pushToken of somePushTokens) {
  // 设备号的格式为：ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]
  // 检查每个设备号格式是否正确
  if (!Expo.isExpoPushToken(pushToken)) {
    console.error(`Push token ${pushToken} is not a valid Expo push token`);
    continue;
  }

  // 构造消息对象
  messages.push({
    to: pushToken,
    sound: 'default',
    body: 'This is a test notification',
    data: { withSome: 'data' },
  })
}

// 将消息转换成块，这样同样内容的消息会进行压缩，Expo 支持且推荐批量推送消息
let chunks = expo.chunkPushNotifications(messages);

(async () => {
  // 分批发送消息
  for (let chunk of chunks) {
    try {
      let receipts = await expo.sendPushNotificationsAsync(chunk);
      console.log(receipts);
    } catch (error) {
      console.error(error);
    }
  }
})();
{% endcodeblock %}

这样就完成了一次远程消息的推送。

## 消息接收

当消息发送到手机上的时候，APP 要如何进行消息接收呢？Expo 提供了一个消息监听器，不管是本地的还是远程的消息都可以监听到，我们来看一段简单的代码示例。

{% codeblock lang:js %}
import React from 'react';
import {
  Notifications,
} from 'expo';

export default class AppContainer extends React.Component {
  state = {
    notification: {},
  };

  componentDidMount() {
    // 推荐在`componentDidMount`生命周期中添加消息监听器
    // 消息的处理方法作为参数传入`Notifications.addListener`方法中
    this.notificationSubscription = Notifications.addListener(this.handleNotification);
  }

  handleNotification = (notification) => {
    // 这里我们简单地将接收到的消息保存到组件的 state 中
    this.setState({notification: notification});
  };
  render() {...}
}

{% endcodeblock %}

### notification 对象

在`handleNotification`方法中我们会对接收消息进行处理，`notification`就是接收的消息对象，它有如下属性：

* remote：`true`表示这个消息是远程消息，`false`则是本地消息
* origin：这个属性有 2 个值——`selected`和`received`。`selected`表示是用户从通知栏上点击消息而接收到的，而`received`则表示是 APP 在打开状态时接收到的。
* data：附加在消息上的 data 对象

### EventSubscription 对象

调用`Notifications.addListener`后返回的`notificationSubscription` 是一个`EventSubscription`对象，它有以下属性和方法：

* remove()：该方法可以用来取消已有的监听器
* origin：同`notification `对象的`origin`属性
* remote：同`notification `对象的`remote`属性
* data：同`notification `对象的`data`属性

## 总结

消息推送是 APP 的常见需求，市面上也有不少解决方案，但 Expo 相对其他解决方案来说最大的优势就是无需与原生的模块打交道，比如其他消息推送方案可能需要对 android 的源文件进行修改，然后再对 ios 的源文件进行修改，而 Expo 则不需要关心这些，只要在 React Native 中写好代码就可以了。唯一的不足就是需要搭建自己的后台服务，但如果你的 APP 本身就带有后台服务的话，则 Expo 的消息推送方案是你的首要选择。


