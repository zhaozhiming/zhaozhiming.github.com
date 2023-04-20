---
layout: post
title: "扒一扒 Chatgpt 背后的 web 开发技术（二）"
date: 2023-04-18 21:23:00
description: 扒一扒 Chatgpt 背后的 web 开发技术
keywords: chatgpt, marked 
comments: true
categories: chatgpt
tags: [chatgpt]
---

{% img /images/post/2023/04/chatgpt.jpeg 400 300 %}

上次聊了 ChatGPT 的后端技术，这次再聊聊它的前端技术。ChatGPT 前端技术在聊天机器人应用中扮演着重要的角色，它不仅能够帮助开发人员构建自然、智能的对话界面，还可以支持各种文本渲染效果。在本文中，我们将会探讨 ChatGPT 文本渲染的实现原理，希望能够帮助你更好地构建你的聊天机器人应用。

<!--more-->

## Markdown 在 ChatGPT 中的应用 

Markdown 是一种轻量级标记语言，它使用简单的标记符号，如星号和井号等，来标记文本的样式和格式，例如加粗、斜体、标题、列表等。Markdown 文本可以很容易地转换成 HTML 或其他格式，因此被广泛用于写作、博客、文档等领域。

ChatGPT 后端返回的对话文本中，可能会包含 Markdown 格式的文本内容，如代码块、表格、数学公式等。ChatGPT 需要能够解析这些文本内容，并将其渲染成人类可以识别的格式。由于 Markdown 简单易用、具有可读性、可扩展性等优点，因此 ChatGPT 中选择使用 Markdown 作为标记语言，并借助相关工具将 Markdown 文本转换成 HTML 格式，实现各种文本效果，使 ChatGPT 能够更好地展示内容给用户。

## marked 介绍

marked 是一个流行的、高性能的 Markdown 解析器和编译器，使用 JavaScript 编写。它可以将 Markdown 文本快速转换为 HTML，因此广泛应用于前端项目和 Node.js 环境中。

在 ChatGPT 的前端页面中，marked 扮演了重要角色。由于 ChatGPT 需要处理和展示大量富文本内容，使用 marked 解析和编译 Markdown 文本成为了一个理想的解决方案。ChatGPT 利用 marked 的高性能特点，在实时预览功能中快速地将用户输入的 Markdown 文本转换为 HTML，以便用户能即时查看格式化后的效果，这为用户提供了一种高效的阅读体验。

## 渲染代码

在 ChatGPT 的前端页面中，使用 marked 结合 highlight.js 来渲染回答里的代码，marked 负责将 Markdown 文本转换为 HTML，而 highlight.js 则用于为代码片段提供语法高亮。

{% img /images/post/2023/04/chatgpt-code.png 600 400 %}

首先，引入 highlight.js 库，安装并导入所需的样式。接下来，创建一个自定义的 marked 渲染器，并重写代码块渲染方法。在这个方法中，使用 highlight.js 的 highlight() 函数对代码片段进行高亮处理，代码示例如下。

```js
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css'; // 或其他样式
```

这里我选择了 github 暗色主题的样式，highlight 还有很多种样式库，可以在`node_modules/highlight.js/styles/`目录下找到。如果你想自定义样式，可以在`node_modules/highlight.js/styles/`目录下找到`github-dark.css`，然后复制到你的项目中，修改样式后引入即可。

然后是创建一个自定义的 marked 渲染器，并重写代码块渲染方法。在这个方法中，使用 highlight.js 的 highlight() 函数对代码片段进行高亮处理，代码示例如下。

```js
const renderer = new marked.Renderer();

renderer.code = (code, language) => {
  const validLang = hljs.getLanguage(language) ? language : 'plaintext';
  const highlightedCode = hljs.highlight(code, { language: validLang }).value;
  return `<pre><code class="hljs ${validLang}">${highlightedCode}</code></pre>`;
};
```

代码块的渲染方法中，返回结果一般都是一个`<pre>`标签，里面包含一个`<code>`标签，`<code>`标签中的内容是经过 highlight.js 处理后的代码片段。如果你想在代码块中像 ChatGPT 那样有个显示代码语言和复制代码的头部，可以在`<pre>`标签中添加一个`<div>`标签来展示这些内容，代码示例如下。

```js
renderer.code = (code, language) => {
  const validLang = hljs.getLanguage(language) ? language : 'plaintext';
  const highlightedCode = hljs.highlight(code, { language: validLang }).value;
  return `<pre>
    <div class="code-header">
      <span>${validLang}</span>
      <button>复制</button>
    </div>
    <code class="hljs ${validLang}">${highlightedCode}</code>
  </pre>`;
};
```

