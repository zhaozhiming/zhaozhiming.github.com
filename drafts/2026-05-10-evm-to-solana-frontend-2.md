# 如何将以太坊协议迁移到 Solana - 前端（第二部分）：Staking Demo 实战

## 概览

随着 Solana 生态的成熟，越来越多的产品方开始考虑将其以太坊（EVM）项目迁移到 Solana，以获得更高的性能、更低的交易成本以及更好的用户体验。我们公司在这一领域拥有丰富的实操经验，曾主导多个以太坊协议在不同类别的迁移与重构工作。我们理解迁移过程中在合约架构、数据模型、交易逻辑以及前后端协作上的复杂性，并已开发出一套系统化的方法论和最佳实践。

为了帮助开发者系统地掌握从以太坊迁移到 Solana 的方法与实践，我们将推出一系列文章，聚焦三个核心层面——智能合约层、后端服务以及前端交互。在系列文章中，我们将分享来自真实项目的经验教训，包括关键注意事项、最佳实践以及迁移过程中常见的问题。此外，我们还会分享案例研究，展示端到端的迁移方法和实现路径，并提供示例代码，帮助开发者快速上手。

通过这一系列文章，我们希望帮助开发者不仅能够顺利完成迁移，还能充分利用 Solana 的高性能潜力及独特机制，重新设计并原生化构建 Solana 协议。

## 文章导航

- 如何将以太坊协议迁移到 Solana — 前言：系统性介绍以太坊与 Solana 在账户模型、执行机制及费用系统上的根本差异。
- 如何将以太坊协议迁移到 Solana — 合约（第一部分）：重点讲解从以太坊到 Solana 的合约开发心态转变及最佳实践。
- 如何将以太坊协议迁移到 Solana - 前端（第一部分）：聚焦从以太坊到 Solana 的前端开发所面临的主要挑战与坑点。
- **如何将以太坊协议迁移到 Solana - 前端（第二部分）：Staking Demo 实战（本文）**

通过一个完整的 Staking Demo（质押案例）将核心前端交互场景"串起来"，对比 EVM 与 Solana 的开发方式，帮助你把前两篇中的"概念与机制"落到真实代码与用户流程上。

## 主要内容概览

本文将以一个简化的 Staking 协议演示为例 — 用户质押 Token 赚取奖励，随时可解除质押或领取，无锁定期 — 深入探讨前端开发中的几个核心场景，并对 EVM 和 Solana 生态的实现差异进行对比分析：

1. **架构划分**：业务逻辑与前端职责的界定。
2. **钱包连接**：EVM 采用 MetaMask + Wagmi；Solana 采用 Wallet Adapter。
3. **状态读取**：EVM 依赖 ABI 和合约地址；Solana 侧重于 PDA 和 Anchor。
4. **数据写入（交易流程）**：比较 EVM 的 approve + stake 双步操作与 Solana 的直接 stake。
5. **事件处理**：对比 EVM 的 logs 与 Solana 的程序 logs 解析。
6. **实战整合**：如何在一个真实的 Demo 中将上述能力有效串联起来。

如果你想直接对照代码实现，可以在这里查看。

## 1. 业务逻辑与前端职责

**核心业务功能需求：**

- **质押（Stake）**：用户能够将其持有的 MyToken 代币进行质押。
- **奖励领取（Claim Rewards）**：根据用户的质押数量和质押时长，协议将向用户发放 RewardToken 奖励代币。
- **状态维护与安全**：协议需记录和管理用户的质押状态，并实施必要的安全措施。

**前端界面需实现的操作：**

- `stake`：发起用户质押 MyToken 的交易。
- `unstake`：发起用户赎回已质押资产的交易。
- `getStakeInfo`：查询并显示用户当前的质押详情。

**注**：本 Demo 的重点在于展示"前端如何与 EVM / Solana 协议进行交互"的流程，不涉及奖励计算公式的具体设计。

## 2. 连接钱包

### 2.1 EVM：MetaMask + Wagmi + RainbowKit

在 EVM 生态中，钱包交互的事实标准几乎由 MetaMask 一家独大带动。前端通常结合 RainbowKit + Wagmi + Viem 来实现钱包连接。

**核心步骤：**

1. 配置链与 RPC（这里我们用 sepolia 测试网）。
2. 创建 wagmi 配置并启用 SSR。
3. 用 `RainbowKitProvider` 包裹应用。
4. 使用 `ConnectButton` 渲染连接按钮。

