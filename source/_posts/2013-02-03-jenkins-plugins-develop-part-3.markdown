---
layout: post
title: 'jenkins插件开发（三）-- 插件编写'
date: 2013-02-03 20:18
description: 'jenkins插件开发（三）-- 插件编写'
keywords: jenkins插件,开发
comments: true
categories: code
tags: jenkins plugins
---

在上一篇 blog 中我们介绍了如何创建我们第一个 jenkins 插件，在这一篇 blog 继续介绍在开发我们的插件过程中需要注意的一些问题。

<!--more-->

### 扩展点选择

Jenkings 插件是基于扩展点来实现的，比如基于 Builder 这个扩展点，那这个插件的功能就是一个构建插件，类似 ant-builder（使用 ant 来执行构建脚本）。Jenkins 插件的扩展点有很多，具体可以查询[这里][url1]。该网页列出了 Jenkins 所有的扩展点，点击每个扩展点下面 Implementations，会列出该扩展点对应的实现类。找到实现类的源码就可以知道具体怎么使用该扩展点了，大部分的代码在 github 上都可以找到。在前面创建的 HelloWorld 插件是一个基于 Builder 扩展点的插件，可以在 job 配置页面的 Build 中增加该插件，以后每次 Job 每次构建的时候就会去调用该插件了。  
[url1]: https://wiki.jenkins-ci.org/display/JENKINS/Extension+points#Extensionpoints-hudson.tasks.Notifier

### Action 介绍

Action 是插件用来在 Job 或 Build 页面增加功能的一种主要方式，是 Jenkins 最常用的一个扩展点。从下图中可以看出什么是 Action，就是页面左边菜单栏的一个菜单项，还可以在右边的主页面显示相应的功能。

