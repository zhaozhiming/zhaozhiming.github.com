---
layout: post
title: "如何在 Rinkeby 网络赚取以太币及代币"
date: 2018-04-18 22:41
description: 如何在 Rinkeby 网络赚取以太币及代币
keywords: eth,rinkeby,token
comments: true
categories: etherum
tags: [eth,rinkeby,token]
---

{% img /images/post/2018/04/earn_eth.jpg 400 300 %}

在开发以太坊应用时最苦恼的事情莫过于手头账户上没有以太币（以下简称 eth），没有 money 什么事情都干不了，好在以太坊提供了几个测试网络，在测试网络上开发约等同于真实环境的主网络，最重要的是测试网络的 eth**不要钱！不要钱！不要钱！**但刚接触以太坊开发的同学可能还不清楚如何操作，下面就来介绍一下在 Rinkeby 测试网络的赚钱大法。

<!--more-->

## MetaMask 钱包

首先你需要有一个以太坊钱钱包，这样你才能拥有自己以太坊账号，才能查看自己账号里面是否有金额进账。

最简单的方式是使用 [MetaMask](https://metamask.io) 这款轻量级钱包，它以浏览器插件的形式提供功能，现在支持的浏览器包括 Chrome、Firefox、Opera。

安装完插件后你就可以在浏览器插件中进行账户创建、查看余额等一系列操作，以及后面介绍的赚取代币，都可以通过这个钱包应用来完成。

### 如何使用 MetaMask 钱包

网上有很多关于 MetaMask 钱包的使用说明，这里就不多介绍了，可以参考以下链接：

* [metamask 简明教程](https://www.94eth.com/tutorial/metamask)
* [METAMASK 以太坊轻钱包（浏览器插件）使用教程](https://www.bitansuo.com/articles/metamask-%E6%95%99%E7%A8%8B/)
* [以太坊轻钱包 MetaMask 详细图文教程](http://8btc.com/thread-76137-1-1.html)

最重要的一点是通过 MetaMask 切换网络，如下图所示，`Main Ethereum Network`是主网络，`Rinkeby Test Network`是测试网络，我们选择后者。

{% img /images/post/2018/04/metamask_network.png 300 400 %}

## 赚取以太币

Rinkeby 网络的 eth 赚取非常简单，不需要你额外花费一分钱，只要你能上网和访问 Twitter 或者 Google 或者 Facebook 即可获取 eth。

那要如何操作呢？首先你要登陆到这个网站：[faucet.rinkeby.io](https://faucet.rinkeby.io/)，这是一个免费发放 eth 的网站，但它只作用在 Rinkeby 网络上。

### 发布你的账户地址

具体方法就是通过以下 3 种方式：

* 使用 [Twitter](https://twitter.com/) 发表一个推文，推文的内容是你的以太坊账号地址（可以通过 MetaMask 创建），然后将该推文的链接复制下来，粘贴到上面那个网址的输入框中就可以了。
* 使用 [Google Plus](https://plus.google.com/) 发布一条公共消息，消息的内容同样为你的账号地址，然后把消息的网址链接复制下来粘贴到上面那个网址。
* 跟上面一样，只不过是换成用 [Facebook](https://www.facebook.com/) 来发布公共消息，但因笔者比较少用 Facebook，还不知道怎么获取 Facebook 的消息地址，所以没怎么用这种方式。

### 选择获取的 eth 数量

把网址链接粘贴到网站上去后，就可以选择获取多少 eth 了，从下图我们可以看到有 3 种选择：

{% img /images/post/2018/04/fauce_eth.png 400 300 %}

* 获取 3 个 eth，间隔时间 8 小时，也就是说你必须等到 8 小时之后才能再次获取 eth。
* 获取 7.5 个 eth，间隔时间 1 天。
* 获取 18.75 个 eth，间隔时间 3 天。

当你选择了其中一种之后，差不多等 10~20 秒，你再查看你 MetaMask 的账号你就会发现你的 eth 变多了。

如果你没有钱包应用也可以直接通过这个网址查看你的账号金额，上面还有你的交易记录等信息。

{% codeblock lang:sh %}
https://rinkeby.etherscan.io/address/42位账号地址
{% endcodeblock %}

### 赚钱 Tip

Rinkeby Faucet 网站限制你获取 eth 的规则是根据你的 twitter 账号或者 Google Plus 账号来判断的，也就是说你如果有 2 个 twitter 账号的话，你可以同时使用这 2 个 twitter 账号来为同一个以太坊账号获取 eth，而不会受到间隔时间的限制。

## 赚取代币
以太坊代币（以下简称 token) 其实也是一种智能合约，通过合约地址可以在 Rinkeby 网站上查看 token 的信息，包括总发币量，交易记录等。

大部分以太坊代币都是基于 ERC20 的标准，也就是都实现了 ERC20 的接口，比如获取账户金额，转账等。

那要如何获取 token 呢？

### 找到 token 的智能合约地址

跟主网络的 [etherscan.io](https://etherscan.io/) 网站一样，Rinkeby 网络也提供了一个类似的网站，方便大家进行信息查询，这个网站就是刚刚提到的 [rinkeby.etherscan.io](https://rinkeby.etherscan.io)。

在这个网站上面我们可以查询已经部署到测试网络的 token，找到 token 后就可以查看其合约地址了。

* 在网站上点击`Token`下拉框，并选择`ERC20 Token Search`

{% img /images/post/2018/04/token_search.png 400 200 %}

* 在输入框中输入要查询的 token 名称，在下拉表格中选择

{% img /images/post/2018/04/token_result.png 400 250 %}

* 进入 token 详情页面后就可以查询到 token 的合约地址了

{% img /images/post/2018/04/token_contract.png 400 200 %}


### 在 MetaMask 添加代币

在 MetaMask 的默认账户上是没有显示任何 token 的，如果想查看自己账户上某种 token 的情况，需要自己添加，操作步骤如下：

* 选择`TOKENS`标签，然后点击`ADD TOKEN`按钮

{% img /images/post/2018/04/add_token1.png 400 300 %}

* 在添加 token 页面输入 token 的合约地址，输入完成后 token 的名称会自动显示在`Token Symbol`中，最后点击添加按钮

{% img /images/post/2018/04/add_token2.png 400 400 %}

* 添加完成后在账户页面下方会显示新增的 token 种类

{% img /images/post/2018/04/add_token3.png 400 200 %}

### 使用 eth 购买 token

之前免费获取到的 eth 就可以用来购买 token 了，具体步骤如下：

* 点击账号上的`SEND`按钮

{% img /images/post/2018/04/token_transfer1.png 400 300 %}

* 输入 token 的智能合约地址和花费的 eth 数量，点击`NEXT`

{% img /images/post/2018/04/token_transfer2.png 400 400 %}

* 确认交易信息，确认没问题点击`SUBMIT`

{% img /images/post/2018/04/token_transfer3.png 400 600 %}

* 交易成功后，等个大概 10 秒钟，再次查看 token 的金额，会发现你的 token 数量已经增加了

{% img /images/post/2018/04/token_transfer4.png 400 200 %}

## 总结

Rinkeby 试网络是一个非常有用的测试环境，在上面获取到 eth 和 token 之后，你就有资源进行各种测试开发工作了。需要注意一点的是测试网络的 token 比较乱，比如说比较出名的 token`EOS` 就有 10 几种，但这个不影响测试开发工作，只需随便选择其中一种就可以了。
