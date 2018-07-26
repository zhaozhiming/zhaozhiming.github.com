---
layout: post
title: "发行代币——编写代币智能合约"
date: 2018-07-22 17:06
description: 发行代币——编写代币智能合约
keywords: ethereum,token,truffle,openzeppelin,rinkeby
comments: true
categories: blockchain
tags: [ethereum,token,truffle,openzeppelin,rinkeby]
---

{% img /images/post/2018/07/token.jpeg 400 300 %}

以太坊在比特币区块链的基础上进行了很多功能的完善，其中最大的一个功能就是让人们在区块链上可以开发图灵完备的程序——智能合约，而智能合约中人们使用最广泛的要数以太坊的代币了，今天我们就来介绍如何编写代币的智能合约。

<!--more-->

## 简易发行代币方法

可能有的人会以为发行代币特别困难，特别是对没有编辑经验的人来说，其实现在已经有一些自动生成代币的工具出现了，即使不会编程也可以发行自己的代币。

### [Token Factory](http://thetokenfactory.com/#/factory)

`代币工厂`是一个主动帮助你生成代币的网站，只要输入代币的几个主要参数就可以帮你自动创建代币。

{% img /images/post/2018/07/token_factory.png %}

### [一键代币](https://token.ftqq.com/)

`一键代币`是国内开发的代币生成网站，同样只需要输入代币的参数即可自动创建代币，需要配合`MetaMask`钱包一起使用。

{% img /images/post/2018/07/one_token.png %}

### [My Token Deployer](https://one-click-token.herokuapp.com/)

这是一个专门给 Rinkeby 测试网络提供代币生成的网站，这里只能输入代币名称和符号。

{% img /images/post/2018/07/rinkeby_token.png %}

## 开发工具介绍

虽然有了这些自动化生成代币的工具，但作为程序员的我们，还是觉得把代码控制在自己手里更稳妥一些。在我们介绍开发代币之前，先介绍一下需要用到的开发工具。

### [Truffle](https://truffleframework.com/)

Truffle 是一个以太坊开发框架，据说是在一次区块链黑客马拉松上获奖的项目，后来慢慢演变成了一个开发者受欢迎的框架，它的主要特点有：

* 模板化工程：可以通过不同项目模板创建项目
* 脚本化部署：通过简单的脚本命令可以方便地进行合约部署
* 本地化测试：在本地可以运行智能合约测试，避免上链后再发现问题
* 等等

### [Openzeppelin](https://openzeppelin.org/)

随着区块链受到越来越多人的关注，不少黑客也盯上了区块链这块蛋糕，目前已在不少代币的智能合约中发现了安全漏洞，一些漏洞甚至可以让代币的价值瞬间归零，所以如何编写一个安全性高的代币合约尤其重要。

幸运的是社区已经开放了一些工具让你避免这个问题，Openzeppelin 是一个智能合约复用框架，它提供了一套经过生产环境验证的合约模板，不但可以让你快速开发自己的智能合约，而且还提供了安全保障。

### [Ganache](https://truffleframework.com/ganache)

Ganache 也是`Truffle`团队开发的一个工具，简单来说它就是一个`testrpc`（现在已改名为 [ganache-cli](https://github.com/trufflesuite/ganache-cli)）的 GUI 工具，让你可以通过界面方式来创建本地的区块链环境，主要用于开发和测试。

## 项目初始化

工具介绍完了，我们可以开始编写代币的智能合约了，首先我们需要创建项目工程，通过`Truffle`我们可以快速创建项目。

{% codeblock lang:sh %}
// 创建空目录
mkdir mytoken
cd mytoken
// 使用模板初始化工程
truffle unbox tutorialtoken
{% endcodeblock %}

这里我们使用了 [tutorialtoken](https://truffleframework.com/boxes/tutorialtoken) 这个项目模板，它是一个专门用来编写代币智能合约的项目模板，里面提供了部署脚本，前端页面代码等内容，我们使用这个模板可以大大减少我们的工作量，创建出来的项目目录结构如下：

{% codeblock lang:sh %}
├── box-img-lg.png
├── box-img-sm.png
├── bs-config.json
├── contracts
│   ├── Migrations.sol
├── migrations
│   ├── 1_initial_migration.js
├── package.json
├── src
│   ├── css
│   │   ├── bootstrap.min.css
│   │   └── bootstrap.min.css.map
│   ├── fonts
│   │   ├── glyphicons-halflings-regular.eot
│   │   ├── glyphicons-halflings-regular.svg
│   │   ├── glyphicons-halflings-regular.ttf
│   │   ├── glyphicons-halflings-regular.woff
│   │   └── glyphicons-halflings-regular.woff2
│   ├── index.html
│   └── js
│       ├── app.js
│       ├── bootstrap.min.js
│       ├── truffle-contract.js
│       └── web3.min.js
├── test
└── truffle.js
{% endcodeblock %}

项目里面我们主要使用到了`contracts`和`migrations`这 2 个目录中的代码，还有`truffle.js`，用来做环境配置，更多的相关内容可以查看[这里](https://truffleframework.com/docs/getting_started/project#exploring-the-project)。

## 编写合约代码

初始化项目后，我们需要在项目中使用`Openzeppelin`来作为合约代码的基类，先安装`Openzeppelin`：

{% codeblock lang:sh %}
yarn add openzeppelin-solidity
{% endcodeblock %}

然后在`contracts`目录中创建我们的合约文件`Mytoken.sol`，代码内容如下：

{% codeblock lang:js %}
pragma solidity ^0.4.24;

import 'openzeppelin-solidity/contracts/token/ERC20/StandardToken.sol';

contract Mytoken is StandardToken {
  string public constant name = "My token"; // 代币名称
  string public constant symbol = "MT"; // 代币符号
  uint8 public constant decimals = 18; // 代币精度

  uint256 public constant INITIAL_SUPPLY = 10000000000000 * (10 ** uint256(decimals)); // 代币的总供应量

  constructor() public {
    totalSupply_ = INITIAL_SUPPLY;
    balances[msg.sender] = INITIAL_SUPPLY;
  }
}
{% endcodeblock %}

可以看到，有了`Openzeppelin`之后我们的代码非常简单，只需要导入它的`StandardToken`来作为我们的合约基类，然后写上代币的几个主要信息就可以了。

这里我们的代币供应量是`10000000000000`，即 10^14 个代币，代币精度是 10^18（与以太币一样）。

## 编译合约

合约代码编写完成后，我们需要将代码进行编译，执行命令如下：

{% codeblock lang:sh %}
truffle compile
{% endcodeblock %}

编译完成后可以看到在工程目录下生成了一个`build`目录，里面是编译后的结果，内容如下：

{% codeblock lang:sh %}
build
└── contracts
    ├── BasicToken.json
    ├── ERC20.json
    ├── ERC20Basic.json
    ├── Migrations.json
    ├── SafeMath.json
    ├── StandardToken.json
    └── Mytoken.json
{% endcodeblock %}

每一个`sol`文件会被编译成一个`json`文件，这里除了我们的文件被生成，还有其他文件因为引用和继承而被生成。

查看`Mytoken.json`文件可以看到，里面包含了智能合约的详细信息。

## 总结

这里主要介绍了代币的智能合约编写过程，其实可以看到有了一些框架和工具库的帮助，我们要写的代码非常之少，但是功能又十分齐全，而且安全性也有保障。

在下一篇文章中，我们再来介绍如何部署我们的合约，敬请期待。
