---
layout: post
title: Vercel AI SDK 避坑指南
date: 2023-07-10 21:49:30
description: Vercel AI SDK 避坑指南
keywords: vercel, ai
comments: true
categories: ai
tags: [vercel, ai]
---

{% img /images/post/2023/07/vercel.jpg 400 300 %}

随着 ChatGPT 的出现，很多开发人员都想开发一款 AI 聊天机器人，Vercel 最近开发了一款 AI SDK，可以帮助开发人员快速开发聊天机器人，包括调用 OpenAI 的 API，对话记录传递，流式输出等功能，非常方便。Vercel 的开发团队也是很多著名工具的开发者，包括前端开发框架 Next.js 、前端部署工具 Vercel 、React 数据查询工具 SWR 等，这些工具都是非常优秀的，所以这款 AI SDK 也是非常值得期待的。然而，每个工具都有其特性和潜在的坑点，Vercel AI SDK 也不例外。本文将为你提供一份详尽的 Vercel AI SDK 避坑指南，帮助你更好地理解和使用这款工具，从而提升你的开发效率和应用质量。

<!--more-->

## 具备的优势

Vercel AI SDK 之所以能帮助开发人员快速开发聊天机器人，主要是因为它具备以下优势：

- 内置的 LLM 适配器：为你的应用选择正确的 LLM（大语言模型）至关重要，因为每种 LLM 都有其各自的特点，可以在不同的场景满足你的需求。Vercel 的 AI SDK 支持 OpenAI、LangChain 和 Hugging Face 等 LLM 或框架。这意味着，无论你选择哪种 AI 模型提供商，你都可以利用 Vercel AI SDK 快速集成 LLM 的能力。
- 流式优先的 UI 助手：使用过 ChatGPT 的朋友都会因为它的流式输出和页面打印机效果而感觉是在跟人正常聊天，而不是在跟机器人聊天。Vercel AI SDK 内置了 React 和 Svelte 的数据获取和渲染流式文本响应的钩子。这些钩子使你的应用能够实时、动态地表示数据，为你的用户提供沉浸式和交互式的体验。只需几行代码，就可以利用 `useChat` 和 `useCompletion` 构建丰富的聊天界面。
- 流式助手和回调函数：Vercel AI SDK 还提供了在同一请求中处理流式请求多个阶段数据的回调函数，这些回调函数可以分别在请求开始前，请求进行中，请求结束时对流式数据进行额外的处理，比如可以将对话完成时的回答结果存储到数据库。这个功能允许高效的数据管理，并简化了处理流式文本响应的整个过程。
- Edge 和 Serverless 就绪：Vercel AI SDK 支持 Serverless 和 Edge 等产品集成。你可以在 Vercel 平台快速部署流式生成响应且成本有效的 AI 应用。如果你在 AI SDK 的 Next.js 框架中编写应用代码，那么只需要一套代码就可以完成前后端的功能，并且 Vercel 会将这些代码快速转换为全球应用。

## 多种项目模板

Vercel AI SDK 可以在多种项目环境下运行，支持的 AI 模型提供商包括：

- OpenAI
- LangChain
- Hugging Face

前端不管你是用 React 还是 Vue，都可以使用 Vercel AI SDK，下面列出一些常用的项目模板：

- Next.js + OpenAI
- Nuxt.js + OpenAI
- Next.js + LangChain
- Nuxt.js + LangChain
- Next.js + Hugoing Face

更多模版信息可以在这里查看：[AI SDK 模版](https://vercel.com/templates/ai)

## 踩坑指南

虽然 Vercel AI SDK 有诸多好处，但可能是刚推出的原因，在实际使用过程中发现了不少问题。笔者以`Next.js` + `LangChain` 模板为例，部署用`Vercel`，记录了一些踩坑的经验，希望能帮助你避免踩坑。

### Edge 不能调用 Node.js 内置 API

Vercel AI SDK 的 Next.js 模板默认是在 Edge 上运行，Edge 是 Vercel 的 CDN 服务，它会将你的应用部署到全球各地的 CDN 节点上，这样可以加速你的应用。但是，Edge 不能调用 Node.js 内置的 API，比如 `fs`、`path` 等，如果你的应用中使用了这些 API，那么在部署时会报错。[官方文档](https://nextjs.org/docs/messages/node-module-in-edge-runtime)是这样说的。

> However, the Edge Runtime does not support Node.js APIs and globals.

在使用 LangChain 的过程中，经常需要读取本地的文件来做文件向量存储，就需要用到 Node.js 的内置 API 了。那要如何解决这个问题呢？我们可以使用 Serverless 来代替 Edge，Serverless 是 Vercel 的云函数服务，它可以调用 Node.js 的内置 API，但是它的缺点是，性能没有 Edge 好，而且 API 的调用时长比较短（这个下面会讲）。

去除 Edge 只需要在 API 路由的代码中将这一行代码去掉即可。

```diff
- export const runtime = 'edge'
```

### Vercel 免费版本的 API 调用时长限制

当我们用 Vercel AI SDK 开发完应用后，可以使用 Vercel 这个平台来部署我们的应用，非常方便。Vercel 支持用户免费部署应用，但免费部署会有一些限制，如下图所示：

{% img /images/post/2023/07/vercel-limit.png 500 400 %}

可以看到 Serverless 的免费版本（Hobby）函数执行时间限制是 10 秒，这意味着只要 API 执行超过 10 秒就会认为失败，这个时间对于需要调用 OpenAI 的 API 来生成文本的应用来说是不够的，因为 OpenAI 的 API 生成的内容会比较多，10 秒的时间很容易超过。好在 Vercel AI SDK 可以使用流式输出，这样至少可以保证用户在等待的过程中可以看到一些内容，而不是一直等待，但是当超时时间到了，流式输出后面的内容就会被截断了。

如果你想保证你的应用没有上述问题，要么就是升级到付费版本（升级后执行时间是 60 秒），要么就是使用 Edge，Edge 的免费版本时长限制是 30 秒，又或者是想办法降低 LLM 的输出内容，保证在 10 秒内输出完毕。

### 打包时无法读取仓库中的自定义文件

还有一个问题是，应用在 Vercel 生产环境上运行时，有时候需要读取仓库中的自定义文件，比如说，我们的应用需要读取一个自定义的词典文件，这个文件是在仓库中的，但是在打包时，这个文件并没有被打包进去，导致应用在运行时无法读取到这个文件。在本地环境运行完全没有问题，但到了生产环境就报错了。

这个问题的临时解决办法是将自定义文件先上传到公网上，然后在应用运行时通过网络请求获取到这个文件，这样就可以解决这个问题了。但是这样做的缺点是，应用首次运行时都需要先请求一次网络，这样会增加应用的运行时间，而且如果网络不好，还会导致应用运行失败。

这个问题也可能是因为我对 Vercel 不熟悉导致的，如果你知道怎么解决这个问题，欢迎在评论区告诉我。

## 总结

Vercel AI SDK 是一个非常有意思的项目，它可以让我们在开发中快速使用 AI 模型，而且还可以在 Vercel 平台上快速部署，非常方便。但是目前 Vercel AI SDK 还处于早期阶段，有很多问题需要解决。但好在 Vercel 团队非常活跃，版本更新迭代很快，比如 OpenAI 刚推出`function_call`功能，Vercel AI SDK 就很快同步更新了。Vercel AI SDK 的出现，无疑是一个非常好的开始，相信未来会有更多的有创意的应用使用 Vercel AI SDK 来开发。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
