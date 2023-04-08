---
layout: post
title: "扒一扒 Chatgpt 背后的 web 开发技术（一）"
date: 2023-04-07 21:52:40
description: 扒一扒 Chatgpt 背后的 web 开发技术
keywords: chatgpt, sse
comments: true
categories: chatgpt
tags: [chatgpt]
---

{% img /images/post/2023/04/chatgpt.png 400 300 %}

ChatGPT 我就不多说了，最近一直都很火。在 ChatGPT 的页面上我们输入一个问题后，答案是以渐进式的输出展示出来的，为了实现实时通信和高效的数据传输，ChatGPT 选择了 SSE（Server-Sent Events） 技术。在本篇博客中，我们将详细介绍这一项重要技术。

<!--more-->

## SSE 技术概述

SSE 是一种基于 HTTP 的实时通信技术，允许服务器向客户端（如 Web 浏览器）实时推送消息。SSE 的基本原理是通过建立一个持久的 HTTP 连接，服务器可以主动将事件发送到客户端，而无需客户端重复发送请求。

以下是 SSE 技术的一些主要特点：

* 基于文本：SSE 使用纯文本格式传输数据，这使得数据的解析和处理变得相对简单。
* 单向通信：与 Websockets 等双向通信技术相比，SSE 主要用于服务器向客户端的单向通信，减少了通信的复杂性。
* 重连机制：在连接中断时，SSE 客户端会自动尝试重新连接服务器，从而确保实时数据的传输不会中断。
* 事件标识：SSE 支持为不同类型的事件设置标识，使客户端能够根据事件类型进行相应的处理。
* 与 Websockets 的比较：

尽管 SSE 和 Websockets 都是实时通信技术，但它们在某些方面有所不同：

* 通信方向：Websockets 支持双向实时通信，而 SSE 主要用于服务器向客户端的单向通信。
* 协议：Websockets 使用独立的协议进行通信，而 SSE 则基于 HTTP 协议。
* 数据格式：Websockets 支持传输二进制和文本数据，而 SSE 仅支持文本数据。
* 根据项目需求和场景，开发者可以选择适合的实时通信技术。在 ChatGPT 中，我们主要关注服务器向客户端推送消息，因此选择了 SSE 作为实现实时通信的技术。

## SSE 的代码示例

在 Nestjs 里面使用 SSE 作为后端接口的代码示例如下：

```js
import { MessageEvent,  Sse } from '@nestjs/common';
import { interval, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Sse('sse')
sse(): Observable<MessageEvent> {
  return interval(1000).pipe(map((_) => ({ data: { hello: 'world' } })));
}
```
Nestjs 中已经内置了 SSE 的实现，只需要使用装饰符`Sse`即可，再通过`rsjs`的 Observable 对象返回流式数据。

然后再看看客户端的代码示例：

```js
const eventSource = new EventSource('/sse');
eventSource.onmessage = ({ data }) => {
  console.log('New message', JSON.parse(data));
};
```
通过创建一个`EventSource`对象来建立后端的 SSE 接口连接，并通过监听 message 事件来获取后端数据。

更多详细代码请参考[这里](https://docs.nestjs.com/techniques/server-sent-events)。

## SSE 的局限性

### 只能使用 GET 请求

SSE 是基于 HTTP GET 请求的，因此无法直接使用 POST、PUT 等其他请求，也就是说无法在请求中传递 data 类的参数。这是因为 SSE 的设计初衷是用于服务器向客户端发送实时事件和数据，而不是用于客户端向服务器发送数据。

### 无法在请求中传递 header

由于浏览器的安全策略，SSE 请求无法直接携带自定义 HTTP 头部。这可能在某些情况下带来限制，例如在需要身份验证时传递令牌。

虽然有以上局限性，但我们还是可以通过一些方法来解决这些问题。

* 使用 URL 参数来传递数据，将所需的数据作为 URL 参数附加到 SSE 请求，适合传递一些简单的参数

```js
// 这里的 uid 就是要传递参数
const eventSource = new EventSource('/sse/uid');
```
* npm 上有个`EventSource`的 [polyfill](https://www.npmjs.com/package/event-source-polyfill)， 使用它替换掉`EventSource`后就可以在请求中携带 header 了，像身份令牌这种重要信息，还是建议放到 header 中。

```js
const es = new EventSourcePolyfill('/sse', {
  headers: {
    'X-Custom-Header': 'value'
  }
});
```

## 总结

SSE 作为一种基于 HTTP 的实时通信技术，使得服务器能够主动将事件发送到客户端，而无需客户端重复发送请求。尽管 SSE 存在一定的局限性，在特定场景下，其简单、易用的特点使其成为实现单向实时数据传输的理想选择。


