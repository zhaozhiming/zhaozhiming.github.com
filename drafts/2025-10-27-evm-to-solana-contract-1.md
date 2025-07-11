---
layout: post
title: 如何将以太坊协议迁移到 Solana ——合约篇（一）
date: 2025-10-27 22:30:00
description: 重点介绍以太坊到 Solana 合约开发中的核心思维转变和最佳实践
keywords: Solana, Ethereum, Smart Contract, Solidity, Anchor
comments: true
categories: code
tags: [Solana, Ethereum, Smart Contract, Solidity, Anchor]
---

## 文章导读

随着 Solana 生态的不断成熟，越来越多的以太坊（EVM）项目开始考虑将自身的协议迁移至 Solana，以获得更高的性能、更低的交易成本和更好的用户体验。我们公司在这一领域拥有丰富的项目经验，曾主导多个以太坊协议的迁移与重构工作，涵盖多种协议类型。我们深知迁移过程中在合约架构、数据模型、交易逻辑以及前后端协同上的复杂性，也积累了一套系统化的迁移方法论与最佳实践。

为了帮助开发者系统掌握从以太坊到 Solana 的迁移方法与实践，我们将推出一系列专题文章，围绕智能合约层、后端服务层与前端交互层三个核心方向展开，分享在实际项目中积累的经验总结、关键注意事项、最佳实践，以及迁移过程中遇到的典型问题，并通过真实案例与示例代码，展示完整的迁移思路与实现路径。
通过这一系列文章，我们希望协助开发者在完成迁移的同时，充分挖掘 Solana 的高性能潜力与独特机制，重塑协议设计，打造原生于 Solana 的创新型协议。

#### 文章导航地址

- 如何将以太坊协议迁移到 Solana ——前导篇：系统介绍了以太坊与 Solana 在账户模型、执行机制和费用体系上的根本差异。
- 如何将以太坊协议迁移到 Solana ——合约篇（一）：重点介绍以太坊到 Solana 合约开发中的核心思维转变和最佳实践。

话不多说，正文开始！

---

随着 Solana 生态系统的迅速崛起和日益成熟，其高性能、低成本的特性吸引了大量开发者和用户的关注。与此同时，以太坊（Ethereum）及其兼容链虽然拥有庞大的生态系统，但长期面临着可扩展性不足、交易费用高昂等挑战。因此，越来越多的 Web3 开发者开始将目光投向 Solana，寻求更优的开发和用户体验，从以太坊到 Solana 的技术迁移已成为一个显著趋势。那么我们该如何将熟练编写的以太坊智能合约，有效地迁移到 Solana 平台上呢？许多开发者最初认为这只是将 Solidity 换成 Rust 的语言学习，但很快会发现，真正的挑战在于底层架构的根本性差异。本文的目标是帮助有经验的以太坊开发者完成一次关键的思维模式转变，最终能将现有的合约逻辑，以 Solana 的方式高效、安全地重新实现。

<!--more-->

## 核心思维转变

从以太坊迁移到 Solana，首要任务是完成一次关键的思维模式转变，如果你还未阅读《如何将以太坊协议迁移到 Solana ——前导篇》，建议先从那篇开始，它系统介绍了以太坊与 Solana 在账户模型、执行机制和费用体系上的根本差异。这里将不再重复那些概念性内容，而是聚焦在合约开发中必须重新理解的三件事：账户与程序的关系、Token 账户与跨程序调用 (CPI)、显式依赖的调用方式，这些部分构成了 Solana 智能合约开发的核心心智模型。

为了让这些概念更具体，本文的大部分代码示例都将引用一个完整的、开源的质押（Staking）迁移项目。我们将在文章的最后一部分详细拆解这个项目，但在此之前，我们会频繁地使用它的代码片段来佐证我们讨论的每一个核心概念。

### 账户模型

从以太坊迁移到 Solana，首先要颠覆的就是对 Solana 中**账户**的认知。

在以太坊及其兼容链上，智能合约的设计遵循一种整体化的模式，一个合约既是代码的载体，也是状态的容器，将处理逻辑和数据存储融为一体。以标准的 ERC20 代币合约为例，它内部不仅包含了 `transfer` 和 `approve` 等功能函数，还内置了一个 `mapping` 数据结构来记录所有用户的余额。这种设计类似于面向对象编程中的类实例（Object），所有相关的方法和数据都被封装在同一个实体中，形成了一个完整且独立的功能单元。让我们来看一个具体的 Solidity 代码示例：

```solidity
// evm-staking/src/Staking.sol
contract Staking {
    // Staking and reward tokens are state variables
    IERC20 public stakingToken;
    IERC20 public rewardToken;

    // User stake information is stored in a mapping
    mapping(address => StakeInfo) public stakes;
    uint256 public totalStaked;

    struct StakeInfo {
        uint256 amount;
        int256 rewardDebt;
        uint256 claimed;
    }

    // ... business logic like stake() and unstake()
}
```

在这个例子中，`stakingToken`、`rewardToken` 地址以及所有用户的质押数据 `stakes` 都直接存储在 `Staking` 合约的内部状态中。

Solana 则采用了完全不同的架构，其核心思想是**将代码与数据彻底分离**，在 Solana 上，**程序（Program）**只包含逻辑，它们是无状态的，本身不存储任何业务数据，而**账户（Account）**则专门用来存储数据。当执行一笔交易时，你必须明确地告诉程序需要去操作哪些账户。程序就像是一个可执行文件，而账户则是它需要读写的数据文件，两者是分开存放和管理的。