```typescript
export const config = getDefaultConfig({
  appName: "Stake App",
  projectId: "EVM-DAPP",
  chains: [sepolia],
  transports: {
    [sepolia.id]: http(process.env.NEXT_PUBLIC_RPC_URL || ""),
  },
  ssr: true,
});

const client = new QueryClient();

<WagmiProvider config={config}>
  <QueryClientProvider client={client}>
    <RainbowKitProvider>
      <Component {...pageProps} />
    </RainbowKitProvider>
  </QueryClientProvider>
</WagmiProvider>
```

然后在页面中：

```html
<ConnectButton
  label="Connect Wallet"
  showBalance={false}
  accountStatus="address"
/>
```

**特点：**

- 钱包生态相对集中，"接入 MetaMask 基本等于覆盖大多数用户"。
- 前端心智模型：一个主流钱包 + 少量其他钱包。

### 2.2 Solana：多钱包生态 + Wallet Adapter

Solana 没有像 MetaMask 那样的"事实标准钱包"，其生态相对多元：常见的有 Phantom、Solflare、Backpack 等。为了解决这种碎片化，Anza 提供了 Wallet Adapter 作为统一接口。

第一步，创建 `WalletProvider`：

```typescript
interface WalletProviderProps {
  children: ReactNode;
}

export const WalletProvider: FC<WalletProviderProps> = ({ children }) => {
  const { network, endpoint } = SOLANA_CONFIG;
  const wallets = useMemo(
    () => [new PhantomWalletAdapter(), new SolflareWalletAdapter()],
    [network]
  );

  return (
    <ConnectionProvider endpoint={endpoint}>
      <SolanaWalletProvider wallets={wallets} autoConnect>
        <WalletModalProvider>{children}</WalletModalProvider>
      </SolanaWalletProvider>
    </ConnectionProvider>
  );
};
```

第二步：使用官方 UI 按钮

```typescript
<WalletModalProvider>
  <WalletMultiButton className={styles.connectButton}>
    Connect Wallet
  </WalletMultiButton>
</WalletModalProvider>
```

EVM 世界里"钱包 = MetaMask"，而在 Solana，开发者必须同时考虑多个钱包入口，因此 Wallet Adapter 的作用尤为关键。

**对比总结：**

- **EVM (以太坊虚拟机)**：钱包生态集中，"MetaMask"占据主导地位，适用于大多数场景。
- **Solana**：钱包种类繁多，前端开发中需借助 Wallet Adapter 作为必需的"抽象层"，以统一接口屏蔽不同钱包之间的差异。

## 3. 读取状态数据（只读操作）

读取状态是所有 DApp 的基础操作，特点是不会修改链上状态、不需要 Gas，任何人都可以发起。

### 3.1 EVM：useReadContract + ABI

使用 Wagmi 的 `useReadContract` 可以方便地读取合约状态：

```typescript
const {
  data: stakeInfoData,
  isLoading: isReading,
  error: readError,
  refetch,
} = useReadContract({
  address: STAKING_CONTRACT_ADDRESS,
  abi: stakingAbi,
  functionName: "getStakeInfo",
  args: [address as Address],
  query: {
    enabled: !!address && isConnected,
  },
});
```

**核心要素：**

- 合约 `address`
- 合约 `abi`
- 函数名 + 参数

调用本质上是对 RPC 的 `eth_call` 封装。

### 3.2 Solana：账户模型 + PDA + Anchor

在 Solana 中，状态是存储在账户（Account）中的。例如，用户的质押信息通常会对应一个 PDA（程序派生地址）账户。

读取 Solana 状态数据有两种主要方式：

1. **直接读取并手动解析**：使用 `@solana/web3.js` 直接读取账户数据，然后手动解析 Buffer。
2. **使用 Anchor 和 IDL 自动解析（推荐）**：结合 `@coral-xyz/anchor` 和 IDL（接口描述语言）来自动解析账户数据。

创建 `useProgram` Hook：

```typescript
const [program, setProgram] = useState<Program<SolanaStaking>>();
const { connection } = useConnection();
const wallet = useAnchorWallet();

useEffect(() => {
  if (wallet) {
    const provider = new AnchorProvider(connection, wallet, {});
    setProvider(provider);
    const program = new Program<SolanaStaking>(idl, provider);
    setProgram(program);
  }
}, [connection, wallet]);

return { program, setProgram };
```

