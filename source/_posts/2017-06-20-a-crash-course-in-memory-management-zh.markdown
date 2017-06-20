---
layout: post
title: "内存管理速成教程"
date: 2017-06-20 19:29
description: 内存管理速成教程
keywords: 内存管理
comments: true
categories: translation
tags: [内存管理]
---

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_07.png 400 350 %}

我一直很佩服那些能将复杂原理讲得通俗易懂的人，[Lin Clark 女神](http://code-cartoons.com/) 就是其中一个，拜读完她新发布的系列文章“通俗漫画介绍 SharedArrayBuffers”之后，深深为之折服，文章不仅一如既往地通俗易懂，作者亲自画的图更是和文章相得益彰。看完萌生出了翻译该系列文章的想法，不过本人英文能力有限，如果觉得翻译地不好的还请看英文原版，英文版也是很容易理解的。

这是 3 篇文章中的第一篇：

* 内存管理速成教程
* 通俗漫画介绍 ArrayBuffers 和 SharedArrayBuffers
* [Avoiding race conditions in SharedArrayBuffers with Atomics]()

原文链接：[https://hacks.mozilla.org/2017/06/a-crash-course-in-memory-management/](https://hacks.mozilla.org/2017/06/a-crash-course-in-memory-management/)

<!--more-->

# 内存管理速成教程
  
为了搞明白我们为什么把 SharedArrayBuffer 加入到 JavaScript，你首选需要搞懂一点关于内存管理方面的知识。  

你可以把机器中的内存想象成一堆盒子，我觉得这个有点像你工作中的办公邮箱，或者学校学生的储物柜。  

如果你想给学生们留一些东西，你可以把东西放到盒子里面。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_01.png 400 350 %}
  
在每个盒子旁边都有一个数字，这就是内存地址，这用来让你告诉别人留给他们的东西在哪个位置。  

每个盒子都有相同的大小，能容纳一定量的信息。盒子的容量指定给了机器，这个容量就叫字长。字长一般是 32 位或者 64 位，但为了让它容易演示，我会使用 8 位的字长。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_02.png 400 350 %}
  
如果我们想要把数字 2 放到其中的一个盒子里，我们可以很容易地做到，因为数字很容易[表现成二进制](https://www.khanacademy.org/math/algebra-home/alg-intro-to-algebra/algebra-alternate-number-bases/v/decimal-to-binary)。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_03.png 400 350 %}
  
如果我们想要放一个不是数字的东西呢？比如字母 H ？  

我们需要一个方法将其展示成数字，为了做到这一点，我们需要编码格式，比如 [UTF-8](https://en.wikipedia.org/wiki/UTF-8)，然后我们需要一个东西将其转换成数字。比如一个编码环，这样我们就可以存储它了。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_04.png 400 350 %}
  
当我们想要从盒子中取出它时，我们需要一个解码器将其转换回字符 H 。  

## 自动内存管理

当你使用 JavaScript 时，实际上你不需要过多考虑内存的事情，它是远离你的一个抽象概念，这意味着你不会直接和内存打交道。  

JS 引擎作为一个中间人的角色来代替你打交道，它替你管理着内存。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_05.png 400 350 %}
  
让我们写一些 JS 代码，比如 React，需要创建一个变量。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_06.png 400 350 %}
  
JS 引擎做的事情就是运行通过编码器转换成二进制表示的值。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_07.png 400 350 %}
  
然后它找到可以存放二进制表示值的空间，这个过程叫做分配内存。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_08.png 400 350 %}
  
然后，引擎会跟踪这个变量是否仍然在程序中被引用，如果变量不在被使用，内存将被回收，这样 JS 引擎就可以存放新的值了。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_09.png 400 350 %}
  
观察内存中的变量（包括字符串，对象和其他类型的值），当它们不再有用的时候清除它们的过程，叫做垃圾回收。 

像 JavaScript 这种不用直接处理内存的语言，叫做内存自动管理语言。  

内存自动管理可以让开发人员开发程序更加简单，但也增加了程序的开销，这些开销有时候会让性能变得不可预测。  

## 手动管理内存

手动管理内存的语言不一样。举个例子，让我们看一下如果用 C 语言来写 React 的话（现在可以使用 WebAssembly 做到这一点），将如何处理内存。  

C 语言没有像 JavaScript 那样有一个抽象层来管理内存，相反，你可以直接操作内存，你可以从内存中加载数据，你也可以直接在内存中存储数据。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_10.png 400 350 %}
  
当你编译 C 或者其他语言成 WebAssembly 时，你使用的工具会添加一些辅助代码到你的 WebAssembly，例如添加编码和解码二进制字节的代码。这个代码叫做运行时环境。运行时代码会做一些像 JS 引擎在 JS 中做的事情。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_11.png 400 350 %}
  
但对于一个手动管理内存语言来说，运行期不包括垃圾回收。  

这并不意味着你什么事情都要自己做，即使是在手动管理内存语言里，你也会常受到语言运行期的帮助，拿 C 语言来说，运行期会跟踪自由列表中打开的内存地址。  

{% img https://2r4s9p1yi1fa2jd7j43zph8r-wpengine.netdna-ssl.com/files/2017/06/01_12.png 400 350 %}

你可以使用`malloc`方法（内存分配）来让运行期找到哪些内存地址可以来存放你的数据，这将从自由列表中取出这些地址，当你处理完这些数据，你必须使用`free`方法来释放内存，然后这些地址将重新回到自由列表中。  

你必须计算出什么时候来调用这些方法，这就是为什么我们叫它做手动内存管理了，你要自己来管理内存。  

对于一个开发人员来说，计算出什么时候该释放哪个区域的内存是很难的，如果你的计算时间出错了，那么将可能引发缺陷甚至会导致一个安全漏洞，如果你不释放内存，那么内存终将会耗尽。  

这就是为什么很多现代语言会使用自动内存管理，为了避免人为的错误，但这也带来了性能上的开销，我会在[下一篇文章](https://hacks.mozilla.org/2017/06/a-cartoon-intro-to-arraybuffers-and-sharedarraybuffers/) 讲更多这方面的内容。