highlight.js 还可以检测代码的语言，如果代码的语言不在支持的语言列表中，可以使用`plaintext`作为默认语言。最后，将自定义的渲染器传入 marked 的`setOptions()`方法中，即可完成代码的渲染。

```js
marked.setOptions({ renderer });
```

如果你是使用 React 来开发的话，可以使用 React 的 dangerouslySetInnerHTML 来渲染代码，代码示例如下。

```js
<div dangerouslySetInnerHTML={{__html: marked.parse(message)}} />
```

## 渲染表格

ChatGPT 不仅可以渲染代码，还可以渲染表格。在 ChatGPT 的前端页面中，使用 marked 结合自定义样式的方式来渲染回答里的表格。

{% img /images/post/2023/04/chatgpt-table.png 600 400 %}

和渲染代码块一样，我们需要在 marked 渲染器上重写表格的渲染方法，代码示例如下。

```js
renderer.table = (header, body) => {
  return `<table class="your-table-style">
    <thead>${header}</thead>
    <tbody>${body}</tbody>
  </table>`;
};
```

表格渲染方法的返回结果一般是一个`<table>`标签，里面包含一个`<thead>`标签和一个`<tbody>`标签，`header`参数是表格的表头，`body`参数是表格的表体。在`table`标签中加上你的自定义 class，然后在样式文件中实现自己想要的效果即可。

## 渲染数学公式

除了代码和表格，ChatGPT 还可以渲染数学公式。在 ChatGPT 的前端页面中，使用 marked 结合 KaTeX 的方式来渲染回答里的数学公式。

{% img /images/post/2023/04/chatgpt-formula.png 600 400 %}

KaTeX 是一个高性能的数学公式渲染库，可以在浏览器和服务器端快速渲染 [LaTeX 数学公式](https://zh.wikibooks.org/wiki/LaTeX/%E6%95%B0%E5%AD%A6%E5%85%AC%E5%BC%8F)。KaTeX 具有出色的性能和高度兼容性，适用于各种现代浏览器。与其他数学公式渲染库相比，KaTeX 更轻量且渲染速度更快。

首先引入 KaTeX 库及其相关的样式：

```js
import katex from 'katex';
import 'katex/dist/katex.min.css';
```

接着定义内联公式解析和渲染规则：

```js
const katexInline = {
  name: 'katexInline',
  level: 'inline',
  start(src: string) {
    return src.indexOf('$');
  },
  tokenizer(src: string) {
    const match = src.match(/^\$+([^$\n]+?)\$+/);
    if (match) {
      return {
        type: 'katexInline',
        raw: match[0],
        text: match[1].trim(),
      };
    }
  },
  renderer(token: marked.Tokens.Generic) {
    return katex.renderToString(token.text, katexOptions);
  },
};
```

这里创建了一个名为`katexInline`的对象，用于处理内联公式。`tokenizer`方法用正则表达式匹配以`$`符号包围的内联公式。如果匹配成功，返回一个包含`type`、`raw`和`text`属性的对象。renderer 方法使用 KaTeX 的 renderToString 方法将匹配到的公式文本转换为 HTML 字符串。

然后定义块级公式解析和渲染规则：

```js
const katexBlock = {
  name: 'katexBlock',
  level: 'block',
  start(src: string) {
    return src.indexOf('\n$$');
  },
  tokenizer(src: string) {
    const match = src.match(/^\$\$+\n([^$]+?)\n\$\$+\n/);
    if (match) {
      return {
        type: 'katexBlock',
        raw: match[0],
        text: match[1].trim(),
      };
    }
  },
  renderer(token: marked.Tokens.Generic) {
    return `<p>${katex.renderToString(token.text, katexOptions)}</p>`;
  },
};
```

这里创建了一个名为`katexBlock`的对象，用于处理块级公式。`tokenizer`方法用正则表达式匹配以`$$`符号包围的块级公式。如果匹配成功，返回一个包含`type`、`raw`和`text`属性的对象。`renderer`方法使用 KaTeX 的`renderToString`方法将匹配到的公式文本转换为 HTML 字符串，并包含在一个`<p>`标签内。

最后将先前定义的`katexInline`和`katexBlock`对象注册为 marked 库的扩展：

```js
marked.use({ extensions: [katexInline, katexBlock] });
```
 
这样，marked 库就能在解析和渲染 Markdown 文本时，应用这两个扩展来处理内联公式和块级公式了，借助 KaTeX 库呈现出美观的数学公式效果。

## 总结

今天介绍了 ChatGPT 页面中几种常见的渲染效果，包括代码、表格和数学公式，以及如何使用 marked 库和其他库来实现这些渲染效果。通过以上的例子，你应该已经掌握了如何在前端项目中使用 marked 库结合其他库来渲染页面。这些技术可以帮助你在开发过程中轻松实现高质量的内容展示，为用户提供更优质的阅读体验。