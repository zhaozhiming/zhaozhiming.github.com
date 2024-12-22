---
layout: post
title: 深入解密 Solana 开发中的资源限制——CU 篇
date: 2024-12-07 21:44:32
description: 详细介绍 Solana 开发过程中经常遇到的资源限制问题，通过示例分析，程序对比来重点介绍 Solana 开发中的 CU 限制。
keywords: web3, blockchain, solana, anchor, computer-unit
comments: true
categories: code
tags: [web3, blockchain, solana, anchor, computer-unit]
---

{% img /images/post/2024/12/solana-limitation-cu.jpeg 400 300 %}

很多开发人员在开发 Solana 程序（智能合约）时经常遇到这样的问题，明明程序逻辑没有问题，但是在程序运行的时候就会出现奇怪的错误，错误的提示会出现`限制`或者`超出`等字眼，这表示你的程序可能落入了 Solana 的资源限制中。Solana 作为高性能区块链，其核心特性之一在于通过并行处理显著提升交易吞吐量，这种高效能背后依赖严格的资源管理机制，开发者需要深刻理解这些限制，才能有效开发和优化 Solana 程序。本文旨在介绍 Solana 开发中的资源限制，并详细介绍其中关于计算单元（Compute Unit，以下简称 CU）的限制，剖析实际场景，探讨优化策略。

<!--more-->

## Solana 介绍

