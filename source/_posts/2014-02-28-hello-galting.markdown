---
layout: post
title: "压力测试工具——Galting"
date: 2014-02-28 07:30
description: 压力测试工具——Galting
keywords: 压力测试工具Galting
comments: true
categories: code
tags: galting
---

{% img /images/post/2014-2/gatling.png %}  
  
为什么要写Gatling呢？网上已经有一些介绍Gatling的好文章了，比如两位TW同事的文章，可以看[这里][url1]（我知道Gatling也是因为这位作者介绍的），还有[这里][url2]。主要是因为最近在使用Gatling做压力测试，感觉这个工具非常好用，所以想结合自己的使用情况也推荐一下。Gatling是基于scala写的一个开源的压力测试工具，它的特点是简单易用，测试报告简洁漂亮，api通俗易懂。
<!--more-->  

## JDK1.7
Gatling是基于jdk1.7开发的，所以还在用jdk1.6的同学需要先下载jdk1.7，才可以启动Gatling。顺便说一下，JDK1.7以后肯定是会替换JDK1.6的，但由于历史原因（主要是JDK1.6用的人实在太多了，我想也是由于JDK1.7升级的太晚了）大家都不想升级，我也这也是JAVA走下坡路的原因。  
  
## Scala
前面讲过Gatling是用scala写的，像scala、groovy和clojuer都是基于jvm开发的一种语言，不仅继承了java众多成熟的api和稳定的jvm，而且提供了更佳简洁易用的语法。基于某种语言的内核来重新设计一门新语言的这种做法，现在慢慢多了起来，这种做法的最大好处就是不用自己再重新设计编程语言底层相关的一些东西，而且可以使用原有语言大量的成熟api，让新语言就只专注地实现自己的功能，从而使新的语言性能稳定，易用性更高，甚至可以吸引一些原有语言的开发人员。

## DSL
看过Galting的api的人就会发现，它的语法很像自然语言，比如测试一个get类型的http请求，它可能会这样写：
  
{% codeblock demo lang:scala %}
exec(http("demo")
    .get("http://www.myweb.com/helloworld")
{% endcodeblock %} 
  
是不是很容易懂，这种就是基于DSL来写的代码，关于DSL可以看[这本书][url3]，这本书我还没怎么看，等看完了以后再做分享，DSL不单单只是把代码写得像自然语言那样简单。

## API
要想了解Gatling的各种api，这里推荐看Gatling源码里面的HttpCompileTest这个测试类，这里包含了http几乎所有的api使用示例。下面是截取的代码片段，全部代码可以看[这里][url4]。  
  
{% codeblock demo lang:scala %}
.group("C'est ici qu'on trouve des Poneys") {
			exec(http("Catégorie Poney").post("/")
				.param("baz", "${qix}")
				.multivaluedParam("foo", Seq("bar")))
				.exec(http("Catégorie Poney").post("/").multivaluedParam("foo", "${bar}"))
				.exec(http("Catégorie Poney").get("/").queryParam("omg", "foo"))
				.exec(http("Catégorie Poney").get("/").queryParam("omg", "${foo}"))
				.exec(http("Catégorie Poney").get("/").queryParam("omg", session => "foo"))
				.exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", List("foo")))
				.exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", "${foo}"))
				.exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", List("foo")))
		}
		.uniformRandomSwitch(exec(http("Catégorie Poney").get("/")), exec(http("Catégorie Licorne").get("/")))
		.randomSwitch(
			40d -> exec(http("Catégorie Poney").get("/")),
			50d -> exec(http("Catégorie Licorne").get("/")))
		.randomSwitch(40d -> exec(http("Catégorie Poney").get("/")))
		.pause(pause2)
{% endcodeblock %} 
  
这里说下randomSwitch这个方法，这个方法可以模拟负载均衡，比如上面40d和50d的两行代码，会有40%的几率执行上面一行代码，50%的几率执行下面一行代码。  
  
## Question
如果看过上面的示例代码代码，还不知道具体的api怎么用，可以到[google group][url5]上面的分组提问题，很快就有人回答你的问题。github上面的问题区是让开发提bug或者feature用的，所以尽量不要在上面提问题。  
  
## Report
下面是Gatling的报告截图，界面很漂亮，而且参数很齐全，包括全部的请求数，成功/失败的请求数，请求的最大/最小/平均响应时间等。  
  
{% img /images/post/2014-2/gatling_report.png %}

## Http & JMS
目前Gatling支持的协议不多，毕竟是一个轻量级的测试工具，目前只支持Http和JMS(以前用的时候还只是支持HTTP)，随着Gatling的发展，以后肯定会支持更多的协议。现在大部分的系统和应用都是提供http协议的api，所以基于http协议的测试也能满足大部分的测试场景了。  
  
  
[url1]: http://www.infoq.com/cn/articles/new-generation-server-testing-tool-gatling
[url2]: http://www.cnblogs.com/huang0925/p/3488313.html
[url3]: http://book.douban.com/subject/21964984/
[url4]: https://github.com/excilys/gatling/blob/f5aeee2492dedd665b0dcf0b5b60f0a2227a53b6/gatling-bundle/src/test/scala/io/gatling/bundle/test/HTTPCompileTest.scala
[url5]: https://groups.google.com/d/forum/gatling
