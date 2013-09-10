---
layout: post
title: "我的octopress配置"
date: 2013-09-05 07:33
keywords: octopress setting
comments: true
categories: octopress
tags: octopress
---
  
把blog从博客园搬到了octopress，octopress号称是“专门给黑客打造的博客（A blogging framework for hackers.)）”，octopress是使用Markdown语法，使用Markdown语法来写blog确实感觉像写代码一样，而且写好的blog可以本地调试好了再上传到github的服务器，就像把你的应用程序调试好了部署到服务器一样。 

我最喜欢的是octopress的代码片段，简洁清晰支持各种语言，而且可以在你的blog中嵌入不同的js框架来展示你的demo，比如jquery和angularJs。octopress支持不同的插件，还可以选择很多不同的主题，下面我也按照惯例秀秀自己的octopress配置。  
  
###主题  
  
octopress有一个默认的主题，你当然可以使用该主题，但这样很容易和其他人的界面“撞衫”，想要让自己的octopress与众不同，就要定制自己的主题界面。在github上面可以找到octopress的第三方主题，地址是：[https://github.com/imathis/octopress/wiki/3rd-Party-Octopress-Themes][url2]，另外还有一个网站也是专门介绍octopress主题的，地址是：[http://opthemes.com/][url3]。上面也有一些国人制作的主题界面，非常漂亮。  
[url2]: https://github.com/imathis/octopress/wiki/3rd-Party-Octopress-Themes
[url3]: http://opthemes.com/
  
###评论插件  
  
原先使用的是国外的一个第三方评论插件Disqus，但由于国内外使用的习惯不同，比如国外多用facebook和twitter等帐号登录，但这些国外主要网站都被墙墙了，国内多是使用新浪微博和QQ帐号登录，所以后来换成国内的一个评论插件“多说”。这个插件可以让用户任意绑定国内主流社交应用的帐号，可以到[这里][url1]下载。  
[url1]: http://duoshuo.com/
  
###标签云  
  
octopress默认的只有目录（categories），没有标签（Tag），目录和标签是不一样的，每一篇p ost只能归在一个目录里面，但却可以归在多个标签中。随着post的增多，标签也会慢慢变多，标签多了之后可以将标签做成标签云，放在blog的边栏上，非常美观。想做标签云，需要使用到2个插件，[robbyedwards/octopress-tag-pages][url4]和[robbyedwards/octopress-tag-cloud][url5]。具体安装步骤请参照该github上的说明。  
[url4]: https://github.com/robbyedwards/octopress-tag-pages
[url5]: https://github.com/robbyedwards/octopress-tag-cloud
  
###同步插件  
  
在github上面搭建自己的octopress博客，有个缺点是在搜索引擎中没有收录你的网站，导致别人很难在google或者百度里面找到你的文章，如果你的博客放在比较大的博客网站的话（比如博客园、博客大巴等），在各大搜索网站就可以很容易搜到你的blog。我曾经试过在每篇post上面加上“keywords"标签来记录post的关键字，希望搜索引擎可以搜录，但结果还是不行。  
在网上搜了一下，发现有人写了一个octopress插件，可以将octopress的post同步到几个比较大的博客网站，可以同步所有的post，也可以同步最近一篇post。网址如下：[https://github.com/huangbowen521/octopress-syncPost][url6]。  
[url6]: https://github.com/huangbowen521/octopress-syncPost
  
上面几个是我的octopress主要插件，主题上面我还是不大满意，可能以后还会更新。


