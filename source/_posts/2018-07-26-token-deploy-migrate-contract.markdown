---
layout: post
title: "发行代币——部署代币智能合约"
date: 2018-07-26 20:52
description: 发行代币——部署代币智能合约
keywords: ethereum,token,truffle,openzeppelin,rinkeby
comments: true
categories: blockchain
tags: [ethereum,token,truffle,openzeppelin,rinkeby]
---

{% img /images/post/2018/07/deploy_contract.jpg 400 300 %}

现在越来越多的公司发行了自己的以太坊代币，不管他们的目的是推进公司建设也好，还是割韭菜也好，其实跟我们开发者的关系并不大，我们应该关注的是其背后的区块链技术。上一次我们讲了如何编写代币的智能合约，这次我们来看下怎么发布智能合约。

<!--more-->

## 部署合约

当我们把智能合约编译完成后，我们就可以将其发布到区块链上了，这里我们介绍如何将智能合约发布到本地环境和 Rinkeby 测试环境。

首先我们需要在`migrations`文件夹中新增一个`2_deploy_contracts.js`文件，文件内容如下，这样部署程序才会部署我们的合约：

{% codeblock lang:js %}
const Mytoken = artifacts.require('./Mytoken.sol');

module.exports = function(deployer) {
  deployer.deploy(Mytoken);
};
{% endcodeblock %}

### 部署本地环境

我们可以使用之前介绍的`Ganache`来创建本地环境，打开`Ganache`客户端我们可以看到本地环境已经启动，同时默认创建了 10 个测试账号。

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

最后执行发布命令，从输出信息中我们可以看到，我们创建了 2 个智能合约，第一个是项目默认的`Migrations`，第二个就是我们的代币合约。

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

因为部署本地环境是用第一个账号来发布合约，并且创建合约是需要花费账号金额的， 所以发布完成后，我们再次查看`Ganache`客户端，可以看到我们的第一个账号的账户金额发生了变化，并且产生了 4 个交易，这样就表示我们的代币已经发布成功了。

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

代币发布完成之后，我们可以在以太坊浏览器中查看代币的详细信息，如果我们是发布到 Rinkeby 网络就可以查看`rinkeby.etherscan.io`这个网址，然后在上面输入合约地址就可以进行代币信息的查询了。

{% img /images/post/2018/07/zzm_token.png %}

在以太坊上很多运作良好的代币都会将代币合约源码进行开源，以证明代币的公正公开公平，但我们刚发布的代币并没有源码信息，所以我们需要手动上传代币的源码。

以 Rinkeby 网络为例，进入`rinkeby.etherscan.io`后在右上角菜单可以看到一个`Verify Contract`选项。

{% img /images/post/2018/07/verify_contract1.png %}

点击选项后进入智能合约源码上传页面，在这里要填写合约地址、合约名称，编译器版本等信息，还有上传合约的源码。

{% img /images/post/2018/07/verify_contract2.png %}

注意事项：

* `Optimization`选项要选择`No`，选择`Yes`会报错，具体原因未知
* 编译器版本可以查看编译后的 json 文件，里面有`compiler`属性
* 因为我们的合约是继承自`Openzeppelin`的`StandardToken`类，所以要将继承的基类源码一起上传才能验证通过，比如我们的代码继承了`StandardToken`这个基类，就需要将它的代码一起上传，然后`StandardToken`又继承了`ERC20`和`BaseToken`这 2 个类，则需要再将这 2 个类的源码一起上传，这些基类的源码可以在`Openzeppelin`的 github 仓库中找到。

最后点击`Verify And Publish`按钮就可以上传智能合约的源码了，上传成功后我们可以在`Code`栏查看上传后的源码。

{% img /images/post/2018/07/contract_code.png %}

## 总结

我们主要介绍了代币的智能合约编写、发布以及源码上传等过程，这里要感谢开源社区的框架和工具，让我们编写智能合约变得越来越简单，希望区块链以后可以吸引到更多开发者来参与基础设施的建设。
