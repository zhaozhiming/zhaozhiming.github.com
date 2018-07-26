---
layout: post
title: "使用 Truffle 编写代币合约并部署到到 Rinkeby 网络"
date: 2018-07-22 17:06
description: 使用 Truffle 编写代币合约并部署到到 Rinkeby 网络
keywords: ethereum,token,truffle,openzeppelin,rinkeby
comments: true
categories: blockchain
tags: [ethereum,token,truffle,openzeppelin,rinkeby]
---

{% img /images/post/2018/07/token.jpeg 400 300 %}

以太坊在比特币区块链的基础上进行了很多功能的完善，其中最大的一个功能就是让人们在区块链上可以开发图灵完备的程序（智能合约），而智能合约中人们使用最广泛的要数以太坊的代币了。

现在越来越多的公司发行了自己的以太坊代币，不管他们的目的是推进公司建设也好，还是割韭菜也好，其实跟我们开发者的关系并不大，我们应该关注的是其背后的区块链技术，今天我们就来介绍下如何发行自己的代币。

<!--more-->

## 简易发行代币方法

可能有的人会以为发行代币特别困难，特别是对没有编辑经验的人来说，其实现在已经有一些自动生成代币的工具出现了，即使不会编程也可以发行自己的代币。

### [Token Factory](http://thetokenfactory.com/#/factory)

`代币工厂`是一个主动帮助你生成代币的网站，只要输入代币的几个主要参数就可以帮你自动创建代币，网络环境为主网络。

{% img /images/post/2018/07/token_factory.png %}

### [一键代币](https://token.ftqq.com/)

`一键代币`是国内开发的代币生成网站，同样只需要输入代币的参数即可自动创建代币，需要配合`MetaMask`钱包进行试用。

{% img /images/post/2018/07/one_token.png %}

### [My Token Deployer](https://one-click-token.herokuapp.com/)

这是一个专门给 Rinkeby 测试网络提供代币生成的网站，这里只能输入代币名称和符号。

{% img /images/post/2018/07/rinkeby_token.png %}

## 开发工具介绍

虽然有了这些自动化生成代币的工具，但作为程序员的我们，还是觉得把代码控制在自己手里更稳妥一些。在我们介绍开发代币之前，先介绍一下我们的开发工具。

### [Truffle](https://truffleframework.com/)

Truffle 是一个以太坊开发框架，据说是在一次区块链黑客马拉松上获奖的项目，后来慢慢演变变成了一个受欢迎的框架。它的主要特点有：

* 模板化工程：可以通过不同项目模板创建项目
* 脚本化部署：通过简单的脚本命令可以方便地进行合约部署
* 本地化测试：在本地可以运行智能合约测试，避免上链后再发现问题

### [Openzeppelin](https://openzeppelin.org/)

随着区块链受到越来越多人的关注，不少黑客也盯上了区块链这块蛋糕，目前已在不少代币的智能合约中发现安全漏洞，一些漏洞甚至可以让代币的价值瞬间归零，所以如何编写一个安全性高的代币合约尤其重要。

幸运的是社区已经开放了一些工具让你避免这个问题，Openzeppelin 是一个智能合约复用框架，它提供了一套经过生产环境验证的合约 m 模板，不但可以让你快速开发自己的智能合约，而且还提供了安全保障。

### [Ganache](https://truffleframework.com/ganache)

