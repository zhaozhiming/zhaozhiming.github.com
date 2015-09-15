---
layout: post
title: "如何学习Python"
date: 2015-09-05 15:01
description: 如何学习Python
keywords: python
comments: true
categories: code
tags: python
---
  
{% img /images/post/2015-9/python.jpg 400 300 %}  
  
Python语言相对Java来说，最大的特点就是易用易学，基本上每个linux系统都有安装python，不像Java一样还需要自己下JVM、安装、设置环境变量等。有了其他编程语言的基础再来学习Python其实是非常简单的，因为Python语言是基于[ABC][abc]语言开发出来的，ABC语言是以教学为目的的语言，其宗旨是让编程变得容易阅读和理解，所以Python语言也很容易理解和学习。
  
## Pyhotn学习介绍
关于Python的学习网上有很多介绍，我在这里介绍一下我的学习过程，包括书籍，网站和一些工具等。  
  
#### 书籍
关于Python的书我推荐[《Python核心编程（第二版）》][python_book]，这本书看起来很厚其实读起来非常浅显易懂，重点可以看看下面这几章。  
  
{% codeblock lang:sh %}
第4章 Python对象
6.8 Unicode
8.11 迭代器和iter()函数
第9章 文件的输入和输出
第10章 错误和异常
第11章 函数和函数式编程
第12章 模块
第13章 面向对象编程
第14章 执行环境
第15章 正则表达式
第18章 多线程编程
20.2 使用Python进行Web应用：创建一个简单的Web客户端
{% endcodeblock %}
  
如果不想看这种太多理论的书的话，可以看这本实战的书[《Learn Python The Hard Way》][learn_python_the_hard_way]，里面的例子从浅到深让你一步步的学习Python，比较适合没有编程经验的新手。里面的例子不要只是把它们打出来，最好自己可以举一反三做一些关于知识点的挑战和扩展。  
  
#### 网站
除了看书编码外，平时业余时间可以订阅一些Python的blog和网站，下面是我最近收集的一些Python博客，分享一下:  
  
* [Planet Python][planet_python]: 这是最出名的python博客其中之一，快去看看，我希望你能找到对你有用的东西。
* [lucumr][lucumr]: 博主是flask（一个python web框架）的创始人
* [Doug Hellmann][doug_hellmann]: 博主是PYMOTW(Python Module Of the Week)成员之一，博客里面包含了很多python library的知识。如果你是在寻找一些实用的python库的话，你可以在这里找找。
* [Mouse Vs Python][mouse_vs_python]: 这个博客更新也比较频繁。
* [pydanny][pydanny]: 这是一个主要关注Django的博客，但也有很多关于Python的东西。
  
#### 工具 & 技巧
pip和setuptools就不说了，Python开发必备，下载管理依赖包就靠他们了，其他最有用的Python工具要数[virtualenv][virtualenv]了，它可以创建一个虚拟的Python执行环境，因为Python的虚拟机不像JVM那么大，创建出来之后的文件夹非常小（大概只有10M），在这个虚拟环境里面安装执行Python程序不会污染你操作系统的主Python环境。  
  
{% codeblock lang:sh %}
# 安装virtualenv
pip install virtualenv
# 创建Python虚拟环境
virtualenv venv
# 让虚拟环境生效
source venv/bin/activate
# 取消虚拟环境
deactivate
{% endcodeblock %}
  
另外Pyhton的调试也非常方便，只要在需要设置断点的地方添加代码`import pdb;pdb.set_trace()`即可，然后执行程序就会在断点出停止，你可以通过打印下面命令进行调试:  
  
* `c`: 让程序继续往下走，continue的意思
* `s`: 进入子方法内部，相当于step into
* `n`: 跳到下一句 
这里介绍的只是一些基本的命令，更多的命令可以看[这里][pdb]，另外还可以打印断点处的各种变量和执行各种语句。  
  
## 简洁的Python
在Python中执行`import this`可以看到Python之禅。  
  
{% blockquote %}
The Zen of Python, by Tim Peters

