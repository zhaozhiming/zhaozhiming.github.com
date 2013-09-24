---
layout: post
title: "利用Sonar定制自定义JS扫描规则（三）——SSLR JavaScript Toolkit 使用说明"
date: 2013-05-20 11:20
comments: true
categories: code
tags: [js,sonar]
---
  
在上一篇blog中讲了在sonar中如何新增自定义的JS规则，这里面比较难的地方是XPath语句的编写，而要编写正确的XPath语句，首先要拿到语法的AST，下面我们就来介绍如何使用SSLR JavaScript Toolkit 工具来得到源代码的语法树。  
  
<!--more-->
首先通过在[这里][url1]下载SSLR JavaScript Toolkit 工具，下载下来是一个jar包，在cmd窗口使用java -jar sslr-javascript-toolkit-1.3.jar打开这个工具，操作界面如下：  
[url1]: http://repository.codehaus.org/org/codehaus/sonar-plugins/javascript/sslr-javascript-toolkit/1.3/sslr-javascript-toolkit-1.3.jar  
  
{% img  /images/post/02073536-58b547f5394041cdb795a717087eca35.png %}  
    
左上方的窗口是用来输入你的源代码，右上方的窗口是来显示你的AST，最下面的窗口是来输入你的XPath规则。  
  
我们现在左上方输入一些简单的javascript代码，然后点击 Parse Source Code按钮，可以得到源代码的语法树：  
  
{% img  /images/post/02074124-ca30984af1704101962348b52d4a5f34.png %}  
  
得到语法树后，我们也可以点击右边窗口的XML页面，得到的是语法树的XML文档。有了XML文档，就可以通过XPath轻松得到你想要的语句了。  
  
举个例子：比如我想得到本地变量名，即var关键字后面的变量名，可以通过XPath的语法来查询。  
  
在最下方的窗口输入XPath语法规则，点击Evaluate XPath按钮，如果语法正确，就会出现返回结果，并高亮选中你想要查询的代码，如下：  
  
{% img  /images/post/02074943-45053229678b4effbec60cf2c363b076.png %}  
  
如果语法错误，右边的窗口会给出提示，如果查询语句不正确，任何源代码都不会被选中。更多的XPath语法可以参考[http://www.w3schools.com/xpath/][url2]。  
[url2]: http://www.w3schools.com/xpath/
  
总结：sonar其实很早就支持了对JS的代码校验，但是你如果想自己定制校验规则，最简单的方式就是通过sonar的web接口增加XPath规则，这个需要在XPath语法上多花点力气，如果写出来的查询语法不正确，可以多试几遍，用SSLR JavaScript Toolkit 可以很方便的调试。最后祝大家都可以在sonar上配置自己的自定义规则。  


