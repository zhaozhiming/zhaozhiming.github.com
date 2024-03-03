---
layout: post
title: '利用Sonar定制自定义JS扫描规则（二）——自定义JS扫描规则'
date: 2013-05-19 11:20
comments: true
categories: code
tags: [js, sonar]
---

在上一篇 blog 中，我们将 sonar 几个需要的环境都搭建好了，包括 sonar 的服务器，sonar runner，sonar 的 javascript 插件。现在我们就来讲如何自定义 JS 扫描规则。

<!--more-->

实际上有 3 种方法可以自定义代码的校验规则：

- 直接在 sonar 的 web 接口中增加 XPath 规则；
- 通过插件的功能来增加自定义规则，比如 checkstyle，pmd 等插件是允许自定义规则的；
- 通过新增一个代码分析器来实现自定义规则；

sonar 官方推荐的方式是使用最简单的 XPath 方式来增加自定义规则，如果语言太复杂或者 XPath 无法查询其结构的，再使用自定义插件的方式，所以我们这里重点介绍的是使用 XPath 的方式。

### 使用 XPath 增加自定义 JS 规则

sonar 为大部分流行的语言（C, C#, C++, Cobol, Flex, JavaScript, PL/I, PL/SQL, Python and VB.NET）提供了一个简洁的方式来增加代码规则。这些新增规则必须使用[XPath](http://en.wikipedia.org/wiki/XPath)来实现，这样的话语言的每一部分都可以映射到[Abstract Syntax Tree](http://en.wikipedia.org/wiki/Abstract_syntax_tree)（AST）。对于每一种语言，SSLR Toolkit 工具提供了代码转 AST 的功能，该工具具体的使用在下面会介绍，这里是[SSLR Javascript Toolkit](http://repository.codehaus.org/org/codehaus/sonar-plugins/javascript/sslr-javascript-toolkit/1.3/sslr-javascript-toolkit-1.3.jar)工具的下载。如果对 XPath 不熟悉，可以参考这里看一下例子[http://www.w3schools.com/xpath/](http://www.w3schools.com/xpath/)。

1、首先使用管理员的角色登录 sonar，然后去到 Settings > Quality Profile 页面，选择 Js 规则（如下图所示）。

{% img  /images/post/02070803-9bce5fab1bf543059071fafff2ae37d8.png %}

2、接着在 Coding rules 页面输入查询条件，找到 XPath 规则：

{% img  /images/post/02070943-c9a5b7732ea6478fba266555c308fc29.png %}

3、查询结果出来后，点击 Copy rule 连接来新增规则：

{% img  /images/post/02071054-da90a48498234fc19d34aff75c84a104.png %}

4、按照 XPath 的语法编写自己的规则：

{% img  /images/post/02071436-15c170e9f4eb4f6684d95df76fe6e628.png %}

如果对 XPath 不熟悉，建议先学习一些 XPath 的语法，下面给出 3 个 javascript xpath 的规则：

##### 不要使用 document.write:

{% codeblock lang:text %}
//callExpression/memberExpression[count(\*) = 3 and primaryExpression[@tokenValue = "document"] and identifierName[@tokenValue = "write"]]
{% endcodeblock %}

##### if/else 语句后面必须接大括号：

{% codeblock lang:text %}
//ifStatement/statement[not(block)]
{% endcodeblock %}

##### 本地变量名以 XX 开头：

{% codeblock lang:text %}
//functionBody//variableDeclaration/IDENTIFIER[not(starts-with(@tokenValue,'zzm'))]
{% endcodeblock %}

5、创建好规则之后，把规则设置为 active，将规则的级别调高一些就会自动变为 active，然后重新跑一下规则分析，这样新增的规则就生效了。

{% img  /images/post/02072520-4308a5a558d04ded998bb54f452c71f7.png %}
