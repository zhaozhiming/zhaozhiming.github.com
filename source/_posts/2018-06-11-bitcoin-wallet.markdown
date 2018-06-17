---
layout: post
title: "比特币钱包原理简介及开发实践"
date: 2018-06-11 07:50
description: 比特币钱包原理简介及开发实践
keywords: bitcoin,blockchain,wallet
comments: true
categories: blockchain
tags: [bitcoin,blockchain,wallet]
---

{% img /images/post/2018/06/bitcoin-wallet.jpg 400 300 %}

比特币是最早出现的也是目前规模最大的加密货币，通过比特币很多人了解到了区块链技术，以太坊就是在其基础上演进形成的。我们之前了解了以太坊，现在回过头来我们再来看看比特币钱包的一些相关技术细节。

<!--more-->

## 钱包地址类型

比特币和以太坊有很多区别，首先在钱包地址上就不一样，以太坊钱包始终只有一种格式（不论是主网络还是测试网络），而比特币钱包有多种地址形式：

* 以`1`开头的`P2PKH（Pay-to-Public-Key-Hash）`地址，顾名思义是基于公钥哈希进行交易的地址，它是基于公钥私钥的地址形式
* 以`3` 开头的`P2SH（Pay-to-Script-Hash）`地址，与`P2PKH`不同的是它是通过`赎回脚本`进行交易的，注意：这种地址可能是`Segwit（隔离见证）`也可能不是
* 以`bj`开头的`Segwit（隔离见证）`地址，后面会详细介绍
* 以`m、n、2` 开头的地址，一般是测试网络上的地址

比特币社区推荐大家尽可能使用以`3`开头的地址，因为它比`1`开头的地址具备更多扩展性。

### SegWit 隔离见证地址

隔离见证的英文是`Segregated Witness`，简称是`SegWit`，那隔离见证地址是为了解决什么问题呢？

随着区块链上的交易越来越多，交易的速度却变得越来越慢，有时候要等好长一段时间才能成功确认，如果矿工费用过低的话要等的时间更长。因为区块链上的区块容量有限，一个区块只能打包一定数量的交易，手续费高的交易优先打包，如果区块容量满了那么交易就只能放到下一个区块。

那隔离见证是怎么解决这个问题的呢？看过一个比喻隔离见证的例子比较生动，这里引用一下：

{% blockquote %}
区块就像容量有限的客运车，每笔交易就像上车的乘客，乘客缴纳的乘车费越高可以越快上车，车满了未上车的乘客就只能等下一班。而 SegWit 就像给客运车加了一节车厢，但这个车厢是不能坐人的，只能用来放乘客的行李，乘客把行李都放到新的车厢里面去之后，原来的车厢空间就变大了，就可以容纳更多的乘客。
{% endblockquote %}

隔离见证还是让区块保留原来的容量——1M，但增加了 3M 的`车厢`，将交易中的交易地址、金额和认证资料等信息分开存放，这样就达到了扩容的目的了。

**另外使用`隔离见证地址`还有一个好处，就是交易的矿工费用比普通地址要低。**

## HD 钱包

HD 钱包其实并不是指硬件钱包（Hardware Wallet），而是分层确定性（Hierarchical Deterministic）的缩写。分层就是一个主私钥对可以生成多个子私钥对，一个子私钥对又可以生成多个孙私钥对，从而形成了一个分层结构。

{% img /images/post/2018/06/HD_wallet.png %}

### BIP

