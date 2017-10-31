---
layout: post
title: "如何处理 Echarts 中的地图散列点点击事件"
date: 2017-10-27 14:33
description: 如何处理 Echarts 中的地图散列点点击事件
keywords: echarts
comments: true
categories: code
tags: [echarts]
---

{% img /images/post/2017/10/echarts.png 400 300 %}

Echarts 是百度比较成功的一个开源图形库，现在国内很多公司和项目都使用它。今天介绍一下基于 Echarts 3.0，如何处理地图散列点的点击事件。

<!--more-->

## 问题介绍

{% img /images/post/2017/10/echarts_map.png %}

我们想在地图上处理散列点（图中红点）的点击事件，比如点击某个点的时候弹出一个 Modal 框，或者跳转一个新页面什么的。但是 Echarts 提供给我们的事件 API 非常有限，就只有一个比较笼统的`click`事件 API，我们如何通过这个 API 来实现我们需要的效果呢？

## 地图散列点效果

首先我们来看下实现该图形的核心代码，如下所示：

{% codeblock lang:js %}
  const options = {
    series: [
      {
        name: 'foo',
        type: 'map',
        geoIndex: 0,
        data: [/* 地图相关数据 */],
        zlevel: 3,
      },
      {
        type: 'effectScatter',
        animation: false,
        data: [/* 散列点数据 */],
        coordinateSystem: 'geo',
        showEffectOn: 'render',
        rippleEffect: {
          scale: 5,
          brushType: 'fill',
        },
        itemStyle: {
          normal: {
            color: 'red',
            shadowBlur: 10,
            shadowColor: 'red',
          },
        },
        zlevel: 1,
      },
    ],
  };
{% endcodeblock %}

这里主要列出 Echarts 里面的 options 参数，对 Echarts 开发比较熟悉的同学应该会知道，options 是每个 Echarts 图形的配置对象。

上面代码显示了中国地图各个省份的指数分布，另外还显示了一些地图上的散列点，所以这里面有 2 种不同类型的数据。

可以看到 options 里面 series 属性包含了 2 种对象，一种 type 是`map`的对象，表示地图各个省份的指数分布；另外一种对象的 type 是`effectScatter`，表示的是地图上面的散列点。

## Echarts 的点击事件

然后我们再来看 Echarts 的点击事件，官方代码如下：

{% codeblock lang:js %}
myChart.on('click', function (params) {
  // 点击事件操作
});
{% endcodeblock %}

对，就只有一个`click`事件，没有针对不同类型图形的点击事件。

我们可以试用一下这个点击事件，看看里面的`params`参数能不能对我们有所帮助？下面是一个点击全国地图某省份事件的`params`参数：

{% codeblock lang:js %}
params: {
    componentSubType: "map",
    componentType:"series",
    data: {name: "西藏", value: 75},
    dataIndex:27,
    name: "西藏",
    seriesId: "0",
    seriesIndex: 0,
    seriesName: "foo",
    seriesType:"map",
    type: "click",
    value: 75,
}
{% endcodeblock %}

可以看到这个对象里面包含了很多属性，有类型、数据对象，系列名称、值等，但这里面看不出有什么是我们需要的。

那我们再来看下点击了某个散列点后的参数，看看有什么不同。

{% codeblock lang:js %}
params: {
    componentSubType : "effectScatter", 
    componentType : "series",
    data : {name: "point1", value: Array(3)}, 
    dataIndex : 0, 
    name : "point1",
    seriesId : "-0", 
    seriesIndex : 1, 
    seriesName : "-", 
    seriesType : "effectScatter", 
    type: "click", 
    value:[125.03, 46.58, 20], 
}
{% endcodeblock %}

对比发现，我们可以通过`seriesType`知道是哪种类型数据的点击事件，或者通过`seriesIndex`也可以知道。

然后通过`dataIndex`我们可以得到数据在原始数组中的位置，或者直接通过`data`和`value`来直接获取点的相关数据。

## 根据参数判断是哪种图形的点击事件

其实饶了这么一大圈就是想让大家知道，我们可以在`click`事件中，通过这些参数来判断哪种图形的点击事件是我们想要处理的，如果不是我们需要处理的点击事件，我们直接跳出函数就可以了。示例代码如下：

{% codeblock lang:js %}
myChart.on('click', function (params) {
  const { seriesIndex, seriesType } = params;
  // 方法一：通过 seriesIndex 来判断，map 类型的 seriesIndex 为 0
  if (seriesIndex === 0) return;

  // 方法二：通过 seriesType 来判断
  if (seriesType === 'map') return;

  // 通过上面判断逻辑走到这里的就是散列点的事件了
});
{% endcodeblock %}

## 缺点分析

虽然这个方法可以解决问题，但是有个不好的地方需要指出，就是点击散列点的时候，鼠标点击的位置必须非常准确，要点击散列点的中心位置才能捕获到该类型事件，否则看起来就像点击没有效果一样。

## Echarts 的地图数据

另外在这里提示一下，很多同学可能以为 Echarts 的地图数据不能下载了，因为 [Echarts 的官方网站](http://ecomfe.github.io/echarts-map-tool/) 上也是这么写的，但是 Echarts 的 github 项目中并没有删除这些数据 ([见这里](https://github.com/ecomfe/echarts/tree/master/map))，所以大家不要被 Echarts 忽悠了😄。

这些数据包括`js`和`json`两种格式的，内容包括中国地图及各省份的地图。

## 总结

文章介绍了处理 Echarts 地图散列点事件的方法，如果大家有更好的方法，希望留言一起讨论，最后附上文章相关的在线 Demo 例子。[本文的 Demo](https://codesandbox.io/s/lxljr76vp7)

