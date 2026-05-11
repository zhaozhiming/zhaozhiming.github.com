# 如何将以太坊协议迁移到 Solana——后端篇

## 文章导读

随着 Solana 生态的不断成熟，越来越多的以太坊（EVM）项目开始考虑将自身的协议迁移至 Solana，我们公司在这一领域拥有丰富的项目经验，曾主导多个以太坊协议的迁移与重构工作，积累了系统化的方法论与最佳实践。

本系列围绕智能合约层、前端交互层与后端服务层展开，在之前的合约篇和前端篇中，我们分别介绍了合约迁移的核心思维转变、限制与实战，以及前端的钱包适配、交易优化等内容，本文聚焦后端，涵盖从核心思维转变到事件同步的完整工程方案。

#### 文章导航地址

- [如何将以太坊协议迁移到 Solana ——前导篇](https://57blocks.com/blog/how-to-migrate-an-ethereum-protocol-to-solana-preamble?tab=engineering)：系统介绍了以太坊与 Solana 在账户模型、执行机制和费用体系上的根本差异
- [如何将以太坊协议迁移到 Solana ——合约篇（一）](https://57blocks.com/blog/how-to-migrate-an-ethereum-protocol-to-solana-contracts-part-1?tab=engineering)：合约开发中的核心思维转变和最佳实践
- [如何将以太坊协议迁移到 Solana ——前端篇（一）](https://57blocks.com/blog/how-to-migrate-an-ethereum-protocol-to-solana-frontend-part-1?tab=engineering)：钱包连接、交易优化、错误处理与 Jito Bundle
- [如何将以太坊协议迁移到 Solana ——后端篇](https://57blocks.com/blog/how-to-migrate-an-ethereum-protocol-to-solana-backend?tab=engineering)：事件同步、日志解析与状态管理的完整方案

## Solana 后端服务的业务价值

智能合约定义了业务规则，但受限于链上计算成本和查询能力，大部分 Web3 产品无法仅靠合约完成业务闭环，比如历史数据统计、积分与奖励计算、用户行为追踪等业务需求，都需要链下后端来承接。

在 EVM 生态中，构建这类后端相对简单，节点对合约事件做了索引，后端通过 `eth_getLogs` 就能直接查询，但 Solana 没有原生的结构化事件系统，交易日志只是非结构化的字符串，事件类型无法直接过滤，这意味着后端必须从**被动等待事件推送**转变为**主动拉取交易并解析日志**，这个转变是 Solana 后端开发的核心挑战，也是本文贯穿始终的主线。本文以 Solana Staking 协议为贯穿案例，它是一个基于 Anchor 开发的质押奖励系统，合约和前端部分已在本系列前文介绍，文中出现的 `PoolConfig`、`PoolState`、`UserStakeInfo` 等账户结构均来自该项目，完整后端代码可参考 [solana-backend](https://github.com/57blocks/evm-to-solana/tree/main/backend/solana-backend) 仓库。

## 核心思维转变

### Solana 链上事件解析

EVM 后端的工作流很自然，合约触发事件，节点构建事件索引，后端按类型过滤查询。而 Solana 不支持这套模式，Anchor 的 `#[event]` 宏只是把数据写进交易日志，链上没有按事件类型查询的能力，后端只能以账户为入口，主动拉取交易，从 `logMessages` 中解析。

差异的根源很简单，EVM 存历史，Solana 不存。EVM 的归档节点记录了每个高度的状态，漏了日志还能回头查，而 Solana 只保留当前值，写一笔覆盖一笔。所以 Solana 后端能依赖的唯一线索就是交易日志，必须自己从日志中重建事件，没法像 EVM 那样直接用现成的索引服务。

### 账户选址

后端通过 `getSignaturesForAddress` 监控特定账户参与的交易，因此监听哪个账户直接决定了数据同步的效率，下面列出三种典型的选址策略：

- 监控全局状态：如果想追踪协议 TVL 变动或管理员更新奖励率，直接监听 `PoolConfig` PDA，这天然过滤掉所有普通用户的质押操作
- 追踪特定用户：如果想监听某个用户的行为，就将该用户的钱包地址或 `UserStakeInfo` PDA 作为入口，这相当于一条极低噪音的专属通道
- 监控全网代币流转：常规思路是监控代币地址，但这里有一个容易踩的坑，Solana 标准转账指令的参数中只包含源账户和目标账户，不强制携带代币地址，这意味着代币地址根本不会出现在大多数转账交易的账户列表中，因此这类需求应直接调用第三方索引服务（如 Solscan 或 Helius）的 Token Transfer API

## 你需要注意的 Solana 事件过滤的限制

### 分页游标必须是真实签名

`getSignaturesForAddress` 是事件抓取的核心接口，但它的分页参数 `before` 和 `until` 不接受 Slot 数值或时间戳，只能传入真实的交易签名，如果你按区块范围同步，不能直接把起止 Slot 扔进去，而需要先用 `getBlock` 扫描目标区间，找到包含交易的有效区块后，提取其中的签名作为分页游标。如果目标区间恰好全是空块，你还需要向前或向后逐个扫描，直到碰到非空块为止。

### 失败交易仍可能包含事件日志

EVM 上交易回滚后事件也随之撤销，但 Solana 不同，即使交易最终执行失败，程序在失败之前输出的日志仍然会保留在链上，RPC 也会返回这些 `logMessages`，后端如果不加过滤直接解析，会把实际上并未发生的状态变更写进数据库，造成链下数据与链上状态不一致。唯一的防护措施是在解析流水线的入口检查 `tx.meta.err`，只有 `null` 时才允许下游处理。

### 账户变化订阅无法替代事件监听

Solana 提供了 WebSocket 订阅账户变化的机制，但它只能感知账户数据被修改，无法直接告诉你发生了什么事件，事件语义仍然需要从账户差异和日志中推断，而且订阅连接可能中断，需要自行处理重连和数据补偿，不适合大规模账户或高吞吐场景。

## Solana 后端开发最佳实践

Web3 后端的两大核心任务是**事件索引**和**自动化执行**。事件索引负责从链上持续拉取历史数据，把交易日志解析为结构化的业务事件，写入数据库供查询和分析。自动化执行则通过定时任务主动触发链上操作，可以只是读取状态并告警，也可以调用合约完成配置更新或奖励结算。两者虽然都用 cron 调度，但职责不同、互不干扰，下面分别展开。

### 事件索引

Solana 没有按事件类型查询的接口，后端需要一套完整的事件同步流水线，从合约端的事件设计，到服务端的交易获取、日志解析、游标持久化，每个环节都有需要留意的地方。

#### 合约端：让事件携带更多数据

在设计 Anchor 合约时，尽量让事件携带完整的业务上下文，即使链上逻辑不需要某个字段，只要后端业务用得上，就应该放进事件里。

以 Staking 协议为例，如果 `Staked` 事件只包含三个基础字段：

```rust
#[event]
pub struct Staked {
    pub user: Pubkey,
    pub amount: u64,
    pub timestamp: i64,
}
```

后端解析出事件后，要想知道用户当前的总质押量和累计奖励，就不得不额外调用一次 RPC 读取 `UserStakeInfo` 账户。

```typescript
const [userStakePda] = PublicKey.findProgramAddressSync(
  [Buffer.from("stake"), statePda.toBuffer(), user.toBuffer()],
  programId
);
const accountInfo = await connection.getAccountInfo(userStakePda);
const totalStaked = coder.accounts.decode(
  "UserStakeInfo",
  accountInfo.data
).amount;
```

如果合约在事件中多带两个字段：

```rust
#[event]
pub struct Staked {
    pub user: Pubkey,
    pub amount: u64,
    pub timestamp: i64,
    pub total_staked: u64,
    pub reward_debt: i128,
}
```

后端就能从事件解析结果中直接拿到这些数据，省掉一次 RPC 调用，Solana 的交易日志存储成本很低，用日志空间换后端的查询效率，这是一笔划算的交易。

#### 服务端：完整的事件同步流水线

后端的事件同步流程可以拆成四步，确定起始签名、拉取签名列表、拉取并解析交易、分批持久化进度。

**第一步，**Slot 转签名作为分页游标。

分页游标必须是真实签名这件事前面已经解释过，这里直接看代码。

```typescript
private async getStartTransactionSignature(
  connection: Connection,
  lastSyncedSlot: number
) {
  const blockHistories = await connection.getBlocks(
    lastSyncedSlot,
    lastSyncedSlot + this.config.slotToCheck
  );

  for (let i = 0; i < blockHistories.length; i++) {
    // getBlock is a heavy operation, taking about 5 seconds
    const startBlock = await connection.getBlock(blockHistories[i], {
      maxSupportedTransactionVersion: 0,
      rewards: false,
      transactionDetails: "accounts",
    });

    if (!startBlock) {
      throw new Error(`Failed to fetch block at slot ${blockHistories[i]}`);
    }

    const transactions = startBlock.transactions;
    if (transactions.length > 0) {
      return {
        startBlockSlot: blockHistories[i],
        startSignature: transactions[0].transaction.signatures[0],
      };
    }
  }

  throw new Error(
    `No valid transactions found between slot ${lastSyncedSlot} and ${
      lastSyncedSlot + this.config.slotToCheck
    }`
  );
}
```

如果目标 Slot 恰好是空块，需要逐个向后扫描相邻 Slot，直到找到包含交易的有效区块，反向查找结束签名的方法与此对称，只是扫描方向相反。

**第二步，**拉取交易签名列表。

拿到起止签名后，通过 `getSignaturesForAddress` 按账户拉取相关交易签名，接口返回的数据是最新的排在最前面，需要手动翻转为时间正序。

```typescript
let beforeSignature = endBlockInfo.endSignature;
let sigsCount = this.config.signaturesPerBatch;

while (sigsCount >= this.config.signaturesPerBatch) {
  const sigs = await connection.getSignaturesForAddress(
    new PublicKey(monitorAddress),
    {
      limit: this.config.signaturesPerBatch,
      until: startBlock.startSignature,
      before: beforeSignature,
    }
  );

  sigsCount = sigs.length;
  if (sigsCount > 0) {
    beforeSignature = sigs[sigsCount - 1].signature;
    sigList.unshift(...sigs.reverse().map((sig: any) => sig.signature));
    await sleep(this.config.sleepTime);
  }
}
```

- 用 `before` 参数向后翻页，每次取最新一批之前的一页
- 拿到后先 `reverse` 再 `unshift`，保证列表整体是时间正序
- 每次请求后 `sleep`，控制对 RPC 节点的访问频率

**第三步，**拉取交易、过滤失败、解析事件。

拿到签名列表后，分批并发调用 `getParsedTransaction` 获取交易详情，每一批内部用 `Promise.all` 并行，批与批之间 `sleep`。

```typescript
for (let i = 0; i < sigList.length; i += batchSize) {
  const promises = [];
  for (const sig of sigList.slice(i, i + batchSize)) {
    promises.push(
      this.solanaEventsService.parseTransactionEvents(
        this.chainId,
        sig,
        eventsParser
      )
    );
  }
  const eventsList = await Promise.all(promises);
  for (const events of eventsList) {
    parsedEvents.push(...events);
  }
  await sleep(this.config.sleepTime);
}
```

单个交易的解析函数内部做了三件事，拉取交易详情、检查是否失败、调用事件解析器。

```typescript
async parseTransactionEvents(
  chainId: number,
  sig: string,
  eventsParser: TransactionEventsParser
): Promise<BaseEvent[]> {
  const connection = this.solanaConnections.getConnection(chainId);
  const ptx = await connection.getParsedTransaction(sig, {
    maxSupportedTransactionVersion: 0,
  });

  if (!ptx) {
    console.error(`Can not get the parsed transaction from rpc, txhash: ${sig}`);
    return [];
  }

  // Check tx.meta.err first — this is the only guardrail against writing dirty data
  if (ptx.meta?.err) {
    return [];
  }

  return eventsParser.parseEvents({ tx: ptx, sig });
}
```

EVM 上失败的交易不会产生事件，但 Solana 不同，即使交易最终执行失败，失败前输出的日志仍然记录在链上，不加过滤地解析会把并未实际发生的状态变更写进数据库，所以这个检查不能跳过。

**第四步，**分批持久化同步进度。

每处理完一个批次的交易就更新同步游标，不要等全量同步完成才写。

```typescript
async onBatchFetched(batchResult) {
  const userActivities = [];
  for (const event of batchResult.events) {
    try {
      const activity = this.convertEventToUserActivity(event, poolConfig);
      userActivities.push(activity);
    } catch (error) {
      console.warn(`Failed to convert event ${event.transactionHash}:`, error);
    }
  }

  for (const activity of userActivities) {
    await this.userActivityRepository.save(activity);
  }

  const updatedSyncStatus = syncStatus.updateLastSyncBlock(
    batchResult.endBlockNumber
  );
  await this.syncStatusRepository.save(updatedSyncStatus);
}
```

如果等到全量同步结束才一次性写游标，中途崩溃就意味着进度全部丢失，分批持久化的设计让每次重启都能从上次的 `lastSyncBlock` 继续，不用从头重跑。

### 自动化任务

除了事件同步，Solana 后端通常还需要承担另一类任务，定期触发链上操作，比如检查状态并告警、调用合约更新配置等。这类任务与事件索引互补，事件索引关注历史重放，自动化任务关注定时触发。

以本项目为例，`PoolBalanceMonitorService` 通过 cron 定时检查每个 Staking Pool 的奖励金库余额，余额低于阈值时告警，恢复后自动解除。

#### 读取链上当前状态

自动任务的第一步是拿到链上数据，通过推导 PDA 读取奖励金库的 SPL Token 余额，代码如下

```typescript
async getRewardVaultBalance(poolConfig: string): Promise<RewardVaultBalance> {
  const programId = new PublicKey(StakingIDL.address);
  const poolConfigPda = new PublicKey(poolConfig);
  const [rewardVaultPda] = PublicKey.findProgramAddressSync(
    [Buffer.from("reward_vault"), poolConfigPda.toBuffer()],
    programId
  );

  const connection = this.solanaConnections.getConnection(this.chainId);
  const balance = await connection.getTokenAccountBalance(rewardVaultPda);

  return {
    rewardVault: rewardVaultPda.toBase58(),
    uiAmount: balance.value.uiAmount,
    rawAmount: balance.value.amount,
  };
}
```

这个模式很通用，推导 PDA、查账户、返回结果，你可以换成读取任意 PDA 或普通账户的状态。

#### 阈值告警与去重

拿到余额后与阈值比较，低于阈值时创建告警，恢复时自动关闭。

```typescript
private async checkPoolBalance(poolConfig: string): Promise<void> {
  const threshold = this.config.getOrThrow<number>("REWARD_BALANCE_THRESHOLD");
  const balance = await this.rewardVaultReader.getRewardVaultBalance(poolConfig);

  if (balance.uiAmount >= threshold) {
    // Balance recovered, resolve any open alerts
    const openAlert = await this.alertRepository.findOpenAlert(
      poolConfig,
      LOW_REWARD_BALANCE
    );
    if (openAlert) {
      await this.alertRepository.resolveOpenAlert(poolConfig, LOW_REWARD_BALANCE);
    }
    return;
  }

  // Balance below threshold; check for existing open alerts to avoid duplicates
  const openAlert = await this.alertRepository.findOpenAlert(
    poolConfig,
    LOW_REWARD_BALANCE
  );
  if (openAlert) {
    return;
  }

  await this.alertRepository.save({
    poolConfig,
    alertType: LOW_REWARD_BALANCE,
    message: `Reward vault balance is below threshold`,
    threshold: String(threshold),
    actualValue: String(balance.uiAmount),
    createdAt: Math.floor(Date.now() / 1000),
    resolved: false,
  });
}
```

这里有两个需要留意的地方。

- 告警去重，创建前先查是否有同类型未解决告警，防止重复发送
- 自动恢复，余额回升后主动关闭告警，否则运维人员无法区分已经好了和还没看过

## 实战案例：从开发到部署

前面几章分别介绍了事件同步、限制与应对、以及两大核心任务的实现细节，现在把这些点串起来，演示一个完整后端系统的搭建过程。

### 项目概述

solana-backend 是为 Solana Staking 协议构建的后端服务，基于 NestJS 和 TypeScript，完整代码可参考 [solana-backend](https://github.com/57blocks/evm-to-solana/tree/main/backend/solana-backend) 仓库，它主要承担三类职责：

- 事件索引：通过 cron 定时从 Solana 链上拉取 Staked、Unstaked、RewardsClaimed 等事件，解析后写入 SQLite 数据库
- 链上查询：对外提供 Pool 配置、Pool 状态和用户质押数据的实时查询接口，包括链下计算的待领取奖励
- 自动监控：定时检查各 Pool 的奖励金库余额，低于阈值时告警，恢复后自动解除

本章聚焦从零搭建这样一个后端系统的完整过程，包括项目结构、核心模块、测试和 Docker 部署，事件同步的完整流水线已在上一章详细介绍，这里不再重复。

### 项目结构

```
solana-backend/
├── src/
│   ├── autotask/          # 定时监控：余额检查 + 告警
│   ├── event-fetch/       # 事件同步：签名拉取 + 交易解析
│   ├── indexer/           # cron 调度，启动同步任务
│   ├── repositories/      # 数据库操作 + 链上账户读取
│   └── domain-services/   # 链下奖励计算
├── prisma/                # 数据库 Schema + SQLite
├── Dockerfile
└── docker-compose.yml
```

- `event-fetch` 负责与 Solana RPC 交互，获取交易签名并解析事件
- `indexer` 通过 NestJS 的 cron 定时触发 FetchScheduler
- `autotask` 运行独立的 cron 流水线，与事件索引互不干扰
- `domain-services` 在链下用 BigInt 复刻合约中的奖励计算公式
- `repositories` 封装 Prisma 操作和链上 PDA 查询

### 核心实现

前面`最佳实践`章节已经展示了事件同步流水线的完整代码，这里补充两个前面没展开的部分——链下奖励计算和同步位点初始化。

#### 链下奖励计算

Solana Staking 合约中的奖励基于 [MasterChef](https://github.com/sushiswap/masterchef) 模型，每次操作前更新 `acc_reward_per_share`，根据用户质押份额计算待领取奖励，后端用 BigInt 在链下复刻同一套公式，避免每次查询用户状态时都调用 RPC。

```typescript
// src/domain-services/RewardCalculationService.ts
projectedAccRewardPerShare(
  state: PoolState,
  config: PoolConfig,
  now: bigint
): bigint {
  if (now <= BigInt(state.lastRewardTime) || state.totalStaked === 0n) {
    return state.accRewardPerShare;
  }
  const reward = (now - BigInt(state.lastRewardTime)) * config.rewardPerSecond;
  const accIncrement = (reward * ACC_REWARD_PRECISION) / state.totalStaked;
  return state.accRewardPerShare + accIncrement;
}

pendingRewards(
  user: { amount: bigint; rewardDebt: bigint },
  state: PoolState,
  config: PoolConfig,
  now: bigint
): bigint {
  const acc = this.projectedAccRewardPerShare(state, config, now);
  const accumulated = (user.amount * acc) / ACC_REWARD_PRECISION;
  const pending = accumulated - user.rewardDebt;
  return pending > 0n ? pending : 0n;
}
```

后端计算公式对应 Solana Staking 合约中 [utils.rs](https://github.com/57blocks/evm-to-solana/blob/main/contract/solana-staking/programs/solana-staking/src/utils.rs) 文件中的方法，输入输出均为 BigInt，避免 JavaScript 浮点精度问题，对外的查询接口直接调用 `pendingRewards`，用户不需要理解链上公式也能拿到待领取数量。

#### 同步位点初始化

后端通过 `SyncStatus` 表跟踪每个 Pool 的同步进度。`db:init` 脚本读取环境变量 `POOL_CONFIGS`，将每个 Pool 的起始 Slot 写入数据库：

```typescript
// scripts/db/init-db.ts
const entries = process.env.POOL_CONFIGS.split(",");
for (const entry of entries) {
  const [poolConfigPda, slot] = entry.split(":");
  await prisma.syncStatus.create({
    data: {
      poolConfig: poolConfigPda,
      lastSyncBlock: Number(slot),
    },
  });
}
```

每处理完一批事件就更新 `lastSyncBlock`，即使服务中途崩溃或重启，下次启动会从上次持久化的位置继续同步，不会重复处理已入库的事件，也不会漏掉未处理的。

### 测试

项目使用 [Jest](https://jestjs.io/) 做单元测试，由于 Solana RPC 和数据库操作都通过接口注入，测试中用 mock 替换即可，不需要连接真实节点。

`EventIndexingService` 的测试验证两件事：cron 任务是否在模块初始化时注册，以及同一时间是否只允许一个同步任务在执行。

```typescript
// src/indexer/event-indexing.service.spec.ts
it("registers the event-indexing cron job on module init", () => {
  service.onModuleInit();
  expect(schedulerRegistry.addCronJob).toHaveBeenCalledWith(
    "event-indexing",
    expect.any(Object)
  );
});

it("blocks overlapping sync cycles", async () => {
  service.onModuleInit();
  let resolveRun: () => void;
  mockRunOnce.mockReturnValue(
    new Promise<void>((resolve) => {
      resolveRun = resolve;
    })
  );
  const firstRun = service.tick();
  const secondRun = service.tick();
  // First cycle still running, second is skipped
  expect(mockRunOnce).toHaveBeenCalledTimes(1);
  resolveRun!();
  await Promise.all([firstRun, secondRun]);
});
```

cron 定时触发同步任务，但一次同步任务可能耗时超过一个 cron 周期，下一个任务可能在前一个没跑完时就进来了，`isRunning` 标记保证后面的任务被直接跳过，同一时刻只有一个同步任务在跑。

`PoolBalanceMonitorService` 的测试遍历奖励余额告警的完整生命周期：

```typescript
// src/autotask/pool-balance-monitor.service.spec.ts
it("goes through the full alert lifecycle: create → resolve → create again", async () => {
  // Balance below threshold → create alert
  rewardVaultReader.getRewardVaultBalance.mockResolvedValue({
    rewardVault: "reward-vault",
    uiAmount: 5,
    rawAmount: "5",
  });
  await service.tick();
  expect(alertRepository.save).toHaveBeenCalledTimes(1);

  // Balance recovered → resolve alert
  rewardVaultReader.getRewardVaultBalance.mockResolvedValue({
    rewardVault: "reward-vault",
    uiAmount: 15,
    rawAmount: "15",
  });
  await service.tick();
  expect(alertRepository.resolveOpenAlert).toHaveBeenCalledTimes(1);

  // Balance below threshold again → create new alert
  rewardVaultReader.getRewardVaultBalance.mockResolvedValue({
    rewardVault: "reward-vault",
    uiAmount: 3,
    rawAmount: "3",
  });
  await service.tick();
  expect(alertRepository.save).toHaveBeenCalledTimes(2);
});
```

三个场景完整覆盖了告警去重和自动恢复的逻辑，mock 的 `rewardVaultReader` 和 `alertRepository` 替代了真实 RPC 和数据库。

### 项目部署

项目使用 Docker 进行多阶段构建，将编译和运行分离，减小最终镜像体积。

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
RUN apk add --no-cache python3 make g++ && corepack enable
COPY package.json pnpm-lock.yaml ./
COPY prisma ./prisma
COPY idl ./idl
RUN pnpm install --frozen-lockfile
COPY src ./src
RUN pnpm db:generate && pnpm build

FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
RUN corepack enable
COPY --from=builder /app/package.json /app/pnpm-lock.yaml ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/idl ./idl
CMD ["node", "dist/src/main.js"]
```

`docker-compose.yml` 挂载 `./prisma` 目录，保证 SQLite 数据库在容器重启后不丢失：

```yaml
services:
  solana-backend:
    build: .
    env_file: .env
    ports:
      - "3000:3000"
    volumes:
      - ./prisma:/app/prisma
    restart: unless-stopped
```

部署流程：

```bash
docker compose build solana-backend
docker compose run --rm solana-backend ./node_modules/.bin/prisma db push
docker compose run --rm solana-backend node dist/scripts/db/init-db.js
docker compose up -d
```

`db push` 创建表结构，`db:init` 写入同步位点，之后 `up -d` 启动服务，日常用 `docker compose logs -f solana-backend` 查看运行日志。

## 总结

本文围绕 Solana 后端开发中最核心的挑战展开，Solana 交易日志缺乏结构化事件系统，后端无法像 EVM 那样直接按事件类型查询，必须从**被动监听**切换为**主动拉取和解析**。

我们梳理了事件同步流水线的完整四步流程，从 Slot 到签名的锚点定位，到分批并发拉取交易，再到解析前先过滤掉失败交易，最后是分批持久化保证断点续传，同时介绍了自动化任务这一后端常见的另一类职责，包括链上状态读取、阈值告警和告警去重与恢复。

通过 solana-backend 项目，我们展示了这些模式如何落地为可运行的系统，包括模块划分、BigInt 链下奖励计算、Jest mock 测试和 Docker 多阶段构建部署。

EVM 后端更像在消费一个已经为你准备好的数据层，而 Solana 后端需要自己搭建这条数据生产线。一旦你接受了这个定位，Slot 转签名、倒序反转、分批持久化这些步骤就不再是门槛，而是你可以精确控制数据走向的杠杆。

本系列从前导篇到这里，走完了从 EVM 到 Solana 迁移的完整链路。合约篇梳理了 Rust 与 Solidity 的思维切换，前端篇解决了钱包适配、交易优化和错误处理，本文则补齐了后端这一环。当合约、前端、后端三层的设计都顺着 Solana 的原生模型展开时，迁移就不再是移植，而是一次基于新约束的重新设计。

## 参考

- [Solana RPC Overview](https://solana.com/docs/rpc)
