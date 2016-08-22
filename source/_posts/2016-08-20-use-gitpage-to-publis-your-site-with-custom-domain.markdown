---
layout: post
title: "使用 GitHub Page 来发布属于你自己的网站"
date: 2016-08-20 14:51
description: 使用 Github Page 来发布属于你自己的网站
keywords: github-page
comments: true
categories: github
tags: [github-page]
---

{% img /images/post/2016/08/github-pages.jpg %}

GitHub Page 是一个由 Github 公司推出的一个静态网站托管服务，可以结合 GitHub 中的用户或者项目来发布相关的静态网站，适用于发布项目 Demo，个人博客，产品介绍等，下面介绍一下 GitHub Page 的使用方法，并介绍如何绑定自定义域名，包括如何在 [namesilo](https://www.namesilo.com/) 上购买域名，以及如何配置 DNS 解析服务等操作。

<!--more-->

## GitHub Page 的使用

Github Page 的使用非常简单，在官网的首页里面就有图文并茂的操作介绍，[官网请看这里](https://pages.github.com/)。创建静态网站有 2 种方式，一种是为用户或者组织创建网站，有些框架甚至集成了从编写博客到发布 GitHub Page 的功能，比如 [Octopress](http://octopress.org/)；另外一种是为项目创建网站，下面着重介绍项目网站，其实跟用户网站差不多。

### 创建网站的 2 种方法

* 通过 setting 配置
  
为项目创建网站可以参照官网首页上面的那种方式，直接在项目 setting 里面进行配置（具体操作请看官网），但这种方式的缺点是只能添加一个 Markdown 的页面，虽然有网站模板可以选择，但自定义的功能还是十分有限。  
  
* 通过命令行方式
  
创建网站的另外一种方法跟 GitHub Page 的个人网站比较类似，通过命令行的操作，包括创建分支，添加网站内容等来发布你的网站，这种方式可以定制的功能就比较多了，甚至可以制作出一些内容非常丰富的网站。具体操作如下：

    * 首先新建一个叫 gh-pages 的 git 分支，并且这个分支跟其他分支没有任何关系，`git checkout --orphan gh-pages`
    * 在这个分支下面将原有的内容都去掉，`git rm -rf .`
    * 添加网站内容比如 index.html，然后`git commit && git push`
    * 在 GitHbu 项目的 setting 中选择 GitHub Page 通过 gh-pages 分支来发布
    * 在浏览器中访问你的网站，`http(s)://<username>.github.io/<projectname>`

{% img /images/post/2016/08/gh-pages.png 200 300 %}

### 发布的 3 种方式

上面介绍的通过`gh-pages`分支来发布网站是老的发布方式，GitHub Page 最近推出新发布方式，可以通过`master`分支和 master 分支里面的`docs`文件夹来发布网站。

* 通过 master 分支来发布跟之前介绍的通过 gh-pages 分支发布差不多，区别是不需要单独创建一个分支，直接在 master 分支里面添加网站内容即可，然后在项目 setting 里面选择通过 master 分支来发布。

* 通过 master 分支的 docs 文件夹发布，首先需要在 master 分支下面新建一个 docs 文件夹，然后在文件夹中添加网站内容，这样在项目 setting 里面就可以看到`master branch/docs folder`这个选项了。这种方式的好处是可以在项目中同时放置项目源码和生产代码，将打包后的生产文件放到 docs 里面就可以发布网站了。

## 自定义域名

使用 GitHub Page 创建的网站域名一般是`http(s)://<username>.github.io/<projectname>`，但可以配置自己的域名，当然，首先你需要有一个自己的域名。

### namesilo

之前准备使用 [Godaddy](https://www.godaddy.com/) 这个域名注册商，但看了一下 Godaddy 的续费价格比较贵，而且网站不知道为什么老是卡在提交支付方式这一环节上，所以就放弃了。namesilo 是另外一个口碑较好的域名注册商，服务质量与 Godaddy 比有过之而不及，而且续费不加价，虽然界面丑了点但是不影响使用。

### 折扣网站

在购买域名之前，可以先到这个域名折扣网站 [DomComp](https://www.domcomp.com/)看看，在上面可以看到各个域名注册商的最新优惠码，而且可以通过分享链接得到购买域名的返利。

比如下面这个截图是 DomComp 网站上的一个 namesilo 的优惠码信息，上面的是优惠码，下面的是有效截止时间。

{% img /images/post/2016/08/domcomp.png 300 300 %}

PS: 网站上面有很多`1&1`这个域名注册商的优惠码，他们的域名价格比较便宜，但是口碑不是很好，图便宜的同学请谨慎考虑。

关于 DomComp 的返利功能可以看下知乎的[这个回答](://www.zhihu.com/question/19551906/answer/31986656)，这里面的介绍比较详细，我也是从这个答案了解到相关信息的。

## GitHub Page 配置自定义域名

购买完域名后，就可以配置到 GitHub 的项目中了。

### 项目 setting 配置

首先在项目的 setting 中，GitHub Pages 那一栏的 Custom domain 填写自己的域名，填写完成后因为域名的 DNS 信息还没有配置，所以会出现黄色的警告信息。

### namesilo DNS 配置

登陆 namesilo 进到域名管理的界面，点击域名那一栏后面那个蓝色图标。

{% img /images/post/2016/08/namesilo_dns1.png 250 100 %}

下面有很多网站的 DNS 配置模板供你选择，我们选择 Github 这个模板。

{% img /images/post/2016/08/namesilo_dns2.png %}

模板会为你添加一个类型为`A`，地址为`192.30.252.153`的记录和类型为 CNAME 的子域名记录，子域名那一行的地址要填上自己的 github 用户域名`<username>.github.io`。

{% img /images/post/2016/08/namesilo_dns3.png %}

配置成功后，一般要等几个小时后才能生效，生效后 GitHub 项目 setting 里面的提示信息会变成绿色。

## 国内 DNS 解析加速

namesilo 是国外的域名注册商，国内访问速度会比较慢，这个时候可以加个 DNS 解析来提高域名的访问速度。这里当然要首选 [DNSPod](https://www.dnspod.cn) 这个免费的 DNS 解析服务商，对于一些个人博客等非盈利的网站是免费使用的。

配置非常简单，还是进到 namesilo 的域名管理界面，选中域名那一栏最前面的勾选框，然后上面一排灰色的图标会显示出颜色，选择其中的`Change Namesevers`图标。

{% img /images/post/2016/08/namesilo_ns1.png %}

在`NameServer1`和`NameServer2`中填写 DNSPod 的 nameserver 地址`f1g1ns1.dnspod.net`，`f1g1ns2.dnspod.net`。

{% img /images/post/2016/08/namesilo_ns2.png 300 100 %}

配置完成后网站的访问速度绝对是一个质的飞跃！

