---
layout: post
title: "在JavaScript中进行文件处理，第四部分：对象URLs"
date: 2012-07-30 22:17
comments: true
categories: translate
tags: javascript
---
  
**译注：原文是《JavaScript高级程序设计》的作者Nicholas Zakas写的，本翻译纯属为自己学习而做，仅供参考。原文链接：[这里](http://www.nczonline.net/blog/2012/05/31/working-with-files-in-javascript-part-4-object-urls)**  

***
  
学习到这里，你已经了解在传统方式中如何使用文件，你可以上传文件到服务端，可以从磁盘上读取文件，这些都是最常见的文件处理方式。但是，有一种全新的文件处理方式可以简化这些常见的任务，这就是使用对象URLs。  

<!--more-->
## 什么是对象URL？  
  
对象URLs是磁盘上的文件地址。比如说，你想要将用户系统的一个图像文件显示到web页面，服务端无需要知道这个文件，所以也不需要上传它。如果你只是想要加载文件到页面，你可以像之前的帖子说的一样，获取一个File对象引用，将数据读取为一个数据URI，然后将数据URI分配到一个`<img>`元素。但想一想这里面的浪费，图像已经在磁盘上存在，为什么还要将图像读取为另外一种形式呢？如果你创建一个对象URL，你可以将其分配给`<img>`，这样就可以直接访问本地文件。  
  
## 它如何工作？  
  
File API*[1]*定义了一个全局对象叫URL，它有2个方法。第一个是createObjectURL()，接受一个File对象作为参数，返回一个对象URL，作用是告诉浏览器创建并且管理一个本地文件的URL。第二个方法是revokeObjectURL()，作用是告诉浏览器去销毁作为参数传入的URL，有效的释放内存。当然，一旦web页面被卸载了，则所有对象URLs都会被注销，当不再需要它们的时候，这是释放它们内存的一个好机会。    
  
File API对URL对象的支持不像其他部分那么好，在我写这篇文章的时候，IE10和Firefox9+支持一个全局URL对象。Chrome支持它的webkitURL形式，Safari和Opera不支持。  
  
## 例子
  
如果你没有读取图片文件的数据，你怎么显示这个图片呢？假设你已经提供给用户文件选择的方式，并且现在有这个文件对象的引用并赋给了一个变量file。你可以如下使用：  
  
{% codeblock lang:javascript %}
var URL = window.URL || window.webkitURL, imageUrl, image; 
if (URL) { 
　　imageUrl = URL.createObjectURL(file); 
　　image = document.createElement("img"); 
　　image.onload = function() { 
　　　　URL.revokeObjectURL(imageUrl); 
　　}; 
　　image.src = imageUrl; 
　　document.body.appendChild(image); 
}
{% endcodeblock %}  
  
这个例子创建了一个本地URL变量，标准化浏览器的实现（即无论是何种浏览器都可以得到）。如果URL存在，程序将创建一个文件对象URL并将其存储在变量imageUrl，然后创建一个新的<img>元素，将imageUrl传入该元素的onloade事件处理方法，该方法将注销对象URL（一分钟内）。然后，src属性被分配了一个对象URL并将元素添加到页面。  
  
当图片加载的时候为什么要注销对象URL呢？在图片加载完之后，该URL就不再需要了，除非你想要在另外一个元素里面复用它。在这个例子中，图片被加载到一个单独的元素中，并且一旦图片完成加载，该URL就不再起任何作用，这是释放任何与其关联的内存的绝佳时刻。  
  
## 安全和注意事项  

乍一看，这种能力有点恐怖。实际上你是通过一个URL直接从用户机器上加载一个文件，这种方式当然存在安全隐患。URL本身其实不是一个大的安全问题，因为URL是通过浏览器动态分配的，在其他电脑上不会起作用。那跨网点会怎么样呢？  
  
File API 不允许在不同网点使用对象URLs。当一个对象URL被创建，它就紧跟着执行JavaScript脚本的页面网点，所以你不能跨www.wrox.com和p2p.wrox.com两个不同网点使用同一个对象URL（会发生错误）。但是，如果两个页面都是来自www.wrox.com，比如其中一个页面嵌套在另外一个页面的iframe里，这样就可以共享对象URLs。  
  
对象URLs只存在于文档创建它们的时候。当文档被卸载，所有对象URLs都会被注销。所以，在客户端存储对象URLs以便以后使用是没有意义的，它们在页面卸载之后就没有用了。  
  
你可以在浏览器中任何一个发起get请求的地方使用对象URLs，其中包括图片，脚本，web worker，样式表，音频，视频。浏览器执行post请求时使用不了对象URL。  

***
  
#### 下集预告  
  
创建直接链接到本地文件的URL是一种很强的能力。对比读取一个本地文件到JavaScript然后在页面上显示，你可以简单地创建一个URL并在页面指定它，后者大大简化了本地文件在页面中的使用情况。尽管如此，JavaScript处理文件有趣的地方才刚刚开始。在下一篇blog，你将学习到一些有趣的方式来处理文件数据。  
  
#### 相关链接  
  
* [File API](http://www.w3.org/TR/FileAPI/)

---

- [在JavaScript中进行文件处理，第一部分：基础](http://zhaozhiming.github.io/2012/07/08/working-with-files-in-javascript-part-1/)
- [在JavaScript中进行文件处理，第二部分：文件读取](http://zhaozhiming.github.io/2012/07/16/working-with-files-in-javascript-part-2/)
- [在JavaScript中进行文件处理，第三部分：处理事件和错误](http://zhaozhiming.github.io/2012/07/23/working-with-files-in-javascript-part-3/)
- [在JavaScript中进行文件处理，第五部分：Blobs](http://zhaozhiming.github.io/2012/08/01/working-with-files-in-javascript-part-5/)