读取用户质押信息：

```typescript
const userStakeInfo = await program.account.userStakeInfo.fetch(userStakeInfoPda);
```

**对比小结：**

- **EVM**：ABI + 合约地址 → 读函数返回值。
- **Solana**：IDL + PDA → 直接读账户结构体。

使用 Anchor 可以将 Solana 的账户模型尽量"包装成 EVM 开发者熟悉的形式"。

## 4. 写入数据（交易操作）

写入数据意味着修改状态，需要用户签名和支付手续费。这里依然以 stake 为例。

### 4.1 EVM：approve → stake 两步走

典型流程：

**1. 查询 allowance（合约拥有的代币使用权限）：**

```typescript
const {
  data: currentAllowance,
  isLoading: isAllowanceLoading,
  refetch: refetchAllowance,
} = useReadContract({
  address: STAKING_TOKEN_ADDRESS,
  abi: stakingTokenAbi,
  functionName: "allowance",
  args: [address, STAKING_CONTRACT_ADDRESS],
  query: {
    enabled: !!address && isConnected,
  },
});
```

**2. 如不足，先进行 approve 授权：**

```typescript
const {
  writeContract,
  data: writeData,
  error: writeError,
} = useWriteContract();

writeContract({
  address: STAKING_TOKEN_ADDRESS,
  abi: stakingTokenAbi,
  functionName: "approve",
  args: [STAKING_CONTRACT_ADDRESS, stakeAmountWei],
});
```

**3. 再调用 stake 完成质押：**

```typescript
await writeContract({
  address: STAKING_CONTRACT_ADDRESS,
  abi: stakingAbi,
  functionName: "stake",
  args: [stakeAmountWei],
});
```

### 4.2 Solana：用户签名即授权

在 EVM 中，用户质押 ERC20 Token 通常需要两步操作：approve → stake。这是因为 ERC20 Token 合约要求必须先获得用户授权，才能从用户账户转移代币。

而在 Solana 中，这个流程被大大简化，原因如下：

1. 每个 SPL Token 的余额存放在 Token Account 中，每个账户都有一个 owner，只有 owner 才能发起代币转账或授权操作。
2. ATA（Associated Token Account）是唯一且可推导的账户。
3. 每个 Token Account 的代币来源于一个 Mint，Mint 定义了代币总量和小数等信息，当用户质押时，staking program 不需要 Mint 的授权，它只是在用户签名下，将用户的 ATA 余额转入 stakingVault。

Solana 中"用户签名 = 授权"，无需单独的 approve 步骤。

#### 4.2.1 Staking Program 的 stake IDL（节选）

```json
{
  "args": [
    {
      "name": "amount",
      "type": "u64"
    }
  ],
  "accounts": [
    {
      "name": "user",
      "writable": true,
      "signer": true
    },
    {
      "name": "state",
      "writable": true,
      "pda": {
        "seeds": ["state", "staking_mint"]
      }
    },
    {
      "name": "user_stake_info",
      "writable": true,
      "pda": {
        "seeds": ["stake", "state", "user"]
      }
    },
    {
      "name": "user_token_account",
      "writable": true
    },
    {
      "name": "staking_vault",
      "writable": true,
      "pda": {
        "seeds": ["staking_vault", "state"]
      }
    },
    {
      "name": "reward_vault",
      "writable": true,
      "pda": {
        "seeds": ["reward_vault", "state"]
      }
    },
    {
      "name": "user_reward_account",
      "writable": true
    },
    {
      "name": "blacklist_entry",
      "pda": {
        "seeds": ["blacklist", "state", "user"]
      }
    },
    {
      "name": "token_program",
      "address": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    },
    {
      "name": "system_program",
      "address": "11111111111111111111111111111111"
    },
    {
      "name": "clock",
      "address": "SysvarC1ock11111111111111111111111111111111"
    }
  ]
}
```

**参数说明：**

- `amount: u64` → 用户想要质押的 MyToken 数量

**账户说明：**