下面是对应的 Solana (Anchor) 实现：

```rust
use anchor_lang::prelude::*;

// The program itself is stateless.
#[program]
pub mod staking {
    pub fn stake(ctx: Context<Stake>, amount: u64) -> Result<()> {
        // Business logic operates on accounts passed via the context.
        // The context `ctx` contains all necessary accounts,
        // such as `GlobalState` and `UserStakeInfo`, defined in the `Stake` struct below.
        let state = &mut ctx.accounts.state;
        let user_info = &mut ctx.accounts.user_stake_info;
        state.total_staked += amount;
        user_info.amount += amount;
        // ...
        Ok(())
    }
    // ... other instructions like unstake()
}

// The `Stake` context struct, defining all accounts for the `stake` instruction.
#[derive(Accounts)]
pub struct Stake<'info> {
    #[account(mut)]
    pub state: Account<'info, GlobalState>,
    #[account(mut)]
    pub user_stake_info: Account<'info, UserStakeInfo>,
    // ... other necessary accounts
}

// State is defined in separate account structs.
#[account]
pub struct GlobalState {
    pub total_staked: u64,
    // ... other global state
}

#[account]
pub struct UserStakeInfo {
    pub amount: u64,
    // ... other user state
}
```

在这里，`staking` 程序是无状态的，它不持有任何数据，所有的数据，无论是全局的 `GlobalState` 还是每个用户的 `UserStakeInfo`，都定义在独立的 `#[account]` 结构体中，程序通过 `Context` 对象（其类型由 `Stake` 结构体定义）接收这些账户作为输入，然后对它们进行操作。

这种设计的根本目的在于实现大规模的[并行处理](https://medium.com/solana-labs/sealevel-parallel-processing-thousands-of-smart-contracts-d814b378192)，因为代码与数据是分离的，Solana 的交易在执行前就明确声明了它将要访问的所有账户，并指明了每个账户是只读还是可写，这使得 Solana 运行时能够构建一个依赖关系图，从而高效地调度交易。如果两笔交易操作的是完全不相关的账户，或者它们都只是读取同一个账户，那么这两笔交易就可以安全地并行执行，只有当一笔交易需要写入某个账户时，其他任何需要访问（无论是读还是写）该账户的交易才会被暂时阻塞并顺序执行。通过这种精细化的调度，Solana 能够最大限度地利用多核处理器，同时执行大量互不干扰的交易，这正是其能够实现高吞吐量和低延迟的关键所在。

### Token 标准

账户模型的差异，最直接地体现在代币标准上。我们可以通过我们的质押合约例子，来观察它在两个平台上的实现有何区别。在以太坊的 ERC20 标准中，代币的状态由一个中心化的合约管理，应用合约通过调用这个代币合约的函数来间接操作用户的余额。

```solidity
// evm-staking/src/Staking.sol
contract Staking {
    IERC20 public stakingToken;
    // ...

    function stake(uint256 amount) external {
        // The Staking contract calls the token contract's transferFrom
        // to move funds from the user to itself.
        stakingToken.transferFrom(msg.sender, address(this), amount);
        // ...
    }
}
```

在这个模型中，用户的代币余额实际上是 `stakingToken` 代币合约内部一个 `mapping` 中的一条记录，`transfer` 操作就是修改这个 `mapping` 中的两个值。

而在 Solana 的 SPL Token 标准中，情况则完全不同。首先，有一个所有代币都共享的、官方的 `Token Program`，用户的代币不存储在中心化的合约里，而是存放在用户自己拥有的、独立的**代币账户（Token Account）**中，我们的质押程序要操作代币，就必须接收这些代币账户作为输入。

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
// The context for the stake instruction requires specific token accounts.
#[derive(Accounts)]
pub struct Stake<'info> {
    #[account(mut)]
    pub user_token_account: Account<'info, TokenAccount>,
    #[account(mut)]
    pub staking_vault: Account<'info, TokenAccount>,
    pub token_program: Program<'info, Token>,
    // ...
}

