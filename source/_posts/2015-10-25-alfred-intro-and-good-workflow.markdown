---
layout: post
title: "Alfred和一些有用的workflow介绍"
date: 2015-10-25 13:36
description: "Alfred和一些有用的workflow介绍"
keywords: Alfred
comments: true
categories: tools
tags: [Alfred,tools]
---

{% img /images/post/2015-10/alfred.png %}  
  
最近发现了一个超级好用的工具——[Alfred][alfred]，可以通过快捷方式找到任何你MAC上的任何应用、文件，甚至可以自定义工作流的方式找到或者打开你想要的资源，后面会推荐几个有用的workflow。  
  
<!--more-->  
  
## Alfred vs. Spotlight
有人可能会说Mac从Yosemite版本开始就有了类似Alfred的自带工具Spotlight，但仔细使用后会发现Spotlight的功能只是Alfred免费版的一小部分而已，如果使用Alfred付费的Powerpack后功能更加强大，可以配合各种软件快速定位你想要的资源。  
  
所以如果你使用Alfred的话，完全可以替代MAC系统默认的Spotlight，替换方法如下：  
  
* Spotlight的默认快捷键是`⌘ + 空格`，可以在系统配置中取消该快捷键。

{% img /images/post/2015-10/spotlight.png %}  
  
* 安装Alfred后，在Alfred设置页面将弹出快捷键设置为`⌘ + 空格`。
  
{% img /images/post/2015-10/alfred_config.png %}  
  
## Alfred使用介绍
  
网上有比较多的关于Alfred的介绍，我这里就不一一列举其功能了，就挑一些重点的讲。  

#### 设置默认搜索引擎
  
Alfred默认使用google，amazon和维基百科来做前3个搜索引擎（可能不同的MAC机器有不同的默认设置，至少我机器上是这3个，这些搜索是在本地应用、文件查找不到之后通过网络进行搜索的设置。  
  
当然你可以更改这些默认设置，操作如下图所示：  
   
{% img /images/post/2015-10/alfred_search_config1.png %}  

{% img /images/post/2015-10/alfred_search_config2.png %}  
  
在`Fetures->Default Results->Setup fallback resULT`中设置，虽然百度搜索不怎么样，但是在国内找一些非科技的东西还是需要的，将其设为默认搜索引擎，去掉原来的amazon和维基百科，效果如下图。  
  
{% img /images/post/2015-10/alfred_search_config6.png %}  
  
#### 自定义Web搜索
  
在上图中可以看到百度是一个自定义的搜索，因为在我的机器上没有百度搜索，所以需要自己添加，添加的步骤如下：  
  
* 在`Fetures->Web Search->Add Custom Search`中添加，如下图所示：  
  
{% img /images/post/2015-10/alfred_search_config3.png %}  
  
* 在弹出窗口中设置url, 其中`{query}`就是你要查询的东西，再填上标题和关键字，有logo图片的话拖放到右边的框框上，最下面的`Validation`是来测试新建的搜索是否有用的。  
  
{% img /images/post/2015-10/alfred_search_config4.png %}  
  
* 这样在Alfred输入框中输入`baidu Alfred`就可以用百度来搜`Alfred`了。  
  
{% img /images/post/2015-10/alfred_search_config5.png %}  
  
#### 其他使用技巧
  
Alfred还有其他比较方便的使用方法，比如：  
  
* 当计算器使用
  
{% img /images/post/2015-10/alfred_use1.png %}  
  
* 输入`>`直接执行bash命令
  
{% img /images/post/2015-10/alfred_use2.png %}  
  
还有其他更多的使用技巧可以在网上找到。  
  
## workflow
  
Alfred的这部分功能是需要付费的，最便宜的个人版本折合人民币大概160多。  
  
{% img /images/post/2015-10/alfred_price.png %}  
  
workflow的安装非常简单，把后缀为alfredworkflow的文件下下来后，点击文件就会提示你是否安装了，这个网站涵盖了很多workflow——[alfredworkflow][alfredworkflow]，有需要的可以在这上面招，我在这里推荐一些程序员比较有用的workflow：  
  
* Dash workflow : [Dash][dash]是一个api查询工具，不仅可以查询各种语言的api，还可以查各种工具或者框架的api，对于程序员来讲非常有用，当然首先你需要安装Dash，再来安装这个workflow——[Dash-Alfred-Workflow][Dash-Alfred-Workflow]。 
* Stackoverflow workflow : Stackoverflow是程序员经常上的网站，通过Alfred可以快速查看网站上的相关问题——[st-workflow][st-workflow]。 
  
{% img /images/post/2015-10/workflow1.png %}  
  
* pkgman workflow: 这个workflow可以查找各种语言和工具的依赖包信息，对程序开发非常有用——[pkgman-workflow][pkgman-workflow]。  
  
{% img /images/post/2015-10/workflow2.png %}  
  
{% img /images/post/2015-10/workflow3.png %}  
  
下面这些workflow跟程序员不是很相关，但是也是非常有用的，个人推荐：   
  
* youdao workflow: 经常看到不会的单词要先打开翻译器，再把要翻译的单词拷贝到里面翻译，是不是很麻烦，有了这个workflow就很方便了，直接在Alfred上翻译就可以了——[yd-workflow][yd-workflow]。
  
{% img /images/post/2015-10/workflow4.png %}  
  
* douban workflow: 这个workflow可以查看豆瓣上的书籍，电影和音乐，非常方便——[douban-workflow][douban-workflow]。
  
{% img /images/post/2015-10/workflow5.png %}  
  
* evernote workflow: 使用evernote的同学可以使用这个workflow快速找到自己的笔记——[evernote-workflow][evernote-workflow]。
  
{% img /images/post/2015-10/workflow6.png %}  
  


[alfred]: https://www.alfredapp.com/
[alfredworkflow]: http://www.alfredworkflow.com/
[dash]: https://kapeli.com/dash
[Dash-Alfred-Workflow]: https://github.com/Kapeli/Dash-Alfred-Workflow
[st-workflow]: https://github.com/xhinking/Alfred
[pkgman-workflow]: https://github.com/willfarrell/alfred-pkgman-workflow
[yd-workflow]: https://github.com/wensonsmith/YoudaoTranslate
[douban-workflow]: https://github.com/lucifr/Alfredv2-Extensions
[evernote-workflow]: https://github.com/hzlzh/AlfredWorkflow.com

