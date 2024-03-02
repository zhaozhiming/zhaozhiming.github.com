---
layout: post
title: "通俗漫画介绍 ArrayBuffers 和 SharedArrayBuffers"
date: 2017-06-20 22:44
description: 通俗漫画介绍 ArrayBuffers 和 SharedArrayBuffers
keywords: SharedArrayBuffers,ArrayBuffers
comments: true
categories: translation
tags: [SharedArrayBuffers,ArrayBuffers]
---

{% img /images/post/2017/06/02_15.png 400 350 %}

这是 3 篇文章中的第二篇：

* [内存管理速成教程](https://zhaozhiming.github.io/2017/06/20/a-crash-course-in-memory-management-zh/)
* 通俗漫画介绍 ArrayBuffers 和 SharedArrayBuffers
* [在 Sharedarraybuffers 中使用 Atomics 来避免竞态条件](https://zhaozhiming.github.io/2017/06/21/avoiding-race-conditions-in-sharedarraybuffers-with-atomics-zh/)

原文链接：[A cartoon intro to ArrayBuffers and SharedArrayBuffers](https://hacks.mozilla.org/2017/06/a-cartoon-intro-to-arraybuffers-and-sharedarraybuffers/)

<!--more-->

# 通俗漫画介绍 ArrayBuffers 和 SharedArrayBuffers
  
在[上一篇文章中](https://zhaozhiming.github.io/2017/06/20/a-crash-course-in-memory-management-zh/)，我解释了像 JavaScript 这样的内存自动管理语言如何操作内存，我也解释了像 C 语言这样的手动内存管理语言如何工作的。  

为什么当我们讨论 [ArrayBuffers](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer) 和 [SharedArrayBuffers](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/SharedArrayBuffer) 时这部分内容很重要呢？  

这是因为 ArrayBuffers 提供了一种让你可以手动操作数据的方式，即使你是使用像 JavaScript 这种内存自动管理的语言。  

为什么你将来会去做这件事（指内存管理）？  

正如上一篇文章所说的，这是自动内存管理的一个权衡，它方便开发人员，但增加了开销，某些情况下，这种开销会导致性能问题。  

{% img /images/post/2017/06/02_01.png 400 350 %}
  
举个例子，当你要在 JS 中创建一个变量，引擎必须猜测这个变量是哪种类型，如何在内存中展示。因为这种猜测，JS 引擎通常会为变量预留比实际占用的更多的空间，依赖于变量，内存槽容量可能比实际需要的要大 2 到 8 倍，这将导致很多内存的浪费。  

另外，某些创建和使用 JS 对象的模式可能让垃圾回收难以进行，如果你可以手动管理内存，你就可以在工作中选择一个正确分配和释放内存的策略。  

大部分情况下，这不会有什么问题。大部分系统性能不会如此敏感，以致要你使用手动内存管理，而且通常情况下，手动管理内存甚至可能让程序变得更慢。  

但有时候你需要处理一些底层的工作来让你的代码运行得尽可能的快，ArrayBuffers 和 SharedArrayBuffers 给你另外一个选择。  

{% img /images/post/2017/06/02_02.png 400 350 %}
  
## 那么 ArrayBuffer 是如何工作的？

基本上它就像 JavaScript 中的其他数组，但是你不能在里面放任何类型的数据，比如字符串和对象，你只能在里面放二进制字节（可以用来表示数字的那个东西）。  

{% img /images/post/2017/06/02_03.png 400 350 %}
  
有件事情我必须在这里澄清，实际上你不能直接添加二进制字节到 ArrayBuffer，ArrayBuffer 本身不知道这个字节应该多大，不同类型的数字应该如何转换成字节。  

ArrayBuffer 本身只是一个 0 和 1 存放在一行里面的一个集合，ArrayBuffer 不知道第一个和第二个元素在数组中该如何分配。  

{% img /images/post/2017/06/02_04.png 400 350 %}
  
为了能提供上下文，为了能真正分解数据并放入到盒子中，我们需要将其封装在一个叫做 view 的东西里面。这些在数据上的 view 可以被添加进确定类型的数组，而且我们有很多种确定类型的数据可以使用。  

例如，你可以使用一个 Int8 的确定类型数组来分离存放 8 位二进制字节。  

{% img /images/post/2017/06/02_05.png 400 350 %}
  
或者你可以使用一个无符号的 Int16 数组来分离存放 16 位二进制字节，这样如果是一个无符号的整数也能处理。  

{% img /images/post/2017/06/02_06.png 400 350 %}
  
你甚至可以在相同基础的 buffer 上使用不同的 view，同样的操作不同的 view 会给你不同的结果。  

比如，如果我们在这个 ArrayBuffer 中从 Int8 view 里获取了元素 0 和 1，在 Uint16 view 中元素 0 会返回给我们不同的值，尽管它们包含的是完全相同的二进制字节。  

{% img /images/post/2017/06/02_07.png 400 350 %}
  
在这种方式中，ArrayBuffer 基本上扮演了一个原生内存的角色，它模拟了像 C 语言才有的那种直接访问内存的方式。  

你可能想知道为什么我们不让程序直接访问内存，而是添加了这种抽象层。直接访问内存将导致一些安全漏洞，我会在以后的文章中解释。  

## 那么什么是 SharedArrayBuffer？

为了解释 SharedArrayBuffers，我需要解释一点关于并行运行代码和 JavaScript 的知识。  

你可以并行运行代码让你的代码跑的更快，或者让其更快地响应用户事件。为了做到这点，你需要分离你的工作。  

在一个典型的 app 中，所有工作都在一个单独独立的主线程中被照看着，我以前讲过这个。主线程就像一个全栈开发，它负责 JavaScript，DOM 结构和页面布局。  

如果你能减少主线程的工作量，不管是什么事情都对工作负载有帮助，在某些情况下，ArrrayBuffer 可以减少主线程的工作量。  

{% img /images/post/2017/06/02_08.png 400 350 %}

但是有些时候减少主线程的工作负载是不够的，有时候你需要一些援助，你需要分离工作。  

在大部分编程语言中，通常分离工作的方式就是使用一种叫做线程的东西，基本上这就像有多个人在做同一个项目。如果你的任务都比较独立，你就可以将任务分配给不同的线程，这样所有线程都可以在相同的时间内独立完成任务。  

在 JavaScript 中，你可以使用一个叫做 web worker 的东西来做这件事情，这些 web worker 跟你在其他语言中用到的线程有些许差别，它们默认不共享内存。  

{% img /images/post/2017/06/02_09.png 400 350 %}
  
这意味着如果你想要共享一些数据给其他线程，你需要复制它们，方法 [`postMessage`](https://developer.mozilla.org/en-US/docs/Web/API/Worker/postMessage) 可以做到这点。  

postMessage 获取你放进去的任何数据，序列化它们，再将其发送给其他 web worker，然后其他 web worker 反序列数据再将其放入到内存。  

{% img /images/post/2017/06/02_10.png 400 350 %}
  
这是一个非常慢的过程。  

某些类型的数据，像 ArrayBuffer，你能做的操作叫做转移内存，这意味着移动指定的内存块让其他 web worker 可以访问数据。  
但第一个 web worker 就不能再访问移动后的数据了。  

{% img /images/post/2017/06/02_11.png 400 350 %}
  
在某些情况下这可以工作，但大部分情况是你需要高性能的并行方式，你真正需要的是拥有可以共享的内存。  

这就是需要 SharedArrayBuffers 的原因。  

{% img /images/post/2017/06/02_12.png 400 350 %}
  
通过 SharedArrayBuffer，不管是 web worker，还是线程，都可以从相同的内存块中读写数据。  

这意味着你不会有使用 postMessage 时的交流开销和延迟。所有 web worker 都可以立即访问数据。  

在同一时刻所有线程都访问同一数据会有些危险，可能引起一个叫竞态条件的问题。  

{% img /images/post/2017/06/02_13.png 400 350 %}
  
我将在[下一篇文章](http://zhaozhiming.github.io/blog/2017/06/21/avoiding-race-conditions-in-sharedarraybuffers-with-atomics-zh/) 中解释。  

## SharedArrayBuffers 当前的现状是怎样的？

SharedArrayBuffers 不久将会出现在所有主流浏览器中。  

{% img /images/post/2017/06/02_14.png 400 350 %}
  
它们已经在 Safari（在 Safari 10.1）里了，Firefox 和 Chrome 将在他们的 7/8 月的发布中引进它们，而且 Edge 也将在秋季的 Windows 更新中引进它们。  

不过即使它们在所有主流浏览器中都可用，我们也不希望应用开发人员直接使用它们。实际上，我们推荐不用它们，你应该使用最高级别的抽象封装。  

我们希望 JavaScript 库开发人员来创建对应的库来让你更方便和安全地使用 SharedArrayBuffers。  

另外，一旦 SharedArrayBuffers 内置到平台中，WebAssembly 就可以使用它们来实现多线程支持。一旦到位，你就可以使用像 Rust 那样的并发抽象技术，Rust 的主要目标之一就是让你无所畏惧地使用并发编程。  

在[下一篇文章](http://zhaozhiming.github.io/blog/2017/06/21/avoiding-race-conditions-in-sharedarraybuffers-with-atomics-zh/)中，我们将介绍这些库作者使用的工具（Atomics），他们用来构建抽象层同时避免竞态条件。  

{% img /images/post/2017/06/02_15.png 400 350 %}
