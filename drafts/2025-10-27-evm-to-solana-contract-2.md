---
layout: post
title: 从以太坊到 Solana——合约篇（二）
date: 2025-10-27 22:30:00
description: 重点介绍以太坊到 Solana 合约开发中的限制和不足，以及通过一个具体的质押合约示例，展示如何将以太坊合约迁移到 Solana
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
- 如何将以太坊协议迁移到 Solana ——合约篇（二）：重点介绍以太坊到 Solana 合约开发中的限制和不足，以及通过一个具体的质押合约示例，展示如何将以太坊合约迁移到 Solana。

话不多说，正文开始！

---

在上一篇文章中，我们重点介绍了以太坊到 Solana 合约开发中的核心思维转变和最佳实践。掌握了这些核心概念和最佳实践，你就已经具备了构建高效、安全程序的基础。然而，与任何技术平台一样，Solana 也有其自身的特性和限制，了解这些潜在的限制与不足，能帮助你在开发过程中做出更明智的架构决策，避免在项目后期遭遇难以解决的问题。

<!--more-->

## 你需要注意的 Solana 的限制与不足

掌握了 Solana 的核心设计哲学和最佳实践，你就已经具备了构建高效、安全程序的基础。然而，与任何技术平台一样，Solana 也有其自身的特性和限制，了解这些潜在的限制与不足，能帮助你在开发过程中做出更明智的架构决策，避免在项目后期遭遇难以解决的问题。

### 测试：主网 Fork 的挑战

在以太坊开发中，一个强大且常用的测试策略是主网分叉（Mainnet Forking）。使用 Hardhat 或 Foundry 等工具，开发者可以在本地环境中轻松创建一个几乎完整的、按需加载的主网状态副本。这使得测试新合约与现有协议（如 Uniswap）的交互变得非常直接，因为所有真实的账户和状态都可以被即时访问。

然而，在 Solana 生态中，这种无缝的测试模式曾一度面临挑战。虽然 Solana 的标准本地测试工具 `solana-test-validator` 提供了 `--clone` 标志，允许开发者在启动时克隆主网上的特定账户，但这与以太坊的全状态分叉（Full State Forking）有本质不同。Solana 的克隆是一个需要预先指定地址的显式操作，而非按需加载整个链的状态，这对开发工作流有直接影响。如果要测试与一个复杂的主网协议（如 Jupiter）的交互，开发者必须手动识别并列出所有相关的账户地址（如流动性池、配置账户、权限账户等）来进行克隆，这个过程可能非常繁琐且容易遗漏。

