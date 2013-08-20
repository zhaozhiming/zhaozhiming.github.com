---
layout: post
title: "location对象的页面跳转方法介绍"
date: 2012-05-22 16:18
comments: true
categories: code
tags: javascript
---
  
JavaScript中使用location对象可以通过很多种方式改变浏览器的位置。最常用的方法应该是下面几种：  
  
{% codeblock demo.js lang:javascript %}
　　location.href = "http://www.google.com";
　　window.location = "http://www.google.com";
　　location.assign("http://www.google.com");
{% endcodeblock %}  
  
其实前面2种方式是调用第3种方式去实现的，这样就可以在浏览器中打开一个新的URL并在历史记录中生成一条记录，因此用户可以通过“后退”按钮回到前一个页面。如果想禁止这种后退行为，可以使用replace()方法。该方法只接受一个参数，即URL，然后会跳转到该URL，但不会在历史记录中生成记录。这样用户不能回到前一页了。  
  
{% codeblock demo.js lang:javascript %}
    location.replace("http://www.google.com");
{% endcodeblock %}  
  
关于页面跳转还有一个reload方法，作用是重新加载当前页面。如果调用该方法时不带参数，页面会以最有效的方式加载页面，也就是说，如果浏览器有该页面的缓存，则会从缓存里面加载该页面。如果不想从缓存中重新加载页面，需要传入true参数，即绕开浏览器缓存，强制从服务器去获取页面数据。  
  
{% codeblock demo.js lang:javascript %}
  location.reload(); //相当浏览器的F5刷新，有可能从缓存中加载
  location.reload(true); //相当于Ctrl+F5，从服务器中加载
{% endcodeblock %}  
  
下面列出location.reload()方法的具体解释：  
  
{% blockquote %}
The reload() method of the Location object reloads the document that is currently displayed in the window of the Location object. When called with no arguments, or with the argument false, it uses the If-Modified-SinceHTTP header to determine whether the document has changed on the web server. If it has, it reloads the document from the server, and if not, it reloads the document from the cache. This is is the same action that occurs when the user clicks on Navigator's Reload button.
{% endblockquote %}  

location对象的reload方法会重新加载当前页面。当没有传入参数或传入false参数时，会用 If-Modified-SinceHTTP header 去判断页面在web服务器上是否有改变。如果改变了，则从服务器加载，如果没有改变，则从缓存中加载。这与用户点击导航栏刷新按钮的行为相同。  
  
{% blockquote %}
When reload() is called with the argument true, then it will always bypass the cache and reload the document from the server, regardless of the last-modified time of the document. This is the same action that occurs when the user shift-clicks on Navigator's Reload button.
{% endblockquote %}  
  
当调用reload方法时传入参数true，会始终绕开缓存而从服务器上加载页面，不管页面有没有改变。这与用户按住shift并点击导航栏刷新按钮的行为相同。  

