---
layout: post
title: "比特币交易开发实战（一）"
date: 2018-07-07 22:35
description: 比特币交易开发实战（一）
keywords: bitcoin,blockchain,wallet
comments: true
categories: blockchain
tags: [bitcoin,blockchain,wallet]
---

{% img /images/post/2018/07/btc_tx2.png 400 300 %}

创建比特币交易有两种方式，一种是通过第三方 Api 进行交易创建并广播，另一种是通过`bitcoinjs-lib`创建交易并生成交易签名，然后调用第三方 Api 进行广播。

<!--more-->

## 使用第三方 Api 交易

这里我们推荐一下 [BlockCypher](https://www.blockcypher.com/dev/bitcoin/) 的 Api，他们提供了一种简易方式来创建交易，用户只需要提供转账地址、收款地址和转账金额即可。

### Api 资源

`BlockCypher`提供的都是 Restful Api，通过不同的 Api 资源我们可以使用不同的链环境，详细信息如下：

 币 | 链 | 资源
:----------|:------------:|:----------:
Bitcoin	| Main | api.blockcypher.com/v1/btc/main
Bitcoin	| Testnet3 | api.blockcypher.com/v1/btc/test3
BlockCypher | Test | api.blockcypher.com/v1/bcy/test

`BlockCypher`除了可以使用比特币区块链的测试环境外，还可以使用其自身提供的一个测试环境，只需要将 Api 的 url 前缀写成`api.blockcypher.com/v1/bcy/test`就可以了。

### 创建交易

我们需要调用`BlockCypher`的`txs/new`Api 来创建交易，这是一个 POST 请求，请求的参数有：转账地址、收款地址和转账金额。下面是使用`curl`来调用 Api 的代码示例：

{% codeblock lang:sh %}
curl -d '{"inputs":[{"addresses": ["mmeVvNVHn2oJ7GZv4rQHhcDR8NLociAtXM"]}],"outputs":[{"addresses": ["mkd1rrTvmbPtGd4KLWWh2tN6ZWCywFNwhD"], "value": 1000000}]}' https://api.blockcypher.com/v1/btc/test3/txs/new

// 返回结果
{
  "tx": {...},
  "tosign": ["xxx"]
}
{% endcodeblock %}

我们使用的是比特币的测试环境`btc/test3`，可以看到返回的结果中带有两个参数： `tx`和`tosign`。

`tx`参数列出这个交易的一些基本信息，但真正的交易并没有生成，所以这个交易信息和实际交易信息可能会不同，但我们可以通过里面的字段来计算矿工费用信息。

`tosign`参数在后面的交易签名中需要用到。

### 交易签名

比特币交易需要私钥来进行签名，但`BlockCypher`不会让你提供私钥来调用 Api，如果让用户提供私钥的话会让用户心有疑虑，毕竟私钥是相当重要的东西。

所以我们得到`tosign`这个参数后，还需要使用其他方式来进行本地签名，这里我们使用`bitcoinjs-lib`这个库来进行交易签名，示例代码如下：

{% codeblock lang:js %}
const bitcoin = require("bitcoinjs-lib");
const buffer = require('buffer');

const testnet = bitcoin.networks.testnet;
const keys = bitcoin.ECPair.fromWIF(
  'your-address-private-key',
  testnet
);

// 假设创建交易后返回的对象为 tmptx，即上面那个包含了`tx`和`tosign`属性的对象
tmptx.pubkeys = [];
tmptx.signatures = tmptx.tosign.map(function(tosign, n) {
  tmptx.pubkeys.push(keys.getPublicKeyBuffer().toString("hex"));
  return keys.sign(new buffer.Buffer(tosign, "hex")).toDER().toString("hex");
});
{% endcodeblock %}

从代码中可以看出，在创建交易的对象上我们又增加了 2 个属性：`pubkeys`和`signatures`，这是最后一步发送交易需要用到的。

### 发送交易

得到新的对象后，我们需要再调用`BlockCypher`的另外一个 Api 来发送交易。这也是一个 POST 请求，请求参数就是我们刚才得到的新对象，示例代码如下：

{% codeblock lang:sh %}
curl -d '{"tx": {...}, "tosign": [ "..." ], "signatures": [ "..." ], "pubkeys": [ "..." ] }' https://api.blockcypher.com/v1/btc/test3/txs/send

// 返回结果
{
  "tx": {...},
  "tosign": [""]
}
{% endcodeblock %}

Api 调用成功后我们会得到类似创建请求返回的结果，只是`tosign`参数变成空的集合。

### 缺点

使用`BlockCypher`的 Api 来进行比特币交易虽然比较方便，但是有一个缺点，就是转账地址只能是普通地址，如果转账地址是隔离见证地址则无法使用这种方法。

## 总结

这里介绍了使用第三方 Api 来进行比特币交易，程序写好后，用户只需要输入转账地址、收款地址和转账金额即可，无需关心交易中的具体原理，但这种方法有个缺点就是无法使用隔离见证地址作为转账地址，所以我们需要另外想办法来实现隔离见证地址的交易，这个方法会在下一篇文章中进行介绍，敬请期待。