不过，值得庆幸的是，Solana 生态系统正在迅速发展以弥补这一差距。社区已经出现了致力于解决主网分叉问题的工具，一个典型的例子就是 [surfpool](https://github.com/txtx/surfpool)，这个工具旨在提供一个类似于以太坊上 [Anvil](https://www.alchemy.com/dapps/foundry-anvil) 的本地测试环境，它能够动态、按需地从主网拉取账户状态，从而在本地实现一个轻量级、高性能的主网分叉，这使得开发者可以在不牺牲性能的情况下，真实地模拟主网环境进行调试和集成测试，极大地改善了开发体验。

### 计算单元 (CU) 限制

在以太坊中，交易的复杂度主要由 Gas 上限来控制，只要支付足够的 Gas，理论上可以执行非常复杂的操作。然而，Solana 对单笔交易的计算资源有严格的硬性上限，这就是**计算单元（Compute Unit, CU）**限制，每笔交易的 CU 预算上限为 **140 万**。交易中的每一个指令都会消耗 CU，从简单的数学运算到复杂的跨程序调用（CPI），都有其对应的开销。一旦交易执行过程中累计消耗的 CU 超出预算，整个交易就会失败，且无法通过支付更多费用来绕过这个限制。

这个硬性上限要求开发者在设计程序时必须时刻保持对计算复杂度的警惕，在以太坊中可能很常见的操作，如遍历一个大的数组或执行复杂的循环计算，在 Solana 上可能很容易就超出 CU 限制。因此，开发者需要转变思维，倾向于将复杂的计算拆分到多个独立的交易中，或者采用更优化的算法来降低单次交易的计算负载。这种对计算效率的极致要求，是 Solana 高性能架构在开发者层面的直接体现。

关于计算单元限制更多的信息，可以参考我们的另外一篇文章：[Deep Dive into Resource Limitations in Solana Development — CU Edition](https://57blocks.io/blog/deep-dive-into-resource-limitations-in-solana-development-cu-edition)

### 无回调/重入

以太坊开发者对重入攻击（Re-entrancy Attack）一定不陌生，它是 Solidity 智能合约中最臭名昭著的安全漏洞之一，这种攻击之所以能生效，是因为以太坊的合约间调用是同步的，并且允许被调用方（合约 B）在完成自己的逻辑前，再回调（call back）调用方（合约 A）。下面是一个典型的易受攻击的 Solidity 函数，将状态更新放在了外部调用之后：

```solidity
// Vulnerable Solidity Code
function withdraw() public {
    uint256 userBalance = balances[msg.sender];
    require(userBalance > 0, "No balance to withdraw");

    // The vulnerability is here: state is updated AFTER the external call.
    (bool success, ) = msg.sender.call{value: userBalance}("");
    require(success, "Transfer failed");

    // If the recipient is a malicious contract, it can re-enter this function
    // before the balance is set to 0, allowing multiple withdrawals.
    balances[msg.sender] = 0;
}
```

Solana 从根本上杜绝了这类问题，因为它采用了严格的单向调用模型，在 Solana 中，程序 A 可以通过 CPI 调用程序 B，但程序 B 在其执行上下文中，不能再回调程序 A，整个调用链必须是单向的、无环的。我们来看一下 Solana 的示例代码，即使我们将状态更新放在 CPI 调用之后，程序依然不会有重入风险：

```rust
// Solana (Anchor) equivalent logic - still safe from re-entrancy
pub fn withdraw(ctx: Context<Withdraw>, amount: u64) -> Result<()> {
    // 1. Perform checks.
    let user_balance = ctx.accounts.user_vault.amount;
    require!(user_balance >= amount, "Insufficient balance");

    // 2. Interaction is performed BEFORE state change (not a best practice, but still safe).
    // The Token Program is a trusted, separate program. It cannot and will not
    // call back into our `withdraw` function. The execution flow is one-way.
    token::transfer(cpi_context, amount)?;

    // 3. State is updated last.
    ctx.accounts.user_vault.amount -= amount;

    Ok(())
}
```

当我们的程序通过 CPI 调用 `Token Program` 时，它的执行会暂停，直到 `token::transfer` 完成。`Token Program` 是一个独立的程序，它的代码里没有任何逻辑可以回调我们的 `withdraw` 函数。此外，Solana 的程序模型中没有可以被外部调用隐式触发的 `fallback` 或 `receive` 函数，所以重入的攻击路径在架构层面就被完全切断了。

虽然上面例子中的顺序不会导致重入，但建议交换第二步和第三步的顺序，在 Solana 开发中，遵循检查（Checks）→ 状态更新（Effects）→ 外部调用（Interactions）的顺序是最佳实践，这一模式的价值并不在于防止重入，而在于保证逻辑一致性与代码可读性。外部调用（CPI）通常是交易中最容易失败的步骤之一，但由于 Solana 的交易执行具有原子性，即使 CPI 失败，之前的状态更新也会随整个交易一起回滚，因此不会产生真正的“无效状态”。在完成所有检查并更新内部状态之后再执行 CPI，可以确保外部调用基于一个已经验证且一致的内部状态，从而减少逻辑复杂度，并使程序行为更加可预测。

然而，这种架构上的安全性也带来了一些设计上的限制。在以太坊中常见的、依赖同步回调的复杂设计模式（例如，某些闪电贷或复杂的 DeFi 组合操作）在 Solana 上无法直接实现，开发者需要转变思路，使用多笔交易或异步的状态检查来完成类似的功能。

### 实现 Hook 的复杂性

为代币转账行为设计前置或后置钩子是常见需求，但以太坊与 Solana 在实现路径上差异明显。

在以太坊中，每个 `ERC‑20` 代币通常是一个可定制合约，开发者可直接在 `transfer` 或 `transferFrom` 中插入逻辑，亦可重写诸如 `_beforeTokenTransfer` 的内部钩子函数，用于实现黑白名单、交易税、免费列表等功能，这种方案内聚、简洁，易于调整和控制。

Solana 的代币逻辑则统一由链上发布的官方 `Token Program` 提供，无法直接修改。因此，`Token-2022` 引入了 `Transfer Hook` 扩展，允许你在代币创建时指定一个独立 Hook 程序：`Token Program` 在每次转账时，会通过 CPI 自动调用这个 Hook 程序，传入所有相关账户（仅读权限），由此决定是否允许继续执行转账，若 Hook 程序返回失败，整个转账过程也会被回滚。 ￼ ￼

这种机制虽比以太坊复杂，要求 Token 创建者额外部署并关联钩子程序，但它体现了 Solana 的模块化哲学：标准通用逻辑（`Token Program`）与自定义业务逻辑（`Hook Program`）分离，通过 CPI 实现组合，既保证标准一致性，也提升扩展性。

不过，需要注意若要启用 `Transfer Hook`，还必须使用 `transferChecked` 调用方式；普通 `transfer` 调用会失败。同时，由于多数钱包与 DEX 未明确支持 Hook 扩展，这会影响生态兼容性，使用前需慎重评估。

Transfer Hook 并不是几行简单逻辑，而是一个独立部署的程序（`Hook Program`），其执行流程如下：

1. 开发者编写并部署一个专门的 `Transfer Hook` 程序，它的唯一职责就是验证代币转账是否合法（例如检查黑白名单、KYC 状态、手续费等）。
2. 创建代币时（Mint 初始化），通过启用 `Token-2022` 的 `Transfer Hook` 扩展，将该代币与该 Hook 程序绑定。
3. 之后，每当该代币发生任何转账时，`SPL Token-2022` 程序会通过 CPI 自动调用对应的 Hook 程序，传入所有相关账户（以只读方式），并执行你定义的逻辑，若 Hook 程序返回失败，则整个转账将被回滚。
4. 通常会利用 SPL-2022 规范中定义的 `execute` 指令和 `extra-account-metas` 列表（PDA 存储额外账户信息）进行处理，保障调用的上下文完整性。

在我们的示例中，为了简化操作，我们选择在每个指令内部进行黑名单检查，但这只能针对特定操作生效，无法覆盖所有可能的转账路径。如果希望构建一个拦截所有转账的黑名单机制，就必须使用上述 `Transfer Hook` 方案，虽然它在开发和部署上更复杂，但这正体现了 Solana 的设计哲学：将核心 `Token` 逻辑与可扩展的业务逻辑解耦，把标准功能保留在官方 `Token Program`，将可定制的检查逻辑放在独立 `Hook Program` 中，通过 CPI 进行组合。

### 日志和事件

在以太坊上，事件（Events）是智能合约与外部世界通信的关键机制，通过 `emit` 关键字，合约可以发出结构化的、可被索引的日志，链下服务可以高效地监听这些事件，用于更新 UI、数据分析或触发其他逻辑。

```solidity
// evm-staking/src/Staking.sol
// Define a structured event
event Staked(address indexed user, uint256 amount);

function stake(uint256 amount) external {
    // ...
    // Emit the event with structured data
    emit Staked(msg.sender, amount);
}
```

Solana 的原生运行时环境（Native）在这一点上则显得较为原始，它提供了一个基础的日志记录功能 `sol_log`（在 Anchor 中通常通过 `msg!` 宏使用），但这本质上只是向交易日志中打印一个简单的字符串，它对于调试很有用，但由于缺乏结构化与索引机制，也无法像以太坊那样按事件或参数进行高效过滤（filter），链下服务只能全量扫描日志，因此很难高效地解析和使用这些信息。为了解决这个问题，Anchor 框架提供了一个轻量的 `#[event]` 宏，它在 Solana 的原生日志之上，实现了基础的事件解析功能。

首先，你需要定义一个事件结构体，并为其加上 `#[event]` 宏：

```rust
// solana-staking/programs/solana-staking/src/events.rs
#[event]
pub struct Staked {
    pub user: Pubkey,
    pub amount: u64,
    pub timestamp: i64,
}
```

然后，在你的指令逻辑中，使用 `emit!` 宏来发出这个事件：

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
pub fn stake_handler(ctx: Context<Stake>, amount: u64) -> Result<()> {
    // ... (staking logic)

    // Emit the structured event
    emit!(Staked {
        user: ctx.accounts.user.key(),
        amount,
        timestamp: ctx.accounts.clock.unix_timestamp,
    });

    Ok(())
}
```

Anchor 的 `#[event]` 宏通过 `emit!` 将事件数据序列化（通常是 Base64 编码）写入交易日志，虽不具备智能合约中的 `EVM-style event` 强结构化机制，但仍可以被链下系统识别。开发者可以在客户端使用 Anchor 的 `EventParser` 来解析这些事件，也可以借助像 Helius 提供的 Webhooks 或增强的交易 API 获取相关日志，再通过自己逻辑提取和解析这些事件。

因此，对于任何需要与链下服务进行可靠通信的应用来说，请务必使用 Anchor 的 `#[event]` 宏，而不是依赖原生的 `msg!`，这确保了你的程序能够以一种标准化的、可解析的方式向外部世界广播其状态变化。

## 动手实践：从编码到部署

### 案例说明

理论是基础，但没有什么比一个完整的、可运行的实例更能巩固所学，我们将动手实践，将一个以太坊 Staking（质押）合约，一步步迁移到 Solana 上，这个案例旨在集中展示前文讨论过的所有核心概念。

**业务逻辑**

这个 Staking 合约的业务逻辑非常经典：

- 用户可以质押项目方发行的代币（`MyToken`）。
- 作为回报，用户会根据其质押的数量和时长，按比例获得奖励代币（`RewardToken`）。

**核心功能**

为了实现上述逻辑，合约需要支持以下三个核心功能：

- `stake`：用户存入 `MyToken` 开始质押。
- `unstake`：用户取回自己质押的 `MyToken`。
- `claimRewards`：用户领取自己应得的 `RewardToken` 奖励。

这个实践案例将把前面所有零散的知识点串联起来，让你对 Solana 开发有一个完整、连贯的认知。

### 以太坊实现 (Foundry)

我们来看一下这个质押逻辑在以太坊上是如何实现的，代码遵循了常见的 Solidity 开发模式，将所有逻辑和状态都封装在一个名为 `Staking.sol` 的合约中。

**合约结构与状态变量**

合约的核心是其内部的状态变量，它们负责记录所有数据。

```solidity
// evm-staking/src/Staking.sol
contract Staking is ReentrancyGuard, Ownable {
    // Token contracts to interact with
    IERC20 public stakingToken;
    IERC20 public rewardToken;

    // Global state
    uint256 public rewardRate = 100; // 1% per day
    uint256 public totalStaked;

    // Per-user state, mapping an address to their stake info
    struct StakeInfo {
        uint256 amount;
        int256 rewardDebt;
        uint256 claimed;
    }
    mapping(address => StakeInfo) public stakes;

    // ... events and constructor
}
```

- `stakingToken` 和 `rewardToken`：存储了需要交互的两个代币的合约地址。
- `rewardRate` 和 `totalStaked`：存储了全局的奖励率和总质押量。
- `stakes`：这是一个关键的 `mapping`，它将每个用户的地址映射到一个 `StakeInfo` 结构体，该结构体记录了该用户的所有个人质押数据（质押数量、时间戳等）。所有用户的状态都集中存储在这个 `mapping` 中。

**核心功能实现**

合约的三个核心功能 `stake`、`unstake` 和 `claimRewards` 都是围绕着直接修改这些状态变量来展开的。

```solidity
// evm-staking/src/Staking.sol
function stake(uint256 amount) external nonReentrant {
    // ... (checks)

    // Pulls tokens from the user into this contract
    stakingToken.transferFrom(msg.sender, address(this), amount);

    // Update user's stake info directly in the mapping
    stakes[msg.sender].amount += amount;
    // Update global state
    totalStaked += amount;

    // ... (update timestamps and emit event)
}

function unstake(uint256 amount) external nonReentrant {
    // ... (checks and claims pending rewards)

    // Update user's stake info
    stakes[msg.sender].amount -= amount;
    // Update global state
    totalStaked -= amount;

    // Push tokens from this contract back to the user
    stakingToken.transfer(msg.sender, amount);

    // ... (emit event)
}

function _claimRewards() private {
    uint256 reward = calculateReward(msg.sender);
    if (reward > 0) {
        // ... (update reward debt)
        // Transfer reward tokens to the user
        rewardToken.transfer(msg.sender, reward);
        // ... (emit event)
    }
}
```

可以看到，整个实现非常直观：合约自身就像一个全能的中央处理器，它不仅持有代币（金库），还记录着所有用户的账本，并直接执行所有的计算和状态更新，这正是典型的以太坊合约设计模式。

完整的合约代码可以[在这里](https://github.com/jimmyzhao-57blocks/evm-to-solana/tree/main/contract/evm-staking)查阅。

### Solana 实现 (Anchor)

现在，我们来看看如何用 Solana 的方式来实现同样的质押逻辑，正如我们反复强调的，核心是将代码与数据分离。

**程序结构与账户定义**

在 Anchor 中，我们首先定义一个无状态的程序，它包含 `stake`、`unstake` 等指令，然后我们再定义所有需要用到的账户结构。

```rust
// solana-staking/programs/solana-staking/src/lib.rs & state.rs
// The program itself is stateless.
#[program]
pub mod staking {
    pub fn stake(ctx: Context<Stake>, amount: u64) -> Result<()> { /* ... */ }
    pub fn unstake(ctx: Context<Unstake>, amount: u64) -> Result<()> { /* ... */ }
    pub fn claim_rewards(ctx: Context<ClaimRewards>) -> Result<()> { /* ... */ }
    // ... other instructions
}

// Global state is stored in a dedicated account.
#[account]
pub struct GlobalState {
    pub admin: Pubkey,
    pub staking_mint: Pubkey,
    pub reward_mint: Pubkey,
    pub reward_per_second: u64,
    pub total_staked: u64,
    // ...
}

// Per-user state is also in its own account, typically a PDA.
#[account]
pub struct UserStakeInfo {
    pub owner: Pubkey,
    pub amount: u64,
    pub reward_debt: i128,
    pub claimed: u64,
    // ...
}
```

- `staking` 程序：只包含业务逻辑，不存储任何数据。
- `GlobalState` 账户：这是一个单例账户（Singleton Account），用于存储整个协议的全局配置，如 `total_staked` 和 `reward_rate`。
- `UserStakeInfo` 账户：这是一个 PDA，为每个质押的用户动态创建，用于存储其个人质押信息。

**指令与上下文 (Context)**

在 Solana 中，每个指令都必须明确定义它需要交互的所有账户。以 `stake` 指令为例，它的上下文（Context）清晰地列出了所有相关方。

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
#[derive(Accounts)]
pub struct Stake<'info> {
    // The user performing the action (signer)
    #[account(mut)]
    pub user: Signer<'info>,

    // The global state account
    #[account(mut)]
    pub state: Box<Account<'info, GlobalState>>,

    // The user's personal stake info PDA
    #[account(init_if_needed, payer = user, ...)]
    pub user_stake_info: Box<Account<'info, UserStakeInfo>>,

    // The user's token account holding the staking tokens
    #[account(mut)]
    pub user_token_account: Account<'info, TokenAccount>,

    // The program's vault to store the staked tokens
    #[account(mut)]
    pub staking_vault: Account<'info, TokenAccount>,

    // Required external programs
    pub token_program: Program<'info, Token>,
    pub system_program: Program<'info, System>,
    // ...
}
```

这个结构体就是 `stake` 指令的账户清单，在执行指令前，Anchor 会根据这些定义和约束，严格验证所有传入账户的合法性。

**核心功能实现**

`stake` 指令的核心逻辑不再是直接修改内部状态，而是通过 CPI 调用 `Token Program` 来完成代币转移，然后更新传入的 `state` 和 `user_stake_info` 账户的数据。

```rust
// solana-staking/programs/solana-staking/src/instructions/stake.rs
pub fn stake_handler(ctx: Context<Stake>, amount: u64) -> Result<()> {
    // ... (checks)

    // 1. Command the Token Program to transfer tokens via CPI
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

    // 2. Update the data on the user_stake_info account
    let user_stake = &mut ctx.accounts.user_stake_info;
    user_stake.amount += amount;
    // ...

    // 3. Update the data on the global state account
    let state = &mut ctx.accounts.state;
    state.total_staked += amount;

    Ok(())
}
```

这种模式清晰地展示了 Solana 的核心思想：无状态的程序（逻辑）操作着外部传入的、独立的账户（数据）。

完整的 Solana 实现代码可以[在这里](https://github.com/jimmyzhao-57blocks/evm-to-solana/tree/main/contract/solana-staking)查阅。

### 合约测试

验证合约的正确性是开发流程中至关重要的一环，在这一点上，以太坊和 Solana 的主流测试框架在理念和实现上有所不同。

**对比测试框架**

- 以太坊/Foundry：测试通常直接在 Solidity 中编写，这种方式的好处是测试代码与合约代码使用同一种语言，共享相同的上下文和数据类型。测试合约可以直接调用主合约的内部和外部函数，并利用 `forge-std` 库（如 `vm.prank`）来模拟不同的调用者和链上环境，非常强大和直观。
- Solana/Anchor：测试使用 TypeScript 或 JavaScript 编写，测试脚本通过客户端库与本地的测试验证器（`solana-test-validator`）进行交互，这意味着测试的本质是模拟一个真实的前端或后端应用，去调用你部署在本地测试网络上的程序，这种方式更贴近真实的用户场景，但需要处理更多与客户端交互相关的设置。

**Foundry 测试示例**

下面是 `evm-staking` 项目中一个典型的测试用例，它直接在 Solidity 中验证 `stake` 功能。

```solidity
// evm-staking/test/Staking.t.sol
function testStake() public {
    uint256 stakeAmount = 1000 * 10 ** 18;

    // Simulate the call coming from user1
    vm.startPrank(user1);
    // User must first approve the staking contract
    myToken.approve(address(staking), stakeAmount);
    // Call the stake function
    staking.stake(stakeAmount);
    vm.stopPrank();

    // Assertions are made directly against the contract's state
    (uint256 stakedAmount, ,) = staking.getStakeInfo(user1);
    assertEq(stakedAmount, stakeAmount);
    assertEq(staking.totalStaked(), stakeAmount);
}
```

**Anchor 测试示例**

在 Solana 这边，测试脚本需要做更多的环境准备工作，例如创建模拟的用户、代币账户，然后构建并发送一个完整的交易来调用 `stake` 指令。

```typescript
// solana-staking/tests/solana-staking.test.ts
describe("Stake", () => {
  it("should allow user to stake tokens", async () => {
    // 1. Setup: Create a test user and their token accounts
    const { user, userSigner } = await createTestUser(svm);
    const { stakingToken, rewardToken } = await setupUserWithTokens(
      provider,
      admin,
      user,
      stakingMint,
      rewardMint
    );
    const stakeAmount = toToken(100);

    // 2. Action: Build and send the transaction to call the 'stake' instruction
    await stakeTokens(user, userSigner, stakingToken, rewardToken, stakeAmount);

    // 3. Assertion: Fetch the account data from the local validator to verify state
    const userStakePda = getUserStakePda(statePda, user.publicKey);
    const userStakeInfo = getUserStakeInfo(provider, userStakePda);
    expect(userStakeInfo).to.not.be.null;
    expect(userStakeInfo!.amount.toString()).to.equal(stakeAmount.toString());
    expect(userStakeInfo!.rewardDebt.toString()).to.equal("0");
    expect(userStakeInfo!.claimed.toString()).to.equal("0");

    const globalState = getGlobalState(provider, statePda);
    expect(globalState!.totalStaked.toString()).to.equal(
      stakeAmount.toString()
    );
  });
});
```

通过对比可以看出，Foundry 的测试更侧重于合约内部逻辑的单元测试，而 Anchor 的测试更像是一次完整的端到端或集成测试。

### 合约部署

将代码部署到区块链上，是开发流程的最后一步，以太坊和 Solana 在此环节的底层机制和工具链也有所不同。

**以太坊/Foundry 部署**

在以太坊上，部署一个合约本质上是向网络发送一笔特殊的交易，这笔交易的 `to` 地址为空，`data` 字段则包含了合约的编译后字节码，一旦交易被矿工打包上链，EVM 就会执行字节码中的构造函数逻辑，创建一个全新的合约账户，并将代码存储在该账户中。

在我们的示例项目中，我们使用 Foundry 的脚本系统来处理部署，通过运行一个部署脚本，可以更灵活地处理复杂的部署逻辑。

```bash
# Run the deployment script using forge
forge script script/Deploy.s.sol --rpc-url <your_rpc_url> --broadcast --verify
```

这个命令会执行 `Deploy.s.sol` 脚本，将 `Staking` 合约部署到指定的网络，并通过 `--verify` 标志在 Etherscan 上自动上传合约源码，完整的部署脚本可以[在这里](https://github.com/jimmyzhao-57blocks/evm-to-solana/blob/main/contract/evm-staking/script/Deploy.s.sol)查阅。

**Solana/Anchor 部署**

Solana 的部署机制则完全不同，由于代码与数据分离，部署一个程序并不是创建一个包含代码和状态的合约账户，而是将编译后的 BPF（Berkeley Packet Filter）字节码上传到一个专门的程序账户中，这个程序账户是可执行的，但本身不存储业务数据，使用 Anchor，部署过程将被极大地简化。

```bash
# First, build the program to get the BPF bytecode
anchor build

# Then, run the deploy command for the initial deployment
anchor deploy --provider.cluster <cluster_name>
```

- `anchor build`：这个命令会编译你的 Rust 代码，生成 BPF 字节码，并将其放置在 `target/deploy/` 目录下。
- `anchor deploy`：此命令负责处理所有底层细节，包括创建一个新的程序账户，并上传字节码。`--provider.cluster` 参数允许你指定目标网络，可以是 `localnet`（本地测试网）、`devnet`（公共开发测试网）或 `mainnet-beta`（主网）。

当需要更新程序逻辑时，你只需修改代码，然后使用 `anchor upgrade` 命令，这会将新的字节码上传到同一个程序 ID 地址，实现无缝升级，而所有与该程序关联的状态账户都将保持不变。

```bash
# After making changes, build the new version
anchor build

# Then, use the upgrade command
anchor upgrade target/deploy/your_program_name.so --provider.cluster <cluster_name>
```

关于 Solana 部署的更详细步骤和注意事项，可以查阅我们项目中的[部署文档](https://github.com/jimmyzhao-57blocks/evm-to-solana/blob/main/contract/solana-staking/DEPLOYMENT.md)。

通过对比，我们可以发现 Foundry 的部署更像是创建一个全新的服务器实例，而 Anchor 的部署则是上传或更新一段可执行的程序逻辑。

## 总结

通过合约篇这两篇文章，我们系统地梳理了从以太坊迁移到 Solana 时，在合约层面必须完成的完整认知转变。这并不是一次简单的语言替换或工具迁移，而是一场围绕执行模型、状态组织方式和协议架构的深度重构。

在上一篇文章中，我们重点回答了一个核心问题：为什么用以太坊的方式写 Solana 合约，往往会越写越别扭？ 答案在于底层模型的根本不同——Solana 通过程序无状态加账户显式传递的设计，将并行执行作为第一原则。这要求开发者从合约即世界的思维，转向程序只是逻辑调度者，账户才是状态本体的新范式。账户模型、Token Account、CPI、PDA、显式 Context，这些并非零散的技术细节，而是同一套并行执行哲学的不同侧面。

在这一篇文章中，我们进一步讨论了一个更现实的问题：当你真正开始迁移一个复杂协议时，Solana 会在哪些地方逼你换思路？无论是计算单元（CU）的硬性上限、主网 Fork 测试体验的差异、无回调/无重入的执行模型，还是 Transfer Hook 这种模块化但更复杂的扩展机制，这些限制并非偶然，而是 Solana 为了性能和确定性所做出的系统性取舍。理解这些限制，才能在设计阶段避免走进 EVM 思维的死胡同。

结合起来我们可以提炼出一个清晰的结论：以太坊更接近一种应用级的编程模型，它倾向于提供高度抽象的合约结构，使状态、权限与业务逻辑自然地内聚在单一合约中；而 Solana 则更接近系统级编程模型，它采用显式的账户与资源模型，促使开发者以组合的方式构建系统。在以太坊中，我们习惯把状态、权限和业务逻辑封装进一个合约，并依赖运行时提供的隐式上下文。而在 Solana 中，我们需要主动拆分状态、明确账户边界、用 PDA 构建权限体系，并通过 CPI 与标准程序进行组合，这种设计确实带来了更高的开发复杂度，但它并非无意义的负担，而是为了换取高吞吐、可并行执行和强确定性所付出的工程代价。

通过完整的质押合约迁移示例，我们也验证了一个重要事实：成功的迁移从来不是逐行翻译合约代码，而是围绕 Solana 原生模型进行的重构设计。哪些状态应该是全局账户，哪些应该是用户级 PDA？哪些逻辑适合放在单体程序中，哪些必须通过 CPI 调用外部标准程序？哪些操作需要拆分为多笔交易，哪些可以在单次指令内完成？这些问题，只有在真正理解两条链的设计哲学之后，才能做出正确判断。

最后，我们希望通过合约篇的文章，帮你建立的不只是会写 Solana 合约的能力，而是一种判断力。当你面对一个既有的 EVM 协议时，能够分辨哪些设计是可以保留的，哪些是必须推翻重来的。当你在 Solana 上设计新协议时，能够从一开始就顺着它的执行模型，而不是事后不断妥协和修补。

在接下来的文章中，我们将把视角从合约层扩展到前端交互与后端服务上继续探讨，介绍当合约从 EVM 迁移至 Solana 后，整个系统架构该如何同步适配调整，核心便是让后端、前端与合约在设计上保持一致，同步完成配套改造。

## 参考

- [Moving from Ethereum Development to Solana](https://solana.com/news/evm-to-svm)
- [EVM vs. SVM: Smart Contracts](https://solana.com/developers/evm-to-svm/smart-contracts)
- [How to Migrate From Ethereum to Solana: A Guide for Devs](https://www.helius.dev/blog/how-to-migrate-from-ethereum-to-solana)
- [Basic Knowledge Needed for Migrating from EVM to Solana](https://medium.com/@easypass.inc/basic-knowledge-needed-for-migrating-from-evm-to-solana-7814b29c8bd5)
- [A Complete Guide to Solana Development for Ethereum Developers](https://solana.com/developers/evm-to-svm/complete-guide)
- [Solana Development for EVM Developers](https://www.quicknode.com/guides/solana-development/getting-started/solana-development-for-evm-developers#key-architectural-differences-between-ethereum-and-solana)
- [Verifying Programs](https://solana.com/docs/programs/verified-builds)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
