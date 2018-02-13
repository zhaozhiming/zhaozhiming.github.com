---
layout: post
title: "基于 React 的 CMS 框架对比：Docusaurus vs. Gatsby"
date: 2018-01-30 20:01
description: 基于 React 的 CMS 框架对比：Docusaurus vs. Gatsby
keywords: react,docusaurus,gatsby
comments: true
categories: [code]
tags: [react,docusaurus,gatsby]
---

{% img /images/post/2018/01/vs.jpg 400 350 %}

最近 Facebook 推出了一个文档工具 [Docusaurus](http://docusaurus.io/)，既可以用来做产品网站，也可以用来写博客，还提供很多很有用的功能，最重要的是它是基于 React 实现的。

同样基于 React 技术的静态网站生成工具 [Gatsby](https://www.gatsbyjs.org/)，也具备了类似的功能，而且它提供了非常丰富的插件。

可见两者各有千秋，那我们应该选择呢？或者说我们应该基于什么场景来使用它们呢？

<!--more-->

## CMS

首先我们先了解一下什么是 CMS，下面是维基百科的定义：

{% blockquote 维基百科 %}
内容管理系统（英语：content management system，缩写为 CMS）是指在一个合作模式下，用于管理工作流程的一套制度。该系统可应用于手工操作中，也可以应用到电脑或网络里。作为一种中央储存器（central repository），内容管理系统可将相关内容集中储存并具有群组管理、版本控制等功能。版本控制是内容管理系统的一个主要优势。
{% endblockquote %}

看完是不是对什么是 CMS 更懵了？没关系，我们接着往下看。

那什么样的网站属于 CMS 呢？其实大部分网站都可以归属于 CMS，但更多时候是指下面这些类型的网站：

- 博客类网站，以个人博客类居多
- 公司类网站，例如[知道创宇的网站](https://www.knownsec.com)
- 产品类网站，比如 [Reactjs 的网站](https://reactjs.org)

总的来说就是指由静态化的页面组成的网站。

一直以来都有很多工具来制作或生成 CMS 网站，下面是几个比较出名的 CMS 框架。

- Wordpress: 老牌的 CMS 框架，以超多插件功能强大而著称，也以多安全漏洞而受广大黑客喜爱。
- Gitbook: 是一个支持 Markdown 格式的文档编写工具，可以很方便地和 github 进行集成。
- Hexo: 是用 Nodejs 编写的博客框架，支持多种博客主题，同样支持 Markdown 格式。

但在现代化的 web 开发体系中，这些框架在前端技术上显得有些落后，目前 React 是全球范围内最受欢迎的前端框架（没有之一），我们当然希望可以有一个基于 React 技术的静态网站开发工具。

## Gatsby 和 Docusaurus

其实基于 React 技术的静态网站生成工具也有很多，但是比较出名的要数`Gatsby`了，一个是因为它出来的时间比较早，另一个是因为它一直在迭代完善，从而让很多开发者都喜欢它。

`Docusaurus`是 Facebook 公司最近刚开源出来的一个建站工具，当然它也是基于 React 的。相比`Gatsby`，它更多是为产品类网站而服务，集成了很多产品类网站所需要的功能，包括：版本化、国际化、站内检索等。

## Gatsby vs. Docusaurus

虽然两者都是基于 React 的静态网站工具，但是在很多方面还是不一样的，下面通过几个方面介绍两者的不同之处，希望大家看完之后可以根据自己的需求做出正确的选择。

### 入门容易度

在入门容易度方面，`Docusaurus`要完胜`Gatsby`，`Docusaurus`的文档简单易懂，花半个小时基本上就可以看完了，然后再花个 5 分钟就可以搭建出一个静态网站，用户只需要通过 markdown 来编写文档就可以了。

`Docusaurus`留给用户自定义配置的地方并不多，基本上都集中在`siteConfig.js`这个文件里面，所以用户要关心的东西并不多。

`Gatsby`虽然也有官方文档，但因为涉及的点比较多所以文档也很长，比如你可能需要了解`GraphQL`（后台通过它来获取博客文章和站点信息），还需要知道有哪些`starter`（可以理解为项目模板，里面不仅包含了网站的样式，还包含了网站的配置）可以选择，还需要知道有哪些插件可以使用（包括官方和第三方的很多插件），光看完这些文档就要费不少时间。

看完文档之后你可能还需要去参考其他`starter`，选择其中一个来作为网站的模板，在其基础上进行修改，如果是用最基本的`starter`来搭建网站的话，那要做的东西就太多了，所以一般是选择一个适合自己的`starter`来创建项目会比较好。

### 页面定制

`Docusaurus`是集成度比较高的一个产品，所以用户可扩展的东西并不多，比如首页和博客文章页面的布局用户是无法进行大幅度改动的，但可以进行一些小范围的修改。比如在`siteConfig.js`里面有个属性是`colors`，里面可以让用户自定义网站的`主颜色`和`次要颜色`。

{% codeblock lang:js %}
  /* colors for website */
  colors: {
    primaryColor: '#2E8555',
    secondaryColor: '#205C3B',
  },
{% endcodeblock %}

另外`Docusaurus`提供了一个`custom.css`文件，让用户可以在里面通过覆盖原来的 class 来达到改变样式的目的。

{% codeblock lang:css %}
/* your custom css */

@media only screen and (min-device-width: 360px) and (max-device-width: 736px) {}

@media only screen and (min-width: 1024px) {}

@media only screen and (max-width: 1023px) {}

@media only screen and (min-width: 1400px) {}

@media only screen and (min-width: 1500px) {}
{% endcodeblock %}

`Gatsby`在这一点上比`Docusaurus`要好很多，因为`Gatsyb`上所有页面文件用户都可以随意修改，包括页面的内容和样式。

`Gatsby`的 src 目录结构如下：

{% codeblock lang:sh %}
.
├── components
│   ├── Bio.js
│   └── profile-pic.jpg
├── layouts
│   └── index.js
├── pages
│   ├── hello-world
│   │   ├── index.md
│   │   └── salty_egg.jpg
│   ├── index.js
│   └── my-second-post
│       └── index.md
└── templates
    └── blog-post.js
{% endcodeblock %}

* layouts/index.js: 这是网站页面整体布局的文件，可以在这里定义页面头部，底部和侧边栏，然后通过`this.props.children`来渲染子页面。有 2 种子页面，一种是网站首页页面，另一种是博客文章的页面。
* pages/index.js: 这个文件是网站首页的页面文件，在这里可以定义首页页面的内容。
* templates/blog-post.js: 这个文件是博客文章的页面文件，在这里可以定义一篇博客文章要如何展示信息，比如标题是什么样式，作者是什么样式等。

如果你的网站不需要博客文章，你甚至可以在`layout/index.js`中去掉`this.props.children`部分，直接换成你需要的页面内容，这样你也就不需要去编辑`pages/index.js`文件了。

总之，`Gatsby`的页面扩展性是非常好的，可以随时将页面修改成你需要的网站样式；而`Docusaurus` 则只能在其定制好的页面框架下进行一些小修改。

### 页面自适应

`Docusaurus`本身已经做好了页面自适应，用户可以不关心这方面的问题，只要是用`Docusaurus`搭建出来的网站都是页面自适应的。

`Gatsby`本身是不具备页面自适应功能的，但一些`starter`会具有页面自适应的能力，只要通过这些`starter`搭建出来的网站也是页面自适应的，但这种情况相当是受限在这个`starter`的页面框架里面，如果需要做一些页面样式上的调整，就要检查是否会破坏原来的自适应功能。

### 文档检索

`Docusaurus`本身集成了 [algolia](https://www.algolia.com/) 来做站内文档检索功能，用户只需要在`siteConfig.js`中配置好`algolia`的选项就好了。

{% codeblock lang:js %}
  algolia: {
    apiKey:
      "your api key",
    indexName: "your index name"
  },
{% endcodeblock %}

这是集成了`algolia`检索功能的截图：

{% img /images/post/2018/01/algolia.png %}

`Gatsby`本身不具备站内检索功能，但它可以通过`GraphQL`进行文章查询，所以我觉得可以通过`GraphQL`来实现站内检索的功能，但这个需要用户自己去实现了。

其实`Gatsby`也可以集成`algolia`，大家可以看 [React](https://reactjs.org/) 的官方网站，它就是用`Gatsby`做的，里面集成`algolia`站内检索功能，感兴趣的可以去看下他们网站关于`docsearch`这一块的[源码](https://github.com/reactjs/reactjs.org/search?utf8=%E2%9C%93&q=docsearch&type=)。

### 版本化和国际化

在一些产品中，文档的版本管理是一个比较常见的需求：文档可以根据不同的版本号进行不同的内容展示，这样可以让老版本用户看到相关文档，老文档不会被新版本的文档所覆盖。

同样的，国际化也是一些跨国产品的强需求，可以通过切换不同的语言来展示不同语言的文档，这样可以吸引更多国家的人来使用产品。

`Docusaurus`本身已经集成了这 2 个功能，这也是它的卖点之一，你可以通过命令行来添加新的版本号。每个版本号都是一个文件夹，在里面存放了这个版本的相关文档。

国际化的功能也是类似的操作。

`Gatsby`本身则不具备这些功能，需要用户自己开发。

### 编辑分离

什么是编辑分离呢？假设我们的网站有成千上百个文档，这些文档如果是让搭建网站的程序员来维护的话可能力不从心，而且文档的专业性也得不到保证，但是专业的文档编辑人员又不懂 markdown 语法，那要让他们怎么进行文档编辑呢？这个时候就需要将编辑功能从网站上分离出去了，比如说网站的外部提供了所见即所得的富文本编辑器，让文档编辑人员进行文档编写，然后将编写完的内容同步到网站。

[`contentful`](https://www.contentful.com/) 是一个专门做文档编辑功能的产品，可以在上面进行文档编辑，并通过 API 获取到文档内容。

而之前我们说了，`Gatsby`是一个拥有丰富插件的框架，它就提供了这样一个[插件](https://www.gatsbyjs.org/packages/gatsby-source-contentful/) 来让网站和 `contentful`进行集成，配置内容如下：

{% codeblock lang:js %}
// In your gatsby-config.js
plugins: [
  {
    resolve: `gatsby-source-contentful`,
    options: {
      spaceId: `your_space_id`,
      accessToken: `your_access_token`,
    },
  },
];
{% endcodeblock %}

而`Docusaurus`不具备这方面的功能。

## 总结

下面的表格总结上面列的点：

 | Docusaurus | Gatsby
:----------|:------------:|:----------:
入门难度 | ⭐️⭐️⭐️ | ⭐️
页面定制 | ⭐️ | ⭐️⭐️⭐️
页面自适应 | ⭐️⭐️⭐️  | ⭐️⭐️
文档检索 | ⭐️⭐️⭐️ | ⭐️⭐️
版本化和国际化 | ⭐️⭐️⭐️ | ⭐️
编辑分离 | ⭐️ | ⭐️⭐️⭐️

总的来说，`Docusaurus`是一个为产品类网站量身定做的建站工具，具备了大部分产品类网站所需的功能，包括国际化、版本化和站内检索等；而`Gatsby`更像一个框架，你甚至可以使用`Gatsby`来制作一个类似`Docusaurus`一样的产品，如果你的网站有自己的样式要求，或者只是想搭建一个自己的博客，那么你可以选择`Gatsby`。
