---
layout: post
title: "在JavaScript中进行文件处理，第二部分：文件读取"
date: 2012-07-16 10:09
comments: true
categories: translate
tags: javascript
---

**译注：原文是《JavaScript高级程序设计》的作者Nicholas Zakas写的，本翻译纯属为自己学习而做，仅供参考。原文链接：[这里][url1]**  
[url1]: http://www.nczonline.net/blog/2012/05/15/working-with-files-in-javascript-part-2/

***
  
在我的前一篇blog中，我介绍了在JavaScript中如何使用文件，具体重点放在如何获得File对象。只有当用户通过上传或者拖拽的方式上传了文件，这些对象才拥有文件的元数据。一旦你有了这些文件，下一步就是从这些文件中读取数据。  

##FileReader 类型  
  
FileReader类型有一个单一的工作，就是从一个文件中读取数据并存储在一个JavaScript变量中。它的API有意设计得与XMLHttpRequest相同，因为它们都是从一个外部资源（浏览器之外）加载数据。读操作是异步的，这样不会使浏览器堵塞。  
  
FileReader可以创建多种格式来表示文件的数据，而当读取文件时返回的格式是必须的。读取操作是通过调用下面任一方法来完成的：    
  
* readAsText() – 使用纯文本的形式返回文件内容
* readAsBinaryString() – 使用加密二进制数据字符串的形式来返回文件内容（该方法已废弃，请使用readAsArrayBuffer()代替）
* readAsArrayBuffer() – 使用ArrayBuffer的形式来返回文件内容（对二进制数据比如图像文件有用）
* readAsDataURL() – 使用数据URL的形式返回文件内容  

像XHR对象的send方法会发起一个Http请求一样，上面的每个方法都会启动一个文件读取。就这一点来说，在开始读取之前，你必须监听load事件，event.target.result总是返回读取的结果。例如：  

{% codeblock demo.js lang:javascript %}
var reader = new FileReader();
reader.onload = function(event) {
    var contents = event.target.result;
    console.log("File contents: " + contents);
};

reader.onerror = function(event) {
    console.error("File could not be read! Code " + event.target.error.code);
};

reader.readAsText(file);
{% endcodeblock %}  
  
在这个例子中，我们简单地读取文件内容，并将内容以纯文本的形式输出到console。当文件被成功读取时会调用onload操作，而因为某些原因无法读取时会调用onerror操作。在事件处理器中可以通过event.target来获得FileReader实例，而且它推荐这样使用，而不是直接使用reader变量。result属性包含读取成功时的文件内容和读取失败时的错误信息。  

##读取数据URI  
  
你可以用差不多的方法来将文件读取为一个数据URI，数据的URI（有时也叫数据URL）是个有趣的选项，比如你想要显示从磁盘上读取的图像文件，你可以用下面的代码这样做：  

{% codeblock demo.js lang:javascript %}
var reader = new FileReader();
reader.onload = function(event) {
    var dataUri = event.target.result,
        img     = document.createElement("img");

    img.src = dataUri;
    document.body.appendChild(img);
};

reader.onerror = function(event) {
    console.error("File could not be read! Code " + event.target.error.code);
};

reader.readAsDataURL(file);
{% endcodeblock %}  
  
这段代码简单地在页面上插入一个从磁盘上读取来的图像文件。因为这个数据URI包含了图像的所有数据，所以它可以被直接传给图像的src属性，并显示在页面上。你可以交替地加载图像和将其绘制到一个`<canvas>`上：  
  
{% codeblock demo.js lang:javascript %}
var reader = new FileReader();
reader.onload = function(event) {
    var dataUri = event.target.result,
        context = document.getElementById("mycanvas").getContext("2d"),
        img     = new Image();
 
    // wait until the image has been fully processed
    img.onload = function() {
        context.drawImage(img, 100, 100);
    };
    img.src = dataUri;
};

reader.onerror = function(event) {
    console.error("File could not be read! Code " + event.target.error.code);
};

reader.readAsDataURL(file);
{% endcodeblock %}  
  
这段代码将图像数据加载到一个新的Image对象，并将其绘制到一个画布上（宽度和长度都指定为100）。  
  
数据URI一般用来做这个，但能用在任何类型的文件上。将文件读取为一个数据URI最普遍的用法是在web页面中快速显示文件内容。  

##读取ArrayBuffers

ArrayBuffer类型*[1]*最初是作为WebGL的一部分被引进的。一个Arraybuffer代表一个有限的字节数，可以用来存储任意大小的数字。读取一个ArrayBuffer数据的方式需要一个特定的视图，比如Int8Array是将其中的字节处理为一个有符号的8位整数集合，而Float32Array是将其中的字节处理为一个32位浮点数的集合。这些称为类型数组*[2]*，这样可以强制你工作在一个特定的数字类型上，而不是包含任意类型的数据（像传统的数组）。  

当处理二进制文件时你可以优先使用ArrayBuffer，这样对数据可以有更细粒度的控制。要解释关于ArrayBuffer的所有ins和outs已经超出本篇blog的范围，你只需要知道在你需要的时候可以很容易地将一个文件读取为一个ArrayBuffer就可以了。你可以直接传一个ArrayBuffer到一个XHR对象的send()方法，发送原始数据到服务器（你会在服务器的请求中读取这个数据去重建文件），只要你的浏览器完全支持XMLHttpRequest Level 2*[3]*（大部分最新的浏览器，包括IE10和Opera12都支持）。  
  
  ***
####下集预告

使用FileReader读取文件数据相当简单。如果你知道怎么使用XMLHttpRequest, 那么你肯定知道怎么从文件中读取数据。在这个系列的下一章，你将学到更多有关如何使用FileReader事件和理解更多潜在错误的内容。   

####相关链接

1. [ArrayBuffer][url2]
2. [Typed Array Specification][url3]
3. [XMLHttpRequest Level 2][url4]
[url2]: https://developer.mozilla.org/en/JavaScript_typed_arrays/ArrayBuffer
[url3]: http://www.khronos.org/registry/typedarray/specs/latest/
[url4]: http://www.w3.org/TR/XMLHttpRequest/