BIP 全名是 `Bitcoin Improvement Proposals`（比特币改进建议），类似开源项目中的`RFC(Request for Comments)`流程，任何人都可以向比特币提出改善建议，提交的交易会放到 [BIP 网站](https://github.com/bitcoin/bips) 上进行公审。

BIP 网站上面罗列了迄今为止所有的 BIP 提议以及他们的类型和状态，状态为`Final`或`Active`的是已经经过公审并已纳入比特币标准的建议。

### BIP32

`HD——分层确定性`最早就是通过 BIP32 提出来的，概括来说就是通过一个`种子（seed）`来生成一个私钥对的分层结构（见上面的图）。

更多 BIP32 的内容可以查看[这里](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki)。

### BIP39

由于`种子`是一长串随机字符串不便于记忆，所以 BIP39 提出了一种方法：通过助记词的方式来记录`种子`，助记词一般是由 3~24 个简单的字或单词组成，方便人们记忆和书写。

在这个网址 (https://iancoleman.io/bip39/) 可以进行 BIP39 助记词测试和获取。

目前 BIP39 支持 8 种语言的助记词，包括：英文、简体中文、繁体中文、日文、韩文、西班牙语、意大利语、法语。

更多 BIP39 的内容可以查看[这里](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)。

### BIP44

BIP44 是在 BIP32 基础上进行功能扩展，让同一个`种子`可以支持多币种和多账户，其路径层级为：

`m / purpose' / coin_type' / account' / change / address_index`

* purpose：值是常量`44`，表示使用的是 BIP44。
* coin_type：代表加密货币的种类，`0`表示比特币，`1`表示比特币测试币，`60`表示以太币。
* 后面三个属性分别表示账号、链类型和索引，一般这几个属性的值都为`0`。

更多 BIP44 的内容可以查看[这里](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki)。

## 比特币区块链网络

比特币也有主网络和测试网络，但不同于以太坊的多种测试网络（Ropsten，Rinkeby，Kovan），比特币只有一种测试网络。

* 主网络：https://blockchain.info/
* 测试网络：https://testnet.blockchain.info/

测试网络同样可以通过`Faucet（水龙头）`获取到比特币，请注意：测试链上的比特币是没有价值的。这里介绍 2 个水龙头地址：

* https://testnet.manu.backend.hamburg/faucet
* http://bitcoinfaucet.uo1.net/send.php

## 钱包创建

下面我们使用 [bitcoinjs-lib](https://github.com/bitcoinjs/bitcoinjs-lib) 来介绍一下比特币钱包是如何创建的。

**PS: bitcoinjs-lib 的 master 分支代码已经是`4.0.0`，但从 npm 上下载的还是`3.3.2`版本，因此要看`3.3.2`的代码示例需要做如下修改：**

{% codeblock lang:js %}
// 示例代码的 url 地址
https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/integration/addresses.js
// 将其中的`master`改成`v3.3.2`
https://github.com/bitcoinjs/bitcoinjs-lib/blob/v3.3.2/test/integration/addresses.js
{% endcodeblock %}

### 创建 P2PKH 钱包

{% codeblock lang:js %}
// 创建钱包
const keyPair = bitcoin.ECPair.makeRandom();
// 钱包地址：19AAjaTUbRjQCMuVczepkoPswiZRhjtg31
console.log(keyPair.getAddress());
// 钱包私钥：Kxr9tQED9H44gCmp6HAdmemAzU3n84H3dGkuWTKvE23JgHMW8gct
console.log(keyPair.toWIF());
{% endcodeblock %}

### 私钥导入 P2PKH 钱包

{% codeblock lang:js %}
// 导入钱包
const keyPair = bitcoin.ECPair.fromWIF('Kxr9tQED9H44gCmp6HAdmemAzU3n84H3dGkuWTKvE23JgHMW8gct')
// 钱包地址：19AAjaTUbRjQCMuVczepkoPswiZRhjtg31
console.log(keyPair.getAddress());
{% endcodeblock %}

### 通过 P2SH 创建 SegWit 钱包

{% codeblock lang:js %}
// 导入 P2PKH 钱包
const keyPair = bitcoin.ECPair.fromWIF('Kxr9tQED9H44gCmp6HAdmemAzU3n84H3dGkuWTKvE23JgHMW8gct')
const pubKey = keyPair.getPublicKeyBuffer()

const redeemScript = bitcoin.script.witnessPubKeyHash.output.encode(bitcoin.crypto.hash160(pubKey))
const scriptPubKey = bitcoin.script.scriptHash.output.encode(bitcoin.crypto.hash160(redeemScript))
const address = bitcoin.address.fromOutputScript(scriptPubKey)

// 钱包地址：34AgLJhwXrvmkZS1o5TrcdeevMt22Nar53
// 它既是 P2SH 也是 SegWit 钱包
console.log(address);
{% endcodeblock %}

### 创建测试网络钱包

{% codeblock lang:js %}
// 测试网络
const testnet = bitcoin.networks.testnet;
// 测试钱包
const keyPair = bitcoin.ECPair.makeRandom({ network: testnet });
// 钱包地址：n1HiJCt8YKujJKqBVdBde95ZYw9WLfVQVz
console.log(keyPair.getAddress());
// 钱包私钥：cQ18zisWxiXFPuKfHcqKBeKHSukVDj7F9xLPxU2pMz8bCanxF8zD
console.log(keyPair.toWIF());
{% endcodeblock %}

### 创建基于 BIP44 的 HD 钱包

{% codeblock lang:js %}
// 助记词
const mnemonic =
  'abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about';
// 种子
const seed = bip39.mnemonicToSeed(mnemonic);
const root = bitcoin.HDNode.fromSeedBuffer(seed);
// 路径层级
const path = "m/44'/0'/0'/0/0";
// 生成钱包
const child = root.derivePath(path);
// 钱包地址：1LqBGSKuX5yYUonjxT5qGfpUsXKYYWeabA
console.log({ childAddress: child.getAddress() });
// 钱包私钥：L4p2b9VAf8k5aUahF1JCJUzZkgNEAqLfq8DDdQiyAprQAKSbu8hf
console.log({ childwif: child.keyPair.toWIF() });
{% endcodeblock %}

### 通过 HD 钱包创建 SegWit 钱包

{% codeblock lang:js %}
// child 为 HD 钱包，参见上面的例子
const keyhash = bitcoin.crypto.hash160(child.keyPair.getPublicKeyBuffer());
const scriptSig = bitcoin.script.witnessPubKeyHash.output.encode(keyhash);
const addressBytes = bitcoin.crypto.hash160(scriptSig);
const outputScript = bitcoin.script.scriptHash.output.encode(addressBytes);
const address = bitcoin.address.fromOutputScript(outputScript);
// 钱包地址：3HkzTaFbEMWeJPLyNCNhPyGfZsVLDwdD3G
console.log({ address });
{% endcodeblock %}

这样我们就可以得到有这样对应关系的钱包：助记词 --> 普通地址钱包（包含私钥）--> SegWit 地址钱包。

## 总结

比特币钱包相对于以太坊来说复杂了一些，我觉得是因为比特币钱包发展的较早，技术在完善的过程中做了一些矫正和弥补导致了形成多种解决方案，而以太坊是在比特币基础上发展起来的，所以就没有这些问题，但随着以后技术的发展，以太坊难免也会出现比特币的这种情况。

## 参考链接

* [详解比特币地址](https://zhuanlan.zhihu.com/p/30290735)
* [通俗易懂解释下bitcoin 的 segwit](https://www.zhihu.com/question/52644297)
* [从 BIP32、BIP39、BIP44 到 Ethereum HD Ｗallet](https://www.jianshu.com/p/54a2b14dfdf2)
* [分层确定性钱包 HD Wallet 介绍](https://zhuanlan.zhihu.com/p/30297080)

