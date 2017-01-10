---
layout: post
title: "Grid 的完整介绍（一）"
date: 2017-01-08 19:30
description: Grid 的完整介绍（一）
keywords: css,grid
comments: true
categories: css
tags: [css,grid]
---

{% img /images/post/2017/01/css_grid.png 400 350 %}

CSS 的 flex 特性刚推出不久，现在 grid 特性又快要出来了，感谢这些新特性，让前端开发者不用像以前那么痛苦地做页面布局了。为了更好的理解这篇博文，决定把它翻译出来。

原文地址见这里：[A Complete Guide to Grid](https://css-tricks.com/snippets/css/complete-guide-grid/)。

* [Grid 的完整介绍（二）](http://zhaozhiming.github.io/blog/2017/01/09/complete-guide-grid-zhcn-part2/)
* [Grid 的完整介绍（三）](http://zhaozhiming.github.io/blog/2017/01/10/complete-guide-grid-zhcn-part3/)

<!--more-->

## Grid 的完整介绍（一）

### 介绍

CSS Grid 布局（又叫“Grid”)，是一个基于网格的二维布局系统，目的是为了完全改变我们基于网格设计用户界面的方式。CSS 可以用来做我们的网页布局，但它在这一方面做的不是很好。开始的时候我们使用`tables`, 然后使用`floats`，`positioning`和`inline-block`，但这些方法本质上都是 hack 的方法并缺少一些重要功能（比如`垂直居中`）。`Flexbox`帮助我们解决了问题，但它是简单的一维布局，而不是复杂的二维布局（实际上 Flexbox 和 Grid 可以很好地组合起来使用）。Grid 是第一个专门为了解决那些我们一直使用 hack 手段而导致的页面布局问题而创建的 CSS 模块。

我写这篇文章主要收到两个事情启发，第一个是`Rachel Andrew`写的一本好书——《Get Ready for CSS Grid Layout》，这本书把对 Grid 全面而清晰的介绍作为全书的基调，我高度推荐大家去买这本书来读一下。我另外一件受启发的事情是`Chris Coyier`对 Flexbox 的完整介绍，这是我推荐学习 flexbox 的首选资源，它帮助了很多人，当你用 Google 搜索 flexbox 时可以从它的搜索结果看出其影响范围。你可以看到那篇文章跟我的文章有很多相似的地方，因为我这篇文章就是通过模仿那篇最好的文章来写的。（译者注：可以看到这两篇文章都是按照两列分布的方式来介绍 flexbox 和 Grid。）

我这篇文章的目的是为了介绍 Grid 在最新规范中的概念，所以我不会涵盖过时的 IE 语法，并且当规范更新时我将尽力更新这篇文章。

### 基础和浏览器支持

开始使用 Grid 非常简单，你只需要通过`display: grid`来定义一个容器元素作为网格，再通过`grid-template-columns`和`grid-templaet-rows`设置列和行的大小，然后通过`grid-column`和`grid-row`来设置网格的子元素，grid 元素的顺序对其实现的效果没有任何影响。你的 CSS 可以任意调节它们的顺序，这可以让你很方便地在媒体查询中重新编排你的网格。想象一下在你的整个页面中定义了一个布局，然后通过几行 CSS 代码就可以重新编排出另外一个布局来适应另外一个屏幕，所以说 Grid 是有史以来最强大一个的 CSS 模块。

**理解 Grid 最重要的一件事情是现在还不能把它用在生产环境。**它现在还只是一个 W3C 的在制品草稿，还没有任何浏览器默认是支持它的。IE10 和 11 可以支持它，但它们是用过时的语法做的一个老旧的实现。最好地使用 Grid 的方式是设置 Chrome，Opera 或者 Firefox 的特殊标志来启用它。在 Chrome 中，在地址栏输入`chrome://flags`然后将`experimental web platform features`选项设置为`enable`，这个方法同样适用于 Opera(`opera://flags`)，在 Firefox 中，将`layout.css.grid`选项设置为可用。

这是一个支持的浏览器表格，我将及时更新它：

<table class="browser-support-table">
<thead>
<tr>
<th class="chrome"><span>Chrome</span></th>
<th class="safari"><span>Safari</span></th>
<th class="firefox"><span>Firefox</span></th>
<th class="opera"><span>Opera</span></th>
<th class="ie"><span>IE</span></th>
<th class="android"><span>Android</span></th>
<th class="iOS"><span>iOS</span></th>
</tr>
</thead>
<tbody>
<tr>
<td class="yep" data-browser-name="Chrome">29+ (Behind flag)</td>
<td class="nope" data-browser-name="Safari">Not supported</td>
<td class="yep" data-browser-name="Firefox">40+ (Behind flag)</td>
<td class="yep" data-browser-name="Opera">28+ (Behind flag)</td>
<td class="yep" data-browser-name="IE">10+ (Old syntax)</td>
<td class="nope" data-browser-name="Android">Not supported</td>
<td class="nope" data-browser-name="iOS">Not supported</td>
</tr>
</tbody>
</table>

除了微软，其他浏览器好像不想太早实现 Grid 直到规范完全成熟为止，这是一件好事，这意味着我们不用担心以后使用 Grid 要使用多种语法。
在生产环境使用 Grid 只是时间上的问题，但现在是时候可以学习它了。

### 重要的术语

在开始了解 Grid 的概念之前先理解其相关的术语是很重要的，因为这里涉及的概念都有点相似，所以如果你不记住它们在规范中的定义的话会很容易被搞混，但请不用担心，这里的术语并不多。

#### 网格容器

网格容器是指这个元素使用了`display: grid`，它是所有网格元素的直接父级，在这个例子`container`的元素就是网格容器。

{% codeblock lang:html %}
<div class="container">
  <div class="item item-1"></div>
  <div class="item item-2"></div>
  <div class="item item-3"></div>
</div>
{% endcodeblock %}

#### 网格子项

网格子项是指网格容器的子元素（比如其后代），在下面的例子中`item`的元素是网格子项，但`sub-item`的元素不是。

{% codeblock lang:html %}
<div class="container">
  <div class="item"></div>
  <div class="item">
    <p class="sub-item"></p>
  </div>
  <div class="item"></div>
</div>
{% endcodeblock %}

#### 网格线

分隔的线组成了网格的结构。它们可以是垂直的（“列网格线”）或者水平的（“行网格线”），也可以在行或列的任一边。下面的例子中黄色的线是一个列网格线的例子。

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-line.png %}

#### 网格轨迹

网格轨迹是指两根毗邻线中间的位置，你可以认为是网格的行或者列，下面例子的中网格轨迹是第二和第三行网格线中间的位置。

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-track.png %}

#### 网格单元

网格单元是指两根毗邻的行网格线和列网格线中间的位置，它是一个单独的网格“单元”，下面的例子中网格单元是指第 1 和 2 根行网格线和第 2 和 3 根列网格线中间的位置。

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-cell.png %}

#### 网格区域

网格区域是指 4 根网格线包围的空间，一个网格空间可能由任意数量的网格单元构成。下面的例子中网格区域是指在第 1 和 3 的行网格线和第 1 和 3 列网格线中间的位置。

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-area.png %}