[Solana](https://solana.com/) 于 2020 年推出，最初被视为新兴区块链网络并且迅速崛起，成为全球最受欢迎的区块链生态系统之一。根据相关数据统计，Solana 在 2024 年占据了全球加密货币投资者对特定链兴趣的 49.3%，显示出其在行业中的主导地位。

Solana 是一个高性能的公有区块链平台，与传统区块链网络（如比特币、以太坊等）相比，其核心特点是高吞吐量与低延迟，其创新的 Proof of History 共识机制与高效的并行处理架构使其能够每秒处理数千笔交易。为了维持网络的稳定与公平，Solana 针对程序执行设置了多种资源限制，以确保系统资源合理分配并最大化利用。

## 限制类型

在 Solana 上运行的程序会受到以下几类资源限制，这些限制设计的目的是为了保证网络的高效性和稳定性，同时为开发者提供明确的开发边界。这些资源限制涵盖从计算能力到数据存储的多方面要求，使得程序在保证性能的前提下能够公平地利用系统资源。

### CU 限制

在 Solana 区块链中，CU 是衡量交易执行时消耗的计算资源最小单位。每笔交易在链上运行时，由于执行不同的指令（如账户写入、跨程序调用、系统调用等），会消耗一定数量的 CU。每个交易都会有一个 CU 限制，该限制可以使用默认设置，也可以通过程序申请提高。当交易的 CU 消耗超过限制时，处理会被中止，导致交易失败。常见的操作如执行指令、程序间数据传递、加密运算等都会产生 CU 消耗。CU 的设计旨在管理资源分配，防止网络滥用并提升整体效率，更多详细信息可以参考[这里](https://solana.com/docs/core/fees#compute-unit-limit)。

以下是 Solana 中 CU 的限制：

- 每个交易的最大 CU 限制是 **140 万**
- 每个指令（每个交易会包含多个指令）的默认 CU 限制是 **20 万**
- 每个区块的 CU 限制是 **4800 万**
- 每个用户在一个区块中的 CU 限制是 **1200 万**

每个指令的默认 CU 限制是 20 万 CU，假设一个交易只包含了一个指令，那么该交易的 CU 默认限制就是 20 万 CU，但可以通过 `SetComputeUnitLimit` 指令来提高或降低交易的 CU 限制，但最大不得超过交易的 140 万 CU 这个限制。

### 存储限制

在 Solana 区块链中，每个账户的数据结构被称为 **AccountInfo**，包含账户状态、程序代码（若为程序账户）、余额（以 lamports 为单位，1 SOL 等于 10 亿 lamports）以及指定的所有者程序（程序 ID）。在 Solana 的账户模型中，每个账户都由一个程序作为其所有者，只有所有者程序可以修改账户数据或减少余额，而增加余额则不受限制。这一模型确保了账户数据的安全性和操作的可控性，更多详细信息可以参考[这里](https://solana.com/docs/core/accounts#accountinfo)。

以下是 Solana 中存储的限制：

- 每个账户的最大存储空间为 **10MB**

### 交易大小限制

Solana 网络遵循最大传输单元（MTU）大小为 1280 字节的限制，与 IPv6 的 MTU 标准一致，以确保通过 UDP 快速可靠地传输集群信息。在扣除 IPv6 和片段头部的 48 字节后，剩余 1232 字节用于承载数据，如序列化交易。这意味着每笔 Solana 交易的总大小不得超过 1232 字节，包括签名和消息部分。其中每个签名占用 64 字节，数量根据交易需求决定，消息部分包含指令、账户和其他元数据，每个账户需占用 32 字节，整体大小视交易所包含的指令而变化。这一限制确保交易数据在网络中高效传输，更多详细信息可以参考[这里](https://solana.com/docs/core/transactions#transaction-size)。

以下是 Solana 中交易大小的限制：

- 每笔交易的大小最大限制为 **1232 字节**

### 调用深度限制

在 Solana 中，为保证程序运行的高效性，每个程序的调用栈深度也有限制。如果超出该限制，将触发 `CallDepthExceeded` 错误。此外，Solana 支持程序直接调用其他程序（跨程序调用），但跨程序调用的深度也有限制，超过这一限制将触发 `CallDepth` 错误。这些限制旨在提高网络性能和资源管理效率，更多详细信息可以参考[这里](https://solana.com/docs/programs/limitations#call-stack-depth-object-object-error)。

以下是 Solana 中调用深度的限制：

- 每个程序的调用栈深度限制为 **64 层**
- 跨程序调用的深度限制为 **4 层**

### 堆栈大小限制

在 Solana 程序设计的虚拟机架构中，每个栈帧大小有一定限制，使用固定栈帧而非可变栈指针。如果程序超出栈帧限制，编译器会发出警告，但不阻止编译。在运行时，如果确实超出栈大小，会触发 `AccessViolation` 错误。在堆管理方面，Solana 程序使用一个简单的堆来做内存管理，采用快速分配的 **bump** 模式，不支持内存释放或重新分配。每个 Solana 程序都可以访问这个内存区域，并根据需要实现自定义堆管理，这些限制有助于优化性能和资源分配，更多详细信息可以参考[这里](https://solana.com/docs/programs/faq#stack)。

以下是 Solana 中堆栈大小的限制：

- 每个栈帧大小为 **4KB**
- 程序堆大小为 **32KB**

### PDA 账号限制

在 Solana 中，程序派生地址（PDA）为开发者提供了一种确定性生成账户地址的机制，通过预定义的**种子**（如字符串、数字或其他账户地址）和程序 ID 来派生地址，从而实现类似链上哈希映射的功能。此外，Solana 运行时允许程序为其派生的 PDA 进行签名操作。PDA 的优势在于无需记住具体的账户地址，只需记住用于生成地址的输入即可，简化了账户管理，提高了开发效率，更多详细信息可以参考[这里](https://solana.com/developers/courses/native-onchain-development/program-derived-addresses#seeds)。

以下是 Solana 中 PDA 账号的限制：

- 每个 PDA 的种子长度不得超过 **32 字节**
- 所有种子的总数量不得超过 **16 个**

## CU 限制详解

介绍完了 Solana 的各种资源限制后，我们来重点了解一下关于 CU 限制的相关内容。前面提到，CU 是衡量交易执行时消耗的计算资源的最小单位，每笔交易的 CU 消耗不能超过 120 万 CU，但对于初入 Solana 开发的开发者来说，这个概念可能还不够直观，下面我们通过一些示例来帮助大家更好地理解 CU 的消耗。

### 显示 CU 消耗

在分析程序的 CU 消耗之前，我们先来了解一下 Solana 程序中显示 CU 消耗的方法。在 Solana 程序中，可以通过 `log` 函数来输出日志，其中包括 CU 消耗，下面是一个简单的示例：

```rust
use solana_program::log::sol_log_compute_units;

sol_log_compute_units();
// other code
sol_log_compute_units();
```

每个 `sol_log_compute_units` 函数调用都会输出当前的 CU 消耗，程序运行后的日志如下：

```sh
Program consumption: 149477 units remaining
# other logs
Program consumption: 137832 units remaining
```

通过这种方式，我们就可以计算出对应程序的 CU 消耗情况，比如在上面的例子中，两次调用 `sol_log_compute_units` 函数的差值就是中间程序的 CU 消耗。

我们也可以将 CU 消耗打印语句封装成一个 Rust 的宏，这样就可以在程序中更方便地调用，代码示例如下：

```rust
// build a macro to log compute units
#[macro_export]
macro_rules! compute_fn {
    ($msg:expr=> $($tt:tt)*) => {{
        msg!(concat!($msg, " {"));
        sol_log_compute_units();
        let res = { $($tt)* };
        sol_log_compute_units();
        msg!(concat!(" } // ", $msg));
        res
    }};
}

// use the macro to log compute units
compute_fn!("create account" => {
    // create account code
});
```

封装成宏的方式可以更方便地在程序中调用，同时也可以输出更多的信息，方便调试。

### Solana 程序示例

为了测试 Solana 程序的 CU 消耗情况，我们可以通过一些示例程序来了解不同操作的 CU 消耗情况。

Solana 为初学者提供了很多学习资料，其中包括一系列简单的示例程序（`program-examples`），可以帮助开发者更好地理解 Solana 的开发流程，示例程序可以查看这个 GitHub [仓库](https://github.com/solana-developers/program-examples)。

在这个仓库中，每个示例程序都有两种示例，一种是原生程序，另一种是 Anchor 程序。

> [Anchor](https://www.anchor-lang.com/) 是 Solana 区块链上广泛使用的开发框架，用于简化程序（智能合约）和去中心化应用（DApp）的开发流程。它为开发者提供了一套高效且直观的工具和库，极大降低了 Solana 应用开发的门槛，Solana 官方推荐使用 Anchor 进行开发。

后面我们在介绍 Solana 程序的 CU 消耗时，会引用这个仓库中的示例程序，通过这些示例程序来了解 Solana 中 CU 的消耗情况。下面我们通过操作和程序的角度来分别介绍 Solana 中 CU 的消耗情况。

### 操作示例

首先是 Solana 中的一些常见操作，我们通过这些操作来了解每个操作的 CU 消耗情况。

**转账 SOL**

在 Solana 中，转账 SOL 是最常见的操作之一，每次转账都会消耗一定数量的 CU，大家肯定很好奇一次转账会消耗多少 CU，我们可以简单的做个实验，在 Solana 的 Devnet 网络上进行一次转账，然后在 [Solana 浏览器](https://explorer.solana.com/)上查看这笔交易的 CU 消耗情况，结果如下：

{% img /images/post/2024/12/solana-transfer-sol-cu.png 1000 600 %}

我们在浏览器的交易详情中可以看到，这笔转账消耗了 **150** CU，这个值不是固定的，但通常不会相差太大，消耗的 CU 大小与转账金额的大小无关，而是与交易的指令数量和复杂度有关。

**创建账号**

创建账号也是 Solana 中常见的操作之一，每次创建账号也会消耗一定数量的 CU，我们可以通过创建账号来了解其消耗情况。

我们可以在 `program-examples` 仓库的 `basic/create-account` 目录下找到创建账号的[示例程序](https://github.com/solana-developers/program-examples/blob/main/basics/create-account/anchor/programs/create-system-account/src/lib.rs#L20C1-L33C12)，在这个程序中，我们通过在代码中添加 CU 消耗打印语句的方式来验证该程序的 CU 消耗情况，程序运行后的打印日志如下所示：

```sh
[2024-12-08T07:34:47.865105000Z DEBUG solana_runtime::message_processor::stable_log] Program consumption: 186679 units remaining
[2024-12-08T07:34:47.865181000Z DEBUG solana_runtime::message_processor::stable_log] Program 11111111111111111111111111111111 invoke [2]
[2024-12-08T07:34:47.865209000Z DEBUG solana_runtime::message_processor::stable_log] Program 11111111111111111111111111111111 success
[2024-12-08T07:34:47.865217000Z DEBUG solana_runtime::message_processor::stable_log] Program consumption: 183381 units remaining
[2024-12-08T07:34:47.865219000Z DEBUG solana_runtime::message_processor::stable_log] Program log: Account created succesfully.
```

测试结果发现，每次创建账号的 CU 消耗大约在 **3000** 左右，这个值是不固定的，但通常不会相差太大。

**创建简单数据结构**

我们再来看下创建一个简单的数据结构的 CU 消耗情况，示例程序可以在 `program-examples` 仓库中的 `basic/account-data` 目录下找到，这个示例程序中定义了一个简单的数据结构，完整的示例程序代码可以查看[这里](https://github.com/solana-developers/program-examples/blob/main/basics/account-data/anchor/)，这个数据结构定义如下：

```rust
use anchor_lang::prelude::*;

#[account]
#[derive(InitSpace)] // automatically calculate the space required for the struct
pub struct AddressInfo {
    #[max_len(50)] // set a max length for the string
    pub name: String, // 4 bytes + 50 bytes
    pub house_number: u8, // 1 byte
    #[max_len(50)]
    pub street: String, // 4 bytes + 50 bytes
    #[max_len(50)]
    pub city: String, // 4 bytes + 50 bytes
}
```

数据结构中有 3 个字符串字段和 1 个 u8 字段，每个字符串字段的最大长度为 50，经过测试后发现，创建这样一个简单的数据结构的 CU 消耗大约在 **7000** 左右。

**计数器**

Solana 的官方示例程序中，还提供了一个简单的计数器程序，可以在 `program-examples` 仓库中的 `basic/counter` 目录下找到，这个示例程序中定义了一个简单的计数器数据结构，并有创建计数器和增加计数等指令，完整的示例程序代码可以查看[这里](https://github.com/solana-developers/program-examples/tree/main/basics/counter/anchor)。

经过测试后发现，创建计数器的 CU 消耗大约在 **5000** 左右，增加计数 的 CU 消耗大约在 **900** 左右。

**转账 Token**

在 Solana 程序中，还有一个比较常见但相对复杂的操作就是转账 Token。

> 在 Solana 中，Token 是通过 SPL Token 标准实现的，SPL Token 是 Solana 官方提供的一个标准，用于实现代币的发行、转账等操作，SPL Token 提供了一套标准的接口，方便开发者在 Solana 上实现代币相关的操作。

在 `program-examples` 仓库中也提供了一个转账 Token 的示例程序，可以在 `token/transfer-tokens` 目录下找到，该示例程序主要功能是转账 Token，但也包含了创建 Token、Mint Token、Burn Token 等操作，完整的示例程序代码可以查看[这里](https://github.com/solana-developers/program-examples/tree/main/tokens/transfer-tokens/native)。

经过测试后发现，Token 相关操作的 CU 消耗情况结果如下：

- 创建 Token 的 CU 消耗大约在 **3000** 左右
- Mint Token 的 CU 消耗大约在 **4500** 左右
- Burn Token 的 CU 消耗大约在 **4000** 左右
- 转账 Token 的 CU 消耗大约在 **4500** 左右

在真实的交易记录中，我们也可以看到转账 Token 的 CU 消耗情况，以[这个交易](https://explorer.solana.com/tx/FiqGufYKmKeGWfnyRXAkSx3UXPwp8iyZroBPCmcSNrdxNm1ydFqtBCvfq7iU5hTscc11ZuxzHP5dowVQFbgKv5s)为例，在其交易详情最下方的日志输出中，我们可以看到转账 Token 的 CU 消耗情况：

{% img /images/post/2024/12/solana-transfer-token-cu.png 1000 600 %}

**小结**

常见操作的 CU 消耗情况总结如下：

| Action                    | CU Cost (approx.)                                                    |
| ------------------------- | -------------------------------------------------------------------- |
| Transfer SOL              | 150                                                                  |
| Create Account            | 3000                                                                 |
| Create Simple data struct | 7000                                                                 |
| Counter                   | 5000 (Init) <br> 900 (Add count)                                     |
| Token                     | 3000 (Create) <br> 4500 (Mint) <br> 4000 (Burn) <br> 4500 (Transfer) |

### 程序示例

了解完 Solana 中一些常见操作的 CU 消耗情况后，我们再来看下一些常用程序语法的 CU 消耗情况。

**循环语句**

在 Solana 程序中，循环语句是常见的语法之一，我们可以通过循环语句来了解其 CU 消耗情况，下面我们来对比一下不同大小的循环语句的 CU 消耗情况：

```rust
// simple msg print, cost 226 CU
msg!("i: {}", 1);

// simple print for loop 1 time, cost 527 CU
for i in 0..1 {
    msg!("i: {}", i);
}

// simple print for loop 2 times, cost 934 CU
for i in 0..2 {
    msg!("i: {}", i);
}
```

经测试后发现，简单的打印语句需要消耗 226 CU，加上 1 次循环后需要消耗 527 CU，加上 2 次循环后需要消耗 934 CU，于是我们可以简单地得出结论，初始化一个循环语句需要大概需要消耗 527-226=301 CU，每次循环大概需要消耗 934 - 226x2 - 301 = 181 CU。

我们再用 CU 消耗较高的语句来验证一下循环语句的 CU 消耗，比如打印账号地址的语句：

```rust
// print account address, cost 11809 CU
msg!("A string {0}", ctx.accounts.address_info.to_account_info().key());

// print account address in for loop 1 time, cost 12108 CU
for i in 0..1 {
    msg!("A string {0}", ctx.accounts.address_info.to_account_info().key());
}

// print account address in for loop 2 times, cost 24096 CU
for i in 0..1 {
    msg!("A string {0}", ctx.accounts.address_info.to_account_info().key());
}
```

可以看到，循环语句的 CU 消耗根据其中的程序逻辑而变化，但是循环语句本身的消耗是比较少的，大概在 **200-300** CU 之间。

**If 语句**

If 语句也是常见的语法之一，我们可以通过 If 语句来了解其 CU 消耗情况，下面我们来对比一下 If 语句的 CU 消耗情况：

```rust
// a base function consumed 221 CU
pub fn initialize(_ctx: Context<Initialize>) -> Result<()> {
      Ok(())
}

// after add if sentence, the CU consumed is 339 CU
pub fn initialize(_ctx: Context<Initialize>) -> Result<()> {
   if (true) {
      Ok(())
   } else {
      Ok(())
   }
}
```

经测试后发现，一个空的函数需要消耗 221 CU，如果在这个函数中加上 If 语句，那么消耗会增加到 339 CU，所以一个简单的 if 语句的 CU 消耗大约在 **100** CU 左右。

**不同大小的数据结构**

使用不同大小的数据结构也会影响 CU 消耗，下面我们来对比一下不同大小的数据结构的 CU 消耗情况：

```rust
// use a default vector and push 10 items, it will consume 628 CU
let mut a Vec<u32> = Vec::new();
for _ in 0..10 {
    a.push(1);
}

// use a 64 bit vector and do the same things, it will consume 682 CU
let mut a Vec<u64> = Vec::new();
for _ in 0..10 {
    a.push(1);
}

// use a 64 bit vector and do the same things, it will consume 462 CU
let mut a: Vec<u8> = Vec::new();
for _ in 0..10 {
    a.push(1);
}
```

经测试后发现，使用默认的数据结构 `Vec<u32>` 来存储 10 个元素需要消耗 **628** CU，使用 64 位的数据结构 `Vec<u64>` 来存储 10 个元素需要消耗 **682** CU，使用 8 位的数据结构 `Vec<u8>` 来存储 10 个元素需要消耗 **462** CU，所以数据结构的大小对 CU 消耗也有一定的影响。

**哈希函数**

在 Solana 程序中，常见的语法还有使用哈希函数，下面我们来对比一下使用哈希函数的 CU 消耗情况，下面是一个简单的哈希函数使用示例：

```rust
use solana_program::hash::hash;

pub fn initialize(_ctx: Context<Initialize>) -> Result<()> {
    let data = b"some data";
    let _hash = hash(data);
    Ok(())
}
```

我们可以对添加哈希函数和不添加哈希函数的程序进行对比，经测试后发现，使用了 Solana hash 函数来计算哈希值大概需要消耗 **200** CU 左右。

**调用函数**

调用函数也是编程过程中必不可少的操作之一，下面我们来对比一下调用函数的 CU 消耗情况，下面是一个简单的调用函数使用示例：

```rust
pub fn initialize(_ctx: Context<Initialize>) -> Result<()> {
    let result = other_func(_ctx)?;
    Ok(())
}
pub fn other_func(_ctx: Context<Initialize>) -> Result<()> {
    Ok(())
}
```

测试后发现，调用函数所消耗的 CU 跟被调函数的实现逻辑有关， 调用一个空的函数方法只需要消耗 **100** CU 左右。

**小结**

常见程序语法的 CU 消耗情况总结如下：

| Program          | CU Cost (approx.)                                  |
| ---------------- | -------------------------------------------------- |
| For              | 301 (Init) <br> 181 (Loop)                         |
| If               | 100                                                |
| Diff Size Struct | 462 (Vec u8) <br> 628 (Vec u32) <br> 682 (Vec u64) |
| Hash             | 200                                                |
| Call Function    | 100                                                |

## Solana 原生程序与 Anchor 程序 CU 消耗对比

之前提到，Solana 程序目前有两种开发方式，一种是原生程序，另一种是使用 Anchor 框架开发程序，Anchor 框架是 Solana 官方推荐的开发框架，提供了一套高效且直观的工具和库，极大降低了 Solana 应用开发的门槛。大家可能会好奇，原生程序和 Anchor 程序的 CU 消耗有什么区别，下面我们就来对比这两种程序的 CU 消耗情况，我们将以**转账 Token** 操作为例来进行对比。

### 原生程序 CU 消耗

我们先来看下原生程序中关于转账 Token 的 CU 消耗情况，我们可以通过分析源码来进行分析，Solana 程序的源码在[这个仓库](https://github.com/solana-labs/solana-program-library)中，转账 Token 的核心方法 `process_transfer` 的源码可以查看[这里](https://github.com/solana-labs/solana-program-library/blob/master/token/program/src/processor.rs#L229-L343)。在这个方法中，我们对这个方法进行了详细的步骤分解，并统计出了每个步骤的 CU 消耗情况，具体分析结果如下：

- 基础消耗：运行空方法的消耗为 939 CU
- 转账初始化：包括账号检查和初始化，消耗为 2641 CU
- 检查账号是否冻结：消耗为 105 CU
- 检查源账号余额是否足够：消耗为 107 CU
- 检查 Token 类型是否匹配：消耗为 123 CU
- 检查 Token 地址和预期小数位数：消耗为 107 CU
- 处理自我转账：消耗为 107 CU
- 更新账号余额：消耗为 107 CU
- 处理 SOL 转账：消耗为 103 CU
- 保存账号状态：消耗为 323 CU

整个转账 Token 的操作消耗大约在 4555 CU，与我们之前的测试结果（4500 CU）接近。其中转账初始化的消耗最大，为 2641 CU，可以再将其拆分为更细的步骤，拆分和分析后的结果如下：

- 初始化源账号：消耗为 106 CU
- 初始化 mint 信息：消耗为 111 CU
- 初始化目标账号：消耗为 106 CU
- 解包源账号：消耗为 1361 CU
- 解包目标账号：消耗为 1361 CU

其中两个账号的解包操作消耗最大，每个账号的解包操作消耗大约在 1361 CU，这个消耗是比较大的，所以在开发过程中需要注意。

### Anchor 程序 CU 消耗

了解了原生程序的 CU 消耗情况后，我们再来看下 Anchor 程序的 CU 消耗情况。我们可以在 `program-examples` 仓库的 `tokens/transfer-tokens` 的目录下找到 Anchor 程序的示例程序，其中关于转账的 Token 的源码文件是 [src/instractions/transfer.rs](https://github.com/solana-developers/program-examples/blob/main/tokens/transfer-tokens/anchor/programs/transfer-tokens/src/instructions/transfer.rs)。

初次运行该指令时，我们惊讶的发现 Anchor 程序转账 Token 的 CU 消耗竟然在 80000 ～ 90000 CU 左右，这个消耗量接近原生程序的 **20 倍**!

为什么 Anchor 程序的 CU 消耗会如此之高呢？我们开始对该程序的源码进行分析，Anchor 程序一般会包含两部分，一部分是账号的初始化，另一部分是指令的执行，这两部分都会消耗一定的 CU，我们对这两部分的消耗进行了详细的分析，具体分析结果如下：

- 程序的整体消耗是 81457 CU
- Anchor 框架的初始化消耗是 10526 CU
- 账号初始化的消耗是 20544 CU（源码中的 L9-L34）
- 转账 Token 的指令消耗是 50387 CU（源码中的 L36-L67）

在账号初始化的过程中，需要初始化包括 sender_token_account，recipient_token_account 等账号，还需要初始化 token_program，associated_token_program 等程序，总共消耗了 20544 CU。

而在转账 Token 的指令执行过程中，总共消耗了 50387 CU，我们再次对转账 Token 的指令执行过程进行了详细的分析，具体分析结果如下：

- 函数初始化的消耗是 6213 CU（即一个空方法也会消耗这么多 CU）
- 程序开始包含了 3 个 CU 消耗很大的打印语句
- 第一个打印语句的消耗是 11770 CU（源码中的 L38-L41），这是因为需要将账号地址隐式转换为 Base58 编码，这个操作非常耗费资源，这也是 [Solana 官方不推荐的操作](https://solana.com/developers/guides/advanced/how-to-optimize-compute)
- 第二个打印语句的消耗是 11645 CU（源码中的 L42-L45）
- 第三个打印语句的消耗是 11811 CU（源码中的 L46-L49）
- 转账指令的消耗是 7216 CU（源码中的 L52-L62），这里调用了 anchor_spl::token::transfer 方法，这个方法是对原生的 transfer 方法的封装，除了调用原生 transfer 方法外，还会进行一些其他操作
- 其他消耗是 1732 CU

通过这些分析发现，程序中关于转账 Token 部分的实际消耗只有 **7216** CU，但是由于 Anchor 框架的初始化、账号初始化和一些打印语句的消耗，才导致整个程序的消耗达到了 81457 CU。

使用 Anchor 开发的程序虽然会消耗更多的 CU，但是 Anchor 框架提供了更多的功能和便利，所以这种消耗是可以理解的，开发者可以根据自己的需求选择合适的开发方式。

## 总结

今天为大家总结了 Solana 开发中的各种资源限制，然后重点介绍了 Solana 中 CU 的限制，分别介绍了一些常见操作和程序的 CU 消耗，最后对比了 Solana 原生程序和 Anchor 程序的 CU 消耗情况。不管你是 Solana 的初学者还是经验丰富的开发者，希望这篇文章能够帮助你更好地理解 Solana 中 CU 的消耗情况，在开发设计过程中更好地规划程序资源，优化程序性能。

我们也根据 Solana 的官方文档总结了一些优化计算资源的方法，帮助开发者更好地避免 CU 限制中的_陷阱_，具体如下：

- 测量计算使用量：使用在日志中显示 CU 消耗信息可以评估代码片段的 CU 消耗，从而识别高计算开销的部分。
- 减少日志记录：日志记录操作（如 `msg!` 宏）会显著增加 CU 消耗，特别是涉及 Base58 编码和字符串连接时。建议仅记录必要的信息，并使用更高效的方法，如 `.key().log()`，来记录公钥等数据。
- 选择合适的数据类型：较大的数据类型（如 u64）比较小的数据类型（如 u8）消耗更多的 CU。在可能的情况下，优先使用较小的数据类型以减少 CU 使用。
- 优化序列化操作：序列化和反序列化操作会增加 CU 消耗。通过使用零拷贝（zero copy）技术，直接与账户数据交互，可以降低这些操作的开销。
- PDA 的查找：`find_program_address` 函数的计算复杂度取决于找到有效地址所需的尝试次数。在初始化时保存 bump 值，并在后续操作中使用已知的 bump 值，可以减少 CU 消耗。

希望在以后的文章中，我们可以继续讨论 Solana 资源限制的其他方面，为大家提供更多的参考，如果你有什么想问想说的，欢迎在评论区留言。

## 参考

- [Solana Docs: Core Fees](https://solana.com/docs/core/fees)
- [Solana Docs: Core Accounts](https://solana.com/docs/core/accounts)
- [Solana Docs: Core Transactions](https://solana.com/docs/core/transactions)
- [Solana Docs: Program-Derived Addresses](https://solana.com/developers/courses/native-onchain-development/program-derived-addresses)
- [Solana Docs: Program Limitations](https://solana.com/docs/programs/limitations)
- [Solana Docs: Program FAQ](https://solana.com/docs/programs/faq)
- [Solana Developers: Program Examples](https://github.com/solana-developers/program-examples)
- [Anchor Lang](https://www.anchor-lang.com/)
- [Solana Developers: How to Optimize Compute](https://solana.com/developers/guides/advanced/how-to-optimize-compute)

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
