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
##扁平化设计
随着iOS 7的发布，扁平化设计(flat design)被更多人所熟识。什么是扁平化设计呢？在实际当中，扁平化设计一词所指的是抛弃那些已经流行多年的渐变、阴影、高光等拟真视觉效果，从而打造出一种看上去更“平”的界面。扁平风格的一个优势就在于它可以更加简单直接的将信息和事物的工作方式展示出来，减少认知障碍的产生。  
  
在主流的css框架bootstrap中，开始是不支持扁平化设计的（bootstrap2），但在最新的bootstrap3中，几乎所有的组件都改用了扁平化的设计，但由于设计的不好，3的版本被很多原使用bootstarp的开发所诟病。  
  
而semantic-ui是天生就是扁平化的设计，让人用起来更加觉得时尚、简洁。

##响应式设计
什么是响应式设计？响应式Web设计(Responsive Web design)的理念是，页面的设计与开发应当根据用户行为以及设备环境(系统平台、屏幕尺寸、屏幕定向等)进行相应的响应和调整。具体的实践方式由多方面组成，包括弹性网格和布局、图片、CSS media query的使用等。无论用户正在使用笔记本还是iPad，我们的页面都应该能够自动切换分辨率、图片尺寸及相关脚本功能等，以适应不同设备;换句话说，页面应该有能力去自动响应用户的设备环境。这样，我们就可以不必为不断到来的新设备做专门的版本设计和开发了。  
  
在semantci-ui的官网里面是这样说的：  
{% blockquote %}
Every component is defined using em and rem so that components can be resized simply on the fly. Want a menu to get smaller on mobile? Simply have it's font-size change using a media query.
{% endblockquote %}
  
semantic-ui的每个组件都是使用“em”和“rem"，所以组件可以在不同的显示设备上自动地调整大小。  
  
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


[url1]: http://semantic-ui.com/
[url2]: https://github.com/Semantic-Org/Semantic-UI/issues/431