// The instruction itself commands the Token Program to perform the transfer.
pub fn stake(ctx: Context<Stake>, amount: u64) -> Result<()> {
    token::transfer(
        CpiContext::new(
            ctx.accounts.token_program.to_account_info(),
            Transfer {
                from: ctx.accounts.user_token_account.to_account_info(),
                to: ctx.accounts.staking_vault.to_account_info(),
                authority: ctx.accounts.user.to_account_info(),
            }
        ),
        amount
    )?;
    // ...
    Ok(())
}
```

这里的质押操作，是通过跨程序调用（CPI）来调用官方 `Token Program` 的 `transfer` 指令，在用户的代币账户和程序的金库代币账户之间进行一次转账，这清晰地展示了数据（代币账户）和逻辑（Token Program）的分离。

这个根本性的差异也解释了为什么新用户初次接触 Solana 时会感到困惑：为什么我接收一个新币种前，钱包总要提示我先创建账户？原因就在于此，在 Solana 上接收 USDC 前，你的钱包必须先为你创建一个用于存放 USDC 的代币账户，这个操作相当是在为你准备一个专门存放该代币的小金库。而在以太坊链上，你的钱包地址可以直接接收任何 ERC20 代币，因为所谓的**接收**仅仅是某个代币合约内部账本上多了一条记录而已，你本身并不需要做任何准备。

### 合约调用

账户模型的不同，直接导致了与合约（程序）交互方式的巨大差异。在 Solidity 中，当我们调用一个函数时，以太坊虚拟机（EVM）会在背后为我们准备好一系列上下文信息，最典型的就是 `msg.sender`，开发者无需在函数参数中指明是谁在调用，EVM 会自动处理，这种隐式上下文让函数调用看起来非常简洁。

**以太坊调用示例 (Foundry Test)**

```solidity
// evm-staking/test/Staking.t.sol
function testStake() public {
    uint256 stakeAmount = 1000 * 10 ** 18;

    // The user's address is implicitly passed as msg.sender
    vm.startPrank(user1);
    myToken.approve(address(staking), stakeAmount);
    staking.stake(stakeAmount);
    vm.stopPrank();

    (uint256 stakedAmount, ,) = staking.getStakeInfo(user1);
    assertEq(stakedAmount, stakeAmount);
}
```

在上面的 Foundry 测试中，`vm.startPrank(user1)` 设定了后续调用的 `msg.sender` 为 `user1`。调用 `staking.stake(stakeAmount)` 时，我们只需要传入业务所需的 `amount` 参数即可。

而在 Solana 中，程序对外界一无所知，它需要的所有信息：包括谁是调用者、它需要读取或写入的账户等等，都必须由调用方在交易指令中明确地、完整地提供，这些账户被打包在一个称为 `Context` 的对象中，作为参数传递给程序。这种设计的背后逻辑与并行处理一脉相承：交易在执行前必须列出所有会涉及的账户，这样运行时才能判断它是否可以跟其他交易并行执行。

**Solana 调用示例 (TypeScript Test)**

```typescript
// solana-staking/tests/solana-staking.test.ts
// Helper function to simplify the call
async function stakeTokens(
  user: Keypair,
  userSigner: any,
  stakingToken: PublicKey,
  rewardToken: PublicKey,
  amount: bigint
) {
  const userStakePda = getUserStakePda(statePda, user.publicKey);

  // All required accounts must be explicitly passed.
  const stakeInstruction = programClient.getStakeInstruction({
    user: userSigner,
    state: address(statePda.toBase58()),
    userStakeInfo: address(userStakePda.toBase58()),
    userTokenAccount: address(stakingToken.toBase58()),
    stakingVault: address(stakingVaultPda.toBase58()),
    // ... and other accounts
    amount: amount,
  });

  return await sendTransaction(provider, stakeInstruction, user);
}
```

在这个 TypeScript 测试中，调用 `stake` 指令需要一个庞大的账户对象，包括 `user`（签名者）、`state`（全局状态账户）、`userStakeInfo`（用户质押数据账户）、`userTokenAccount`（用户的代币账户）、`stakingVault`（程序的金库账户）等等。这种设计虽然让客户端调用看起来更繁琐，但它带来了极高的透明度和安全性。在发送交易前，客户端代码就清晰地定义了该交易将与哪些账户进行交互，没有任何隐藏的上下文依赖。

另外，以太坊合约升级后，往往需要修改客户端代码，让客户端调用新的合约地址，但在 Solana 中，程序的升级只需要将新的程序代码部署到原有的程序 ID 地址上，实现无缝升级。在整个过程中，所有存储着业务数据的账户都保持原样，完全不受影响，因为它们与程序逻辑是解耦的。这对于客户端调用来说，由于程序地址没有改变，客户端代码无需修改，完全兼容新的程序代码。

如果你想从架构层面更深入地理解本文中的代码设计思路，建议回顾《如何将以太坊协议迁移到 Solana ——前导篇》中内容，它提供了更系统的概念背景。

## 开发工具链对比

仅仅理解了思维模式的转变还不够，要将这些新的设计理念付诸实践，我们还必须熟悉一套与之配套的、全新的开发工具链。从开发语言到标准库，Solana 的生态系统与你所熟悉的以太坊环境有显著差异，下表总结了两个生态在关键工具链上的差异，帮助你快速建立新的认知地图。

| **领域**     | **以太坊生态**                     | **Solana 生态**                         | **关键备注**                                                                                                                                                                                                                                                                 |
| :----------- | :--------------------------------- | :-------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **开发框架** | Hardhat / Foundry（Solidity）      | Anchor（Rust）                          | 在以太坊生态中，Hardhat 和 Foundry 是两款被广泛使用的智能合约开发工具。而 Anchor 是 Solana 开发的事实标准，它通过强大的宏功能极大地简化了 Solana 合约开发的复杂性。                                                                                                          |
| **接口标准** | ABI (Application Binary Interface) | IDL (Interface Definition Language)     | Anchor 会根据你的程序代码自动生成 IDL，这与以太坊上的 ABI 概念类似——ABI 是以太坊定义的合约交互标准，而 Solidity 编译器会自动生成 ABI 文件，用于描述合约函数、参数和返回值的二进制编码方式。客户端通过这些 IDL 或 ABI 文件即可知道如何与你的程序交互，而无需关心底层实现      |
| **标准库**   | OpenZeppelin                       | SPL (Solana Program Library)            | OpenZeppelin 是可供你导入和继承的代码库，而 SPL 是一组已经部署在链上的、可重用的标准程序。你需要通过跨程序调用（CPI）来与它们交互，而不是将代码复制到你的项目中。                                                                                                            |
| **合约验证** | 在 Etherscan 上传并验证源码        | 提交源码进行可验证构建 (Verified Build) | Solana 支持“可验证构建”，模式上与以太坊类似。开发者提交源代码，由构建服务在确定性环境中编译，并比对编译产物与链上字节码的哈希。这确保了源码与链上程序的一致性，而不仅是验证 IDL 接口。                                                                                       |
| **网络 RPC** | Infura, Alchemy, QuickNode         | Helius, Alchemy, QuickNode              | 两大生态在 RPC 基础设施上都有顶级服务商，其中只有少数（如 QuickNode）同时支持多链；而 Solana 的高吞吐量特性则催生了像 Helius 这样专注 Solana 的增强型 API 服务商。                                                                                                           |
| **浏览器**   | Etherscan, Blockscout              | Solscan, Solana Explorer, X-Ray         | 以太坊生态拥有 Tenderly 这样的强大工具，可以对交易进行深入的模拟和调试。在 Solana 生态中，Helius (其产品 X-Ray) 等工具提供了类似的功能。由于 Solana 的并行交易模型，这些工具更侧重于清晰地可视化账户间的价值流动和 CPI（跨程序调用）调用链，以帮助开发者理解复杂的指令交互。 |

从这个对比中，我们可以看到一个清晰的模式：以太坊开发更像是继承和扩展（例如，继承 OpenZeppelin 的合约），而 Solana 开发则更侧重于组合与交互（通过 CPI 与链上已有的 SPL 程序互动）。

对于刚接触 Solana 的开发者来说，最重要的一点是：尽量使用 Anchor 框架。它与你熟悉的以太坊开发框架不尽相同，Hardhat/Foundry 更多是解决了合约开发流程中的外部问题（如测试、部署、脚本化等），而 Anchor 则深入到合约内部，通过其宏系统和约束，极大地简化了合约本身的编写复杂度，为你处理了大量繁琐且易错的底层安全校验和数据序列化工作。掌握了 Anchor，你就掌握了在 Solana 上高效、安全地实现业务逻辑的关键。

## Solana 开发最佳实践

了解了 Solana 在开发工具链上的差异后，我们还需要掌握如何正确地运用这些工具，以符合 Solana 的设计哲学，仅仅知道用 Rust 替代 Solidity 是不够的，真正的效率和安全性来自于遵循 Solana 生态中沉淀下来的最佳实践。下面我们来看一些具体的开发实践。

### 拥抱 Anchor 框架

在 Solana 上进行合约开发，主要有两种方式：原生（Native）开发与使用 Anchor 框架。

原生开发要求开发者直接与 Solana 的底层库交互，这意味着你需要手动处理账户数据的序列化与反序列化，并自行编写大量代码来验证账户所有权、签名者权限以及账户可变性等，这种方式虽然提供了最大的灵活性，但开发过程复杂、代码冗长，且容易引入安全漏洞。

因此，Solana 官方也推荐开发者（尤其是从以太坊生态迁移过来的开发者）优先使用 Anchor 框架，Anchor 的核心目标是简化开发流程并提升安全性，它通过 Rust 的宏（Macros）系统，将原生开发中的复杂环节自动化。

我们来看一个简单的 `initialize` 指令，该指令用于创建一个新的全局状态账户。

**使用 Anchor 框架的实现：**

在 Anchor 中，你只需要通过声明式的宏来定义账户和约束，框架会自动处理所有验证和初始化逻辑。

```rust
// solana-staking/programs/solana-staking/src/instructions/initialize.rs
#[program]
pub mod staking {
    pub fn initialize_handler(ctx: Context<Initialize>, reward_per_second: u64) -> Result<()> {
        // Business logic is clean and focused.
        let state = &mut ctx.accounts.state;
        state.reward_per_second = reward_per_second;
        state.admin = ctx.accounts.admin.key();
        // ...
        Ok(())
    }
}