Ganache 也是`Truffle`团队开发的一个工具，简单来说它就是一个`testrpc`（现在已改名为 [ganache-cli](https://github.com/trufflesuite/ganache-cli)）的 GUI 工具，让你可以通过界面方式来创建本地的区块链环境，主要用于开发和测试。

## 项目初始化

工具介绍完了，我们可以开始编写代币的智能合约了，首先我们需要创建项目工程，通过`Truffle`我们可以快速创建项目。

{% codeblock lang:sh %}
mkdir mytoken
cd mytoken
truffle unbox tutorialtoken
{% endcodeblock %}

这里我们使用了 [tutorialtoken](https://truffleframework.com/boxes/tutorialtoken) 这个项目模板，它是一个专门用来编写代币智能合约的项目模板，里面提供了部署脚本，前端页面代码等，我们使用这个模板就可以大大减少我们的开发工作量，创建出来的项目目录结构如下：

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

其中我们主要使用到了`contracts`和`migrations`这 2 个目录中的代码，还有`truffle.js`，用来做环境配置，更多的相关内容可以查看[这里](https://truffleframework.com/docs/getting_started/project#exploring-the-project)。

## 编写合约代码

接着我们需要在项目中使用`Openzeppelin`来作为代币合约代码的基类，安装`Openzeppelin`：

{% codeblock lang:sh %}
yarn add openzeppelin-solidity
{% endcodeblock %}

然后在`contracts`目录中创建我们的代币合约文件`Mytoken.sol`，代码内容如下：

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

可以看到，有了`Openzeppelin`之后我们的代码非常简单，只需要导入它的`StandardToken`来作为我们的代币基类，然后只需要在我们的代码里写上代币的几个主要信息就可以了。

这里我们的代理供应个数是`10000000000000`，即 10^14 个代码，代币精度是 10^18（与以太币一样）。

## 编译合约

代币合约编写完成后，我们需要将合约代码进行编译，执行命令如下：

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

查看`Mytoken.json`文件我们可以看到合约代码已被编译成了一个 json 对象，里面有智能合约的详细信息，其中的`ABI`将在后面使用到。

## 部署合约

编译完成后，我们就可以将智能合约发布到区块链上了，这里我们介绍如何发布到本地环境和 Rinkeby 测试环境。

首先我们需要在`migrations`文件夹中新增`2_deploy_contracts.js`文件，文件内容如下：

{% codeblock lang:js %}
const Mytoken = artifacts.require('./Mytoken.sol');

module.exports = function(deployer) {
  deployer.deploy(Mytoken);
};
{% endcodeblock %}

### 部署本地环境

这里我们可以使用之前介绍的`Ganache`来创建本地环境，打开`Ganache`客户端我们可以看到本地环境已经启动，同时默认创建了 10 个测试账号。

{% img /images/post/2018/07/Ganache.png %}

然后打开编辑器开始修改工程目录下的`truffle.js`文件，新增开发环境网络。

{% codeblock lang:js %}
module.exports = {
  networks: {
    // 本地环境配置
    development: {
      host: '127.0.0.1',
      port: 7545,
      network_id: '*',
    },
  },
}
{% endcodeblock %}

最后执行发布命令，从输出信息中我们可以看到创建 2 个智能合约，第一个是项目默认的`Migrations`，第二个就是我们的代币合约。

{% codeblock lang:sh %}
$ truffle migrate
Using network 'development'.

Running migration: 1_initial_migration.js
  Deploying Migrations...
  ... 0x77229d85d7a60ba492254db506561ab1ce56a50198c616ddc9b96555e10b796c
  Migrations: 0xf6a0e4e40f0ae34dba890ebacb775371fb965658  // 这是项目默认的智能合约
Saving successful migration to network...
  ... 0xe0fbc8fa8f42e7a920184c597e5ecf14a1dca8ab7b246a68153f3117cc24aced
Saving artifacts...
Running migration: 2_deploy_contracts.js
  Deploying ZzmCoin...
  ... 0x2aab385e1ea9ab9dd65be93b3df99118a521822b18c48d76e3f6ae7933de9e4d
  Mytoken: 0x593e548762ae1ca31bcdfa1be54f7e93d8265b9c // 这是我们的代币合约
Saving successful migration to network...
  ... 0xc1a702e5d6582aaa6cb57903849a007024c887a89d67a982afa13f09ede7e736
Saving artifacts...
{% endcodeblock %}

发布完成后，我们再次查看`Ganache`客户端，可以看到我们的第一个账号的账户金额发生了变化，因为需要花费 ETH 来创建合约，并且产生了 4 个交易，这样就表示我们的代币已经发布成功了。

{% img /images/post/2018/07/local_account_balance.png %}

{% img /images/post/2018/07/local_txs.png %}

### 部署 Rinkeby 环境

下面我们将代币发布到与生产环境比较相似的测试环境 Rinkeby，这里有两种方式可以来发布代币。

#### 第一种方式：使用 Geth

我们先用`Geth`在本地启动一个 Rinkeby 节点，启动时我们指定节点的`datadir`目录，同时指定我们需要的几个`rpcapi`，并将日志写入到`rinkeby.log`文件。

{% codeblock lang:sh %}
geth --datadir "rinkeby" --rinkeby --rpcapi eth,web3,personal,db,net --rpc --rpcaddr=0.0.0.0 --rpccorsdomain "*" console 2>>rinkeby.log
{% endcodeblock %}

这种方式需要将 Rinkeby 的全部数据先同步下来，可以在`Geth 的 console 页面`输入`eth.syncing`，如果返回值是`false`则表示已经同步完成了，如果是返回一个 json 对象，则表示还在同步。

**PS：同步 Rinkeby 全节点数据大概是 16G 左右（截止 2018 年 7 月）**

{% codeblock lang:sh %}
> eth.syncing
{
  currentBlock: 2678044,
  highestBlock: 2701227,
  knownStates: 10200179,
  pulledStates: 10200179,
  startingBlock: 2677747
}
> eth.syncing
false
{% endcodeblock %}

接着我们编辑`truffle.js`文件中的网络配置信息：

{% codeblock lang:js %}
module.exports = {
  networks: {
    rinkeby: {
      host: '127.0.0.1',
      port: 8545,
      from: '0xd2e7c99558878629bb841678a022508b8ff585ff', // 这里指定由哪个账号来创建智能合约
      network_id: 4,
    },
  },
}
{% endcodeblock %}

注意`from`属性是指通过哪个账号来创建合约，但这个账号必须在 Rinkeby 网络中有一定数量的 ETH，否则创建合约将失败，Rinkeby 网络获取 ETH 方法可以参照[如何在 Rinkeby 网络赚取以太币及代币](https://zhaozhiming.github.io/blog/2018/04/18/how-to-earn-eth-and-token-in-rinkeby/)。

在发布合约之前，我们还需要将刚才提到的账号解锁，在`Geth 的 console 页面`输入解锁命令和账号密码，看到返回结果为`true`则表示解锁成功。

{% codeblock lang:sh %}
> personal.unlockAccount('0xd2e7c99558878629bb841678a022508b8ff585ff')
Unlock account 0xd2e7c99558878629bb841678a022508b8ff585ff
Passphrase:
true
{% endcodeblock %}

最后同样是执行发布命令，但需要在后面加上参数：`truffle migrate --network rinkeby`，执行成功的输出信息和本地环境是一样的。

#### 第二种方式：使用 HDwallet

使用`Geth`方式比较麻烦的一点就是需要同步全节点的数据，下面介绍通过`infura`网络直接部署到 Rinkeby 测试环境的方法。

首先我们需要先安装`truffle-hdwallet-provider`：

{% codeblock lang:sh %}
yarn add truffle-hdwallet-provider
{% endcodeblock %}

然后再修改`truffle.js`的配置信息，将 Rinkeby 网络配置改成如下所示：

{% codeblock lang:js %}
const HDWalletProvider = require('truffle-hdwallet-provider');
const mnemonic =
  'trumpet human tree genius reject purity secret off regret join orbit tent';

module.exports = {
  networks: {
    rinkeby: {
      provider: function() {
        return new HDWalletProvider(mnemonic, 'https://rinkeby.infura.io/');
      },
      network_id: 4,
    },
  },
};
{% endcodeblock %}

注意，这里通过助记词创建的账号是用来发布智能合约的，所以账号里面也需要有一定数量的 ETH。

最后执行发布命令`truffle migrate --network rinkeby`就可以了。

## 合约源码开源

代币发布完成之后，我们可以在以太坊浏览器中查看代币的详细信息，如果我们是发布到 Rinkeby 网络就可以查看`rinkeby.etherscan.io/`这个网址，然后在上面输入合约地址就可以进行代币信息的查询了。

{% img /images/post/2018/07/zzm_token.png %}

在以太坊上很多运作良好的代币都会将代币合约的源码开源，以证明代币的公正公开公平，但我们刚发布的代币并没有源码信息，所以我们需要手动上传代币的源码。

以 Rinkeby 网络为例，进入`rinkeby.etherscan.io`后在右上角菜单可以看到一个`Verify Contract`选项。

{% img /images/post/2018/07/verify_contract1.png %}

点击选项后进入智能合约源码上传页面，在这里要填写合约地址、合约名称，编译器版本等信息，还有上传合约的源码。

{% img /images/post/2018/07/verify_contract2.png %}

注意事项：

* `Optimization`选项要选择`No`，选择`Yes`会报错，具体原因未知
* 编译器版本可以查看编译后的 json 文件，里面有`compiler`属性
* 因为我们的合约是继承自`Openzeppelin`的`StandardToken`类，所以要将集成的基类的所有源码一起上传才能验证通过，比如`StandardToken`又继承了`ERC20`和`BaseToken`这 2 个类，则需要再将这 2 个类的源码一起上传，这些基类的源码可以在`Openzeppelin`的 github 仓库中找到。

最后点击`Verify And Publish`按钮就可以上传智能合约的源码了，上传成功后我们可以在地址的`Code`栏查看上传后的源码。

{% img /images/post/2018/07/contract_code.png %}

## 总结

这里主要介绍了代币的智能合约编写、发布以及源码上传等过程，随着区块链技术的发展使得智能合约的编写越来越简单，希望区块链可以吸引到更多开发者来参与基础设施的建设。
