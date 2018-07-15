---
layout: post
title: "比特币交易开发实战（二）"
date: 2018-07-15 09:39
description: 比特币交易开发实战（二）
keywords: bitcoin,blockchain,wallet
comments: true
categories: blockchain
tags: [bitcoin,blockchain,wallet]
---

{% img /images/post/2018/07/btc_tx3.png 400 200 %}

上次介绍了通过`BlockCypher`的 Api 来创建并发送普通地址的比特币交易，但对于比特币中的隔离见证地址这种方式就不能用了，所以我们再推荐另外一种创建比特币交易的方式——通过`bitcoinjs-lib`来创建交易。

<!--more-->


## [Bitcoinjs-lib](https://github.com/bitcoinjs/bitcoinjs-lib)

在之前的文章就已经介绍过`bitcoinjs-lib`这个库了，它是一个比特币 JS 工具库，是众多比特币工具库中使用人数比较多的，它包含了创建钱包、导入钱包、创建交易等功能。

`bitcoinjs-lib`没有提供说明文档，但提供了一套比较完整的测试案例，通过测试案例我们就可以知道 Api 的使用方法。

## 普通地址的交易

使用`bitcoinjs-lib`创建比特币交易，主要是通过`TransactionBuilder`这个 Api 来完成，下面是普通地址一对一交易的代码示例。

{% codeblock lang:js %}
// 创建钱包
const alice = bitcoin.ECPair.fromWIF('L1uyy5qTuGrVXrmrsvHWHgVzW9kKdrp27wBC7Vs6nZDTF2BRUVwy');
// 构建交易 builder
const txb = new bitcoin.TransactionBuilder();

// 添加交易中的 Inputs，假设这个 UTXO 有 15000 satoshi
txb.addInput('61d520ccb74288c96bc1a2b20ea1c0d5a704776dd0164a396efec3ea7040349d', 0);
// 添加交易中的 Outputs，矿工费用 = 15000 - 12000 = 3000 satoshi
// addOutput 方法的参数分别为收款地址和转账金额
txb.addOutput('1cMh228HTCiwS8ZsaakH8A8wze1JR5ZsP', 12000);

// 交易签名
txb.sign(0, alice);
// 打印签名后的交易 hash
console.log(txb.build().toHex());
{% endcodeblock %}

这里要注意，`addInput`方法的第一个参数是交易的 hash（每一笔比特币交易都有一个 hash 号来作为唯一标识），而第二个参数是这笔交易中的 Outputs 的索引，这个索引指向的 Output 首先必须是一个 UTXO，其次必须和转出地址相关。比如一笔交易包含了 2 个 Output：

* 地址 A，索引 0
* 地址 B，索引 1

如果是从地址 A 转账出去的交易，那么就应该选索引 0。

## 隔离见证地址的交易

隔离见证地址的交易需要用到隔离见证地址的回执脚本`redeemScript`，用于签名方法的第三个参数，代码示例如下：

{% codeblock lang:js %}
const keyPair = bitcoin.ECPair.fromWIF('cMahea7zqjxrtgAbB7LSGbcQUr1uX1ojuat9jZodMN87JcbXMTcA', testnet);
const pubKey = keyPair.getPublicKeyBuffer();
const pubKeyHash = bitcoin.crypto.hash160(pubKey);
// 得到隔离见证地址的回执脚本
const redeemScript = bitcoin.script.witnessPubKeyHash.output.encode(pubKeyHash);

// 构建交易 builder
const txb = new bitcoin.TransactionBuilder();

// 添加交易中的 Inputs，假设这个 UTXO 有 15000 satoshi
txb.addInput('61d520ccb74288c96bc1a2b20ea1c0d5a704776dd0164a396efec3ea7040349d', 0);
// 添加交易中的 Outputs，矿工费用 = 15000 - 12000 = 3000 satoshi
// addOutput 方法的参数分别为收款地址和转账金额
txb.addOutput('1cMh228HTCiwS8ZsaakH8A8wze1JR5ZsP', 12000);

// 交易签名
txb.sign(0, keyPair, redeemScript, null, 15000);
// 打印签名后的交易 hash
console.log(txb.build().toHex());
{% endcodeblock %}

隔离见证地址的交易和普通地址交易的区别就仅仅在签名函数上，其他地方基本相同。

这里要说明一点，交易的签名次数是根据交易中的 Inputs 来决定的，如果 Inputs 有多个的话，就需要多次调用`txb.sign`方法。

我们得到交易的签名 hash 后，就可以通过第三方的 Api 进行广播了。

## 获取 UTXO

创建比特币交易时调用`bitcoinjs-lib`的 Api 其实并不难，比较难的是如何添加交易中的 Inputs。

Inputs 需要获取转出地址的 UTXO，那有什么简便的方式来获取地址的 UTXO 呢？所幸`BlockChain`提供了一个 Api 可以来获取地址的 UTXO，Api信息如下：

* 网址：https://blockchain.info/unspent?active=$address
* 方法：GET
* 参数
  * active: 要查询 UTXO 的地址，多个地址可以用`|`号分隔
  * limit: 返回的记录数限制，默认是 250，最大是 1000
  * confirmations: 查询的 UTXO 必须是大于多少个确认，比如 confirmations=6
* 返回结果：

{% codeblock lang:json %}
{
    "unspent_outputs":[
        {
            "tx_age":"1322659106",
            "tx_hash":"e6452a2cb71aa864aaa959e647e7a4726a22e640560f199f79b56b5502114c37",
            "tx_index":"12790219",
            "tx_output_n":"0",
            "script":"76a914641ad5051edd97029a003fe9efb29359fcee409d88ac", (Hex encoded)
            "value":"5000661330"
        }
    ]
}
{% endcodeblock %}

在返回的结果中有我们需要的交易 hash 和索引值，以及用来计算需要多少个 input 的 value 值，这样就可以算出交易需要哪些 Inputs 了。

## size 的计算公式

在比特币的交易中，如果矿工费用设置过高或者过低，交易都不能成功生成，所以我们还需要计算交易中的矿工费用，这里有一个公式可以大致预估出交易所需的 size，然后将 size 再乘以`每比特的价格`就可以得到矿工费用了。

{% blockquote %}
size = inputsNum * 180 + outputsNum * 34 + 10 (+/-) 40
{% endblockquote %}

* inputNum 指交易中的 Input 个数
* outputNum 指交易中的 Output 个数
* 最后一部分是加减 40

## 总结

通过`bitcoinjs-lib`的 Api 我们不但可以完成普通地址的交易，而且还可以完成隔离见证地址的交易，而且调用的第三方 Api 更少（只需要一个广播交易的 Api），但缺点就是需要自己去统计交易的 Inputs 和 Outputs，但只要我们熟悉了统计方法其实也挺简单的。

