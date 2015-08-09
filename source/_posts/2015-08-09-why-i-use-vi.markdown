---
layout: post
title: "为什么我要使用Vim"
date: 2015-08-09 08:47
description: 为什么我要使用
keywords: vim
comments: true
categories: tool
tags: [vi,vim]
---
  
{% img /images/post/2015-8/vi.png 250 250 %}  
  
之前做Java开发的时候一直使用IDE，因为Java的IDE工具开发的太好了，以致没有机会深入接触像`vi`和`emacs`之类的神编辑器，但最近转python让我有机会深入了解了`vi`，使用过后不仅觉得方便，而且被其强大深深所吸引。
  
<!--more-->  
  
## 为什么使用vi
最早是使用Eclipse，然后转IntelliJ IDEA，可以看看我之前的这篇文章——[《为什么Intellij-IDEA比Eclipse好》][why_idea]，之前觉得使用IDEA效率已经很高了，熟悉快捷键的话可以完全不用鼠标进行编码。  
  
其实Jetbarins(开发IDEA的公司)也有python的IDE——PyCharm，功能、操作和IDEA差不多，熟悉IDEA的人可以很快上手PyCharm。  
  
但是作为一个程序员，我觉得如果没有深入使用过一款"神之编辑器"（vi或emacs）是有遗憾的，而且通过使用vi发现，IDE有的功能vi都可以有，通过vi的配置，可以将vi打造成任一种语言的IDE，也就是说，使用IDE功能是有限的，但是使用vi，你的编辑器功能是没有边界的，任何你想要的功能都可以通过安装插件或者自己开发插件来实现。  
  
使用IDE你可能不用鼠标进行编码，但是使用vi你可以不用鼠标来操作整个操作系统，这就是vi的魅力，可以让你像弹钢琴一样优雅的编写代码，行云流水，酣畅淋漓。  
  
最后为什么选vi不选emacs呢？因为公司都是vi党，用emacs怕被人打，还是用vi好了-_-#
  
## 简明Vim练级攻略
  
说实话，vi的学习曲线非常陡峭，但是经过前人的积累，已经有不少好用的vi插件和工具帮助我们尽快的熟悉vi，省去自己配置vi的烦恼，剩下的就只有不断地使用vi，以增强vi的熟练程度，做到得心应手。  
  
这里要推荐陈皓老师翻译整理的这篇文章——[简明Vim练级攻略][vim_learn]，里面详细介绍了从入门到高级的vi使用命令，分阶段让学习者掌握vi的编辑命令，让你从一大堆繁杂的vi命令中了解到真正实用的vi指令。  
  
还是之前说的，文章只是给你指明方向，实际的练习才是最重要的，要熟悉vi就要每天都使用vi写写东西，如果忘记了命令再重温一下文章的内容，这样不断地强化精神和肌肉的记忆才能掌握好vi。  
  
## [k-vim][k-vim]
  
这里要推荐k-vim这个国人打造的vim配置，里面集成了各种强大的vi插件，功能堪比甚至远超各种IDE，其主要用于python开发，也支持一些基础的前端开发，这里主要讲一下里面配置的一些强大的插件。  
  
#### YouCompleteMe
  
很多IDE都有代码自动补全的功能，通过这个插件，vi也可以做到这一点，这个插件还可以配置各种语言的自动补全提示。  
  
#### gmarik/vundle
  
像pip，npm一样，vundle可以管理vi的插件，通过简单的install/uninstall命令可以帮你安装你想要的vi插件。  
  
#### scrooloose/syntastic
  
IDE有语法错误之类的检查，这个vi插件实现了类似的功能，并且可以配置各种语言的语法检查。  
  
  
还有其他很多强大的插件，比如工程目录结构菜单，代码模板，源码跳转等。总之，使用k-vim可以省却你大部分配置vim的时间，让你得以马上体验vi的强大。  
  
## [tmux][tmux]
  
tmux是一个terminal管理软件，通过tmux配合vi可以使开发工作更加简单快捷，网上有很多介绍tmux的文章，我就不多介绍了，附一下是我的tmux配置，也是从网上查的一份配置，使用之后觉得还不错，推荐给大家，这里使用`ctl+A`作为tmux前缀，相对默认的`ctl+B`来说按键相对要近一些。  
  
{% codeblock tmux.conf lang:sh %}
# -- base -- #
unbind C-b
set -g prefix C-a
set -g status-keys vi
setw -g mode-keys vi
bind : command-prompt
bind r source-file ~/.tmux.conf \; display-message "Reloading..".
set -g default-terminal "screen-256color"
bind-key a send-prefix

# -- windown -- #
bind s split-window -h -c "#{pane_current_path}"
bind v split-window -v -c "#{pane_current_path}"
bind-key c  new-window -c "#{pane_current_path}"

bind h select-pane -L
bind j select-pane -D
bind k select-pane -U
bind l select-pane -R

bind ^k resizep -U 10
bind ^j resizep -D 10
bind ^h resizep -L 10
bind ^l resizep -R 10
bind ^u swapp -U
bind ^d swapp -D

bind u choose-session
bind o choose-window
bind \ last
bind q killp

bind-key -n C-S-Left swap-window -t -1
bind-key -n C-S-Right swap-window -t +1
set -g base-index 1
setw -g pane-base-index 1
set -g history-limit 5000

# pane border
set -g pane-border-fg black
set -g pane-border-bg white
set -g pane-active-border-fg black
set -g pane-active-border-bg '#afd787'

# -- command -- #
bind m command-prompt "splitw 'exec man %%'"
bind space copy-mode
bind -t vi-copy v begin-selection
bind -t vi-copy y copy-selection
bind -t vi-copy C-v rectangle-toggle
bind ] paste-buffer

# -- statusbar --#
set -g status-justify centre
set -g status-right-attr bright
set -g status-right "%H:%M %a %m-%d"
set -g status-bg default
set -g status-fg '#afd787'
setw -g window-status-current-attr bright
setw -g window-status-current-fg black
setw -g window-status-current-bg '#afd787'
set -g status-utf8 on
set -g status-interval 1
{% endcodeblock %}
  
## vi学习
  
* 简明Vim练级攻略只是介绍了一些比较实用和常用的命令，如果在实际使用过程中发现一些不清楚的vi操作，最好的办法就是通过googl搜索找到答案，并将答案记录下来以免下次忘记。  
* 在从IDE过渡到vi的期间，可以用IDE完成一些比较紧急的工作，其他的就使用vi来完成。  
* 在使用vi的过程中，可以模拟IDE的操作来了解更多vi的命令，比如IDE可以用`ctl+w`选中整个单词，在vi里面要如何操作？这样慢慢一步一步地将vi的命令掌握，并替换掉你的IDE。  


[why_idea]: http://zhaozhiming.github.io/blog/2014/10/09/why-intellij-idea-better-than-eclipse
[vim_learn]: http://coolshell.cn/articles/5426.html
[k-vim]: https://github.com/wklken/k-vim
[tmux]: https://tmux.github.io
