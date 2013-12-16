---
layout: post
title: "用腻了bootstrap的可以试试semantic-ui"
date: 2013-12-16 07:16
description: semantic-ui
keywords: semantic-ui
comments: true
categories: code
tags: [css,semantic-ui]
---

{% img /images/post/semantic-ui.png %}

##semancti-ui介绍
[semantic-ui][url1]是html/css框架的新贵，是继bootstrap和foundation之后的又一css神器。semantic-ui一出现在github上就受到火热的关注，一直在关注排行榜前列。semantic-ui最大的特点：充分利用CSS3动画特效，简洁实用漂亮的样式这些都是其最受欢迎的原因之一。  
<!--more-->  

##semantic-ui示例
刚刚进入到semantci-ui的网站的时候，就被它的各种控件样式吸引住了，感觉比bootstrap好看很多，又很符合当前流行的“扁平化设计”的风格,这里列一下几个看起来比较酷的控件。  

####好看的按钮
<div class="ui buttons">
  <div class="ui button">Cancel</div>
  <div class="or"></div>
  <div class="ui positive button">Save</div>
</div>
  
####代码如下
{% codeblock lang:html %}
<div class="ui buttons">
  <div class="ui button">Cancel</div>
  <div class="or"></div>
  <div class="ui positive button">Save</div>
</div>
{% endcodeblock %}  

<div class="ui divider"></div>  

####好看的标签
<div class="ui two column grid" style="display: block;">
  <div class="row">
    <div class="column">
      <div class="ui raised segment">
        <div class="ui ribbon label">Dogs</div>
        <p>Pretty nice animals.</p>
        <div class="ui teal ribbon label">Cats</div>
        <p>Also pretty nice animals, but can prefer solitude.</p>
        <div class="ui red ribbon label">Ogres and monsters</div>
        <p>Never seen one as a pet before, but I imagine they'd make pretty terrible companions.</p>
      </div>
    </div>
  </div>
</div>
  
####代码如下
{% codeblock lang:html %}
<div class="ui two column grid" style="display: block;">
  <div class="row">
    <div class="column">
      <div class="ui raised segment">
        <div class="ui ribbon label">Dogs</div>
        <p>Pretty nice animals.</p>
        <div class="ui teal ribbon label">Cats</div>
        <p>Also pretty nice animals, but can prefer solitude.</p>
        <div class="ui red ribbon label">Ogres and monsters</div>
        <p>Never seen one as a pet before, but I imagine they'd make pretty terrible companions.</p>
      </div>
    </div>
  </div>
</div>
{% endcodeblock %}  

<div class="ui divider"></div>  

####好看的注解
<div class="ui comments">
  <div class="comment">
    <a class="avatar">
      <img src="/images/post/photo2.jpg">
    </a>
    <div class="content">
      <a class="author">Dog Doggington</a>
      <div class="text">
        I think this is a great idea and i am voting on it
      </div>
    </div>
  </div>
</div>

####代码如下
{% codeblock lang:html %}
<div class="ui comments">
  <div class="comment">
    <a class="avatar">
      <img src="/images/post/photo2.jpg">
    </a>
    <div class="content">
      <a class="author">Dog Doggington</a>
      <div class="text">
        I think this is a great idea and i am voting on it
      </div>
    </div>
  </div>
</div>
{% endcodeblock %}  
  
是不是非常酷？想使用这些很酷很炫的特性，就赶快使用semantic-ui吧～

##semantic-ui兼容性

**支持的浏览器如下**  

* Last 2 Versions FF, Chrome, IE (aka 10+)
* Safari 6
* IE 9+ (Browser prefix only)
* Android 4
* Blackberry 10
  
前几天在semantic-ui的github项目里提了个问题，问[semantic-ui是否支持IE6～8][url2]，下面有个老外来了句：Support IE6? Are you creazy?呵呵，看来在国外还是不鸟IE的偏多。  

在这里吐槽一下IE，IE可以算是浏览器界的一朵奇葩...不对，奇葩还有些褒意在里面，应该是浏览器界的一个毒瘤，一直阻碍着前端框架的发展，好在现在很多前端框架都不care IE了，比如angularjs，bootstrap等就直接只支持IE8+（不包括IE8），就连鼎鼎大名的Jquery也宣布从2.x版本开始不支持IE6～8，看来IE的淘汰是在所难免了哈。  
  
不过IE9以后情况还是有所好转，《Javascript高级程序设计》的作者 Nicholas C. Zakas也让大家对IE9以后的版本可以另眼相看。但不管怎么样，IE6～8的各种bug和不兼容让web前端开发举步维艰是个不争的事实，该淘汰的时候就应该淘汰。最好的做法让用户升级浏览器或改用chrome，不要在兼容性上面浪费精力。

[url1]: http://semantic-ui.com/
[url2]: https://github.com/Semantic-Org/Semantic-UI/issues/431
