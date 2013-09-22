---
layout: post
title: "在JavaScript中进行文件处理，第五部分：Blobs"
date: 2012-08-01 17:28
comments: true
categories: translate
tags: javascript
---
  
  
**译注：原文是《JavaScript高级程序设计》的作者Nicholas Zakas写的，本翻译纯属为自己学习而做，仅供参考。原文链接：[这里][url1]**  
[url1]: http://www.nczonline.net/blog/2012/05/31/working-with-files-in-javascript-part-5-Blobs  

***
  
到目前为止，这个系列的帖子集中在和这些文件交互——用户指定的文件和通过File对象访问的文件。File对象实际上是Blob的一个特殊版本，表示一块块的二进制数据。Blob对象继承了File对象的size和type属性。  
  
<!--more-->  
在大部分情况下，Blobs和Files可以用在同一个地方。例如，你可以使用一个FileReader从一个Blob中读取数据，并且你可以在一个Blob中使用URL.createObjectURL()方法来创建一个对象URL。  
  
##slice  

使用Blobs的一件有趣的事情是可以基于另外一个Blob的小部分来创建一个新的Blob。由于每个Blob代表的是数据的内存地址，而不是数据本身，所以你可以快速创建一个指向其他Blob子部分数据的Blob对象。这可以通过使用slice()方法来做到。  
  
你可能对类似slice()的方法比较熟悉，可以用来处理字符串和数组，还有Blob。这个方法接收3个参数：起始字节的下标，结束字节的下标，还有一个可选且适用于Blob的MIME类型。如果MIME类型没有指定，新的Blob跟原始的BLob对象有相同的MIME类型。  
  
浏览器对slice()的支持还不是很普遍，只有Firefox通过mozSlice()和webkitSlice()来支持它（其他浏览器现在都不支持）。这里有一个例子：    

{% codeblock lang:javascript %}
function sliceBlob(blob, start, end, type) { 
　　type = type || blob.type; 
　　if (blob.mozSlice) { 
　　　　return blob.mozSlice(start, end, type); 
　　} else if (blob.webkitSlice) { 
　　　　return blob.webkitSlice(start, end type); 
　　} else { 
　　　　throw new Error("This doesn't work!"); 
　　} 
}
{% endcodeblock %}  
  
比如，你可以使用这个函数将一个大文件拆分成一块块然后进行上传。每一个新产生的Blob都和原始的文件互不相干，即使每个blob的数据有重叠的部分。网络相册的工程师们使用blob分割来读取照片的可交换图片文件信息，这些照片是正在上传*[1]*的而不是已经上传到了服务器。当文件被选择的时候，上传文件和从照片中读取可交换图片文件信息，这2个动作在网络相册上传页面是同时开始的。这就允许在文件上传的时候，可以同时预览已经上传的部分数据的图像。  
  
##创建Blobs的老方法  
  
ile对象在浏览器中开始出现后不久，开发人员意识到Blob对象是如此强大，以致想不通过用户交互就可以直接创建它们。毕竟，任何数据都可以放在Blob里面，而不一定要绑定一个文件。浏览器可以快速的创建BlobBuilder，这个对象类型的唯一目的就是将数据封装在一个Blob对象里面。这是一个非标准类型并且已经在Firefox（像MozBlobBuilder），IE10（像MSBlobBuilder）和Chrome（像WebKitBlobBuilder）中实现。  
  
BlobBuilder通过创建一个实例，然后调用append()方法紧跟一个字符串、ArrayBuffer或者Blob来工作。一旦这些数据都被添加之后，你可以调用getBlob()并传递一个可选的MIME类型参数来使用Blob。这有个例子：  

{% codeblock lang:javascript %}
var builder = new BlobBuilder(); 
builder.append("Hello world!");
var blob = builder.getBlob("text/plain"); 
{% endcodeblock %}  
  
为数据的任意片段创建URLs的能力是非常强大的，允许你在浏览器中动态的创建链接到文件的对象。例如，你可以使用一个Blob来创建一个web worker，而不需要为web worker指定文件。这项技术写在Web Workers*[2]*的基础里：  
  
{% codeblock lang:javascript %}
// Prefixed in Webkit, Chrome 12, and FF6: window.WebKitBlobBuilder, window.MozBlobBuilder 
var bb = new BlobBuilder(); 
bb.append("onmessage = function(e) { 
　　postMessage('msg from worker'); 
}"); 
// Obtain a blob URL reference to our worker 'file'. 
// Note: window.webkitURL.createObjectURL() in Chrome 10+. 
var blobURL = window.URL.createObjectURL(bb.getBlob()); 
var worker = new Worker(blobURL); 
worker.onmessage = function(e) { 
　　// e.data == 'msg from worker' 
}; 
worker.postMessage(); 
// Start the worker.
{% endcodeblock %}
  
这段代码创建了一个简单的脚本，然后创建一个对象URL。将对象URL赋予一个web worker来代替一个脚本URL。  
  
你可以任意次调用append()来创建Blob的内容。  

##创建Blobs的新方式  

因为开发人员一直呼吁可以有一种方式来直接创建Blob对象，然后浏览器出现了BlobBuilder，它决定添加一个Blob构造器。这个构造器现在是规范的一部分，将是未来创建Blob对象的一种方式。  
  
这个构造器接收2个参数。第一个参数是一个分配了Blob块的数组。数据的元素跟传入BlobBuilder的append()方法的值相同，可以是任意数量的字符串，Blobs和ArrayBuffers。第二个参数是一个包含了新创建的Blob属性的对象。当前有2个属性已经定义：类型——指定Blob的MIME类型；endings——值分别是“transparent”（默认值）和“native”。这里有个例子：  

{% codeblock lang:javascript %}
var blob = new Blob(["Hello world!"], { type: "text/plain" }); 
{% endcodeblock %}  
    
像你看到的一样，这比使用BlobBuilder更加简单。Chrome的nightly builds版本和未来的Firefox 13将支持Blob构造器。其他浏览器还没有宣布实现该构造器的计划，尽管如此，现在它是File API*[3]*标准的一部分，期望以后会被普遍支持。  
  
##总结  
  
这是“在JavaScript中进行文件处理”这一系列的最后一部分。我希望你了解到，File API非常强大，在web应用中开辟了很多全新的方式来处理文件。当用户需要上传文件时你不再需要坚持使用文件上传框，现在你可以在客户端读取文件，为客户端操作开辟了多种可能性。你可以在上传文件之前重置图片的大小（使用FileReader和`<canvas>`）；你可以单纯在浏览器里创建一个文本编辑器；你可以分隔大文件进行逐步上载。可能性不是无穷无尽的，但也很接近无穷尽了。  
  
####引用  

1. [Parsing Exif client-side using JavaScript][url2] by Flickr Team
2. [The Basics of Web Workers][url3] by Eric Bidelman
3. [File API][url4] – Blob Constructor
[url2]: http://www.google.com/gwt/x?wsc=tb&source=wax&u=http%3A%2F%2Fcode.flickr.com/blog/2012/06/01/parsing-exif-client-side-using-javascript-2/&ei=DcsYUMuwAcSmkAXmvoHoAg  
[url3]: http://www.google.com/gwt/x?wsc=tb&source=wax&u=http%3A%2F%2Fwww.html5rocks.com/en/tutorials/workers/basics/&ei=DcsYUMuwAcSmkAXmvoHoAg
[url4]: http://www.google.com/gwt/x?wsc=tb&source=wax&u=http%3A%2F%2Fdev.w3.org/2006/webapi/FileAPI/&ei=DcsYUMuwAcSmkAXmvoHoAg&whp=3AconstructorBlob





