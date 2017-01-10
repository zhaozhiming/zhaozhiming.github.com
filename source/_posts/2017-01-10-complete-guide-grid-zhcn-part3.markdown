---
layout: post
title: "Grid 的完整介绍（三）"
date: 2017-01-10 19:30
description: Grid 的完整介绍（三）
keywords: css,grid
comments: true
categories: css
tags: [css,grid]
---

{% img /images/post/2017/01/css_grid3.jpeg 400 350 %}

接着上篇 blog，这篇继续介绍 Grid 的网格子项属性，内容没有那么长了。

原文地址见这里：[A Complete Guide to Grid](https://css-tricks.com/snippets/css/complete-guide-grid/)。

* [Grid 的完整介绍（一）](http://zhaozhiming.github.io/blog/2017/01/08/complete-guide-grid-zhcn-part1/)
* [Grid 的完整介绍（二）](http://zhaozhiming.github.io/blog/2017/01/09/complete-guide-grid-zhcn-part2/)

<!--more-->

## Grid 的完整介绍（三）

### 网格子项的属性

* [grid-column-start](#grid-column-start)
* [grid-column-end](#grid-column-end)
* [grid-row-start](#grid-row-start)
* [grid-row-end](#grid-row-end)
* [grid-column](#grid-column)
* [grid-row](#grid-row)
* [grid-area](#grid-area)
* [justify-self](#justify-self)
* [align-self](#align-self)

#### grid-column-start
#### grid-column-end
#### grid-row-start
#### grid-row-end
  
通过参考指定的网格线来决定网格中一个网格子项的位置，`grid-column-start/grid-row-start`是指网格子项开始的线，`grid-column-end/grid-row-end`是指网格子项结束的线。

值有：

* `<line>` - 可以是一个数字以适用被标记了数字号的网格线，或者是一个名字以适用命名了的网格线
* span `<number>` - 子项将跨越指定数字的网格轨迹
* span `<name>` - 子项将跨越到指定名字之前的网格线
* auto - 表示自动布局，自动跨越或者默认跨越一个

{% codeblock lang:css %}
.item{
  grid-column-start: <number> | <name> | span <number> | span <name> | auto
  grid-column-end: <number> | <name> | span <number> | span <name> | auto
  grid-row-start: <number> | <name> | span <number> | span <name> | auto
  grid-row-end: <number> | <name> | span <number> | span <name> | auto
}
{% endcodeblock %}

举个例子：

{% codeblock lang:css %}
.item-a{
  grid-column-start: 2;
  grid-column-end: five;
  grid-row-start: row1-start
  grid-row-end: 3
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-start-end-a.png %}

{% codeblock lang:css %}
.item-b{
  grid-column-start: 1;
  grid-column-end: span col4-start;
  grid-row-start: 2
  grid-row-end: span 2
}
{% endcodeblock %}

{% img http://chris.house/images/grid-start-end-b.png %}

如果`grid-column-end/grid-row-end`没有生命，网格子项将默认跨越一个网格轨迹。

网格子项可以互相重叠，你可以使用`z-index`来控制他们的层叠顺序。

#### grid-column
#### grid-row

`grid-column-start` + `grid-column-end`，和`grid-row-start` + `grid-row-end`的简写，分别独立。

值有：

* <start-line> / <end-line> - 每一个属性都可以接收普通模式的值，包括`span`

{% codeblock lang:css %}
.item{
  grid-column: <start-line> / <end-line> | <start-line> / span <value>;
  grid-row: <start-line> / <end-line> | <start-line> / span <value>;
}
{% endcodeblock %}

举例：

{% codeblock lang:css %}
.item-c{
  grid-column: 3 / span 2;
  grid-row: third-line / 4;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-start-end-c.png %}

如果没有声明结束网格线的值，那么网格子项将默认跨越 1 个网格轨迹。

#### grid-area

给网格子项取一个名字以让它被由`grid-template-areas`属性创建的模板引用。同时，这个属性也可以用来更简短地表示`grid-row-start`+ `grid-column-start` + `grid-row-end`+ `grid-column-end`。

值有：

* `<name>` - 一个你选择的名字
* <row-start> / <column-start> / <row-end> / <column-end> - 可以是网格线的数字或名字

{% codeblock lang:css %}
.item{
  grid-area: <name> | <row-start> / <column-start> / <row-end> / <column-end>;
}
{% endcodeblock %}

举例：

作为分配一个名字给网格子项的一种方式：

{% codeblock lang:css %}
.item{
.item-d{
  grid-area: header
}
{% endcodeblock %}

作为`grid-row-start`+ `grid-column-start` + `grid-row-end`+ `grid-column-end`的一种简写：

{% codeblock lang:css %}
.item-d{
  grid-area: 1 / col4-start / last-line / 6
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-start-end-d.png %}

#### justify-self

让网格子项的内容以列轴对齐（与之相反`align-self`是跟行轴对齐），这个值可以应用在单个网格子项的内容中。

值有：

* start - 让内容在网格区域左对齐
* end - 让内容在网格区域右对齐
* center - 让内容在网格区域中间对齐
* stretch - 填充着呢个网络区域的宽度（默认值）

{% codeblock lang:css %}
.item{
  justify-self: start | end | center | stretch;
}
{% endcodeblock %}

举例：

{% codeblock lang:css %}
.item-a{
  justify-self: start;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-justify-self-start.png %}

{% codeblock lang:css %}
.item-a{
  justify-self: end;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-justify-self-end.png %}

{% codeblock lang:css %}
.item-a{
  justify-self: center;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-justify-self-center.png %}

{% codeblock lang:css %}
.item-a{
  justify-self: stretch;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-justify-self-stretch.png %}

为了让网格中的所有子项都对齐，这个行为也可以通过设置网格容器中的`justify-items`属性来实现。

#### align-self

让网格子项的内容以行轴对齐（与之相反`justify-self`是跟列轴对齐），这个值可以应用在单个网格子项的内容中。

值有：

* start - 让内容在网格区域上对齐
* end - 让内容在网格区域下对齐
* center - 让内容在网格区域中间对齐
* stretch - 填充着呢个网络区域的高度（默认值）

{% codeblock lang:css %}
.item{
  align-self: start | end | center | stretch;
}
{% endcodeblock %}

举例：

{% codeblock lang:css %}
.item-a{
  align-self: start;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-align-self-start.png %}

{% codeblock lang:css %}
.item-a{
  align-self: end;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-align-self-end.png %}

{% codeblock lang:css %}
.item-a{
  align-self: center;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-align-self-center.png %}

{% codeblock lang:css %}
.item-a{
  align-self: stretch;
}
{% endcodeblock %}

{% img https://cdn.css-tricks.com/wp-content/uploads/2016/03/grid-align-self-stretch.png %}

为了让网格中的所有子项都对齐，这个行为也可以通过设置网格容器中的`align-items`属性来实现。


