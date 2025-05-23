---
layout: post
title: 深入解析 Solana 交易
date: 2025-05-23 14:02:09
description: 深入解析 Solana 交易，包括查看方式、交易结构、交易解析等。
keywords: solana, transaction, solana-explorer, solscan, solana-rpc
comments: true
categories: solana
tags: [solana, transaction, solana-explorer, solscan, solana-rpc]
---

{% img /images/post/2025/05/solana-transaction.png 400 300 %}

在区块链的世界里，交易是一切活动的核心，它记录着价值的转移、状态的变更以及智能合约的执行。对于 Solana 这样一个高性能区块链网络而言，理解交易的结构和机制显得尤为重要。与传统的区块链网络相比，Solana 的交易设计更加复杂和精巧，它不仅支持高并发处理，还提供了丰富的指令类型和灵活的账户模型。无论你是区块链开发者、数据分析师，还是对 Solana 生态感兴趣的技术爱好者，深入了解 Solana 交易的内部结构都是必不可少的技能。本文将带你全面解析 Solana 交易，从多种查看方式到详细的数据结构，从 RPC 调用到浏览器界面，帮助你掌握 Solana 交易分析的各种技巧和最佳实践。

<!--more-->

## 了解 Solana 交易信息的几种方式

在 Solana 生态中，如果你想了解交易的详细信息，有多种方式可以帮助到你，以下是最常用的几种方法：

1. Solana Explorer：最直观的图形界面方式，适合快速查看交易详情和状态
2. RPC 方法：通过编程方式获取原始交易数据，适合需要深度分析或集成到应用程序中
3. Web3.js SDK：提供更高级的 API 接口，简化交易数据的获取和处理

可以根据你的需求来选择具体的方式，下面我们将详细介绍每种方式的具体使用方法。

### 使用 Solana 浏览器查看交易