| 账户 | 类型 | 说明 | PDA 种子组成 |
|------|------|------|-------------|
| `user` | 签名账户 | 用户签名账户 | — |
| `state` | PDA | 全局质押状态 | `["state", staking_mint]` |
| `user_stake_info` | PDA | 用户质押信息 | `["stake", statePda, user_publicKey]` |
| `user_token_account` | ATA | 用户质押代币账户 | `getAssociatedTokenAddressSync(stakingMint, user_publicKey)` |
| `staking_vault` | PDA | 托管质押代币 | `["staking_vault", statePda]` |
| `reward_vault` | PDA | 托管奖励代币 | `["reward_vault", statePda]` |
| `user_reward_account` | ATA | 用户奖励代币账户 | `getAssociatedTokenAddressSync(rewardMint, user_publicKey)` |
| `blacklist_entry` | PDA | 黑名单账户 | `["blacklist", statePda, user_publicKey]` |
| `token_program` / `system_program` / `clock` | 系统账户 | Anchor 交易必需 | — |

**说明：**

- 对于依赖用户公钥的 PDA（如 `user_stake_info`、`blacklist_entry`），种子中包含 `user_publicKey`，保证每个用户唯一。
- 对于非用户相关 PDA（如 `staking_vault`、`reward_vault`），只依赖常量种子 + `statePda`，保证全局唯一。
- ATA 是通过 `getAssociatedTokenAddressSync` 自动生成，和 PDA 不同，但也是唯一且可推导的账户。

#### 4.2.2 PDA 生成方式

**用户相关 PDA：**

```typescript
import { PublicKey } from "@solana/web3.js";

// 全局状态 PDA
const [statePda] = PublicKey.findProgramAddressSync(
  [Buffer.from("state"), stakingMint.toBuffer()],
  program.programId
);

// 用户质押信息 PDA
const [userStakeInfoPda] = PublicKey.findProgramAddressSync(
  [Buffer.from("stake"), statePda.toBuffer(), user.publicKey.toBuffer()],
  program.programId
);

// 黑名单 PDA
const [blacklistEntryPda] = PublicKey.findProgramAddressSync(
  [Buffer.from("blacklist"), statePda.toBuffer(), user.publicKey.toBuffer()],
  program.programId
);
```

**非用户相关 PDA**（如 `staking_vault` / `reward_vault`）通常与 `statePda` 绑定，可由合约写入到 state 里，或通过固定种子推导：

```typescript
const [stakingVault] = PublicKey.findProgramAddressSync(
  [Buffer.from("staking_vault"), statePda.toBuffer()],
  program.programId
);

const [rewardVault] = PublicKey.findProgramAddressSync(
  [Buffer.from("reward_vault"), statePda.toBuffer()],
  program.programId
);
```

#### 4.2.3 Token 账户（ATA）

使用 `getAssociatedTokenAddressSync` 获取唯一的 ATA：

```typescript
import { getAssociatedTokenAddressSync } from "@solana/spl-token";

const userTokenAccount = getAssociatedTokenAddressSync(stakingMint, user.publicKey);
const userRewardAccount = getAssociatedTokenAddressSync(rewardMint, user.publicKey);
```

这里 `stakingMint` 是质押代币 MyToken 的 mint 地址，`rewardMint` 是奖励代币 RewardToken 的 mint 地址。

生成 ATA 后，用户的代币就会存放在这个账户里，Anchor 程序可以直接在交易中使用它。

在 Solana 中，每种 SPL Token 都对应一个独立的账户来存放用户余额，而不是直接存在用户主账户里。

#### 4.2.4 执行 Stake / Unstake 交易

**Stake**

```typescript
const transaction = await program.methods
  .stake(new BN(convertToLamports(stakeAmount)))
  .accounts({
    user: publicKey,
    state: statePda,
    userStakeInfo: userStakeInfoPda,
    userTokenAccount: userTokenAccount,
    stakingVault: state.stakingVault,
    rewardVault: state.rewardVault,
    userRewardAccount: userRewardAccount,
    tokenProgram: TOKEN_PROGRAM_ID,
    blacklistEntry: blacklistPda,
    systemProgram: anchor.web3.SystemProgram.programId,
    clock: anchor.web3.SYSVAR_CLOCK_PUBKEY,
  })
  .rpc();
```

**Unstake**

