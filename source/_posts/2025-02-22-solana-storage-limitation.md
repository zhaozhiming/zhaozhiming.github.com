---
layout: post
title: 深入解密 Solana 开发中的资源限制——存储篇
date: 2025-02-22 17:04:40
description: 深入浅出地介绍 Solana 开发中的账号存储限制。
keywords: web3, blockchain, solana, account, storage limitation
comments: true
categories: code
tags: [web3, blockchain, solana, account, storage limitation]
---

{% img /images/post/2025/02/solana-limitation-storage.jpeg 400 300 %}

上一期我们介绍了 Solana 程序资源限制中的 Compute Unit（以下简称 CU ）部分，今天我们再来介绍 Solana 程序资源限制中的存储部分。Solana 的存储限制主要是指 Solana 账号中的存储限制，这个限制是为了保障 Solana 网络的稳定性和安全性，同时也是为了防止恶意程序占用过多的存储空间。在这个限制下，如何设计程序的数据结构，如何确保账户的数据不超过限制，这些都是 Solana 开发者关心的问题，我们今天将通过详细的讲解和一些实际案例分析来为大家揭开 Solana 存储限制的神秘面纱。

<!--more-->

## Solana 存储限制

在了解 [Solana](https://solana.com/) 存储限制之前，我们需要先来了解一下 Solana 的账号模型。

{% img /images/post/2025/02/solana-account-model.png 600 400 %}

在 Solana 区块链中，每个账户的数据结构被称为 `AccountInfo`，这个数据结构包含了以下字段：

- Data：账户数据
- Executable：是否为可执行程序
- Lamports：账户余额（以 lamports 为单位，1 SOL 等于 10 亿 lamports）
- Owner：账户所有者程序

账户根据 `Executable` 字段可以分为两种类型：**程序账户**和**数据账户**。程序账户用来保存运行在 Solana 网络上的程序（也称为智能合约），它是无状态的，仅存储执行代码。而程序相关的数据则需要保存在数据账户中，数据账户可以保存任意格式的数据。两者的内容都保存在 `Data` 字段中，程序账户保存的是程序的字节码，数据账户保存的是具体数据。

Solana 的存储限制是：每个账户的最大存储空间为 **10MB**，这里的存储空间指的就是 `Data` 字段的大小，保存到账户的数据单位一般是字节，所以 10MB 等于 10 x 1024 x 1024 字节。

## 存储大小查询

既然我们已经了解了 Solana 的存储限制，那么如何确定账户实际占用了多少存储空间呢？实际上，Solana 提供了多种方法来查询账户的存储大小，无论是在程序代码中还是在测试代码中，都可以轻松实现这一功能。

> Solana 的程序代码使用 Rust 语言编写，而测试代码一般使用 JavaScript 或 TypeScript 语言编写。
> Solana 的程序开发分为 Native 和 [Anchor](https://www.anchor-lang.com/docs) 两种方式，在下面的代码示例中，我们默认以 Anchor 为例进行说明。

如果你希望在 Solana 的程序中查询账户的存储大小，我们可以在 Rust 程序通过 `AccountInfo` 结构的 `data.len()` 方法来查询账户的存储大小，如下所示：

```rust
let account_size = ctx.accounts.your_account.to_account_info().data_len();
msg!("account size: {}", account_size);
```

- 其中 ctx 为 Anchor 的上下文对象，其中包含了当前程序的所有账户信息，`your_account` 为你的账户变量名。

另外比较常见的做法是在测试代码中查询账户的存储大小，我们可以通过 Solana 的 JavaScript SDK 来查询账户的存储大小，如下所示：

```ts
import { Connection,PublicKey } from "@solana/web3.js";

const connection: Connection = ...;
const yourAccount = new PublicKey("your_account_address");
const accountInfo = await connection.getAccountInfo(yourAccount);
console.log("account size:", accountInfo?.data.length);
```

- 其中 Connection 为 Solana SDK 的连接对象，`your_account_address` 为你的账户地址。

通过以上方法可以查询到账号的大小，单位为字节，比如我们得到的查询结果为：`account size: 1000`，那么这个账户的大小就是 1000 字节。

## 存储大小计算

现在我们知道如何查询账户的存储大小了，那么账户的存储大小究竟是如何计算的呢？Anchor 的官方文档中详细上列出了[基础数据类型的大小以及相应的计算方法](https://www.anchor-lang.com/docs/references/space#type-chart)，借助这些信息，我们就能准确地计算出账户实际占用的存储空间。

我们列举几个文档上的数据类型的大小来做示例说明：

- `bool`：1 字节
- `u8/i8`：1 字节
- `Vec<T>`：4 + (space(T) x amount)

其中 `bool` 和 `u8/i8` 类型的大小都是 1 字节，`Vec` 类型的大小是 4 字节加上泛型 `T` 类型的大小乘以 `Vec` 的长度，我们以具体的示例来说明 `Vec` 大小的计算方法：

```rust
#[account]
#[derive(InitSpace)]
pub struct MyStorage {
    #[max_len(10)]
    data: Vec<u8>,
}
```

在这个数据结构中，`data` 字段是一个 `Vec` 类型的数据，`Vec` 的长度最大为 10，`Vec` 中每个元素的类型是 `u8`，那么这个数据结构的大小就是 4 + 1 x 10 = 14 字节。

由于 `MyStorage` 结构体采用了 `#[derive(InitSpace)]` 宏，在 Solana 的 Anchor 框架中，该宏能够自动计算并初始化账户所需的存储空间，因此我们可以直接通过 `MyStorage::INIT_SPACE` 方法来获取该数据结构的大小。

## 存储费用查询

现在我们已经知道了如何计算账户大小，接下来让我们来探讨在 Solana 区块链上存储数据所需支付的费用。实际上每个账户都必须为其存储的数据支付一定的费用，那么我们要如何得知账户所需的费用呢？实际上 Solana 提供了一个命令行工具，可以通过输入字节数就能计算出相应的存储费用，工具的使用示例如下：

```bash
solana rent 100

# 输出结果
Rent-exempt minimum: 0.00158688 SOL
```

这里表示存储 100 字节的数据需要支付 0.00158688 SOL 的费用，可能有朋友会比较好奇，如果一个 10MB 的账号需要多少费用，我们来看一下：

```bash
solana rent 10485760

# 输出结果
Rent-exempt minimum: 72.98178048 SOL
```

如果我们存储一个 10MB 的账户，字节数为 10 x 1024 x1024 = 10485760，则需要支付 72.98178048 SOL 的费用，截至目前为止（2025-02），1 SOL 约等于 196 美元，所以存储一个 10MB 的账户大约需要支付 14330.5 美元的费用，这是一笔非常昂贵的开销。因此，在设计账户时，应谨慎评估哪些数据必须保存在链上，哪些数据可以存储在其他地方，以便有效地控制成本。

我们再来看一个例子，当我们输入的字节数为 0 时，我们会发现输出结果并不是 0 SOL：

```bash
solana rent 0

# 输出结果
Rent-exempt minimum: 0.00089088 SOL
```

这是因为 Solana 区块链中的账户有一个最小存储开销，即使账户中没有数据，也需要支付一定的费用。

## 存储费用计算

在实际的开发中，我们不需要手动计算账户的存储费用，Solana 可以根据账户的大小自动计算出存储费用。但我们还是有必要了解一下 Solana 的存储费用计算方法，这样我们可以更好地理解账户的存储费用。

在 Solana 的源码定义了一些常量和方法来计算账户的存储费用，我们可以通过查看[这些源码](https://github.com/anza-xyz/agave/blob/v2.1.13/sdk/rent/src/lib.rs#L93-L97)来了解存储费用的计算方法，源码内容如下：

```rust
pub const DEFAULT_LAMPORTS_PER_BYTE_YEAR: u64 = 1_000_000_000 / 100 * 365 / (1024 * 1024);
pub const DEFAULT_EXEMPTION_THRESHOLD: f64 = 2.0;
pub const ACCOUNT_STORAGE_OVERHEAD: u64 = 128;

pub fn minimum_balance(&self, data_len: usize) -> u64 {
    let bytes = data_len as u64;
    (((ACCOUNT_STORAGE_OVERHEAD + bytes) * self.lamports_per_byte_year) as f64
        * self.exemption_threshold) as u64
}
```

- DEFAULT_EXEMPTION_THRESHOLD：默认豁免时间点，默认是 2.0 年
- DEFAULT_LAMPORTS_PER_BYTE_YEAR：默认每字节每年度费用，默认是 3480 lamports
- ACCOUNT_STORAGE_OVERHEAD：账户最小开销，默认是 128 字节，这里就是我们之前查询的 0 字节账户时的 最小字节开销
- 费用的计算公式为：`((ACCOUNT_STORAGE_OVERHEAD + 账户存储大小) x DEFAULT_LAMPORTS_PER_BYTE_YEAR) x DEFAULT_EXEMPTION_THRESHOLD`

以 0 字节账号为例，当账户存储大小为 0 时，存储费用为：`((128 + 0) x 3480) x 2.0 = 890880 lamports`，转换为 SOL 即为 0.00089088 SOL，结果和我们之前查询的一致。

### 租借机制

在存储费用计算中，有一个常量引起了我们的注意，比如 `DEFAULT_EXEMPTION_THRESHOLD`，表示默认的豁免时间点，默认值为 2.0 年，为什么会设计这个常量呢？这还要从 Solana 的租借机制说起。

还记得之前介绍过的账户结构 `AccountInfo` 吗？在这个结构中，大家通常会注意到 `Data`、`Executable` 等主要字段，它们分别存储了账户的数据和是否为可执行程序等信息。但实际上，`AccountInfo` 内部还包含了一个历史遗留字段，名为 `rent_epoch`。这个字段用于记录账户开始进入租借状态的 epoch 值，即标志着账户租金计算的起始时间。最初设计时，Solana 希望借助这个字段来追踪账户的租金缴纳情况和剩余租金状态，确保系统能够及时回收那些长时间未活跃且租金不足的账户。尽管如今系统的租金扣费方式已经发生了较大变化，`rent_epoch` 依然保留在账户结构中，以便兼容历史数据和为需要查询账户租借信息的应用程序提供参考。

最初的 Solana 系统中采用的是按年计费的租金机制，也就是说，账户需要定期支付租金来维持其在链上的存在。如果用户只预付了半年的租金，那么系统会在每个新的 epoch 开始时，从账户余额中扣除相应的租金金额。这样一来，经过大约六个月后，账户余额会逐步减少至零，届时系统就会自动删除该账户，认为它已不再满足持续存在的条件。相反，如果用户选择预付**两年**的租金，那么该账户就会获得一个特殊状态，被称为**免租账户**，从而在之后的周期中不再需要继续扣除租金。这样一来，账户可以长期稳定地存在于链上，而不必担心因余额耗尽而被清理。

随着技术的不断发展和生态系统的成熟，Solana 对租金机制进行了优化和调整，旧有的定期扣费方式已经被全面废弃。现在，所有新创建的账户必须在初始化时至少预存两年租金，以确保账户有足够的余额来支持长时间的存续。这种机制不仅简化了租金计算和管理流程，也大大提高了系统资源的利用效率。更重要的是，当用户决定关闭账户时，系统会全额退还预存的租金押金，从而避免了资金的长期占用和不必要的经济负担。这种改进使得账户管理变得更加透明、简单，并且更好地保护了用户的资产权益。

## 账户重分配大小限制

虽然 Solana 的账户最大存储大小为 10MB，但是在实际创建账户时，我们无法一下子创建一个 10MB 的账户，下面是创建账户的代码示例：

```rust
use anchor_lang::prelude::*;

#[account]
#[derive(InitSpace)]
pub struct MyStorage {
    #[max_len(10230)]
    data: Vec<u8>,
}

#[derive(Accounts)]
pub struct Init<'info> {
    #[account(mut)]
    pub user: Signer<'info>,

    #[account(
        init,
        payer = user,
        space = 8 + MyStorage::INIT_SPACE,
        seeds = [b"my_storage"],
        bump
    )]
    pub my_storage: Box<Account<'info, MyStorage>>,

    pub system_program: Program<'info, System>,
}

pub fn init(_ctx: Context<Init>) -> Result<()> {
    Ok(())
}
```

- 我们首先定义了一个数据结构 `MyStorage`，根据之前的计算方法，这个数据结构的大小为 4 + 1 x 10230 = 10234 字节
- 在 `Init` 结构中，我们定义了一个 `my_storage` 账户，这个账户的大小为 8 字节加上 `MyStorage` 的大小，即 8 + 10234 = 10242 字节

当运行这个程序，程序在初始化 `my_storage` 账号时，会报一个 `Account data size realloc limited to 10240 in inner instructions` 的错误，这是因为 Anchor 通过 [Cross Program Invocation（CPI）](https://solana.com/docs/core/cpi)作为内部指令来为账户分配存储空间，每个内部指令允许的最大重新分配空间为 10240 字节，也就是 **10KB**，但我们的账户大小为 10242 字节，已经超过了这个限制，所以会报错。

这个限制的常量名为 `MAX_PERMITTED_DATA_INCREASE`，我们可以通过[这里](https://docs.rs/solana-account-info/2.2.0/src/solana_account_info/lib.rs.html#17)来查看相关代码。

### 增加账户存储大小

初始创建的账户大小最大只有 10240 字节，那么我们该如何扩展其存储容量呢？我们可以通过重新分配账户存储空间来增加账户的存储大小，下面是扩展账户存储大小的代码示例：

```rust
#[derive(Accounts)]
pub struct IncreaseAccountSize<'info> {
    #[account(mut)]
    pub user: Signer<'info>,

    #[account(mut,
              realloc = my_storage.to_account_info().data_len() + 10240,
              realloc::payer = user,
              realloc::zero = false,
              seeds = [b"my_storage"],
              bump)]
    pub my_storage: Account<'info, MyStorage>,

    pub system_program: Program<'info, System>,
}

pub fn increase_account_size(_ctx: Context<IncreaseAccountSize>) -> Result<()> {
    Ok(())
}
```

- 在 `IncreaseAccountSize` 结构体中，我们使用[程序派生地址（PDA）](https://solana.com/docs/core/pda)得到之前创建的 `my_storage` 账户，其重新分配的大小是在当前账户大小上再增加 10240 字节
- `realloc::zero` 属性用于控制在重新分配账户存储空间时，是否将新分配的空间初始化为零，false 表示不会将新分配的空间初始化为零

这里我们在账户**原有的基础**上增加了 10240 字节的存储空间，这个大小刚好满足 CPI 最大重新分配空间的限制，因此程序可以正常执行。但如果增加的大小超过 10240 字节，那么程序会报之前同样的错误。也就是说，每次扩展账户存储空间的大小不能超过 10240 字节，如果需要增加更多的存储空间，可以多次执行这个操作。每次为账户增加 10240 字节的存储空间，需要执行 1024 次操作才能达到 10MB 的上限。

此外，由于账户存储空间会产生费用，请务必确保账户余额充足以覆盖新增存储空间的费用。

## 总结

通过本文的介绍，我们了解到 Solana 的存储限制包括：

- 账户的最大存储空间为 **10MB**
- 创建或者重分配账户存储空间的限制为 **10KB**

我们还介绍了如何查询和计算账户的存储大小与存储费用，以及账户的租借机制等内容。在实际开发中，我们需要根据账户的存储需求合理设计数据结构，确保账户存储空间不超限，同时合理控制存储费用，从而提升程序的性能和效率。希望本文能够帮助大家更好地理解 Solana 的存储限制，为 Solana 开发提供一些参考。

## 参考

- [Solana Docs: Core Accounts](https://solana.com/docs/core/accounts)
- [Cost of storage, maximum storage size, and account resizing in Solana](https://www.rareskills.io/post/solana-account-rent)
- [Account Space](https://www.anchor-lang.com/docs/references/space)

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