// Define accounts and constraints declaratively.
#[derive(Accounts)]
pub struct Initialize<'info> {
    #[account(mut)]
    pub admin: Signer<'info>,
    // Anchor handles the creation and rent payment for this account.
    #[account(init, payer = admin, space = 8 + GlobalState::INIT_SPACE)]
    pub state: Account<'info, GlobalState>,
    pub system_program: Program<'info, System>,
}

#[account]
pub struct GlobalState {
    pub admin: Pubkey,
    pub reward_per_second: u64,
    // ...
}
```

**使用原生方式的实现：**

如果用原生方式实现相同功能，开发者必须手动处理所有细节，代码如下所示：

```rust
// A functionally equivalent implementation in native Rust.
use solana_program::{
    account_info::{next_account_info, AccountInfo},
    entrypoint,
    entrypoint::ProgramResult,
    pubkey::Pubkey,
};
use borsh::{BorshDeserialize, BorshSerialize};

// The single entrypoint for the program.
entrypoint!(process_instruction);

pub fn process_instruction(
    program_id: &Pubkey,
    accounts: &[AccountInfo],
    instruction_data: &[u8],
) -> ProgramResult {
    // 1. Manually parse and validate accounts from the raw slice.
    let accounts_iter = &mut accounts.iter();
    let state_account = next_account_info(accounts_iter)?;
    let authority = next_account_info(accounts_iter)?;
    // ... must manually check that authority is a signer, state is writable, etc.

    // 2. Manually create the state account via a Cross-Program Invocation (CPI).
    //    This requires calculating space, rent, and building the instruction.
    //    (This is a complex, multi-line operation in real code).

    // 3. Manually deserialize instruction data to get the `reward_rate`.
    let reward_rate = u64::try_from_slice(&instruction_data)?;

    // 4. Manually serialize the new state and write to the account's data buffer.
    let state = State { authority: *authority.key, reward_rate };
    state.serialize(&mut &mut state_account.data.borrow_mut()[..])?;

    Ok(())
}
```

通过对比可以清晰地看到，Anchor 将原生开发中大量手动、易错的代码模板（如账户校验、CPI 调用、数据序列化）抽象成了简洁、安全的宏，这使得开发者可以更专注于业务逻辑本身，显著提高了开发效率和代码的可读性。

当然，这种便利性并非没有代价，选择 Anchor 框架也意味着你需要接受它带来的一些额外开销。首先 Anchor 的抽象层，包括它自动插入的各种安全检查，会比高度优化的原生代码消耗更多的计算单元（CU），其次 Anchor 生成的字节码通常会比等效的原生程序要大，虽然 Solana 程序账户允许的最大数据容量高达 10 MB，但这仍是在设计复杂协议时需要注意的一点。虽然 Anchor 极大地简化了开发，但要成为一名优秀的 Solana 开发者，你最终仍需理解其宏和约束背后的原生 API 是如何工作的，因为完全依赖抽象而不理解底层，可能会在遇到复杂问题或进行深度优化时遇到障碍。

对于绝大多数项目而言，Anchor 带来的开发效率和安全性提升远超其性能开销，但如果你想精通 Solana 开发，依然需要对 Solana 的底层机制有更深入的理解。

### 账户生命周期与租金

在以太坊上，合约存储状态的成本在部署时一次性支付，且状态会永久存在，而 Solana 则引入了租金（Rent）机制来管理链上的存储。

什么是租金？在 Solana 上，所有账户（无论是数据账户还是程序账户）都需要支付租金来占用链上存储空间。这是一种持续性的成本，旨在防止区块链状态无限膨胀，并补偿验证者存储数据的成本。在实践中，为了避免账户因余额耗尽而被网络回收，标准做法是在创建账户时存入足以支付约两年租金的 SOL（以 lamports 计算），满足此条件的账户将获得**免租金**状态，其存储空间被视为永久保留。当账户被关闭时，这笔预存的 lamports 会被全额退还。

这种机制引入了**账户生命周期**的概念，开发者需要主动管理账户的创建、使用和销毁。许多著名的 Solana 项目都遵循这一模式，我们将以 [Mango Markets v4](https://github.com/blockworks-foundation/mango-v4) 为例来说明，它是一个基于 Solana 构建的、去中心化的交易、借贷和杠杆交易平台，其程序设计充分体现了 Solana 的账户生命周期管理。

**创建与初始化**

在 Mango v4 中，当一个用户首次开户时，程序就需要为该用户创建一个新的 `MangoAccount`，这个过程通过 `AccountCreate` 这个上下文结构体来定义，其中 `#[account(init, ...)]` 约束是处理账户创建和租金支付的核心。

