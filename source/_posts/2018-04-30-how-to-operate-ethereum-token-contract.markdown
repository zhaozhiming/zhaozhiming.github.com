---
layout: post
title: "使用 Ehters.js 进行以太坊代币相关操作"
date: 2018-04-30 20:10
description: 使用 Ehters.js 进行以太坊代币相关操作
keywords: ethereum,ethers.js,token,contract
comments: true
categories: ethereum
tags: [ethereum,ethers.js,token,contract]
---

{% img /images/post/2018/04/erc20.png 400 300 %}

在[上一篇文章](http://zhaozhiming.github.io/blog/2018/04/25/how-to-use-ethers-dot-js/) 中介绍了 [Ethers.js](https://github.com/ethers-io/ethers.js) 这个工具库，但在介绍智能合约时觉得这一部分涉及的内容会比较多，感觉重新写一篇文章来介绍会更好，所以我们今天就来看下怎么使用 Ethers.js 来进行以太坊代币（以下简称 token）的操作。

<!--more-->
首先要说明一下，这里介绍的 token 都是基于`ERC20`标准，其他标准的 token 暂时不涉及。

在使用 Ethers.js 的 API 之前，我们需要先获取 token 的两个信息：

* token 的智能合约地址
* token 的智能合约接口

找 token 智能合约地址的方法在[之前的文章](http://zhaozhiming.github.io/blog/2018/04/18/how-to-earn-eth-and-token-in-rinkeby/) 里面有介绍过，大家可以参考一下。

## 获取 token 的智能合约接口

除了 token 的地址外，还需要 token 的接口，它的全称是`Application Binary Interface`，简称`ABI`，更多信息可以参考[以太坊的文档](https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI)，我们现在只需要知道它是一个 json 对象就可以了。

那要如何获取 abi 呢？下面介绍两种方法。

### 直接从 etherscan 上面查找

第一种方法是直接在 etherscan 网站上查找，一些相对靠谱的 token 会将自己的智能合约源码公开并放到 etherscan 上，我们可以通过下面的方式来查找到 abi 对象。

* 以 EOS 为例，首先通过之前提到的找 token 地址的方法进到 token 的 etherscan 页面，然后点击上面的`Contract`链接

{% img /images/post/2018/04/eosabi1.png 800 600 %}

* 进到智能合约页面后，再点击下方的`Code`标签

{% img /images/post/2018/04/eosabi2.png 800 600 %}

* 进入`Code`页面后，可以看到里面有一栏叫`Contract ABI`，这个就是我们想要的 abi 对象了，可以点击右边的`Copy`按钮将其复制到剪切板，也可以点击`Export ABI`来导出文件。

{% img /images/post/2018/04/eosabi3.png 800 600 %}

### 通过 etherscan API 获取

另外一种方式是通过 [etherscan 提供的 API](https://etherscan.io/apis) 来获取，执行命令如下：

{% codeblock lang:sh %}
// 后面的 address 为 token 的地址
curl https://api.etherscan.io/api?module=contract&action=getabi&address=0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0
{% endcodeblock %}

返回结果是和第一种方法一样的，可以看出这种方式更加简单快捷。

## 创建智能合约对象

接着咱们再来看 Ethers.js 的 API，在文档中创建智能合约的方法如下所示：

{% codeblock lang:js %}
new ethers.Contract(addressOrName , interface , providerOrSigner)
{% endcodeblock %}

* 第一个参数是智能合约的地址
* 第二个参数是智能合约的接口，即 abi
* 第三个参数可以是一个钱包对象，也可以是一个 provider 对象

{% codeblock lang:js %}
import ethers from 'ethers';
// abi json 对象
const abi = [...];
// 智能合约地址
const address = '0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0';
// 创建一个连接主网络的 provider
const provider = ethers.providers.getDefaultProvider();
// 创建智能合约
const contract = new ethers.Contract(address, abi, provider);
{% endcodeblock %}

## 获取钱包 token 金额

创建了智能合约之后，我们来看下怎么使用它。基于`ERC20`标准的 token 都需要实现一套接口，这套接口一般会有下面几个方法：

* name：返回 token 名称
* symbol：返回 token 的符号，比如`EOS`
* totalSupply：返回 token 的总供应量
* balanceOf：返回账户的金额
* transfer：对 token 进行交易

我们可以使用`ERC20`的接口来获取钱包的 token 金额，代码如下：

{% codeblock lang:js %}
const address = '0x788692Ff1D0A38f6cCFf95BC597022049CAE15A4';
const balance = await contract.balanceOf(address);
{% endcodeblock %}

注意，这里获取的不是钱包 eth 的金额，而是 token 的金额。比如代码中的钱包里面有 1 个 eth 和 10 个 eos，那么返回的 balance 就是 10。

## token 转账

token 的转账也是基于`ERC20`的标准接口来实现。

{% codeblock lang:js %}
const address = '0x788692Ff1D0A38f6cCFf95BC597022049CAE15A4';
await contract.transfer(address, utils.parseEther('5'));
{% endcodeblock %}

在这里我们转账了 5 个 token 给示例中的钱包地址。

## 查询 token 交易记录

我们再来看下钱包中 token 的交易记录如何获取，同样这里介绍两种方式。

### 通过事件日志 API 进行查询

第一种方式比较麻烦，就是通过 etherscan 事件日志的 API 来进行交易记录查询。

{% codeblock lang:sh %}
curl https://api.etherscan.io/api?module=logs&action=getLogs
    &fromBlock=0
    &toBlock=latest
    &address=『智能合约地址』
    &topic1=『转出的钱包地址，需要把钱包地址变成 64 位，前面补零』
    &topic2=『转入的钱包地址，需要把钱包地址变成 64 位，前面补零』
{% endcodeblock %}
注意，其中`topic1`和`topic2`这 2 个参数不能同时写同一个钱包地址，因为不存在钱包自己转账给自己的交易，所以如果想获取到钱包所有的 token 交易记录（包括转出和转入），那么必须通过 2 次调用才能得到。

比如钱包地址是`0x788692Ff1D0A38f6cCFf95BC597022049CAE15A4`，token 智能合约地址是`0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0`，那么命令如下：

{% codeblock lang:sh %}
// 得到转出记录
curl https://api.etherscan.io/api?module=logs&action=getLogs
    &fromBlock=0
    &toBlock=latest
    &address=0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0
    &topic1=0x0000000000000000000000788692Ff1D0A38f6cCFf95BC597022049CAE15A4
// 得到转入记录
curl https://api.etherscan.io/api?module=logs&action=getLogs
    &fromBlock=0
    &toBlock=latest
    &address=0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0
    &topic2=0x0000000000000000000000788692Ff1D0A38f6cCFf95BC597022049CAE15A4
{% endcodeblock %}

注意：`topic1`和`topic2`的地址都通过前面补零变成了 64 位， 这样才能正常调用 API，取到 2 部分的结果后再将它们合并就形成了完整的钱包 token 交易记录了。

### 通过 token 交易记录 API 查询

另一种方式也是通过 etherscan 的 API，通过这个 API 我们可以一次性取得钱包的的 token 所有交易记录。

{% codeblock lang:sh %}
curl http://api.etherscan.io/api?module=account&action=tokentx
    &startblock=0
    &endblock=latest
    &contractaddress=0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0
    &address=0x788692Ff1D0A38f6cCFf95BC597022049CAE15A4
    &sort=asc
{% endcodeblock %}

可以明显看出，与第一种方式相比这种方式更加简单易用。

## 总结

在 token 的操作中，最主要的还是要能获取到 token 的 abi，有些 token 虽然公布了其智能合约的代码，但是里面的代码可能包含错误，从而导致创建智能合约对象报错，这样就没办法去操作这种 token 了，所以在发布这种 token 到生产环境之前需要多测试一下。
