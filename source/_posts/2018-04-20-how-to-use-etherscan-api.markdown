---
layout: post
title: "如何使用 Etherscan 的 API"
date: 2018-04-20 20:51
description:
keywords: etherscan,ethereum,api
comments: true
categories: ehtereum
tags: [etherscan,ethereum,api]
---

{% img /images/post/2018/04/etherscan.png 400 300 %}

虽然以太坊提供了 [Web3](https://github.com/ethereum/wiki/wiki/JavaScript-API) 和 [Json Rpc](https://github.com/ethereum/wiki/wiki/JSON-RPC) 这 2 种 API，geth 也额外提供了一些 [API](https://github.com/ethereum/go-ethereum/wiki/Management-APIs)，但是对于开发以太坊应用来说还是显得有些不足，比如说获取交易记录的时间，需要先通过交易的 hash 找到该交易对应的区块 id，然后才能找到对应的时间，查询起来相当不方便。

好在`Etherscan`对外提供了一些公共的 API，给我们提供了额外的能力来处理更多的业务场景。

<!--more-->

## Etherscan api

为了方便开发人员更好地使用`ethersacn.io`，网站提供了[一系列 API](https://etherscan.io/apis) 供开发人员使用。

### 使用方法

API 的使用非常简单，基本上都是 get 方法，通过 http 请求就可以直接调用，在每个 Api 的说明文档都有对应的例子可以查看。

### 模块介绍

API 主要包含以下模块：账号、智能合约、交易、区块、事件日志、代币及工具等。

### 账号 API

账号相关的 API，有获取账号金额，获取交易记录等，该模块提供的 API 最多。

#### 获取单个账号金额

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=balance&address=0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a&tag=latest&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

* module：对应的模块名称，这里是账户模块，所以是`moudle=account`
* action：对应的操作，这里是`balance`，即获取金额。
* address：要查询金额的账号地址。
* tag：之前在介绍 geth 的 API 时讲过获取账号金额需要 2 个参数，一个是账号地址，另外一个就是 tag，一般写`latest`就可以了。
* apikey：你在`Etherscan`上创建的 apikey，带上没有请求的限制，也可以不带，下面会介绍 API 的请求限制。

其中`module、action、apikey`是每个 API 都需要的参数，其他的参数因不同 API 而不同。

**返回结果**

{% codeblock lang:sh %}
{"status":"1","message":"OK","result":"670456215218885498951364"}
{% endcodeblock %}


#### 获取多个账号金额

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=balancemulti&address=0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a,0x63a9975ba31b0b9626b34300f7f627147df1f526,0x198ef1ec325a96cc354c7266a038be8b5c558f67&tag=latest&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

（前面有讲过的参数就不讲了，下同）

与单个账号金额 API 相比，参数`address`用`,`号分隔多个账号，最多可支持 20 个账号的金额查询。

**返回结果**
{% codeblock lang:sh %}
{
    "message": "OK",
    "result": [
        {
            "account": "0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a",
            "balance": "40807168564070000000000"
        },
        {
            "account": "0x63a9975ba31b0b9626b34300f7f627147df1f526",
            "balance": "332567136222827062478"
        },
        {
            "account": "0x198ef1ec325a96cc354c7266a038be8b5c558f67",
            "balance": "0"
        }
    ],
    "status": "1"
}
{% endcodeblock %}

#### 获取"正常"交易记录

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=txlist&address=0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

* action：为`txlist`，表示列出交易记录。
* address：要查询交易记录的账号地址。
* startblock：起始查询块 id，可选，默认值为 0。
* endblock：结束查询块 id，可选，默认值为最后一个区块。
* page: 页数（没错，这个 API 还支持分页），可选。
* offset: 查询到记录数，可选，默认是查询 10000 条记录。
* sort: 排序规则，可以正序`asc`和倒序`desc`。

**返回结果**

{% codeblock lang:sh %}
{
    "message": "OK",
    "result": [
        {
            "blockHash": "0x2d0a9228f22fe85596d246040d4fd7dc6b1a55920bae02b68e731d55a890b315",
            "blockNumber": "47894",
            "confirmations": "5435815",
            "contractAddress": "",
            "cumulativeGasUsed": "21612",
            "from": "0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a",
            "gas": "23000",
            "gasPrice": "400000000000",
            "gasUsed": "21612",
            "hash": "0x7e1503d2001cab2f432b56a62a3ee874782c8e33cbd79a664d155a758c1784a2",
            "input": "0x454e34354139455138",
            "isError": "0",
            "nonce": "1",
            "timeStamp": "1438948043",
            "to": "0x2910543af39aba0cd09dbb2d50200b3e800a63d2",
            "transactionIndex": "0",
            "txreceipt_status": "",
            "value": "9001000000000000000000"
        },
    ],
    "status": "1"
}
{% endcodeblock %}

#### 获取"内部"交易记录

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=txlistinternal&address=0x2c1ba59d6f58433fb1eaee7d20b26ed83bda51a3&startblock=0&endblock=2702578&page=1&offset=10&sort=asc&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

参数与上一个 API 基本相同，只有`action`是`txlistinternal`这一点不同，这 2 种交易的区别是什么呢？简单的理解就是“正常”的交易是会记录到区块链上的，而“内部”交易是指不会记录到区块链上的记录，比如交易失败的记录。

另外这个 API 还可以通过交易 hash 查看交易的详情。

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=txlistinternal&txhash=0x40eb908387324f2b575b4879cd9d7188f69c8fc9d87c901b9e2daaea4b442170&apikey=YourApiKeyToken
{% endcodeblock %}

**返回结果**

{% codeblock lang:sh %}
{
    "message": "OK",
    "result": [
        {
            "blockNumber": "2547619",
            "contractAddress": "",
            "errCode": "Bad jump destination",
            "from": "0x2c1ba59d6f58433fb1eaee7d20b26ed83bda51a3",
            "gas": "346878",
            "gasUsed": "0",
            "hash": "0x2896441f9d1f167b4a3f987d82233e7d238e6a50a227c4b612dbc82f34bb533d",
            "input": "",
            "isError": "1",
            "timeStamp": "1478013203",
            "to": "0x20d42f2e99a421147acf198d775395cac2e8b03d",
            "traceId": "0",
            "type": "call",
            "value": "71000000000000000000"
        },
    ],
    "status": "1"
}
{% endcodeblock %}

#### 获取 ERC20 代币交易事件记录

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=tokentx&contractaddress=0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2&address=0x4e83362442b8d1bec281594cea3050c8eb01311c&page=1&offset=100&sort=asc&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

* action: 值为`token`，表示和代币相关。
* contractaddress: 代币的智能合约地址。
* address: 只查询和该账户地址相关的记录，可选。

**返回结果**

{% codeblock lang:sh %}
{
    "message": "OK",
    "result": [
        {
            "blockHash": "0xb3ff25909ae9ae5b65baecab1114dff885fbd5a1607081229ea50b6a2db13ae8",
            "blockNumber": "5278009",
            "confirmations": "205861",
            "contractAddress": "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2",
            "cumulativeGasUsed": "1050219",
            "from": "0x4e83362442b8d1bec281594cea3050c8eb01311c",
            "gas": "191157",
            "gasPrice": "4000000000",
            "gasUsed": "97646",
            "hash": "0x497f15095877bc06b9e0f422673c1e0f13a2b4224b615ef29ce8c46e249364d1",
            "input": "0x73b38101000000000000000000000000000000000000000000000000000000000000003d0000000000000000000000000000000000000000000046aefaa28844d7d839d7",
            "nonce": "730",
            "timeStamp": "1521386734",
            "to": "0x69076e44a9c70a67d5b79d95795aba299083c275",
            "tokenDecimal": "18",
            "tokenName": "Maker",
            "tokenSymbol": "MKR",
            "transactionIndex": "28",
            "value": "553365126770755906"
        },
    ],
    "status": "1"
}
{% endcodeblock %}

#### 获取已开采的区块列表

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=account&action=getminedblocks&address=0x9dd134d14d1e65f84b706d6f205cd5b1cd03a46b&blocktype=blocks&page=1&offset=10&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

* action: 值为`getminedblocks`。
* blocktype：可以选区块`blocks`和叔块`uncles`，不了解叔块的可以查看[这里](https://www.bixuncn.com/baike/block/1141.html)。

**返回结果**

{% codeblock lang:sh %}
{
    "message": "OK",
    "result": [
        {
            "blockNumber": "3462296",
            "blockReward": "5194770940000000000",
            "timeStamp": "1491118514"
        },
    ],
    "status": "1"
}
{% endcodeblock %}


### 合同 API

智能合约相关的 API，其实只有一个获取智能合约接口的 API，但是这个 API 非常有用。

#### 获取智能合约接口

**API 示例**

{% codeblock lang:sh %}
https://api.etherscan.io/api?module=contract&action=getabi&address=0xBB9bc244D798123fDe783fCc1C72d3Bb8C189413&apikey=YourApiKeyToken
{% endcodeblock %}

**参数说明**

* module: 值为`contract`。
* action: 值为`getabi`。
* address: 智能合约地址。

其实智能合约的 abi 就是一个 json 对象，通过这个对象我们可以调用其接口方法，后面会写一篇文章介绍如何操作 abi 对象，敬请期待。

**返回结果**

返回结果内容比较长，这里省略，就是一个 json 对象，感兴趣的可以自行调用该 API 看结果。

### 使用限制

账号和智能合约的 API 已经能满足大部分的业务需求了，其他模块的 API 感觉没什么太大的作用，这里就不介绍了，感兴趣的读者可以自行查阅。

这里再说下 API 的使用限制，刚才提到每个 API 都有一个`apkkey`参数，如果 API 没加上这个参数的话，每个 API 的请求次数不能超过 5 次每秒。

## 总结

`Etherscan`提供的这些 API 有些是和以太坊提供的 API 有重复的，比如说获取账号金额，获取事件日志记录等，但有一些 API 给我们带来了很大的便利性，比如获取账号交易记录，有了这个 API 就不用使用几个原生 API 进行各种数据拼接了。

另外`Etherscan`的这套 API 在 Rinkeby 测试网络也有一套一模一样的，区别只是前面的 url 不同，Rinkeby 的是：`api-rinkeby.etherscan.io`，感兴趣的同学也可以去试试。