```rust
#[derive(Accounts)]
pub struct AccountCreate<'info> {
    // ... other accounts
    #[account(
        init,
        seeds = [b"MangoAccount".as_ref(), group.key().as_ref(), owner.key().as_ref(), &account_num.to_le_bytes()],
        bump,
        payer = payer,
        space = MangoAccount::space(token_count, serum3_count, perp_count, perp_oo_count, 0),
    )]
    pub account: AccountLoader<'info, MangoAccountFixed>,
    pub owner: Signer<'info>,

    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}
```

- `init`：告诉 Anchor，account 这个账户需要被创建。
- `seeds = [...]` 和 `bump`：这些约束表明 MangoAccount 是一个 PDA，其地址由协议的 group、owner 和一个账户序号 account_num 共同派生。
- `payer = payer`：指定 payer 账户（通常也是 owner）作为支付方，它将为新账户支付创建时所需的免租金。
- `space = ...`：明确定义了需要分配的存储空间大小，这里通过一个函数动态计算得出。

更多详细信息请参考 [Mango Markets v4 的源码](https://github.com/blockworks-foundation/mango-v4/blob/dev/programs/mango-v4/src/accounts_ix/account_create.rs)。

**关闭与销毁**

当一个账户不再需要时（例如，用户决定清空并关闭其在 Mango 的交易账户），可以将其关闭以回收预存的租金，在 Mango v4 中，`AccountClose` 上下文结构体使用了 `close` 约束来实现这一功能。

```rust
#[derive(Accounts)]
pub struct AccountClose<'info> {
    // ... other accounts
    #[account(
        mut,
        has_one = group,
        has_one = owner,
        constraint = account.load()?.is_operational() @ MangoError::AccountIsFrozen,
        close = sol_destination
    )]
    pub account: AccountLoader<'info, MangoAccountFixed>,
    pub owner: Signer<'info>,

    #[account(mut)]
    pub sol_destination: UncheckedAccount<'info>,
}
```

- `close = sol_destination`：这个约束非常关键，它告诉 Anchor，在指令成功执行后，自动关闭 account，并将其持有的所有 lamports（即之前存入的免租金）退还给 sol_destination 账户，这种明确的创建和销毁模式，是 Solana 程序设计中资源管理的核心部分。

更多详细信息请参考 [Mango Markets v4 的源码](https://github.com/blockworks-foundation/mango-v4/blob/dev/programs/mango-v4/src/accounts_ix/account_close.rs)。

我们的 `solana-staking` 示例项目也完全遵循了这种账户生命周期管理模式，在 `initialize` 指令中创建了全局状态账户和金库账户，并在 `stake` 指令中通过 `init` 约束为首次质押的用户创建其个人信息账户，同样，在 `unstake` 指令中，如果用户取回了所有代币，程序也会通过 `close` 约束来销毁其个人信息账户并退还租金。具体实现可以参考我们的项目仓库：[solana-staking](https://github.com/57blocks/evm-to-solana/tree/main/contract/solana-staking)。

### 程序派生地址（PDA）

在管理账户生命周期时，程序派生地址（Program Derived Address, PDA）是一个至关重要的工具，也是 Solana 安全模型的核心。PDA 是一个由程序 ID 和一组种子（seeds）确定性地派生出来的地址，它没有对应的私钥，只有派生它的程序才能代表它进行签名。PDA 允许程序拥有和控制其他账户，这使得 PDA 可以作为安全的数据存储、代币金库（Vault）或权限中心。在指令中，Anchor 会使用传入的种子和 `bump`（一个用于确保地址不在椭圆曲线上的随机数）重新计算并验证 PDA 地址，这个过程是防止客户端传入伪造账户、确保程序安全的关键。通过将一个 PDA 设置为另一个账户的 `authority`，可以构建出复杂的、由程序逻辑控制的权限系统，这是 Solana 程序设计的常见模式。

让我们通过代码来看 PDA 是如何被定义和使用的，在我们的质押程序中，`UserStakeInfo` 账户就是一个 PDA，它为每个用户存储其个人的质押信息。

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
// The Stake context defines how the user_stake_info PDA is derived.
#[derive(Accounts)]
pub struct Stake<'info> {
    #[account(mut)]
    pub user: Signer<'info>,

    // The 'seeds' and 'bump' constraints define this as a PDA.
    #[account(
        init_if_needed,
        payer = user,
        space = 8 + UserStakeInfo::INIT_SPACE,
        seeds = [STAKE_SEED, state.key().as_ref(), user.key().as_ref()],
        bump
    )]
    pub user_stake_info: Box<Account<'info, UserStakeInfo>>,
    // ... other accounts
}

