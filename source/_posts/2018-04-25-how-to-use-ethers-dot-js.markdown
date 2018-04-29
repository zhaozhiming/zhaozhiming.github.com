---
layout: post
title: "以太坊工具包 Ethers.js 使用介绍"
date: 2018-04-25 20:18
description: 以太坊工具包 Ethers.js 使用介绍
keywords: ethereum,ethersjs
comments: true
categories: ethereum
tags: [ethereum,ethersjs]
---

{% img /images/post/2018/04/ethers.png 400 300 %}

在之前的文章介绍过，以太坊提供了两种形式的 API，一种是 [JSON RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC)，另外一种是 [Javascript API](https://github.com/ethereum/wiki/wiki/JavaScript-API) —— 通过 Web3 这个工具包进行 API 调用，Web3 功能强大但缺点就是账号相关的 API 比较少，而且它底层用到了一些 Node 原生库的依赖，导致其在 React Native（以下简称 RN） 中使用会有一些问题，因为 Node 和 RN 是 2 个不同的环境。

所以今天给大家介绍另外一个功能强大的 JS 以太坊工具库——[Ethers.js](https://github.com/ethers-io/ethers.js/)。

<!--more-->

## 简介

Ethers.js 的官方介绍是这样的——针对以太坊钱包功能完整实现的工具包，其 API 文档也十分详尽，感兴趣的同学可以看[这里](https://docs.ethers.io/ethers.js/html/)。

在仓库维护上作者比较用心，issue 都能及时解答，更新也比较频繁，如果觉得这个库还不错的可以考虑给作者一些 Eth 捐赠，这是作者的以太坊账户地址：`0xEA517D5a070e6705Cc5467858681Ed953d285Eb9`。

## 创建 / 导入钱包

与 Web3 相比 Ethers.js 的账号相关 API 比较丰富，在文档介绍中，这一类 API 叫`钱包`API，钱包就是账户的意思，创建钱包有以下方式：

### 创建随机地址的钱包

{% codeblock lang:js %}
const wallet = Wallet.createRandom();
console.log("Address: " + wallet.address);
// "每次都会生成不一样的钱包地址"
{% endcodeblock %}

### 通过明文私钥创建钱包

{% codeblock lang:js %}
const privateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
const wallet = new Wallet(privateKey);

console.log("Address: " + wallet.address);
// "Address: 0x14791697260E4c9A71f18484C9f997B308e59325"
{% endcodeblock %}

### 通过助记词创建钱包

{% codeblock lang:js %}
const mnemonic = "radar blur cabbage chef fix engine embark joy scheme fiction master release";
const wallet = Wallet.fromMnemonic(mnemonic);

console.log("Address: " + wallet.address);
// "Address: 0xaC39b311DCEb2A4b2f5d8461c1cdaF756F4F7Ae9"
{% endcodeblock %}

### 通过 keystore 创建钱包

{% codeblock lang:js %}
// keystore 是一个 json
const json = JSON.stringify(keystore);
const password = "foo";
Wallet.fromEncryptedWallet(json, password).then(function(wallet) {
    console.log("Address: " + wallet.address);
    // "Address: 0x88a5C2d9919e46F883EB62F7b8Dd9d0CC45bc290"
});
{% endcodeblock %}

注意：这种方式在 RN 环境中执行效率非常低，在电脑上执行只要 5 秒不到，但在 RN 上要执行差不多 5 分钟。

### 创建脑记忆的钱包

{% codeblock lang:js %}
const username = "support@ethers.io";
const password = "password123";
Wallet.fromBrainWallet(username, password).then(function(wallet) {
    console.log("Address: " + wallet.address);
    // "Address: 0x7Ee9AE2a2eAF3F0df8D323d555479be562ac4905"
});
{% endcodeblock %}

脑记忆方式其实就是用户名和密码的方法，同样这种方式在 RN 环境执行效率也很差。

## 导出钱包

导出钱包也是钱包应用的一个主要业务场景，分别有以下几种方式：

### 导出明文私钥

因为每个钱包对象都有一个`privateKey`属性，所以导出私钥只要直接获取这个属性就可以了。

{% codeblock lang:js %}
const wallet = Wallet.createRandom();
console.log('导出私钥：' + wallet.privateKey);
{% endcodeblock %}

### 导出助记词

跟私钥不同，不是每个钱包对象都有助记词属性，只有通过助记词导入的钱包对象有助记词`mnemonic`属性。

{% codeblock lang:js %}
const mnemonic = "radar blur cabbage chef fix engine embark joy scheme fiction master release";
const wallet = Wallet.fromMnemonic(mnemonic);
console.log('导出助记词：' + wallet.mnemonic);
{% endcodeblock %}

### 导出 keystore

钱包对象有一个`encrypt`方法可以导出钱包的 keystore，但该方法在 RN 环境中同样存在效率低下的问题。

{% codeblock lang:js %}
const password = "password123";
// 回调函数可以获取导出进度
function callback(percent) {
  console.log("Encrypting: " + parseInt(percent * 100) + "% complete");
}
const keystore = await wallet.encrypt(password, callback);
console.log('导出 keystore：' + keystore);
{% endcodeblock %}

## Provider

钱包的创建是离线的，不需要依赖网络即可创建钱包地址，但如果想获取钱包的相关信息，比如金额、交易记录，又或者想广播交易的话，就需要让钱包连上以太坊的网络了。

在 Web3 中是用 provider 来进行网络连接的，Ethers.js 也是一样，而且 Ethers.js 提供了集成多种 Provider 的方式。

### Provider 类型

* Etherscan Provider：连接 Etherscan API 的 provider，需要 2 个参数，一个是网络名称，一个查询 API 所需的 token（之前的文章有讲过，查询 Etherscan 的 API 时 apitoken 不是必须的，但如果没有的话会受到每秒 5 次的调用限制）。
* Json Rpc Provider：连接本地以太坊网点的 Provider。
* Infura Provider：连接 Infura 网络的 Provider，Infura 是一套以太坊的基础设施服务，同样有以太坊的主网络和测试网络。
* Web3 Provider：连接已有 web3 对象的 provider。
* Fallback Provider：连接一个可以是多种类型的 provider 集合，如果前面的 provider 有问题，会自动去连接后面的。

### Provider network

在 Provider 创建方法中都有一个参数`network`，它是一个字符串，代表网络名称，有如下值：

* homestead/mainnet：以太坊主网络
* morden: morden 测试网络（现在已经退役了）
* ropsten/testnet: ropsten 测试网络
* rinkeby：rinkeby 测试网络
* kovan：kovan 测试网络

### 与钱包集成

在通过私钥创建钱包的方法中，除了第一个参数私钥外，还有一个可选参数就是 provider，所以我们可以这样将 provider 集成到钱包中：

{% codeblock lang:js %}
const provider = providers.getDefaultProvider();
const wallet = new Wallet(privateKey, provider);
{% endcodeblock %}

也可以直接通过给钱包对象的 provider 属性赋值来集成 provider。
{% codeblock lang:js %}
const provider = providers.getDefaultProvider();
const wallet = new Wallet(privateKey);
wallet.provider = provider;
{% endcodeblock %}

## 智能合约

关于智能合约后面我会另外写一篇文章来介绍怎么使用 Ethers.js 来实现以太坊代币的业务操作，包括获取金额和交易等。

## 交易

交易功能比较简单，在钱包对象有对应的方法：`sendTransaction ( transaction )`和`send ( addressOrName, amountWei [ , options ] )`，大家可以自行查阅文档。

## 生成助记词

还有一个比较常见的业务场景是生成助记词，Ethers.js 也很贴心地提供了这个功能：

{% codeblock lang:js %}
const entropy = utils.randomBytes(16);
const mnemonicTemp = HDNode.entropyToMnemonic(entropy);
const walelt = Wallet.fromMnemonic(mnemonicTemp);
{% endcodeblock %}

## 工具类

Ethers.js 还提供了一些比较常用的工具方法，比如对 BigNumber 的操作。

### BigNumber 计算

有人可能会问为什么需要操作 BigNumber？因为以太坊的计量单位是`Wei`，一个以太币是 10^18 Wei，如果用普通的 JS number 对象来存储操作的话，可能会因为数据溢出而导致结果异常。

比如常见的业务场景是：获取钱包账户金额（以太币数量）并乘以货币单位汇率（美元或者人民币）得到最终结果。

{% codeblock lang:js %}
// 汇率，截止 2018-04-29，ETH 价格为 693.01 USD
const USD_RATE = 693.01;
// 钱包金额
const balance = await wallet.getBalance();
// bigNumber 不能和小数进行计算，所以要先将汇率变成整数
const rate = Math.round(USD_RATE * 100);
const result = balance.mul(rate).div(100)
{% endcodeblock %}

### BigNumber 格式化

计算好了结果后，我们需要将其转换成正常的数量单位并展示到前台，还好 Ethers.js 提供了相关的方法，还可以通过不同参数展示不同格式的结果。

{% codeblock lang:js %}
const wei = utils.bigNumberify("1000000000000000000000");

console.log(utils.formatEther(wei));
// "1000.0"

console.log(utils.formatEther(wei, {commify: true}));
// "1,000.0"

console.log(utils.formatEther(wei, {pad: true}));
// "1000.000000000000000000" 

console.log(utils.formatEther(wei, {commify: true, pad: true}));
// "1,000.000000000000000000"
{% endcodeblock %}

### 其他

其他工具方法还有 UTF8 字符串转换，地址 icap 转换等，感兴趣的同学可以自行参考文档。

## 总结

Ethers.js 是一个非常适合开发以太坊钱包应用的工具库，这里介绍的功能只是仓库功能的冰山一角，如果需要了解其更多功能的话，还请参阅官方文档。