{% img https://wiki.jenkins-ci.org/download/thumbnails/58001011/actions.png %}

每个继承了 Action 这个扩展点的插件都要实现 3 个方法，方法如下：

{% codeblock Action.java lang:java %}
public interface Action extends hudson.model.ModelObject {
java.lang.String getIconFileName();

    java.lang.String getDisplayName();

    java.lang.String getUrlName();

}
{% endcodeblock %}

第一个是菜单项图片，第二个是菜单名称，第三个是菜单链接。

Action 分瞬时和持久 2 种，这里主要介绍的是瞬时的 Action。瞬时的 Action 可以随时废弃，让另外一个新的 Action 来取代，适合一些每次构建都要执行操作的插件，但不适合需要保存持久数据的插件。

在 Jenkins 官网的插件开发指南中，推荐使用 Transient\*\*\*ActionFactory 系列的继承点， 有 TransientViewActionFactory，TransientProjectActionFactory，TransientBuildActionFactory 等，使用该系列的继承点，只需要简单的覆写父类的 createFor 方法，就可以实现创建瞬时 Action 的目的，可以根据不同的需要创建 Job，Build，View 的 Action。

更多内容可以参考[这里](https://wiki.jenkins-ci.org/display/JENKINS/Action+and+its+family+of+subtypes)。  

### Jelly 页面

Jenkins 插件开发中涉及到页面的开发，比如 Job 的配置页面，相关插件需要加上相关的配置开关，配置参数时，就需要有一个配置页面来做相应的显示。插件开发中用到的页面是 Jelly 页面，在第一篇 blog 中介绍了 IDEA 中关于 jelly 的插件，使用该插件可以更加方便 Jelly 页面的编写。

Jelly 页面的简单例子就不介绍了，官网上都有，这里简单介绍一下 Jelly 页面相关的一些标签。与 JSTL 类似，Jelly 也有自己的一些标签，如下：

{% codeblock lang:html %}
<j:jelly
xmlns:j="jelly:core"
xmlns:st="jelly:stapler"
xmlns:d="jelly:define"
xmlns:l="/lib/layout"
xmlns:t="/lib/hudson"
xmlns:f="/lib/form"
xmlns:i="jelly:fmt"
xmlns:p="/lib/hudson/project">
{% endcodeblock %}

比如想使用 jelly:core 标签，就可以在页面中直接使用 j:XX 来调用标签的相关功能，其他标签同样按照这个方式来使用。其中 jelly:core 是常用的功能，比如设置变量，循环，判断等功能。jelly:layout 是分层相关的标签，/lib/hudson 是 jenkins 相关的功能，比如设置 Job 页面图标，Build 页面图标等。更多标签相关的内容可以参考[这里][url2]。  
[url2]: https://jenkins-ci.org/maven-site/jenkins-core/jelly-taglib-ref.html

### 配置文件

Jenkins 插件开发中还涉及到一类文件，就是配置文件。配置文件不仅可以在 Jelly 页面中使用，而且可以在 Java 文件中使用，不过在 Java 文件中使用的话需要先将配置文件编译成对应的 Java 文件。

**如何在 Jelly 页面中使用配置文件？**首先要在 Jelly 页面所在的文件夹中放置配置文件，比如 页面在这里 dir/myAction.jelly，那么页面对应的配置文件就应该在 dir/myAction.properties。注意，配置文件名字需要和页面名字相同，这样页面才可以找到对应的配置文件。比如有个 Jelly 页面如下：

{% codeblock demo.jelly lang:html %}
<?jelly escape-by-default='true'?>
<j:jelly xmlns:j="jelly:core">
<th>${\%allBuildsColumnHeader}</th>
</j:jelly>
{% endcodeblock %}

其对应的配置文件内容如下：

{% codeblock lang:properties %}
allBuildsColumnHeader=mean time to repair for all history
{% endcodeblock %}

这样配置了以后，展示 Jelly 页面时就可以自动调用配置文件里面的内容了。

**如何在 Java 文件中使用配置文件？**一般插件所用的 Java 配置文件名称都叫做 Messages.properties，文件保存在 resources 目录下。比如我们的插件包结构是 jenkins.plugins.myplugin，那么 Messages.properties 文件就保存在 resources/jenkins/plugins/myplugin/目录下。

如果 Java 文件中想要使用配置文件里面的值，需要先执行 mvn pacakge 命令，执行之后，以上面的例子为例，在 target 目录下会产生 generated-sources/localizer/jenkins/plugins/myplugin/Messages.java 这个 Java 文件，如果配置文件里面有这样的一个配置项：

{% codeblock lang:properties %}
allBuildsColumnHeader=mean time to repair for all history
{% endcodeblock %}

那么 Messages.java 里面就会产生这样的 2 个方法：

{% codeblock Message.java lang:java %}
/\*\*
_ mean time to repair for all history
_
\*/
public static String allBuildsColumnHeader() {
return holder.format("allBuildsColumnHeader");
}

    /**
     * mean time to repair for all history
     *
     */
    public static Localizable _allBuildsColumnHeader() {
        return new Localizable(holder, "allBuildsColumnHeader");
    }

{% endcodeblock %}

这样在 Java 文件中就可以直接调用这 2 个方法了，一般是调用第一个方法。

**多语言配置。**配置文件的文件名如果不带后缀就是内容是英语的配置文件，比如 Messages.properties。如果想让你的插件可以在不同的国家语言下都显示正常的话，就需要将你的配置文件复制多份，在名字后面加上不同的后缀，然后内容换上相关语言的内容。

比如，以上面例子为例，中文的配置文件名称就是 Messages_zh_CN.properties，配置文件的页面也要换上相应的中文 ASCII 编码，这样就可以在中文系统环境下显示中文内容了。如何将中文转 ASCII 码？可以直接使用 Java Home 里面的 native2ascii 命令。

### 开发经验

在开发 Jenkins 插件的过程中自己摸索了一些方法，在这里介绍一下，以免让其他同学走弯路。  
首先是去到 Jenkins 的[官网 WIKI][url3]。这里介绍了如何创建你的插件工程，插件扩展点，插件编码和页面如何编写等，内容比较多，但你只需要关注你需要的内容就可以了。  
[url3]: https://wiki.jenkins-ci.org/display/JENKINS/Extend+Jenkins

光看 Jenkins 的 WIKI 是不够的，有一些页面和类的用法上面没有直接给出，那肿么办呢？很简单：看源码。在 github 上 download 下 jenkins 的源码（[这里][url4]），在 IDE 里面打开 Jenkins 工程，比如想要查看某个 jelly 页面的用法，就可以在工程中搜索所有 jelly 页面，看看源码中的页面是怎么写的，我们再拿过来改一下就可以了。类和扩展点不知道如何写，也可以参照这个方法，通过源码了解其他扩展点子类是如何写的，然后再写出自己的插件。  
[url4]: https://github.com/jenkinsci/jenkins

可能在 jenkins 的源码中还是找不到扩展点的用法，那怎么办呢？这个时候可能就需要去参考其他插件是如何写的了，幸运的是，大部分插件的代码现在都放在了 github 上，我们可以通过了解其他插件来写出自己的插件。

---

- [jenkins 插件开发（一）-- 环境搭建](http://zhaozhiming.github.io/2013/01/31/jenkins-plugins-develop-part-1/)
- [jenkins 插件开发（二）-- HelloWorld](http://zhaozhiming.github.io/2013/02/02/jenkins-plugins-develop-part-2/)
- [jenkins 插件开发（四）-- 插件发布](http://zhaozhiming.github.io/2013/02/04/jenkins-plugins-develop-part-4/)