// The data structure for the PDA account.
#[account]
#[derive(InitSpace)]
pub struct UserStakeInfo {
    pub owner: Pubkey,
    pub amount: u64,
    pub reward_debt: i128,
    pub claimed: u64,
    pub bump: u8,
}
```

- `seeds = [STAKE_SEED, state.key().as_ref(), user.key().as_ref()]`：这行代码是定义 PDA 的核心。它告诉 Anchor，`user_stake_info` 账户的地址是由一个固定的常量 `STAKE_SEED`、全局状态账户的地址 `state.key()` 以及发起交易的用户公钥 `user.key()` 共同派生出来的。这确保了每个用户在同一个质押池中都有一个唯一的、可预测的 `UserStakeInfo` 账户地址。
- `bump`：Anchor 会自动找到一个 `bump` 值，并将其存储在 PDA 账户的数据中。在后续的指令中，Anchor 会使用这个存储的 `bump` 值来重新派生和验证地址，确保传入的 `user_stake_info` 账户是合法的，而不是由恶意用户伪造的。
- `init_if_needed`：这是一个方便的约束，它会在用户第一次质押时自动创建这个 PDA 账户，但 `init_if_needed` 在 Anchor 里是被特性开关保护的可选功能，主要因为它容易引入可重初始化（reinitialization）等安全隐患，所以应该尽量避免使用。

通过这种方式，程序为每个用户创建了一个由程序自己控制的、独一无二的数据存储账户，诠释了 PDA 的强大功能。

### 程序架构：倾向于单体程序

在以太坊生态中，开发者习惯于构建可组合的小合约，每个合约负责一个独立的功能，然而，在 Solana 上，更推荐采用一种不同的架构模式：将关联度高的业务逻辑整合进一个功能相对完整的单体程序中。

这种差异的根源有几个方面，首先是源于跨程序调用（Cross-Program Invocation, CPI）的复杂性，其次是以太坊在合约部署阶段对字节码大小设有上限（约 24,576 字节／即 24 KB），如果把过多逻辑堆在一个合约里，可能会因超出大小限制而无法部署。在以太坊中，一个合约调用另一个合约的函数相对直接，上下文（如 `msg.sender`）通常是隐式传递的。而在 Solana 中，每次 CPI 都要求调用方在代码中手动构建并传递一个完整的账户列表，这些账户是目标程序执行其指令所必需的。这个过程不仅繁琐，容易因遗漏或错序账户而出错，还会增加交易的整体大小和复杂性。让我们来看一次真实的 CPI 调用。下面的代码展示了我们的质押程序如何调用官方的 `Token Program` 来执行代币转账：

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
// Transfer staking tokens from user to vault
let cpi_accounts = Transfer {
    from: ctx.accounts.user_token_account.to_account_info(),
    to: ctx.accounts.staking_vault.to_account_info(),
    authority: ctx.accounts.user.to_account_info(),
};
let cpi_program = ctx.accounts.token_program.to_account_info();
let cpi_ctx = CpiContext::new(cpi_program, cpi_accounts);
token::transfer(cpi_ctx, amount)?;
```

要完成这次转账，我们的程序必须手动构建一个 `Transfer` 结构体，其中包含了 `from`（用户的代币账户）、`to`（程序的金库账户）和 `authority`（签名授权方）三个账户。然后，再将这个结构体与目标程序（`token_program`）本身一起打包成一个 `CpiContext`，最后才能执行 `token::transfer`。

现在，想象一下如果我们将质押逻辑拆分到多个程序中：比如，一个程序负责处理用户质押数据，另一个程序负责更新全局的 `total_staked` 计数。那么，一次简单的 `stake` 操作就可能需要两次甚至更多的 CPI 调用，每次调用都需要构建这样一套独立的、繁琐的账户上下文，这会极大地增加代码的复杂性、计算单元的消耗以及出错的概率。