[Solana Explorer](https://explorer.solana.com/) 是 Solana 官方提供的区块链浏览器，它提供了丰富的交易信息查看功能。通过 Solana Explorer，你可以轻松查看交易详情、账户状态、区块高度等重要信息。

进入 Solana Explorer 网站后，你可以在搜索框中输入交易的哈希值，回车后就可以看到交易详情。交易详情包含 `Overview` 、 `Account Input(s)` 、 `Token Balances` 、 `Instructions` 和 `Program Instruction Logs` 等几个部分。

{% img /images/post/2025/05/solana-explorer.png 1000 600 %}

- 在 `Overview` 区域，你可以看到交易的基本信息，包括交易哈希、区块高度、时间戳、交易金额等。
- 在 `Account Input(s)` 区域，你可以看到交易涉及的账户信息，包括账号的地址和账户名称。
- 在 `Token Balances` 区域，你可以看到交易涉及的代币信息，包括转账代币的账号地址、代币地址、代币转账数量、交易后该账号的代币数量等。
- 在 `Instructions` 区域，你可以看到交易涉及的指令信息，包括顶层指令以及内部指令、指令的程序地址、消耗的 CU 数量、指令参数等。
- 在 `Program Instruction Logs` 区域，你可以看到交易涉及的程序指令日志信息，包括每个指令的详细日志输出内容等。

除了在 Solana Explorer 网站上查看交易详情，你还可以在其他第三方的 Solana 浏览器上查看交易信息，其中比较出名的是 [Solscan](https://solscan.io/) 网站。

Solscan 作为 Solana 生态中知名的第三方浏览器，相比官方的 Solana Explorer 提供了更多特色功能，包括更丰富的代币市场数据、更强大的账户分析工具、更友好的用户界面、额外的实用功能（如交易中代币转账的可视化地图）以及更完善的开发者工具。这些功能使 Solscan 成为一个更全面的 Solana 生态工具，特别适合需要深入分析交易和账户数据的用户。

### 使用 Solana RPC 方法获取交易信息

Solana 官方提供了很多 RPC 方法， 包括 `HTTP` 和 `Websocket` 的接口，这些接口可以获取账号信息、获取区块信息、获取交易信息等，其中获取交易信息是一个 `HTTP` 接口：[getTransaction](https://solana.com/docs/rpc/http/gettransaction)。

这种方式比较适合需要深度分析交易信息的用户，比如开发者、数据分析师等。以下是一个使用 `curl` 命令获取交易信息的示例：

```bash
curl https://api.mainnet-beta.solana.com -s -X \
   POST -H "Content-Type: application/json" -d '
   {
     "jsonrpc": "2.0",
     "id": 1,
     "method": "getTransaction",
     "params": [
       "3AnirZmjF1U64zfCUyrzWBzNGvcaa3Ya7PacAD5Y5LRoHsnZsHSekQEAKGuMCbtGNWXChPe5uWQkvipDGwfUVNAx",
       {
         "commitment": "confirmed",
         "maxSupportedTransactionVersion": 0,
         "encoding": "json"
       }
     ]
   }
 '
```

`getTransaction` 方法的参数包括：

- `transactionSignature`：交易哈希
- `config`：Object 类型的配置参数，包括以下几个参数：
  - `commitment`: 交易确认的级别，可选值包括 `confirmed` 和 `finalized`，默认值是 `finalized`。
  - `maxSupportedTransactionVersion`: 最大支持的交易版本号，当前的有效值是 `0`。
  - `encoding`: 交易编码格式，可选值包括 `json`、`base64`、`jsonParsed` 和 `base58`，默认值是 `json`。

返回的结果是一个 `JSON` 对象，其中包含交易信息，包括交易哈希、区块高度、时间戳、交易金额等。

```json
{
  "jsonrpc": "2.0",
  "result": {
    "blockTime": 1747674547,
    "meta": {
      "computeUnitsConsumed": 169182,
      "err": null,
      "fee": 17621,
      "innerInstructions": […],
      "loadedAddresses": {"readonly":…},
      "logMessages": […],
      "postBalances": […],
      "postTokenBalances": […],
      "preBalances": […],
      "preTokenBalances": […],
      "returnData": {"data":…},
      "rewards": [],
      "status": {
        "Ok": null
      }
    },
    "slot": 341094579,
    "transaction": {"message":…},
    "version": 0
  },
  "id": 1
}
```

这些数据完全等价于我们在 Solana Explorer 中看到的交易信息，只是浏览器将这些数据进行了可视化的展示，方便我们查看，后面我们会详细介绍数据中各个字段的含义。

### 使用 `Solana@web3.js` NPM 包获取交易信息

还有一种交易查询方式方式适合开发者使用，那就是使用 [Solana@web3.js NPM](https://www.npmjs.com/package/@solana/web3.js) 包，这个包是 Solana 官方提供的 JavaScript 库，可以方便地获取交易信息。

这个包提供了两个方法来获取交易信息：`getTransaction` 和 `getParsedTransaction`，这两个方法的底层都是调用 Solana 的 RPC 方法：`getTransaction`，但返回的数据结构有所不同。

以下是使用 `Solana@web3.js` NPM 包获取交易信息的示例：

```js
import {
  Connection,
  PublicKey,
  clusterApiUrl,
  type GetVersionedTransactionConfig,
} from "@solana/web3.js";

const connection = new Connection(clusterApiUrl("mainnet-beta"), "confirmed");

let signature =
  "3AnirZmjF1U64zfCUyrzWBzNGvcaa3Ya7PacAD5Y5LRoHsnZsHSekQEAKGuMCbtGNWXChPe5uWQkvipDGwfUVNAx";

let config: GetVersionedTransactionConfig = {
  commitment: "finalized",
  maxSupportedTransactionVersion: 0,
};

let transaction = await connection.getTransaction(signature, config);
let parsedTransaction = await connection.getParsedTransaction(
  signature,
  config
);

console.log(transaction);
console.log(parsedTransaction);
```

`getTransaction` 方法返回的是一个 `Transaction` 对象，相比 `getParsedTransaction` 方法返回的对象，前者的数据量更小，传输更快，适合需要低层级访问或自定义解析逻辑的场景，对于简单的查询，处理速度可能更快。

`getParsedTransaction` 方法返回的是一个 `ParsedTransaction` 对象，它的数据已经预先解析，更容易直接使用，提供更友好的访问格式，尤其是指令部分的数据，减少客户端的解析负担，更适合需要理解交易内容的应用程序。

以交易中的某个指令数据为例来说明这 2 个方法返回数据的主要不同：

```json
// getTransaction instruction
{
  "programIdIndex": 9,
  "accountKeyIndexes": [0, 2, 0, 25, 7, 30],
  "data": {
    "type": "Buffer",
    "data": [1]
  }
}

// getParsedTransaction instruction
{
  "parsed": {
    "info": {
      "account": "7ubjuqeAFvE3yjsDNdrD31bS8Do9DHB2yGJ2b5yCCvJL",
      "mint": "59obFNBzyTBGowrkif5uK7ojS58vsuWz3ZCvg6tfZAGw",
      "source": "21w1M4yesWyzVVryHuHqg7rLUMWtp78RRaH1Zz6vhwfB",
      "systemProgram": "11111111111111111111111111111111",
      "tokenProgram": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
      "wallet": "21w1M4yesWyzVVryHuHqg7rLUMWtp78RRaH1Zz6vhwfB"
    },
    "type": "createIdempotent"
  },
  "program": "spl-associated-token-account",
  "programId": "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL",
  "stackHeight": null
}
```

在 `getTransaction` 方法返回的数据中，指令数据是原始的二进制数据，程序 ID 和账号地址都是索引值，且数据是二进制格式，需要客户端自己解析，而 `getParsedTransaction` 方法返回的数据中，指令数据已经解析为人类可读的格式，包括指令的类型、参数、账户等信息，其中的 `parsed` 字段就是解析后的数据。

这两个方法都可以用来获取交易的信息，选择哪个方法取决于你的具体需求。如果你需要最高效的数据传输和处理，并且计划自己解析交易数据，`getTransaction`方法可能更合适。如果你需要更易用、更人类可读的数据格式，并且不想处理复杂的解析逻辑，`getParsedTransaction` 可能是更好的选择。

在大多数应用场景中，尤其是开发者工具、区块浏览器或需要展示交易详情的应用，`getParsedTransaction`  方法通常更有优势，因为它提供了更方便的数据访问方式，减少了客户端的解析工作量，并且能够更好地处理复杂的交易指令内容。

## 交易数据结构分析

下面我们以 `getParsedTransaction` 方法返回的数据为例，来分析交易数据结构。首先我们来看下返回的数据结构顶层的字段：

```json
{
  "blockTime": 1747674547,
  "meta": {"computeUnitsConsumed":…},
  "slot": 341094579,
  "transaction": {"message":…},
  "version": 0
}
```

- blockTime: 交易被确认的 UNIX 时间戳（秒）。
- meta: 包含交易执行结果的元数据，包括状态、费用、令牌余额变化等。
- slot: 交易所在区块的槽位号，也可以理解为区块高度，Solana 区块链中用于标识区块高度的唯一标识符。
- transaction: 包含交易详细信息的对象，如签名、指令和账户等。
- version: 交易格式的版本号。

### meta 字段

```json
{
  "meta": {
    "computeUnitsConsumed": 169182,
    "err": null,
    "fee": 17621,
    "innerInstructions": […],
    "logMessages": […],
    "postBalances": […],
    "postTokenBalances": […],
    "preBalances": […],
    "preTokenBalances": […],
    "returnData": {"data":…},
    "rewards": [],
    "status": {
      "Ok": null
    }
  }
}
```

- computeUnitsConsumed: 交易执行消耗的计算单元数量。
- err: 如果交易失败，这里会显示错误信息；成功则为 null。
- fee: 交易处理费用（以 lamports 为单位，1 SOL = 10^9 lamports）。
- innerInstructions: 包含由其他指令触发的内部指令数组。
- logMessages: 交易执行时程序输出的日志信息数组。
- preBalances 和 postBalances: 交易前后账户的 lamports 余额数组。
- preTokenBalances 和 postTokenBalances: 交易前后涉及的代币账户余额信息。
- returnData: 交易返回的数据。
- rewards: 交易奖励信息。
- status: 交易状态对象，通常成功会显示为 {"Ok": null}。

`innerInstructions` 字段是一个数组，每个元素是一个内部指令对象，内部指令对象包含以下字段：

```json
"innerInstructions": [
  {
    "index": 2,
    "instructions": [
      {
        "parsed": {
          "info": {
            "extensionTypes": [
              "immutableOwner"
            ],
            "mint": "59obFNBzyTBGowrkif5uK7ojS58vsuWz3ZCvg6tfZAGw"
          },
          "type": "getAccountDataSize"
        },
        "program": "spl-token",
        "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
        "stackHeight": 2
      },
      // ......
    ]
  }
]
```

- index: 对应顶层指令的索引
- instructions: 内部指令列表，包含程序 ID 和指令数据，指令数据已经解析为人类可读的格式，包括指令的程序名称、程序 ID、指令参数等。

`preBalances` 和 `postBalances` 字段的数据类型是数组，是所有参与交易的账户的原生 SOL 余额（以 lamports 为单位），数组的索引位置与 `transaction.message.accountKeys`（后面会介绍）中的账户位置一一对应。

```json
"postBalances": [
  493860324,
  2039280,
  2039280,
  // ......
],
"postBalances": [
  495917225,
  2039280,
  0,
  // ......
]
```

`preTokenBalances` 和 `postTokenBalances` 这 2 个数组字段只包含该交易中涉及代币变动的账户的代币余额信息，而不是所有账户。每个元素都包含 `accountIndex` 字段，指向 `accountKeys` 中的索引位置。

```json
"postTokenBalances": [
  {
    "accountIndex": 1,
    "mint": "59obFNBzyTBGowrkif5uK7ojS58vsuWz3ZCvg6tfZAGw",
    "owner": "3CgvbiM3op4vjrrjH2zcrQUwsqh5veNVRjFCB9N6sRoD",
    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
    "uiTokenAmount": {
      "amount": "8802868",
      "decimals": 6,
      "uiAmount": 8.802868,
      "uiAmountString": "8.802868"
    }
  },
  // ......
]
"preTokenBalances": [
  {
    "accountIndex": 1,
    "mint": "59obFNBzyTBGowrkif5uK7ojS58vsuWz3ZCvg6tfZAGw",
    "owner": "3CgvbiM3op4vjrrjH2zcrQUwsqh5veNVRjFCB9N6sRoD",
    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
    "uiTokenAmount": {
      "amount": "8506130",
      "decimals": 6,
      "uiAmount": 8.50613,
      "uiAmountString": "8.50613"
    }
  },
  // ......
]
```

代币余额信息包括：

- accountIndex: 对应 `accountKeys` 中的索引
- mint: 代币铸造地址
- owner: 代币账户所有者
- uiTokenAmount: 代币数量信息（包括数量、精度和 UI 显示数量）

`logMessages` 字段是交易执行时程序输出的日志信息数组，每个元素是一行日志信息：

```json
"logMessages": [
  "Program ComputeBudget111111111111111111111111111111 invoke [1]",
  "Program ComputeBudget111111111111111111111111111111 success",
  "Program ComputeBudget111111111111111111111111111111 invoke [1]",
  // ......
]
```

`rewards` 字段主要用于记录与区块验证和共识机制相关的奖励信息，是 Solana 经济模型的一个重要组成部分。对普通用户分析交易时，该字段通常不是重点关注内容，但对理解 Solana 的经济模型和奖励机制非常重要。

`returnData` 字段是 Solana 程序向调用者返回数据的机制，允许程序在执行完成后传递结果数据。这个机制为 Solana 生态系统中的程序间通信提供了标准化的数据返回方式，特别在复杂的 DeFi 操作中非常有用。

### transaction 字段

```json
"transaction": {
  "message": {
    "accountKeys": […],
    "addressTableLookups": […],
    "instructions": […],
    "recentBlockhash": "64DTRb3pnTsxvjNEnVLEpqwdoyFc388ydcf7EyMdcbTm"
  },
  "signatures": [
    "3AnirZmjF1U64zfCUyrzWBzNGvcaa3Ya7PacAD5Y5LRoHsnZsHSekQEAKGuMCbtGNWXChPe5uWQkvipDGwfUVNAx"
  ]
}
```

- message: 包含交易消息的对象。
  - accountKeys: 参与交易的账户列表。
  - addressTableLookups: 地址查找表相关信息。
  - instructions: 交易包含的指令数组。
  - recentBlockhash: 交易使用的最近区块哈希。
- signatures: 交易签名数组，第一个通常是交易 ID。

`accountKeys` 字段是参与交易的账户列表，每个元素是一个账户对象，之前的 `meta` 字段中很多属性都与该字段有关联，包含以下信息：

```json
"accountKeys": [
  {
    "pubkey": "21w1M4yesWyzVVryHuHqg7rLUMWtp78RRaH1Zz6vhwfB",
    "signer": true,
    "source": "transaction",
    "writable": true
  },
  // ......
]
```

- pubkey: 账户公钥
- signer: 是否为交易签名者
- source: 账户来源（transaction 或 lookupTable）
- writable: 是否可写

`addressTableLookups` 是 Solana 交易中的一个关键字段，主要用于解决交易账户数量限制问题，Solana 交易默认最多可包含 32 个直接账户引用，通过地址查找表可以引用更多账户，这样就突破账户数量限制。另外一个作用是减小交易大小，避免在交易中重复包含完整的 32 字节账户地址，仅包含查找表地址和索引，大幅减少交易数据体积。它的工作原理是事先在链上创建地址查找表（Address Lookup Table），该表存储多个常用账户地址，每个地址分配一个索引，交易中通过指定查找表地址和索引来引用账户，不需要包含完整的账户地址。

```json
"addressTableLookups": [
  {
    "accountKey": "6dfGWWq4sTPFHPvnVBZNjQd2PTqzgGAexLAHXao7TRSb",
    "readonlyIndexes": [
      17
    ],
    "writableIndexes": [
      18,
      12,
      10,
      19,
      15
    ]
  },
  // ......
]
```

`addressTableLookups` 字段中各字段含义：

- accountKey：地址查找表的公钥地址
- readonlyIndexes：从查找表引用的只读账户索引
- writableIndexes：从查找表引用的可写账户索引

在交易时，交易处理器会找到 `accountKey` 指定的地址查找表，查找表中检索指定索引位置的账户地址，将这些地址添加到交易的账户列表中，根据是在 `readonlyIndexes` 还是 `writableIndexes` 中标记账户权限，这就是为什么在 `accountKeys` 数组中，有些账户的 `source` 值为 `lookupTable`，表明它们是通过地址查找表加载的，而不是直接包含在交易中。

`transaction.message.instructions` 字段是交易包含的顶层指令数组，每个元素是一个顶层指令对象，顶层指令与 `meta` 字段中的 `innerInstructions` 内部指令的区别是：

- 顶层指令是用户/客户端显式包含在交易中的指令，在交易创建和签名时直接指定，代表交易的主要意图和行为，由交易者直接控制和构建，在交易提交时已确定，数量通常有限，按数组顺序执行，相当于程序的主函数或入口点，是触发后续一系列操作的起点。
- 内部指令是顶层指令执行过程中动态生成的派生指令，不在原始交易中显式指定，是程序执行逻辑的细节体现，当一个程序调用另一个程序（CPI）时产生，每组内部指令通过 `index` 字段关联到生成它的顶层指令，一个顶层指令可能生成多个内部指令，内部指令可能进一步产生更深层次的内部指令，形成指令调用树结构。

我们可以在这些指令对象中看到 `stackHeight` 字段，该字段是指令在程序调用栈中的深度级别，反映了指令之间的调用层级关系，值为 `null` 通常表示顶层指令，值为 `1` 表示由顶层程序直接调用的指令，值为 `2` 表示由 `stackHeight` 为 `1` 的程序调用的指令，值为 `3+` 表示更深层次的嵌套调用。

```json
"transaction": {
  "message": {
    "instructions": [
      {
        "accounts": [],
        "data": "EC5es9",
        "programId": "ComputeBudget111111111111111111111111111111",
        "stackHeight": null
      },
      // ......
    ]
  }
}
```

## 总结

本文从实用角度出发，全面解析了 Solana 交易的查看方式和数据结构。无论你是通过浏览器快速查看交易详情，还是使用 RPC 接口进行深度分析，或是利用 Web3.js SDK 构建应用程序，都能在本文中找到最适合的方法和最佳实践。通过深入理解交易的 meta 和 transaction 字段、指令层级关系以及地址查找表等核心概念，你将具备分析任何 Solana 交易的能力。这些知识不仅是 Solana 开发的基础技能，更是在这个高性能区块链生态中构建优秀应用的必备工具。掌握了这些，你就拥有了解读 Solana 链上世界的钥匙。

## 参考

- [交易和指令](https://solana.com/docs/core/transactions)
- [Solscan 交易详情](https://docs.solscan.io/transaction-details/transaction-details)
- [Solscan 账号页面](https://info.solscan.io/exploring-account-page/)
- [Solana RPC 接口文档](https://solana.com/docs/rpc)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
