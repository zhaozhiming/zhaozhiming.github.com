---
layout: post
title: "geth 环境搭建及使用说明"
date: 2018-04-13 09:09
description: geth 环境搭建及使用说明
keywords: geth,ethereum, 以太坊
comments: true
categories: ethereum
tags: [geth,ethereum, 以太坊]
---

{% img /images/post/2018/04/geth.png 400 300 %}

[Geth](https://geth.ethereum.org/) 全称是**Go Ethereum**，是使用 Go 语言实现的一个以太坊环境搭建工具，其实也可以用其他语言来搭建以太坊，比如 C++，Python 等，但 Geth 是使用最广泛的。下面就来介绍一下 Geth 环境的搭建和使用。

<!--more-->

## 安装方法

在 Geth 的[官方网站](https://geth.ethereum.org/) 上提供了自动安装和手动下载安装两种方式，还有一种安装方式是源码构建，这种方式大部分人都不需要，这里就不介绍了。

### 自动安装

自动安装涵盖了各主流操作系统的安装方法。

{% img /images/post/2018/04/geth_install.png 400 300 %}

以 Mac 为例，最简单的方式就是通过 [Homebrew](https://brew.sh/) 进行安装。

{% codeblock lang:sh %}
brew install geth
{% endcodeblock %}

安装完成后，执行命令`geth version`看到 geth 的版本信息就算安装成功了。

### 手动安装

手动安装其实也很简单，就是下载一个压缩包，然后将其解压就可以了，在官网的下载页面上有各大系统的安装包链接。

{% img /images/post/2018/04/geth_download.png 400 300 %}

解压后执行命令`./geth version`同样可以得到 geth 的版本信息。

## 快速开始

{% codeblock lang:sh %}
geth --datadir "dev" --dev --rpcapi eth,web3,penersonal --rpc --rpcaddr=0.0.0.0 --rpccorsdomain "\*" console 2>>geth.log
{% endcodeblock %}

通过执行以上命令可以快速启动一个开发环境的服务，下面会提到各个参数的作用，最后是将 console 控制台的日志打印到一个 log 文件中。

## 命令行参数说明

### 服务启动

最快速启动 geth 的方式就是创建一个全节点的以太坊主网络，使用命令如下：

{% codeblock lang:sh %}
geth console
{% endcodeblock %}

其中 console 是指开启交互式命令控制台，我们可以在控制台上输入各种 geth 的命令，比如调用查看账号信息，查看账号金额，转账交易等。

服务启动后过一会就会开始同步主网络的信息，主网络的信息是非常庞大的，以前有人同步了十几天都没有同步完成，如果只是本地测试，可以用下面的方式搭建测试网络。

### 文件存储

geth 的默认存储路径是`~/.ethereum`，当然你也可以通过`--datadir 文件路径` 这个命令来改变存储的位置。

### http rpc 参数

使用 Geth 搭建服务最大的好处就是可以通过 http 请求进行以太坊的 API 调用，下面这些参数就是让 geth 启动 http 服务的。

#### rpc

设置启动 http-rpc 服务，让开发人员可以发 http 请求调用以太坊 API。

#### rpcaddr

设置 http 服务的地址，默认是`localhost`，如果想让其他机器可以访问你的 geth 服务，需要将其设置为`0.0.0.0`。

#### rpcport

设置 http 服务的端口，默认值是`8545`，一般这个选项可以不修改，除非端口被占用了。

#### rpcapi

设置 http 服务可以调用的 API 方法，默认值是`eth,net,web3`，如果想启用更多的 API 方法，可以通过这个选项进行设置，比如要启用账户管理服务可以添加`personal`，这样就可以调用账号创建等 API 了。

#### rpccorsdomain

设置 http 服务的跨域域名，如果想别的机器访问你的 geth 服务，通过`--rpcaddr`将地址设置为`0.0.0.0`是不够的，还需将`--rpccorsdomain`设置为`*`。

## 测试节点 Rinkeby 的搭建

如果你想开发以太坊程序，但是手头上又没有以太币怎么办？其实以太坊有很多测试网络可以用，在上面获取以太币是不需要花钱的（后面再写一篇文章介绍怎么获取测试网络的以太币，敬请期待）。比较常用的有 Rinkeby、Ropsten、Kovan, 这些是以太坊官方或者第三方维护的测试网络。

geth 提供了对 Rinkeby 测试网络的支持，想要搭建一个全节点的 Rinkeby 网络，只要执行以下命令：

{% codeblock lang:sh %}
geth --rinkeby console
{% endcodeblock %}

服务启动后同样会去同步 Rinkeby 网络的数据到本地，但测试网络的数据相对主网络来说是非常少的，截止 2018 年 4 月，同步完 Rinkeyby 的信息大概需要 5G 的磁盘空间，时间取决于网络速度，我本地机器大概同步了 4~5 个小时。可能有人在网上看到过这篇文章——[如何在 10 分钟内搭建 Rinkeby 测试网络](https://gist.github.com/cryptogoth/10a98e8078cfd69f7ca892ddbdcf26bc)，这篇文章是写于 2017 年 6 月，当时 Rinkeby 的数据没有多少，要同步完所有数据当然很快了。

## API 的使用

geth 不仅提供了以太坊的标准 API，另外还提供了 geth 自己的 API。以太坊的标准 API 可以在[这里](https://github.com/ethereum/wiki/wiki/JSON-RPC) 查看，分别有`web3、eth、db、shh`这些 API，比如可以用来执行查询本地节点的账号信息，查询账号金额等操作。

geth 的专属 API 可以查询[这里](https://github.com/ethereum/go-ethereum/wiki/Management-APIs)，分别有`admin、debug、miner、personal、txpool`等 API，比较常用的是 personal 这个 API，可以通过其创建账号，因为标准的 web3
API 暂时还没有实现创建账号这个功能（有个 [register](https://github.com/ethereum/wiki/wiki/JavaScript-API#web3ethregister) 方法但还未实现，而且它还是 JS 的 API，需要使用 web3 的 JS 库才能用），所以一般可以 personal 来创建账号。

### 调用方式

geth API 的调用方式有 2 种，分别是控制台输入和发 http 请求。

#### 控制台

通过 console 交互命令控制台进行调用：

{% codeblock lang:sh %}

eth.accounts
> ["0xab123b7d83af73b873d58eb898828287b08c4d8f", "0x8e153bae6d1cd5a2e3438dda89c6ad6d25fbe9a5"]
eth.getBalance('0xab123b7d83af73b873d58eb898828287b08c4d8f')
> 6000000000000000000
{% endcodeblock %}

注意示例里面的账号金额是 6 个以太币，但以太坊的单位是`Wei`，一个以太币是 10^18 Wei，具体单位的介绍可以查看[这篇文章](https://www.jianshu.com/p/b56552b1d1a0)。

#### http 请求

另一种方式通过 http 请求进行调用：

{% codeblock lang:sh %}
http POST :8545 jsonrpc=2.0 method=eth_getBalance params:='["0x643551033ae00eb4b62cd41c1cbb98a752e4575d", "latest"]' id=1

HTTP/1.1 200 OK
Content-Length: 57
Content-Type: application/json
Date: Tue, 17 Apr 2018 13:54:26 GMT
Vary: Origin
{
    "id": "1",
    "jsonrpc": "2.0",
    "result": "0x6128f15c6c13cbf6"
}
{% endcodeblock %}

这里要推荐一个类似`curl`的工具——[httpie](https://httpie.org/)，主要是它的用法简单，需要的命令行代码更少，最重要的是返回结果更加清晰好看。

从上面可以看到我们对本地 8545 的端口服务发起 http 请求，需要带上几个请求参数，`jsonrpc`和`id`这 2 个参数的值是固定的，`method`和`params`分别指要调用的 API 方法以及该方法所需的参数。

需要注意的是`method`中的方法是`eth_getBalance`，而在控制台中我们输入的是`eth.getBalance`，中间的符号有所不同。另外`params`的值是一个 json 格式的数组，里面不仅包含了账号地址还有`latest`这个参数，而在控制台调用该命令只需要输入账号地址就足够了，后面这个参数在方法调用时会默认加上。

可以看到返回的结果是有`id`和`jsonrpc`这 2 个固定返回值，跟请求参数那 2 个参数一致，另外一个就是方法的返回结果了，注意返回的方法结果是 16 进制的数字。

### 常用 API

#### personal.newAccount

创建 geth 节点的账号，创建完成后会在服务的`datadir`目录的`keystore`里面新建一个文件，文件名大概是这个样子：

{% codeblock lang:sh %}
UTC--2018-03-24T07-18-26.741872893Z--f58397f67c6c148f9f5b816126425fe93cab0f18
{% endcodeblock %}

文件名分别由创建时间和账号地址组成，这个就是账号的`keystore`，通过这个文件可以在其他节点导入账号的信息。

可能有人担心在自己节点生成的账号导入到其他节点时发生地址冲突怎么办，其实完全不用担心，以太坊账号地址是通过一系列算法生成的，绝对不会有冲突的情况，想了解钱包地址更多内容可以参考[这篇文章](https://ethfans.org/flfq/articles/142)。

而这个文件里面的内容是这样的：

{% codeblock lang:json %}
{
  "address":"9f49f0736655c87c7d26edebcfd2407ebccdff65",
  "id":"9b1a1f3b-fb17-44ba-8ab0-9d1fa8badef4",
  "version":3,
  "Crypto":{
    "cipher":"aes-128-ctr",
    "cipherparams":{
      "iv":"3d921b20e735f27f888a6ed682d4ffdb"
    },
    "ciphertext":"bfc78805cef944baf46fce6d710c0de02aef07ab2b1fada26564c3efd83f5d20",
    "kdf":"scrypt",
    "kdfparams":{
      "salt":"e913c11f19a644f8fa68c6d661973d106c721b710ff2a78b9bb741037d56431b",
      "n":131072,
      "dklen":32,
      "p":1,
      "r":8
    },
    "mac":"5750c5865d53977c941945925c76911420dbf6c56930c5ffd62f9d9c022a9720"
  }
}
{% endcodeblock %}

里面分别记录和账号的地址和加密信息，包括加密算法、参数以及其他信息，想了解更多内容可以参考[这篇文章](https://medium.com/@julien.m./what-is-an-ethereum-keystore-file-86c8c5917b97)。

#### eth.accounts 或者 personal.listAccounts

列出 geth 阶段创建的所有账号。

#### eth.getBalance

查询账号的金额，需要输入账号地址。

#### eth.gasPrice

查询矿工"工作量"价格，想了解更多关于`gas`的内容请参考[这篇文章](https://www.jianshu.com/p/bc1a27adeaa4)。

#### eth.sendTransaction 或者 personal.sendTransaction

进行转账交易，需要输入转账的账号地址和转账金额。

## 总结

你的开发环境可以访问互联网的话，其实可以不用搭建自己的以太坊节点服务，直接使用以太坊的测试网络就可以了，在测试网络上可以调用和主网络同等功能的 API，并且测试数据更加接近主网络的真实数据。除非你的服务还需要包含一些自己的定制功能，那就要考虑搭建自己的以太坊节点了。

以上就是 Geth 的搭建及使用介绍，本人水平有限文章中有不对的地方还请指正。