因此，为了提升开发效率和代码可维护性，最佳实践是将一个应用或协议的核心功能（例如，一个 DeFi 协议的质押、借贷和奖励计算逻辑）实现在同一个程序内部。只有当需要与完全独立的、标准化的外部程序（如 SPL Token Program）交互时才使用 CPI。这种单体架构模式可以有效减少 CPI 的使用频率，简化客户端的调用逻辑，并使程序逻辑更内聚，易于审计和管理。

### 费用模型

Solana 的费用模型和以太坊在理念上有一定相似之处，但实现方式差异明显。以太坊采用统一的 Gas 概念，交易需要声明一个 Gas Limit，表示可消耗的上限，执行时实际消耗的 Gas Used 会与单价相乘，最终得到 Gas Cost。自 EIP-1559 之后，费用由 Base Fee（随网络拥堵自动调整）和 Priority Fee 组成，总费用就是 `Gas Used × (Base Fee + Priority Fee)`。

在 Solana 中，对应的执行成本则用计算单元（Compute Unit, CU）来衡量。每笔交易会有一个 CU 预算，超出预算交易就会直接失败，因此 CU Limit 与以太坊的 Gas Limit 有点类似。不过和以太坊不同的是，Solana 的基础交易费并不取决于消耗多少 CU，而是与交易的字节大小相关，签名数量越多，交易体积越大，相应的基础费用也会增加，这意味着基础费和计算复杂度关系不大。至于计算资源本身的优先级竞争，则通过优先费（Priority Fee）来体现。开发者可以使用 `ComputeBudgetProgram` 显式指定愿意为每百万 CU 支付多少 microLamports，从而让验证者更倾向于优先打包这笔交易，这一点和以太坊中设置 Gas Price 或 Priority Fee 的思路更为接近。

换句话说，Solana 的交易成本由三部分构成：一是与交易大小挂钩的基础费，二是由租金机制体现的存储成本，三是通过优先费表达的计算资源价格。基础费更多是**进场门票**，而计算资源的竞争主要体现在优先费上。

```js
import { ComputeBudgetProgram, Transaction } from "@solana/web3.js";

const transaction = new Transaction();

// Request a specific compute unit limit
transaction.add(
  ComputeBudgetProgram.setComputeUnitLimit({
    units: 400_000,
  })
);

// Set a priority fee
transaction.add(
  ComputeBudgetProgram.setComputeUnitPrice({
    microLamports: 100_000,
  })
);

// ... add your main instruction here
// transaction.add(myInstruction);
```

### 合约升级

合约升级往往是项目发展的重要环节，以太坊和 Solana 提供了截然不同但各具优势的解决方案。

在以太坊的早期，升级智能合约确实是一个复杂且高风险的操作，由于代码和数据紧密耦合在同一个合约地址中，想要升级功能通常意味着要部署全新合约并完成数据迁移，这不仅流程复杂，还容易出现风险。为此，以太坊社区发展出了成熟的代理（Proxy）模式，在这种模式下，所有数据都存储在一个稳定的代理合约中，而可替换的逻辑合约则通过指针方式调用，这样一来，合约逻辑升级时用户可以在不改变代理合约地址的情况下，指向一个新的逻辑实现，从而实现了无缝切换，这已成为业界事实标准。

Solana 在设计上则更加简洁优雅，部署的程序本身和状态存储被天然分离，开发者只要将新的 BPF 字节码重新部署到原有的 program ID，就能实现程序升级，同时原有的状态账户（程序之外）保持完整、不受影响，这种方式避开了数据迁移的麻烦，大幅降低了操作复杂度和风险。然而，这种机制也带来了新的挑战，状态账户的结构一旦确定，后续无法自动扩展其大小。如果你之后想要为某个状态账户添加新的字段，但原先账户是在更小的大小上分配的，就会发生数据错位或读取错误的问题。建议在定义初版时为结构体预留一定未使用空间（`padding`），这样在未来可以安全地挪用这部分空间来添加新字段，而不会改变账户大小或破坏兼容性，代码示例如下：

```rust
#[account(zero_copy)]
#[repr(C)]
pub struct MyState {
    pub data_field_a: u64,
    pub data_field_b: bool,
    // Reserve 128 bytes for future upgrade
    pub _reserved: [u8; 128],
}
```

通过这种设计，当未来需要新字段时，开发者可以用部分 `_reserved` 字段的空间替换为新字段，而无需变更账户大小，保证旧数据账户与新版程序依旧兼容。

此外，当你部署 Solana 程序时，必须指定一个升级权限人（`upgrade authority`），通常是部署时使用的钱包或多签地址，这个权限人拥有唯一可以更新程序字节码的权力，若该权限被误交或移除，程序可能被恶意升级或变得不可修改，因此这也是实际操作时必须重视的安全风险。

### 授权模式差异：`transferFrom` vs `transfer`

在以太坊的 ERC20 标准中，代用户转账通常需要经过两步，首先用户调用 `approve` 授权某个地址可花费自己的一定额度，然后授权方（通常是合约）再调用 `transferFrom` 发起转账。这两步的存在是因为以太坊的账户模型区分*代币持有者*和*转账执行者*，而执行者必须单独发起链上交易。

在 Solana 的 SPL Token 模型中，这一机制被大幅简化。SPL Token 的每个账户都明确记录了*拥有者（authority）*是谁，只要交易中包含该拥有者的签名，程序即可直接通过 `token::transfer` 指令完成代币转移，不需要单独的 `transferFrom`。换言之，Solana 的运行时已经天然支持**谁签名、谁授权**的模型，而不是依赖合约再去检查二次授权。