```typescript
const {
  statePda,
  userStakeInfoPda,
  blacklistPda,
  userTokenAccount,
  userRewardAccount,
} = await createStakingAccount(publicKey);

const state = await program.account.globalState.fetch(statePda);

const transaction = await program.methods
  .unstake(new BN(convertToLamports(unstakeAmount)))
  .accounts({
    user: publicKey,
    state: statePda,
    userStakeInfo: userStakeInfoPda,
    userTokenAccount: userTokenAccount,
    stakingVault: state.stakingVault,
    rewardVault: state.rewardVault,
    userRewardAccount: userRewardAccount,
    tokenProgram: TOKEN_PROGRAM_ID,
    blacklistEntry: blacklistPda,
    clock: anchor.web3.SYSVAR_CLOCK_PUBKEY,
  })
  .rpc();
```

**对比小结：**

- **EVM**：approve → stake 两步 + allowance 管理。
- **Solana**：签名时已经指定所有账户，用户签名本身就包含授权语义，通常只需要一笔交易。

## 5. 解析事件

在 DApp 中，监听合约事件是前端实时响应链上变化的重要手段。例如，当用户质押成功时，前端可以立即更新 UI 或触发后续操作。

### 5.1 EVM

在 EVM 中，事件是 Solidity 合约中的 `event`，触发交易时会生成日志（logs），前端可以通过 RPC 或库函数监听实时事件，也可以回溯历史事件。

**实时监听事件**

使用前端库，如 wagmi 的 `useWatchContractEvent`，可以订阅指定合约和事件：

```typescript
useWatchContractEvent({
  address: STAKING_CONTRACT_ADDRESS,
  abi: stakingAbi,
  eventName: "Staked",
  // Only watch events for the current user, if userAddress is defined
  args: userAddress ? { user: userAddress } : undefined,
  onLogs(logs) {},
});
```

**回溯历史事件**

通过 RPC 或 SDK 查询历史 logs，例如 viem 提供的 `getLogs`（注意这里只是提供示例，该示例并不在我们提供的 demo 里面）：

```typescript
const logs = await client.getLogs({
  address: STAKING_CONTRACT_ADDRESS,
  event: {
    type: "event",
    name: "Staked",
    inputs: [
      { name: "user", type: "address", indexed: true },
      { name: "amount", type: "uint256", indexed: false },
    ],
  },
  fromBlock: actualFromBlock,
  toBlock: actualToBlock,
});
```

**特点总结：**

- **持久化**：事件数据存储在区块链日志中，可随时回溯。
- **索引参数（topics）**：加快前端按条件筛选事件的效率。
- **前端调用方便**：可以实时订阅，也可以查询历史数据。

### 5.2 Solana

Solana 不同于 EVM，程序（Program）本身不持久化状态，也不直接存储事件数据。事件通过**程序日志（program logs）** 输出，前端可以实时监听，但日志只在交易执行时存在，并不会长期存储在链上。

**实时监听**

使用 Anchor 的事件机制，前端通过 `program.addEventListener` 订阅：

```typescript
program.addEventListener("staked", (event, slot, signature) => {
  console.log("Staked event received:", { event, slot, signature });
});
```

**特点：**

- **临时性**：日志仅存在于交易执行期间，不可长期存储。
- **适用场景**：前端可实时响应链上变化，但回溯历史事件需依赖第三方索引服务（如 Helius、Tensor）。

## 6. 总结

通过这个 Staking Demo，我们把前两篇中的理论与抽象机制落到了一个具体的前端实现上，可以清晰地看到：

**钱包接入：**

- **EVM**：一个主流钱包（MetaMask）+ 标准组件即可；
- **Solana**：多钱包生态，Wallet Adapter 抽象层必不可少。

**状态读取：**

- **EVM**：ABI + 合约地址 + 函数名；
- **Solana**：IDL + PDA + 账户结构，借助 Anchor 可获得接近 ABI 的开发体验。

**交易写入：**

- **EVM**：approve → stake 典型双交易流程；
- **Solana**：利用账户模型和用户签名，通常一笔 stake 即可完成授权与转账。

**事件处理：**

- **EVM**：logs 持久化，天然适合做「实时 + 历史」结合；
- **Solana**：program logs 偏实时，历史依赖索引服务。

结合前言、合约篇、前端（第一部分）以及本篇实战，你已经拥有了一条从概念理解 → 合约迁移 → 前端适配 → 完整 Demo 的完整路径。通过将手续费优化、交易重试、优先费与错误分层处理等能力系统性地集成进前端工程，这套方法论本身就已经构成了一套可直接用于生产环境的 Solana DApp 前端基础设施。
