---
layout: post
title: "在JavaScript中进行文件处理，第三部分：处理事件和错误"
date: 2012-07-23 21:46
comments: true
categories: translate
tags: javascript
---
  
**译注：原文是《JavaScript高级程序设计》的作者Nicholas Zakas写的，本翻译纯属为自己学习而做，仅供参考。原文链接：[这里][url1]**  
[url1]: http://www.nczonline.net/blog/2012/05/22/working-with-files-in-javascript-part-3/

***

FileReader对象用来读取浏览器可以访问的文件的内容。在我前一篇blog中，你学习到了如何使用FileReader对象轻松读取文件，并将文件内容转换为各种形式。FileReader在很多方面和XMLHttpRequest非常相似。  

<!--more-->  
##进度事件  
  
progress事件变得如此普遍，以致实际上它们写在一个独立的规范*[1]*里面。这些事件设计用来表示数据传输的进度。这些传输不只发生在从服务器端请求数据的时候，而且也发生在从磁盘上请求数据的时候，这都是FileReader可以做的。  
  
这里有6个进度事件：  

* loadstart – 表示加载数据的进度开始，这个事件总是首先被触发。
* progress – 在加载数据过程中多次被触发，可以访问中间的数据。
* error – 当加载失败时触发。
* abort - 当通过调用abort()取消数据加载时触发（在XMLHttpRequest和FileReader上都可使用）。
* load - 只有当所有数据被成功读取后才触发。
* loadend - 当对象停止传输数据时触发。在error，abort和load后始终被触发。    
  
error和load事件我们在前一篇blog已经讨论了。其他事件让你更好地控制在数据传输。  
  
##跟踪进度  
  
当你想要跟踪一个文件读取的进度，你可以使用progress事件。这个事件对象包含了3个属性来监控数据的传输：  

* lengthComputable - 一个布尔值，表示浏览器是否能侦测数据的完整大小。
* loaded - 已经读取的数据字节数大小。
* total - 所要读取的数据字节数总大小
  
这些数据是为了生成一个使用了progress事件数据的进度条。例如，你可以使用HTML5`<progress>`元素来监控文件的读取进度。你可以像下面的代码一样让你的进度条和实际数据相关联：  

{% codeblock demo.js lang:javascript %}
var reader = new FileReader(), 
　　progressNode = document.getElementById("my-progress"); 
reader.onprogress = function(event) { 　　
　　if (event.lengthComputable) { 
　　　　progressNode.max = event.total; 
　　　　progressNode.value = event.loaded; 
　　} 
}; 
reader.onloadend = function(event) { 
　　var contents = event.target.result, 
　　　　 error = event.target.error; 
　　
　　if (error != null) { 
　　　　console.error("File could not be read! Code " + error.code); 
　　} else { 
　　　　progressNode.max = 1; 
　　　　progressNode.value = 1; 
　　　　console.log("Contents: " + contents); 
　　} 
}; 
reader.readAsText(file);  
{% endcodeblock %}  
  
这与Gmail用拖拽方式进行文件上传的方法相似，拖拽一个文件到email后你可以立即看到一个进度条。这个进度条表示有多少文件已经被传输到服务器。  

##错误处理  
  
即使你已经在读取一个本地文件，但仍然有可能读取失败。在File API规范*[2]*中定义了4种错误类型：    
  
* NotFoundError – 找不到该文件。　　
* SecurityError – 文件或者读取操作可能包含某些危险。浏览器有一些补救措施来处理这种情况，但一般来讲，如果加载到浏览器的文件有危险或者浏览器被限制了不能有太多的读取动作，你将会看到这个错误。　　
* NotReadableError – 文件存在但不可读，大部分情况可能是权限问题。　　
* EncodingError – 主要当尝试将文件内容读取为一个数据URI并且数据URI结果的长度超过浏览器可支持的最大长度时，会抛出这个错误。  
  
当读取文件发生错误时，上述的4个错误类型之一会被实例化，并分配到FileReader对象的error属性上。至少，规范上是这样写的。实际上，浏览器是通过一个FileError对象来实现的，FileError对象有一个code属性，表示当前发生的错误类型。每个错误类型通过一个数字常量来表示：  
  
* FileError.NOT_FOUND_ERR对应找不到该文件错误。
* FileError.SECURITY_ERR对应安全错误。
* FileError.NOT_READABLE_ERR对应不可读错误。
* FileError.ENCODING_ERR对应编码错误。
* FileError.ABORT_ERR当没有读取进度时调用abort()方法。    
  
你可以在error或者loadend事件中测试错误类型：  

{% codeblock demo.js lang:javascript %}
var reader = new FileReader(); 
reader.onloadend = function(event) {
　　var contents = event.target.result, 
　　　　error = event.target.error; 
　　if (error != null) { 
　　　　switch (error.code) {
　　　　　　case error.ENCODING_ERR: 
　　　　　　　　console.error("Encoding error!"); 
　　　　　　　　break; 
　　　　　　case error.NOT_FOUND_ERR: 
　　　　　　　　console.error("File not found!"); 
　　　　　　　　break; 
　　　　　　case error.NOT_READABLE_ERR: 
　　　　　　　　console.error("File could not be read!"); 
　　　　　　　　break; 
　　　　　　case error.SECURITY_ERR: 
　　　　　　　　console.error("Security issue with file!"); 
　　　　　　　　break; 
　　　　　　default: 
　　　　　　　　console.error("I have no idea what's wrong!"); 
　　　　} 
　　} else { 
　　　　progressNode.max = 1; 
　　　　progressNode.value = 1; 
　　　　console.log("Contents: " + contents); 
　　} 
}; 
reader.readAsText(file);   
{% endcodeblock %}
  
***
####下集预告  
  
FileReader是一个全面的对象，有着非常多的功能，并且和XMLHttpRequest很相似。在学习了这3篇blog之后，你现在应该可以使用JavaScript来读取文件数据，如果需要的话，还可以将数据发送回给服务器。尽管如此，File API的功能和内容比我们在这个系列里讨论的东西要多得多，在下一章你将学习到一个强大全新的功能设计来处理文件。  
  
####相关链接  
  
1. [Progress Events][url2]
2. [File API][url3]
[url2]: http://www.w3.org/TR/progress-events/
[url3]: http://www.w3.org/TR/FileAPI/




