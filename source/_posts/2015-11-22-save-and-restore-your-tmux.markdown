---
layout: post
title: "保存并恢复你的tmux"
date: 2015-11-22 19:11
description: 保存并恢复你的
keywords: tmux
comments: true
categories: tool
tags: tmux
---

{% img /images/post/2015-11/tmux.jpg 400 350 %}  
  
[Tmux][tmux]是一个终端下的工具，可以方便地在一个终端窗口下进行分屏，配合vim可以打造任何IDE所需要的窗口模式，让你的生产力大大提升。介绍Tmux的文章网上有很多，所以我就不赘述了，今天介绍Tmux的一款工具，可以方便的保存Tmux的状态并在任何时候进行恢复。  
  
<!--more-->  
  
## 为什么需要恢复这个工具
  
Tmux虽然功能十分强大，但却不能像IDE一样保存已经定制好的窗口模式，每次电脑重启后，你需要重新划分你的终端屏幕。程序员都是比较懒的，为了不用每次重启机器都要重新设置一遍Tmux，我们找到了[tmux-resurrect][tmux-resurrect]这个工具。在介绍这个工具之前，我们先介绍tmux的插件管理工具，因为tmux-resurrect推荐使用它来进行安装。  
  
## Tmux插件管理工具
  
Tmux也像Vim等其他软件一样可以通过安装插件来扩充其强大的功能，推荐的方式是使用Tmux的插件管理工具[TPM][tpm]（Tmux Plugin Manager）来安装插件。安装TPM非常简单，参考其github工程的README文档就可以了。  
  
安装了TPM之后，以后要安装Tmux插件就很方便了，首先在`~/tmux.conf`中写入你要安装的插件名：  
  
{% codeblock lang:sh %}
set -g @plugin '...'
{% endcodeblock %}
  
然后在tmux中按`前缀键+大写I`就可以提示你进行插件安装了，安装过程如下：  
  
{% codeblock lang:sh %}
Already installed "tpm"                                                                                                                                                         [0/0]

TMUX environment reloaded.

Done, press ENTER to continue.
{% endcodeblock %}  
  
## tmux-resurrect
  
安装完TPM后，我们再来安装tmux-resurrect，跟安装其他插件一样，先在`~/tmux.conf`下录入tmux-resurrect：  
  
{% codeblock lang:sh %}
set -g @plugin 'tmux-plugins/tmux-resurrect'
{% endcodeblock %}
  
然后按`前缀键+I`就可以了。  
  
tmux-resurrect的使用非常简单，在Tmux窗口中按`前缀键+ctrl+s`就是保存你的tmux窗口，按`前缀键+ctrl+r`是恢复你的tmux窗口。以后只要在开机后，进入Tmux，然后用`前缀键+ctrl+r`就可以一键恢复你之前的tmux窗口了:-)
  
## tmux-sessionist
  
刚用tmux-resurrect的同学可能还以为它只是保存Tmux当前一个窗口，其实它保存的是Tmux的所有窗口。那要如何切换Tmux的窗口呢？那就要用到另外一个Tmux的插件——[tmux-sessionist][tmux-sessionist]了，它可以快速的切换Tmux之间的seesion窗口，安装之后通过`前缀键+g`就可以了，感兴趣的小伙伴赶紧试一试吧。  
  

[tmux]: https://tmux.github.io/
[tmux-resurrect]: https://github.com/tmux-plugins/tmux-resurrect
[tpm]: https://github.com/tmux-plugins/tpm
[tmux-sessionist]: https://github.com/tmux-plugins/tmux-sessionist
