---
layout: post
title: "在JavaScript中进行文件处理，第一部分：基础"
date: 2012-07-08 16:26
comments: true
categories: translate
tags: javascript
---
  
**译注：原文是《JavaScript高级程序设计》的作者Nicholas Zakas写的，本翻译纯属为自己学习而做，仅供参考。原文链接：[这里][url1]**  
[url1]: http://www.nczonline.net/blog/2012/05/08/working-with-files-in-javascript-part-1/

***
很多年前，我在一次Goole面试被问到，如何在web应用中提供更好的用户体验。浮现在我脑海里的第一个想法是，通过比`<input type="file">`标签更好的方式来进行文件操作。虽然web发展一路高歌猛进，但自从该标签引进以来，我们操作文件的方式就一直使用它而从来没有改变过。幸运的是，有了HTML5和相关API，我们在最新版本的浏览器上可以有更多的方式进行文件操作（iOS 仍然不支持File的API）。  

<!--more-->
##File 类型
File类型在File的API*[1]*中有详细定义，是一个文件的抽象表征。每个File实例有如下几个属性：  
  
* name - 文件名
* size - 文件的二进制大小
* type - 文件的MIME类型
  
一个File对象是在不直接访问文件内容的情况下，给你文件的基本信息。这一点很重要，因为从磁盘上读取文件内容，视乎文件的大小，如果文件很大，可能会在读取上等待很长时间。File对象只是一个文件的引用，和获取文件内容是两个独立的过程。  
  
##获取文件引用    
基于安全的考虑，访问用户文件是严格禁止的，你不希望在加载某个页面时页面自动扫描你的硬盘然后把你的文件罗列出来吧？你访问用户电脑上的文件时要经过他们的许可，在想象中会弹出授权窗口给用户进行确认，但当用户通过页面上载东西时，实际上已经授权页面可以一直访问文件，所以不会弹出那些杂乱无章的授权窗口。  
  
当你使用`<input type="file">`标签时，你已经授权web页面（或者服务器）去访问该文件，通过`<input type="file"> `标签去检索到文件对象。  
  
HTML5为所有`<input type="file">`标签定义了一个文件集属性FileList，是一个类型数据类型的的数据结构，包含了每一个被选中的文件（HTML5允许多文件选择操作）。所以无论何时，你都可以使用以下代码来访问用户选择的文件。   

{% codeblock demo.html lang:html %}
<input type="file" id="your-files" multiple>
<script> 
    var control = document.getElementById("your-files"); 
    control.addEventListener("change", function(event) { 
        // When the control has changed, there are new files 
        var i = 0, files = control.files, len = files.length; 
        for (; i < len; i++) { 
            console.log("Filename: " + files[i].name); 
            console.log("Type: " + files[i].type); 
            console.log("Size: " + files[i].size + " bytes"); 
        } 
    }, false); 
</script>
{% endcodeblock %}  

这段代码监听了文件操作的change事件，一旦change事件触发，表示选中的文件已经发生改变，然后程序会迭代输出每个File对象的信息。记住，始终是通过javascript来访问文件的属性，所以不会有读取文件内容的动作。  

##拖拽文件
在表单中进行文件访问，需要用户先浏览查询再选中所需的文件。幸运的是，HTML5的拖拽功能*[2]*给用户提供了另外一种方式去授权需要访问的文件：通过简单的将文件从本地拖拽到浏览器。要实现这个功能你只需监听2个事件。  
  
为了读取从某些区域拖拽到浏览器的文件，你需要监听dragover和drop事件，并取消它们原有的默认行为。做这些是为了告诉浏览器无需处理，你可以直接处理这些动作，例如，打开一个图像文件。  

{% codeblock demo.html lang:html %}
<div id="your-files"></div>
<script>
var target = document.getElementById("your-files");

target.addEventListener("dragover", function(event) {
    event.preventDefault();
}, false);

target.addEventListener("drop", function(event) {

    // cancel default actions
    event.preventDefault();

    var i = 0,
        files = event.dataTransfer.files,
        len = files.length;

    for (; i < len; i++) {
        console.log("Filename: " + files[i].name);
        console.log("Type: " + files[i].type);
        console.log("Size: " + files[i].size + " bytes");
    }

}, false);
</script>
{% endcodeblock %}  
event.dataTransfer.files是另外一个文件集合对象，你可以从中获取文件信息。这段代码的功能等同于你手工选择打开一个文件。    
  
##Ajax文件上传  
一旦你有了一个文件的引用，你可能会做一些非常酷的事情，比如用Ajax上传文件。由于XMLHttpRequest Level 2*[3]*的FormData对象，使得这完全有可能。这个对象表示一个HTML表单，并且允许你在里面通过append方法来添加键值对数据，然后提交到服务器。  
  
{% codeblock lang:javascript %}
var form = new FormData();
form.append("name", "Nicholas");
{% endcodeblock %}  
  
FormData的伟大之处在于它可以直接添加一个文件对象，从而有效地模仿表单的文件上传。你所需要做的仅仅是添加一个File引用，并且指定一个文件名，剩下的由浏览器全部搞定。例如：  
  
{% codeblock lang:javascript %}
// create a form with a couple of values
var form = new FormData();
form.append("name", "Nicholas");
form.append("photo", control.files[0]);

// send via XHR - look ma, no headers being set!
var xhr = new XMLHttpRequest();
xhr.onload = function() {
    console.log("Upload complete.");
};
xhr.open("post", "/entrypoint", true);
xhr.send(form);
{% endcodeblock %}  
  
一旦FormData对象被传递到send方法，适当的HTTP头将会为你设置好。你不需要担心使用文件的表单如何设置正确的编码格式，服务器会将其当作一个常规的HTML表单来提交，然后读取“photo"键的文件数据和“name"键的文本数据。这让你在写后台代码时可以很自由，很容易的处理表单数据，不管是传统HTML表单还是Ajax表单形式。  
  
所有这些都可以在最新版本的浏览器上工作，包括IE10。  

***
####下集预告
你现在知道两种在浏览器中访问文件信息的方法，一种是通过文件上传操作，一种是通过本地的拖拽操作。未来可能有其他的方法，但现在你只需要了解这两种就好了。当然，读取文件信息只是问题的一部分，下一步是如何读取文件的内容，这将在第二部分做讲解。  
  
####相关链接：
* [File API specification (editor’s draft)](http://dev.w3.org/2006/webapi/FileAPI/)
* [HTML5 Drag and Drop](http://www.whatwg.org/specs/web-apps/current-work/multipage/dnd.html#dnd)
* [XMLHttpRequest Level 2](http://www.w3.org/TR/XMLHttpRequest/)

---

- [在JavaScript中进行文件处理，第二部分：文件读取](http://zhaozhiming.github.io/blog/2012/07/16/working-with-files-in-javascript-part-2/)
- [在JavaScript中进行文件处理，第三部分：处理事件和错误](http://zhaozhiming.github.io/blog/2012/07/23/working-with-files-in-javascript-part-3/)
- [在JavaScript中进行文件处理，第四部分：对象URLs](http://zhaozhiming.github.io/blog/2012/07/30/working-with-files-in-javascript-part-4/)
- [在JavaScript中进行文件处理，第五部分：Blobs](http://zhaozhiming.github.io/blog/2012/08/01/working-with-files-in-javascript-part-5/)


