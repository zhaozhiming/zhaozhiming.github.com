---
layout: post
title: "现代网页兼容 IE11"
date: 2019-01-24 17:31
description: 现代网页兼容 IE11
keywords: react,es6,css3,ie11
comments: true
categories: react
tags: [react,es6,css3,ie11]
---

{% img /images/post/2019/01/ie11.png 400 300 %}

{% blockquote %}
前端：兼容 IE 是不可能的，这辈子都不可能兼容 IE
BOSS：给你加薪 50%
前端：IE 是我用过最好用的浏览器（真诚脸）
{% endblockquote %}

当你的网页使用了最新的流行技术（比如 React、CSS3, ES6/7 等）却被要求兼容 IE 浏览器的时候你的内心其实是崩溃的，但毕竟 IE 存在了这么久要指望它一下子消失是不太现实的，所以作为一个有 (mei) 追 (ban) 求 (fa) 的前端我们还是来看看如何兼容 IE 吧（这里只考虑 IE11，其他的 IE 古董版本建议放弃）。

<!--more-->

React 是目前用于编写前端页面最流行且使用最多的前端视图库（没有之一），所以我们主要介绍在 React 环境下如何兼容 IE11。

## 使用 IE 最高版本

React 可以在支持 ES5 语法的浏览器上运行，这要求 IE 的版本必须不低于 IE9。在一些低版本的 Window 系统中会默认用低版本的 IE 引擎来渲染页面，如果页面需要在高版本的 IE 引擎上运行的话，需要在 html 页面中加入下面的代码。

{% codeblock lang:html %}
<meta http-equiv="X-UA-Compatible" content="IE=edge">
{% endcodeblock %}

这样页面就会使用系统中最高版本的 IE 引擎来加载页面，更多资料可以参考[这里](https://stackoverflow.com/questions/6771258/what-does-meta-http-equiv-x-ua-compatible-content-ie-edge-do)。

## ES 6/7

现在越来越多的网页开始使用 ES6 的语法，ES6 相对于 ES5 新增了很多新特性和语法糖，减少了原始 JS 的一些语法混乱，让代码更加简洁，让 JS 开发人员更加高效。

React 可以在支持 ES5 的浏览器上运行，包括 IE9 和 IE10，但如果你使用了 ES6/7 的语法，则需要额外做一些操作才能兼容 IE11。

### React Polyfill

大部分 React 开发人员通过 [Create React App](https://github.com/facebook/create-react-app) 来创建项目，它是 Facebook 提供的 React 脚手架工具，可以方便 React 新人快速创建 React 项目。

在这个脚手架的代码仓库中，包含了一些 polyfill（可以理解为一种补丁，让不支持该功能的设备或环境可以正常运行的补丁），可以让 React 项目在 IE 浏览器中完美运行。

#### 功能特性

* 支持 `Promise`，`async/await`
* 支持 `window.fetch`
* 支持 `Object.assign`
* 支持 `Symbol`
* 支持 `Array.from`

#### 使用步骤

* 通过`npm install react-app-polyfill`安装 polyfill
* 在入口文件（通常是`src/index.js`）的第一行引入该库

{% codeblock lang:js %}
// 在文件的第一行导入，兼容 IE11
import 'react-app-polyfill/ie11';
{% endcodeblock %}

更多内容可以查阅[这里](https://github.com/facebook/create-react-app/tree/master/packages/react-app-polyfill)。

### core-js

即使使用了`React Polyfill`你的项目也可能在 IE11 上运行不起来，因为`React Polyfill`并没有支持全部 ES6 的语法，这时候我们可以选择另外一个第三方库 [core-js](https://github.com/zloirock/core-js)，这个库除了支持`React Polyfill`的功能特性外（window.fetch 不支持），**还支持 ES6/7/8 一些常用的 API，比如 array.includes，string.includes，string.startsWith 等**，可以在不支持这些特性的浏览器上正常运行。

#### 使用步骤

* 通过`npm install core-js`命令进行安装
* 在入口文件（通常是`src/index.js`）的第一行引入该库

{% codeblock lang:js %}
// 在文件的第一行导入
import 'core-js';
{% endcodeblock %}

`core-js`包含了`React Polyfill`大部分的功能（除了 window.fetch），所以通常没有必要两个库一起使用，只需要使用`core-js`就好了。

## CSS 变量

在很多 CSS 预处理器中已经包含了变量的特性，比如 Less、Sass 等，但在 CSS 的最新语法中已经可以支持变量，用法如下：

{% codeblock lang:css %}
:root {
  --main-bg-color: brown;
}

.app {
  background-color: var(--main-bg-color);
}
{% endcodeblock %}

虽然微软已经在 Edge 浏览器中支持 CSS 变量，但很遗憾，在 IE11 中仍然不支持，如果想在 IE11 中运行有 CSS 变量的代码，我们需要引入 [css-vars-ponyfill](https://github.com/jhildenbiddle/css-vars-ponyfill) 这个库。

### 使用方法

* 首先通过命令安装这个库`npm install css-vars-ponyfill`。
* 然后在入口文件引入这个库就可以了。

{% codeblock lang:js %}
import cssVars from 'css-vars-ponyfill';

// 默认配置
cssVars();

// 还可以带配置参数
cssVars({
  // 配置参数
});
{% endcodeblock %}

`css-vars-ponyfill`会将项目中的 CSS 变量进行转换，在 IE11 中会直接转换成对应的变量值，这样带有 CSS 变量的项目就可以在 IE11 中运行了。

## Flexbox

Flexbox 弹性盒子布局出来已经有一段时间了，广受前端开发人员喜欢，现在慢慢地其他平台也在使用这种布局方式，比如 React Native 等。

Flexbox 已经被所有浏览器所支持...... 除了 IE，IE 因为有些 bug 无法修复所以只做到了部分支持，具体原因可以查阅 [Can I Use](https://caniuse.com/#search=flexbox)。

那如何在 IE11 中正常运行 Flexbox 呢？这个时候我们可以使用一些小技巧来让我们的 CSS 特殊样式只在 IE 上运行。

其实针对每种浏览器都有对应的 CSS 选择器来让 CSS 只在对应的浏览器上运行，比如 IE11 的特殊选择器是这样：

{% codeblock lang:css %}
// IE11
_:-ms-fullscreen, :root .selector {
  property: value;
}
{% endcodeblock %}

在这个选择器里面的 CSS 代码只会在 IE11 浏览器中生效，在其他浏览器不会有任何作用。

还有 IE10 的特殊选择器也列举下：

{% codeblock lang:css %}
// IE10
_:-ms-input-placeholder, :root .selector {
  property: value;
}
{% endcodeblock %}

这样我们就可以通过一些只有 IE 会运行的 CSS 代码来微调我们的 Flexbox 布局了，从而达到实现 Flexbox 布局效果的目的。

更多的资料可以查阅这里：

* [Separating Internet Explorer 10 and 11 via CSS Hacks](https://jeffclayton.wordpress.com/2014/07/14/separating-internet-explorer-10-and-11-via-css-styles/)
* [CSS3 Media Query to target only Internet Explorer (from IE6 to IE11+), Firefox, Chrome, Safari and/or Edge](https://www.ryadel.com/en/css3-media-query-target-only-ie-ie6-ie11-firefox-chrome-safari-edge/)


## 总结

文章从 HTML、JS 和 CSS 几个方面介绍了如何兼容 IE11 浏览器，但这里只是介绍了笔者经常遇到的一些问题，其实在兼容浏览器过程还会遇到更多更复杂的场景，需要根据具体问题进行具体分析，希望有相关经验的同学一起留言讨论，谢谢。