Beautiful is better than ugly.
Explicit is better than implicit.
Simple is better than complex.
Complex is better than complicated.
Flat is better than nested.
Sparse is better than dense.
Readability counts.
Special cases aren't special enough to break the rules.
Although practicality beats purity.
Errors should never pass silently.
Unless explicitly silenced.
In the face of ambiguity, refuse the temptation to guess.
There should be one-- and preferably only one --obvious way to do it.
Although that way may not be obvious at first unless you're Dutch.
Now is better than never.
Although never is often better than *right* now.
If the implementation is hard to explain, it's a bad idea.
If the implementation is easy to explain, it may be a good idea.
Namespaces are one honking great idea -- let's do more of those!
{% endblockquote %}
  
从Python之禅中我们可以看到Python是一门追求简洁，追求优雅的语言，语言设计者对代码质量要求比较高，希望编写Python代码的开发人员也能写出高质量的代码。  
  
由于Python简洁而强大的函数式编程，我们可以很方便的创建结构稍微复杂的对象，特别复杂的还是建议使用class来创建对象。  
  
比如我们有这样的一个数据结构，一个字典里面包含多个集合，用Java来初始化大概是这个样子:   
  
{% codeblock lang:java %}
Map<String, List<String>> map = new HashMap<String, List<String>>();
List<String> list1 = new ArrayList<String>();
list1.add("foo1");
list1.add("bar1");
map.put("list1", list1);

List<String> list2 = new ArrayList<String>();
list2.add("foo2");
list2.add("bar2");
map.put("list2", list2);
{% endcodeblock %}
  
而用Python一行代码就可以搞定:  
  
{% codeblock lang:python %}
my_map = {'list1': [foo1, bar1], 'list2': ['foo2'，'bar2']}
{% endcodeblock %}
  
所以说Java是一门很啰嗦的语言，虽然新版本的Java加了lambda，然而并不能减少多少Java语言本身的繁杂性。  
  
## Djaogo学习介绍
  
Python来实现一些小工具小应用是十分方便的，但Python也可以用来做Web开发，Python比较有名的Web框架有Django，Flask，Tormado等，其中Djaongo使用最为广泛，集成的东西也比较多，不管你是使用关系系数据库还是非关系数据库，是否使用缓存等都可以使用Django，其最好的一个特点是集成了管理员功能，可以省却开发者很大的一部分开发量。  
  
有人可能会说Django比较重，对于新手来说学习成本比较高，对于这个我没有什么意见，因为工作需要使用Django，对于其他的框架还没有接触过，但我使用后的体验是Django虽然要配置的东西比较多，但只要配置好了，开发效率还是挺快的，也可能是我以前大部分时间使用Spring来开发，相对Spring来说Django的配置还算比较轻的，所以对我来说没有什么感觉:)  
  
学习Django可以上Django的[官网][django]，上面有startup的向导，通过向导一步步的操作可以让你快速了解一个web项目的开发，另外上面还有很多文档，可以知道Django的所有内容。  
  
另外一个学习Django比较好的是《Django Book》这本书，这里有它的[网站][django_book]，书很浅显易读，详细讲解了Django各个部分的内容，有时候是先通过一个比较简单的实现方案实现需求，然后利用Django的特性让代码得到优化，更少的代码来实现更多的内容，这也体现了Django的思想，简洁优雅，复用性高。  
  
理论知识学习的差不多了，最后当然是做项目练手了，可以自己动手做一个web项目来熟悉Python和Django，在实际操作的过程中遇到不懂的问题就google一下，然后记录下来，做过一两个项目之后你应该对Python和Django比较了解了:)  
  


[abc]: https://en.wikipedia.org/wiki/ABC_(programming_language)
[python_book]: http://book.douban.com/subject/3112503/
[learn_python_the_hard_way]: http://learnpythonthehardway.org/
[lucumr]: http://lucumr.pocoo.org/
[doug_hellmann]: https://doughellmann.com
[planet_python]: http://planetpython.org/
[mouse_vs_python]: http://www.blog.pythonlibrary.org/
[pydanny]: http://www.pydanny.com/
[virtualenv]: http://docs.python-guide.org/en/latest/dev/virtualenvs/
[pdb]: https://docs.python.org/2/library/pdb.html
[django]: http//www.djangoproject.com
[django_book]: http://www.djangobook.com/en/2.0/index.html

