---
layout: post
title: "我的octopress配置"
date: 2013-09-05 07:33
keywords: octopress setting
comments: true
categories: octopress
tags: octopress
---
  
在github上用octopress搭建了自己的blog，octopress号称是“专门给黑客打造的博客（A blogging framework for
把hackers）”，使用Markdown语法来写blog确实感觉像写代码一样，而且写好的
把blog可以本地调试好了再上传到github的服务器，就像你的应用程序调试好了部署到服务器一样。
  
<!--more-->  
我最喜欢的是octopress的代码片段，简洁清晰，而且支持各种语言的高亮显示，你还可以在你的blog中嵌入不同的js框架（比如jquery或angularJs），展示js代码的效果。octopress支持的插件很多，还可以选择各种主题来装饰你的blog，下面我也按照惯例秀秀自己的octopress配置。  
  
###主题  
  
你当然可以使用octopress的默认主题，但这样很容易和其他人的界面“撞衫”，想要让自己的octopress与众不同，就要定制自己的主题界面。在github上面可以找到octopress的第三方主题，地址在[这里][url2]，另外还有一个网站也是专门介绍octopress主题的，地址在[这里][url3]。其中也有一些国人制作的主题界面，非常漂亮。  
[url2]: https://github.com/imathis/octopress/wiki/3rd-Party-Octopress-Themes
[url3]: http://opthemes.com/
  
###评论插件  
  
原先使用的是国外的一个第三方评论插件Disqus，但由于国外多用facebook或twitter等帐号来登录这个插件，而这些网站都被我们伟大的GFW墙了，所以我选择了国内的一个同类产品——多说（其实应该是山寨Disqus的）。多说可以使用国内主流应用的帐号来登录，比如新浪微博或QQ等，可以将你blog中的评论保存到服务器上，后续不管你把blog迁移到什么地方，评论都不会消失。可以到[这里][url1]下载。  
[url1]: http://duoshuo.com/
  
###标签云  
  
octopress默认的只有目录（categories），没有标签（Tag），目录和标签是不一样的，每一篇blog只能归在一个目录里面，但却可以归在多个标签中。随着标签慢慢增多，可以将标签做成标签云，放在blog的边栏上，不仅非常美观，还可以看出你的blog关注方向有哪些。想做标签云，需要使用到2个插件，[robbyedwards/octopress-tag-pages][url4]和[robbyedwards/octopress-tag-cloud][url5]。具体安装步骤请参照该github上的说明。  
[url4]: https://github.com/robbyedwards/octopress-tag-pages
[url5]: https://github.com/robbyedwards/octopress-tag-cloud
  
###同步插件  
  
在github上面搭建自己的octopress博客，有个缺点是在搜索引擎中没有收录你的网站，导致别人很难在google或者百度里面找到你的文章，如果你的blog放在比较大的博客网站的话（比如博客园、博客大巴等），在各大搜索网站就可以很容易搜到你的blog。我曾经试过在每篇blog上面加上“keywords"标签来记录post的关键字，希望搜索引擎可以搜录，但结果还是不行。  
在网上搜了一下，发现有人写了一个octopress插件，可以将octopress的blog同步到几个比较大的博客网站，可以同步所有的blog，也可以同步最近一篇blog。网址在[这里][url6]。  
[url6]: https://github.com/huangbowen521/octopress-syncPost
  
上面几个是我的octopress主要插件，主题上面我还是不大满意，可能以后还会更新。


