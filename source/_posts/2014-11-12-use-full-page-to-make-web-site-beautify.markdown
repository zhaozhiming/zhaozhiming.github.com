---
layout: post
title: "使用fullPage美化你的网站"
date: 2014-11-12 20:07
description: 使用FullPage美化你的网站
keywords: fullpage,js
comments: true
categories: code
tags: [fullpage,js]
---
  
{% img /images/post/2014-11/fullpage.png %}  
  
这段时间逛网站，发现有一些网站的页面做的跟手机屏幕一样，大图片大字体铺满整个屏幕，还可以像手机一样通过鼠标上下左右滑动来切换不同的页面，让人感觉很炫很酷很炫。好奇心的驱使下找到了[fullPage.js][fullpage]这个jquery插件，它可以让你轻松地制作漂亮的全屏滑动页面。   
  
<!--more-->

## 支持的浏览器
可以看到好东西都是不支持IE6的。  
  
{% img /images/post/2014-11/support-brower.gif %}  
  
## 用法
  
* fullPage.js使用非常简单，先在你的html页面里面加上fullPage的css和js文件，注意还需要包括jquery的js文件，而且要放在fullPage的js文件前面。
  
{% codeblock index.html lang:html %}
<link rel="stylesheet" type="text/css" href="jquery.fullPage.css" />

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

<script type="text/javascript" src="jquery.fullPage.js"></script>
{% endcodeblock %}     
  
* 然后在html里面定义一个div，id为`fullpage`，里面再嵌套div，class为`section`，代码示例如下，这是一个简单的全页滑动页面。  
  
{% codeblock index.html lang:html %}
<div id="fullpage">
    <div class="section">Some section</div>
    <div class="section">Some section</div>
    <div class="section">Some section</div>
    <div class="section">Some section</div>
</div>
{% endcodeblock %}     
  
* 最后在对fullPage对象进行初始化。
  
{% codeblock index.js lang:js %}
$(document).ready(function() {
    $('#fullpage').fullpage();
});
{% endcodeblock %}   
  
## fullPage属性
这是一个比较复杂的fullPage初始化，可以看到fullPage可以设置很多的属性，比如menu(菜单)，设置为`true`则在屏幕右边会显示导航菜单，其他的属性说明可以参考fullPage的[官方说明][fullpage-github]。
  
{% codeblock index.js lang:js %}
$(document).ready(function() {
    $('#fullpage').fullpage({
        //Navigation
        menu: false,
        anchors:['firstSlide', 'secondSlide'],
        navigation: false,
        navigationPosition: 'right',
        navigationTooltips: ['firstSlide', 'secondSlide'],
        slidesNavigation: true,
        slidesNavPosition: 'bottom',

        //Scrolling
        css3: false,
        scrollingSpeed: 700,
        autoScrolling: true,
        scrollBar: false,
        easing: 'easeInQuart',
        easingcss3: 'ease',
        loopBottom: false,
        loopTop: false,
        loopHorizontal: true,
        continuousVertical: false,
        normalScrollElements: '#element1, .element2',
        scrollOverflow: false,
        touchSensitivity: 15,
        normalScrollElementTouchThreshold: 5,

        //Accessibility
        keyboardScrolling: true,
        animateAnchor: true,

        //Design
        verticalCentered: true,
        resize : true,
        sectionsColor : ['#ccc', '#fff'],
        paddingTop: '3em',
        paddingBottom: '10px',
        fixedElements: '#header, .footer',
        responsive: 0,

        //Custom selectors
        sectionSelector: '.section',
        slideSelector: '.slide',

        //events
        onLeave: function(index, nextIndex, direction){},
        afterLoad: function(anchorLink, index){},
        afterRender: function(){},
        afterResize: function(){},
        afterSlideLoad: function(anchorLink, index, slideAnchor, slideIndex){},
        onSlideLeave: function(anchorLink, index, slideIndex, direction){}
    });
});
{% endcodeblock %}   
  
## 参考示例
在fullPage的github工程里面，有一个example文件夹，主页面是`demoPage.html`，在浏览器打开它可以看到里面列举了19个fullPage的Demo，有上下翻页，左右翻页，嵌套视频等各种例子，如果想实现自己想要的效果，可以参照对应demo的html文件来进行修改。  
  
{% img /images/post/2014-11/fullpage-demo.png %}  
  

[fullpage]: http://alvarotrigo.com/fullPage/#firstPage
[fullpage-github]: https://github.com/alvarotrigo/fullPage.js#options