此外，Solana 的执行环境支持跨程序调用（CPI）时的签名传递机制：

- 如果外层交易中包含了用户签名，内层调用的程序同样能识别该签名；
- 如果调用方是 PDA，则可通过 `invoke_signed` 由运行时生成并验证其派生签名，从而完成授权。

由于 Solana 的运行时能够在系统层面理解并传递签名的授权含义，程序只要获得合法签名（或 PDA 派生签名），就能安全地代用户执行转账，无需额外的*代理转账*指令。

我们的质押流程走的是用户直接签名授权的路径，不依赖任何代理或 PDA，用户在调用 `stake` 指令时直接签名授权程序操作其代币账户，程序即可通过 CPI 执行 `token::transfer`，将代币转入质押金库，无需像 ERC20 那样走 `approve + transferFrom` 的两步，示例代码如下：

```rust
pub fn stake_handler(ctx: Context<Stake>, amount: u64) -> Result<()> {
    let cpi_accounts = Transfer {
        from: ctx.accounts.user_token_account.to_account_info(),
        to: ctx.accounts.staking_vault.to_account_info(),
        authority: ctx.accounts.user.to_account_info(),
    };
    let cpi_program = ctx.accounts.token_program.to_account_info();
    let cpi_ctx = CpiContext::new(cpi_program, cpi_accounts);
    token::transfer(cpi_ctx, amount)?;

    Ok(())
}
```

Solana 之所以不需要 `transferFrom`，正是因为它的运行时模型将*授权*和*执行*合二为一——只要交易中包含合法签名，就代表用户已授权并同意执行这笔转账，无需额外的授权步骤。

### 数值计算

在 Solana 上进行数值处理也需要进行思维转换。首先在数值精度上，由于 SPL Token 的小数位（decimals）通常是 6 或 9，而非以太坊 ERC20 标准的 18 位，因此在大多数场景下，代币数量在 `u64` 的范围内即可表示，这相比 `u128` 或更大的整数类型，不仅简化计算，同时也能在每个账户中节省 8 个字节的存储，从而在大规模场景下降低租金成本。

在进行乘除混合运算时，要特别注意中间计算结果出现精度损失的风险。在许多编程语言中，当你写 `r = a / b * c` 作为单个表达式时，编译器可能会使用扩展精度寄存器，在 x86 架构上，FPU（浮点处理单元）内部使用 80 位扩展精度进行计算，只在最终结果时才转换回 64 位；或者会优化计算顺序，编译器可能重新排列或合并运算以减少精度损失。但当你进行分步计算时，比如：`t = a / b; r = t * c;`，则会将结果存储到内存（64 位），然后从内存读取结果，从而导致损失精度。

在整数代币数量的场景下，推荐始终使用 `u64/u128` 等整数类型，避免浮点精度问题。但在涉及比例、利率、价格等换算时往往需要用浮点数，这时要注意潜在的精度损失。例如在 x86 架构上，FPU 内部可能使用 80 位扩展精度寄存器，在单个表达式中可以保持更高精度，但若拆分成多步计算，结果会被截断到 64 位，从而引入误差。

另一方面，在代币数量等场景下，建议始终使用 u64 或 u128，这样可以完全避免浮点精度丢失问题。但当计算涉及比例、价格或利率等换算时，往往需要依赖浮点数，此时就必须警惕中间结果的精度风险。例如，在某些编译器优化下，单个表达式如 `r = a / b * c` 可能会被放到扩展精度寄存器中计算（在 x86 架构上，FPU 会使用 80 位扩展精度），直到最终结果才截断到 64 位；而若改为分步计算，如先算 `t = a / b` 再算 `r = t * c`，中间结果会被写入内存并以 64 位形式存储，读取时就可能导致额外的精度损失。

## 总结

本文为经验丰富的以太坊开发者提供了一份详尽的 Solana 迁移指南，旨在帮助他们顺利跨越两个生态之间的鸿沟。文章首先强调了从以太坊到 Solana 所需的核心思维转变，深入剖析了两者在账户模型、代币标准以及调用机制上的根本差异。接着，文章对比了两个生态的关键开发工具链，并提出了一系列 Solana 开发的最佳实践。通过理解并采纳本文提出的核心概念和最佳实践，开发者可以更高效、更安全地在 Solana 上构建高性能的去中心化应用。

在本系列的下一篇文章《从以太坊到 Solana——合约篇（二）》中，我们将继续探讨 Solana 开发中的一些限制和不足。最终，我们还会通过一个具体的质押合约示例，一步步展示如何将一个完整的以太坊合约迁移到 Solana，敬请期待。

## 参考

- [Moving from Ethereum Development to Solana](https://solana.com/news/evm-to-svm)
- [EVM vs. SVM: Smart Contracts](https://solana.com/developers/evm-to-svm/smart-contracts)
- [How to Migrate From Ethereum to Solana: A Guide for Devs](https://www.helius.dev/blog/how-to-migrate-from-ethereum-to-solana)
- [Basic Knowledge Needed for Migrating from EVM to Solana](https://medium.com/@easypass.inc/basic-knowledge-needed-for-migrating-from-evm-to-solana-7814b29c8bd5)
- [A Complete Guide to Solana Development for Ethereum Developers](https://solana.com/developers/evm-to-svm/complete-guide)
- [Solana Development for EVM Developers](https://www.quicknode.com/guides/solana-development/getting-started/solana-development-for-evm-developers#key-architectural-differences-between-ethereum-and-solana)
- [Verifying Programs](https://solana.com/docs/programs/verified-builds)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
