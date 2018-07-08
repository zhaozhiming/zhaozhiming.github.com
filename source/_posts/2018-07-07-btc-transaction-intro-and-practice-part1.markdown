---
layout: post
title: "比特币交易原理简介"
date: 2018-07-07 16:35
description: 比特币交易原理简介
keywords: bitcoin,blockchain,wallet
comments: true
categories: blockchain
tags: [bitcoin,blockchain,wallet]
---
{% img /images/post/2018/07/btc_tx1.png 400 300 %}

上次介绍了比特币的钱包原理和开发实现，但一个钱包的功能不仅仅只有钱包的创建，更重要的是钱包的转账交易功能，这次我们再来看看比特币的交易。

<!--more-->

## 原理简介

比特币的交易和以太坊的交易差别比较大，以太坊的交易是现实生活中普遍存在的那种基于账户的交易方式，比如从 A 账户中转出 100 元给 B 账户，那么 A 账户金额就减少 100 元，B 账户则增加 100 元，这种方式由于已经在广大人民群众中形成了普遍共识，所以在理解上不存在困难。但是比特币的交易却完全不一样！

### inputs 和 outputs

每个比特币交易都存在两个重要组成部分：`inputs`和`outputs`。`inputs`相当于交易的转出，而`outputs`相当于交易的接收，二者的金额转换关系是这样的：`inputs`的金额 = `outputs`的金额 + 矿工费用。

`inputs`和`outputs`都是数组结构，数组里面可以存放多个元素，这意味着每笔比特币交易可以有多个`input`和多个`ouput`。那`input`和`output`到底是什么东西呢？

`input`其实是一个历史交易的 UTXO（下面会介绍），暂时可以理解为金额转出信息（包括转出的地址和金额），所以一笔比特币交易的转出来源可以是多个地址，比如有一笔交易的`inputs`包括张三 100 BTC 和李四的 100 BTC，那么该笔交易的转出金额为 200 BTC，来自张三和李四两个账号。

`output`是交易转出的信息（包括转出的地址和金额），一笔比特币交易可以同时指定多个转出地址，比如同时转给王五 50 BTC 和赵六 150 BTC（含矿工费用）。

所以比特币的交易不是基于账户的交易，而是基于交易的交易，每一笔交易都依赖于其他的历史交易，按照上面的假设情况形成的一笔比特币交易的结构大概是这样的：

* `inputs` = [ 张三的 100 BTC，李四的 100 BTC ]
* `outputs` = [ 王五的 50 BTC，赵六的 149 BTC ]
* 矿工费用 = 1 BTC

### UTXO

UTXO 的英文全称是`Unspent Transaction Output`，中文叫`未花费输出`。那么`Unspent`是什么意思呢？其实它表示这个输出还没有被使用过的，可以拿来使用的意思。每一个`input`必须是历史交易的 UTXO，已经`Spent`过的输出不能拿来做`input`。

每笔刚形成的交易的`output`都是`Unspent`状态，当下一笔交易将该`output`作为`input`时，这个`output`才从`Unspent`变为`Spent`，表示该`output`已经被使用（花费）了。

所以**一个地址的账户总额等于该地址所有 UTXO 的金额总和**，举个栗子来加深理解：

* 在区块 1 中，地址 A 有 50 个挖矿所得的 BTC
* 在区块 2 中，地址 A 也有 50 个挖矿所得的 BTC，这时发生了一笔交易：地址 A 转出 20 个 BTC 给地址 B 和 30 个 BTC 给地址 C
* 在区块 3 中，地址 A 还有 50 个挖矿所得的 BTC，这时又发生一笔交易：地址 B 转出了 20 个 BTC 给地址 D

那么在这 3 个区块中，各个地址的 UTXO 情况如下：

* 地址 A 有 2 个 UTXO，每个 UTXO 是 50 个 BTC，所以 A 共有 100 个 BTC
* 地址 B 没有 UTXO，所以 B 的账户金额为 0
* 地址 C 有 1 个 UTXO，金额为 30 个 BTC，所以 C 共有 30 个 BTC
* 地址 D 有 1 个 UTXO，金额为 20 个 BTC，所以 D 共有 20 个 BTC

### 普通一对一转账

在比特币交易中，如果要进行普通的一对一账户转账，经常会遇到这样的情况，地址 A 要转账给地址 B 10 个 BTC，但地址 A 只有一笔 UTXO 且里面有 100 个 BTC，那么在这种情况下就要把原来的 UTXO 拆分成 2 个`output`，一个是转出给地址 B 10 个 BTC，另外一个是转给自己（即地址 A）90 个 BTC，这样`inputs`和`outputs`的金额才能相等，交易才能形成。

###  交易广播

另外一点与以太坊不相同的是，比特币的交易不会自动广播到区块链，而是需要通过第三方的广播平台来将交易签名信息进行广播，而以太坊的交易方法中自动就将交易进行广播了。

这里列举了一些支持比特币交易广播的网站：

* https://blockchain.info/pushtx
* https://live.blockcypher.com/btc/pushtx/
* https://blockexplorer.com/tx/send
* https://insight.bitpay.com/tx/send
* https://coinb.in/#broadcast

## 总结

这里主要介绍了比特币交易中的一些基本原理，以及与以太坊不同的一些地方，后续的文章会继续介绍具体如何通过代码实现比特币的交易过程，敬请期待。

