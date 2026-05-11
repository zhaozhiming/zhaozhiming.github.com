# 如何将以太坊协议迁移到Solana——后端篇

## 文章导读

从 EVM 迁移到 Solana，后端开发面临着从“轻量级接入”到“重量级构建”的巨大转变 。为了帮助 开发者构建高可靠的 Solana 后端，本文将从底层架构差异、常见工程陷阱及最佳实践到业务代 码实战等多方面进行全面拆解 。全文结构如下，读者可根据自身当前情况和工程进度按需跳读：

- 第一部分：核心思维与底层差异（包含“Solana后端服务的业务价值”和“核心思维转变”章 节）：本部分解释了为何在 Solana 中无法像 EVM 那样直接订阅结构化事件 。探讨了从 EVM 的“被动监听”向 Solana 的“主动拉取与解析交易”的核心思维转变 。同时分析了 Solana 以账户为中心的数据模型对事件处理的根本性影响 。推荐刚接触 Solana 架构设 计的开发者优先阅读本部分。

- 第二部分：你需要注意的Solana事件过滤的限制：本部分直击开发痛点，深入剖析了 Solana 多步查询模式带来的高复杂度 。重点梳理了 RPC 接口返回倒序数据的处理难题 、容易被忽视的签名锚点（Signature Anchoring）有效性陷阱 、高吞吐带来的 RPC 限流压 力 ，以及无法读取历史 Slot 状态的限制 。适合正在搭建数据管道或排查同步问题的工程 师重点排雷。

- 第三部分：Solana后端开发最佳实践：本部分说明了为何 Solana 合约通常需要后端作为 “保姆”来触发自动化任务（Cranks）。总结了从数据流水线设计、异构日志解析到状态断点 续传的后端架构最佳实践 。适合负责系统架构设计和追求高可用性的开发人员阅读。

- 第四部分：Staking 历史记录与数据聚合服务：本部分结合真实的 Solana Staking 协议，提 供了一套开箱即用的后端代码实现 。具体展示了如何通过推导 PDA 实时查询链上全局与 用户状态 ，以及如何编写支持断点续传、抗 RPC 限流的多步事件同步逻辑 。适合需要参 考具体代码落地的开发者直接复制或借鉴。

## Solana后端服务的业务价值

智能合约定义了不可篡改的业务规则，但受限于链上计算成本、执行模型以及查询能力，绝大多 数 Web3 产品无法仅依赖链上逻辑完成完整的业务闭环 。复杂的数据聚合、历史统计、积分与奖 励计算以及用户体验优化，通常必须由链下后端系统承担 。 在实际应用中，无论是 DeFi 协议中的 staking 奖励计算、NFT 项目的空投分配，还是跨链桥与聚 合器对用户行为的持续追踪，都需要后端服务对链上活动进行长期同步与解析，并在链下维护派 生的业务状态 。这些状态并不参与链上共识，但却直接决定了产品功能的可用性与用户体验 。 借助于 EVM 成熟的生态系统与开箱即用的事件索引设施，构建后端服务相对简单 。然而，要在 Solana 链上构建后端服务，则要求开发者完成从“轻量级接入”到“自建索引器”的核心思维转变 。 因为 Solana 缺乏直接读取任意历史状态快照的能力，交易及其执行日志成为了还原历史业务状 态的唯一凭证 。下面我们将逐一分析 Solana 独特的账户模型及其工程限制，以及后端数据抓取 与处理的最佳实践。

## 核心思维转变

solana链上事件解析 在 EVM 生态中，后端系统通常围绕合约事件构建 。智能合约通过 emit 主动输出结构化事件，节 点对日志进行索引，后端可以通过标准 RPC 接口直接查询和过滤事件 。这形成了一种以事件为 中心的“被动监听、主动查询”模式 。当业务迁移至 Solana 时，这一模式不再成立 。这种转变的深 层原因在于链上状态时间维度的缺失 ：

- EVM 的数据安全网：在 EVM 中，归档节点允许我们通过指定 blockTag 查询任意历史高 度的合约状态，即使漏掉了事件也能回溯查询 。

- Solana 的状态易失性：Solana 标准 RPC 通常仅支持查询账户的“当前状态” 。一旦交易执 行完成且状态被更新，旧的状态就从链上“消失”了 。

除此之外，由于 Solana 缺乏原生可索引的事件系统，交易（Transaction）及其附带的执行日志成 为了还原历史业务状态的唯一凭证 。事件在 Solana 中更像是一种“派生数据”，需要后端从非结 构化的日志字符串中重新读取链上状态 。 这就要求后端系统必须采用一种“主动获取、主动解析”的模式，接下来我将介绍solana后端事件 同步系统的典型范式供大家参考。

- 确定账户过滤入口：Solana 无法直接按事件类型查询 。后端必须以核心业务账户（如程序 的 PDA、目标钱包或相关 Token Account）作为索引维度。链上账户成为抓取和过滤链上 事件的主要入口 。

- 计算真实签名锚点：确定好需要监控的链上账户地址后，我们需要调用RPC提供的接口来 获取该地址上指定区块范围内的所有交易签名。 由于获取签名的 API ( getSignaturesForAddress ) 强制按时间倒序返回，且分页参数（ before/until）不接受 Slot 数值，只能使用真实存在的交易签名 。在按区块范围同步前，必须先调用 getBlock 扫描并 跳过空块，精准提取出该区间的首尾有效交易签名，作为启动分页回溯的“锚点” 。

- 批量拉取与主动流控：获得目标签名列表后，通过 getParsedTransaction 批量获取包含完 整日志与账户变更的交易详情 。由于 Solana 数据密度极大，此环节建议引入并发控制、 请求队列以及主动休眠机制，避免触发 RPC 节点的频率限流 。

- 异构日志解析与语义重构：在拿到交易数据后，需将交易日志（ logMessages ）视为“事件 源” 。 对于使用Anchor框架开发的合约，可以利用Anchor提供的解码器自动解码事 件数据 ；对于只有原生事件的官方程序（如 SPL Token），则需通过特征匹配手动提 取。

- 游标持久化与断点续传：Solana 无法直接查询任意历史高度的账户状态快照 。因此，每次 成功解析并写入业务数据后，必须将当前的同步游标（如 lastSyncBlock 或最新处理的 Signature ）持久化到本地数据库中。这是保证服务重启或网络崩溃后能够确定性断点续 传、防止数据丢失或重复计算的唯一生命线 。

账户选址 Solana 采用以账户（Account）为中心的状态模型，程序本身不持有状态，所有持久化数据均存储 在外部独立账户中 。这种设计与 EVM 以合约为中心、直接由合约产生结构化事件的模式存在根 本差异 。由于缺乏原生可索引的事件系统，Solana 的事件处理无法直接围绕“程序事件”展开，而 必须全面转向“账户与交易”层面 。 因此在设计solana后端系统时，对链上监控地址的选址变得尤 为重要。 从实现细节来讲，后端只能通过 getSignaturesForAddress 监控特定账户参与的交易 。因此，“监 听哪个账户”直接决定了数据同步的效率、成本与准确性。根据不同的业务目标选择合适的监控地 址，可以大幅过滤链上噪音，实现事半功倍。 以本文的 Solana Staking 协议为例，不同的监控目的对应着截然不同的选址策略：

- 监控全局状态：如果业务是统计协议的整体 TVL 变动，或是监控管理员是否更新了全局奖 励率，最佳选址是协议唯一的 GlobalState PDA 。以此为入口，可以天然屏蔽掉海量的普 通用户个人质押行为，精准捕获全局级别的关键交易。

- 追踪特定用户行为：如果业务需求是为某个 VIP 用户推送其质押（Staked）或领取奖励（ RewardClaimed）的实时通知，应当直接将该用户的钱包地址或其专属的 UserStakeInfo PDA 作为监控入口 。这种基于具体实体的选址，相当于开辟了一条极低噪音的专属数据 通道。

- 监控全网代币流转： 假设你想监控协议代币在所有用户之间的流转情况， 直接能想到的是 监控Token全局的Token Mint地址 。但这是一个陷阱：在 Solana 标准程序的 Transfer 指令 中，参数仅包含源账户（Source）和目标账户（Destination），通常并不强制携带 Token Mint 地址 。这意味着 Mint 地址根本不会出现在大多数转账交易的账户列表中。面对此类需求 ， 可以直接调用第三方索引服务商（如 Solscan、Helius）提供的 Token Transfer API来获取 全网代币的流转信息 。

## 你需要注意的Solana事件过滤的限制

高级索引服务的成熟度差距 在 EVM 生态中，Substreams、The Graph、Alchemy Enhanced APIs 等高级数据索引服务已经非 常成熟。开发者可以轻松通过这些服务实现实时事件订阅、复杂历史查询和结构化数据映射，极 大简化了后端架构。相比之下，Solana 生态在这一领域虽然进展迅速，但仍存在明显的成熟度差 距。虽然 StreamingFast (Substreams) 和 The Graph 等协议已开始支持 Solana，部分 RPC 提供 商（如 Helius、Triton）也推出了增强版索引 API，但整体生态仍处于早期阶段。 RPC功能限制和API复杂性 getSignaturesForAddress 是 Solana 事件抓取中最核心的 RPC 接口之一，但它有一个容 易被低估却影响深远的特性：返回的交易签名按时间倒序排列（最新在前）。这一设计在区块浏览 和实时查询场景下是合理的，但在构建稳定、可重放的事件同步系统时，会显著增加工程复杂度。 签名锚点的有效性限制：是一个极易被忽视的工程陷阱。在实际业务中，我们通常基于时间轴或区 块高度（Slot / Block）来确定同步范围，比如“抓取 Slot 20000 到 20100之间的所有事件”。然而， getSignaturesForAddress 接口的分页参数（before / until）不接受 Slot 数值或时间戳， 而必须传入链上真实存在的、与该账户相关的有效交易签名（Transaction Signature）。如果你传 入一个伪造的 Hash 或不存在于该账户历史中的签名，RPC 将无法定位游标，直接返回空数组或 报错。 无法读取指定 Slot 的状态 在 EVM 中，可以通过 eth_call 或 Archive Node 按指定区块高度查询合约状态，这不仅便于事 件回放，也方便链上状态验证和数据一致性检查。Solana 则不同：RPC 接口只能返回当前或最 终确认的账户状态，无法直接按历史 Slot 查询任意账户快照。链上并未保存完整的账户快照，每 个 Slot 仅记录交易执行记录和日志。因此，要获取历史状态，需要通过回放该 Slot 的所有交易或 自建索引器逐步重建账户状态。这一限制对事件处理有直接影响：Solana 后端无法简单地通过 “读取历史账户状态”来验证或重建事件，只能依赖 交易日志 ( logMessages) + 账户参与信息 + 回放逻辑 来推导事件，从而增加了工程复杂度和系统开销。 失败交易的事件处理 在EVM中，失败的交易不会产生事件（因为交易回滚，所有状态变化都被撤销）。但在Solana中， 失败交易仍然可能包含事件信息，这给事件处理带来了额外的复杂性。 其他限制与常见坑点 历史数据查询限制 Solana RPC 对历史数据的可访问性有限：节点通常只保留最近一段时间的数据，过早的 Slot 可 能无法查询，或需要访问归档节点。由于官方 RPC 不提供统一的历史索引能力，长期历史数据通 常只能通过自建索引器或第三方服务维护。 交易可能被跳过（skipped） 在网络拥堵或交易优先级较低的情况下，交易可能被标记为 skipped 或未被打包执行。这会直接 影响事件抓取的完整性，而 RPC 层面又很难准确判断是否遗漏了某些本应发生的业务行为，通 常需要额外的完整性校验或状态对账机制。 账户变化订阅的局限性 Solana 支持通过 WebSocket 订阅账户变更，但该机制只能感知账户数据变化，无法直接订阅“事 件”；事件语义仍需通过账户差异和日志推断。此外，订阅连接可能中断，需要自行处理重连与数据 补偿，也不适合大规模账户或高吞吐事件场景。

## Solana后端开发最佳实践

1. 数据同步：多步查询与锚点策略

- 两步走策略： 必须先通过 getSignaturesForAddress 获取交易签名列表，再通过 getParsedTransaction 批量获取交易详情，因为 Solana 没有直接按时间范围查询事 件的接口 。

- 倒序与锚点处理： getSignaturesForAddress 返回的是倒序（最新在前）数据。同步历 史数据时，必须利用 before 参数向旧数据回溯，且必须确保传入的签名是链上真实有效 的，通常需要先定位一个“锚点签名”才能启动分页 。

- 并发与限流： 面对 Solana 的海量数据，必须实现请求队列与并发控制，避免触发 RPC 限 流 。

- Slot 到 Signature 的转换：在启动分页同步前，后端必须先调用 getBlock(targetSlot) 获取目标起始/结束区块的完整数据，并从中提取出第一笔（或 最后一笔）交易的签名，将其作为合法“锚点”传入 getSignaturesForAddress 才能启 动游标。

- 处理空块与跳过块（Skipped Slots）：更复杂的是，如果目标 Slot 恰好是一个空块（没有包 含任何交易）或者是一个被网络跳过的无效块（Skipped Slot）， getBlock 将无法返回有 效签名。此时，后端程序必须向后（或向前）逐个扫描相邻的 Slot，直到找到一个包含交易 的有效区块，才能提取出合法的起始签名。

Solana典型的events抓取流程

```
// Solana事件抓取：需要多步操作
// 第一步：获取交易签名列表
const signatures = await connection.getSignaturesForAddress(
new PublicKey(accountAddress),
{ limit: 1000, before: lastSignature }
);
// 第二步：批量获取交易详情
const transactions = await Promise.all(
signatures.map(sig =>
connection.getParsedTransaction(sig.signature, {
maxSupportedTransactionVersion: 0
})
)
);
// 第三步：从交易中解析事件
for (const tx of transactions) {
const events = eventParser.parseLogs(tx.meta?.logMessages ?? []);
// 处理事件...
}
```

倒序返回的影响：由于接口天然以“最新交易”为起点，历史数据同步必须采用“从新向旧回溯”的方 式，通过 before 参数逐页向前查询；分页过程中需要持续维护 lastSignature 作为游标，用 于断点续传和避免重复抓取；获取到的交易与事件天然是倒序的，若业务逻辑依赖时间正序（如状 态机、累计计算、回放执行），还需要在本地额外排序或整体反转；这些都意味着更复杂的状态管 理和更高的出错风险。 代码示例：倒序返回

```
let lastSignature: string | undefined = undefined;
const allSignatures: string[] = [];
while (true) {
const signatures = await connection.getSignaturesForAddress(
accountAddress,
{ limit: 1000, before: lastSignature }
);
if (signatures.length === 0) break;
allSignatures.push(...signatures.map(s => s.signature));
lastSignature = signatures[signatures.length - 1].signature; // 最旧的一条
}
// 如需按时间正序处理
const sortedSignatures = allSignatures.reverse();
```

以下代码示例展示了如何获取有效的起始交易签名和扫描区间内的最后一个签名。 代码示例：从 Slot 到 Signature 的转换（锚点定位）

```
// Lines 77-81: 主流程中必须先获取起始签名
const startBlock = await this.getStartTransactionSignature(
connection,
startSlotOfBatch // 传入的是 Slot 数值
);
console.log(`Start block ${startBlock.startBlockSlot}`);
// Lines 87-91: 同样需要获取结束签名
endBlockInfo = await this.getLastTransactionSignature(
connection,
endSlotOfBatch // 传入的是 Slot 数值
);
console.log(`End block ${endBlockInfo.endBlockSlot}`);
// Lines 104-111: getSignaturesForAddress 必须使用真实签名，不能使用 Slot
const sigs = await connection.getSignaturesForAddress(
new PublicKey(monitorAddress),
{
limit: this.config.signaturesPerBatch,
until: startBlock.startSignature, // ⚠ 必须是真实签名，不能是 Slot！
before: beforeSignagure, // ⚠ 必须是真实签名，不能是 Slot！
}
);
```

代码示例：处理空块与跳过块的"锚点定位"逻辑

```
// Lines 216-261: 找到起始交易签名
private async getStartTransactionSignature(
connection: Connection,
lastSyncedSlot: number // 输入：Slot 数值
) {
// 列出 lastSyncedSlot 到 lastSyncedSlot + slotToCheck 的已确认 blocks
const blockHistories = await connection.getBlocks(
lastSyncedSlot,
lastSyncedSlot + this.config.slotToCheck // 扫描范围：向后查找
);
// ⚠ 关键：遍历每个 block，直到找到第一个包含交易的区块
for (let i = 0; i < blockHistories.length; i++) {
// getBlock 是重量级操作，大约耗时 5 秒
const startBlock = await connection.getBlock(blockHistories[i], {
maxSupportedTransactionVersion: 0,
rewards: false,
transactionDetails: "accounts",
});
if (!startBlock) {
throw new Error(`failed to fetch block found for slot
${blockHistories[i]}`
);
}
const transactions = startBlock.transactions;
if (transactions.length > 0) { // ⚠ 找到第一个非空块！
return {
startBlockSlot: blockHistories[i],
startSignature: transactions[0].transaction.signatures[0], // 提取第一笔
交易签名
};
}
// 如果是空块，继续循环到下一个块
}
// ⚠ 如果扫描范围内都是空块，抛出错误
throw new Error(
`No valid transactions found between slot ${lastSyncedSlot} and slot ${
lastSyncedSlot + this.config.slotToCheck
}`
);
}
```

代码示例：反向查找最后一个有效签名

```
private async getLastTransactionSignature(
connection: Connection,
endSlot: number // 输入：Slot 数值
) {
const blockHistories = await connection.getBlocks(
endSlot - this.config.slotToCheck, // 扫描范围：向前查找
endSlot
);
// ⚠ 从后往前遍历，找到最后一个包含交易的区块
for (let i = blockHistories.length - 1; i >= 0; i--) {
const endBlock = await connection.getBlock(blockHistories[i], {
maxSupportedTransactionVersion: 0,
rewards: false,
transactionDetails: "accounts",
});
if (!endBlock) {
throw new Error(`failed to fetch block found for slot
${blockHistories[i]}`
);
}
const transactions = endBlock.transactions;
if (transactions.length > 0) { // ⚠ 找到最后一个非空块！
return {
endBlockSlot: blockHistories[i],
endSignature: transactions[transactions.length -
1].transaction.signatures[0], // 提取最后一笔交易签名
};
}
// 如果是空块，继续向前查找
}
throw new Error(
`No valid transactions found between slot ${
endSlot - this.config.slotToCheck
} and slot ${endSlot}`
);
}
```

2. 合约与后端协同：富数据事件 (Data-Rich Events)

- 原则： 在设计自定义合约（如 Anchor）时，应确保事件携带完整的业务上下文数据。

- 实践： 即使链上逻辑不需要某些字段（如用户当前的 VIP 等级、关联的外部 ID 或计算后 的 USD 价值），只要后端业务需要，就应将其通过 Event 抛出。

- 价值： 避免后端在监听到事件后被迫发起二次 RPC 查询（getAccountInfo ），利用 Solana 低廉的 Log 存储成本换取后端的高效索引性能。

以本文的 Staking 协议为例，在原始设计中，每次用户质押时，合约发出的 Staked 事件只包含 三个基础字段， 假设用户在前端刚完成一笔质押，产品要求页面立刻刷新，展示该用户当前的总 质押份额和 累计领取奖励代币数量，因为缺失了同时发放的 rewards（奖励金额）和用户当前的 total_staked（总质押量），后端在监听到这个事件后，就得被迫再发起一次 RPC 请求（调用 getAccountInfo 读取 UserStakeInfo PDA）来获取最新当前的总质押份额和 累计领取奖励代币数 量 。这样做增加了RPC调用的开销。 代码示例：二次RPC调用开销

```
#[event]
pub struct Staked {
pub user: Pubkey, // 进行质押操作的用户钱包地址
pub amount: u64, // 用户本次质押的代币数量
pub timestamp: i64,// 质押操作发生的链上时间戳
}
const { user, amount, timestamp } = eventData.data;
// 痛点：二次 RPC 请求
// 为了获取用户最新的总质押量，再次请求RPC
const [userStakePda] = PublicKey.findProgramAddressSync(
[Buffer.from("stake"), statePda.toBuffer(), user.toBuffer()],
programId
);
// 当前的总质押份额和累计领取奖励代币数量
const accountInfo = await connection.getAccountInfo(userStakePda);
const totalStaked = coder.accounts.decode("UserStakeInfo",
accountInfo.data).amount;
const totalReward = coder.accounts.decode("UserStakeInfo",
accountInfo.data).reward_debt;
```

代码示例：富数据事件

```
// ✅ 最佳实践：富数据事件（Data-Rich Event）
#[event]
pub struct Staked {
pub user: Pubkey,
pub amount: u64, // 本次质押金额
pub timestamp: i64,
// --- 新增的富数据字段 ---
pub rewards_debet: u64, // 奖励金额
pub total_staked: u64, // 质押后的总金额
}
// 直接从 Anchor EventParser 解析出的事件中获取全部富数据
const {
user,
amount,
timestamp,
total_staked,
total_reward
} = eventData.data;
```

经过这样简单的合约改造，后端在调用 Anchor 的解码器解析出事件后，立刻就能得到带有明确 类型限制的 rewards_claimed 和 total_staked 数据。避免了一次多余的RPC调用。 3. 严谨处理“失败交易”的幽灵日志

- Solana 失败交易的特征：在 EVM 中，一旦交易回滚（Revert），所有的状态变更和事件触 发都会被抹除，后端根本查不到这些事件。但在 Solana 中，即使交易最终执行失败（例如 在最后一步校验不通过导致报错），程序在失败前打印的执行日志（ logMessages）依然会 被记录在链上并返回给 RPC 。这意味着失败的交易同样可能包含看似正常的“事件”。

- 对后端设计的影响：如果后端不加甄别地解析所有日志，就会把“实际上并未发生的状态变 更”写入数据库，导致链下状态与链上真实状态出现严重的数据不一致（Data Inconsistency）。

- 最佳实践：在事件解析流水线的极早期阶段（通常在拉取到交易详情后），必须第一优先检 查 tx.meta.err 字段 。只有当 tx.meta.err === null 时，才允许将提取出的事件送入下游的 业务处理逻辑。这必须成为所有 Solana 索引器（Indexer）不可逾越的护栏。

代码示例：处理失败交易

```
// Solana：失败交易可能包含事件
const tx = await connection.getParsedTransaction(signature, {
maxSupportedTransactionVersion: 0
});
// 即使交易失败（tx.meta.err存在），logMessages中仍然可能包含事件
if (tx.meta?.err) {
// 交易失败，但logMessages中可能仍有事件信息
// 需要决定是否处理这些事件
}
```

## 实战案例：Staking 历史记录与数据聚合服务

在前面的章节中，我们深入分析了Solana事件处理的机制、限制和挑战。现在，让我们通过一个 完整的实战案例——solana-backend项目，来展示如何在实际项目中应用这些知识，构建一个健 壮的Solana后端服务。 业务说明 solana-backend是一个为Solana Staking协议构建的后端服务系统，实现了链上事件同步到数据 查询的完整功能。该项目清晰地分离了事件同步、数据查询和业务逻辑，为Solana后端开发提供 了一个优秀的实践案例。 Solana Staking协议 Solana Staking协议是一个基于Solana区块链的质押奖励协议，允许用户质押代币并获得持续奖 励。该协议采用Anchor框架开发，如果你对具体的协议感兴趣，可以访问 GitHub: Solana Staking 。 实时查询链上数据 项目中实现了读取当前GlobalState和UserStakePosition两个账户的方法。想要读取指定账户的 状态其实很简单，只要能得到账户的地址，再调用rpc的getAccountInfo就可以拿到账户的数据 了。GlobalState和UserStakePosition是两个PDA地址，PDA（Program Derived Address）：由程 序和种子确定性生成、无私钥、只能由程序通过 invoke_signed 控制的账户，是 Solana 中承 载合约状态和权限的核心机制。 代码片段：PDA账户-GlobalState, UserStakeInfo

```
#[account(
init,
payer = admin,
space = 8 + GlobalState::INIT_SPACE,
seeds = [STATE_SEED, staking_mint.key().as_ref()],
bump
)]
pub state: Box<Account<'info, GlobalState>>
#[account(
init_if_needed,
payer = user,
space = 8 + UserStakeInfo::INIT_SPACE,
seeds = [STAKE_SEED, state.key().as_ref(), user.key().as_ref()],
bump
)]
pub user_stake_info: Box<Account<'info, UserStakeInfo>>
```

代码片段：GlobalStateRepository - 实时查询全局状态

```
// src/repositories/implementations/GlobalStateRepository.ts
async getGlobalState(
programId: string,
stakingMint: string,
stateSeed: string = "state"
): Promise<GlobalState> {
const programPubkey = new PublicKey(programId);
const stakingMintPubkey = new PublicKey(stakingMint);
// 推导 GlobalState 的 PDA
const [statePda] = PublicKey.findProgramAddressSync(
[Buffer.from(stateSeed), stakingMintPubkey.toBuffer()],
programPubkey
);
// 查询账户数据
const connection = this.solanaConnections.getConnection(this.chainId);
const accountInfo = await connection.getAccountInfo(statePda);
if (!accountInfo) {
throw new Error(
`GlobalState account not found at PDA: ${statePda.toBase58()}`
);
}
const coder = new BorshCoder(StakingIDL as Idl);
const decodedState = coder.accounts.decode("GlobalState", accountInfo.data)
as {
admin: PublicKey;
staking_mint: PublicKey;
// ... 其他字段
};
return GlobalState.fromChainData({
admin: decodedState.admin.toBase58(),
stakingMint: decodedState.staking_mint.toBase58(),
// ... 其他字段
});
}
```

代码片段：UserStakePositionRepository - 实时查询用户状态

```
// src/repositories/implementations/UserStakePositionRepository.ts
async getUserStakePosition(
userAddress: string,
programId: string,
stakingMint: string,
stateSeed: string = "state",
stakeSeed: string = "stake"
): Promise<UserStakeStatus | null> {
// 第一步：推导 GlobalState 的 PDA
const [statePda] = PublicKey.findProgramAddressSync(
[Buffer.from(stateSeed), stakingMintPubkey.toBuffer()],
programPubkey
);
// 第二步：推导 UserStakeInfo 的 PDA（依赖第一步的结果）
const [stakePda] = PublicKey.findProgramAddressSync(
[Buffer.from(stakeSeed), statePda.toBuffer(), userPubkey.toBuffer()],
programPubkey
);
// 查询账户数据
const accountInfo = await connection.getAccountInfo(stakePda);
if (!accountInfo) {
return null; // 用户尚未质押
}
const coder = new BorshCoder(StakingIDL as Idl);
const decodedStake = coder.accounts.decode("UserStakeInfo", accountInfo.data) as
{
owner: PublicKey;
amount: bigint;
// ... 其他字段
};
return UserStakeStatus.fromChainData({
owner: decodedStake.owner.toBase58(),
amount: decodedStake.amount,
// ... 其他字段
});
}
```

通过 PublicKey.findProgramAddressSync 确定性推导 PDA，保证程序状态地址可预测且 无私钥；通过 Anchor 的 BorshCoder 按 IDL 解码账户数据，实现类型安全、标准化的状态解析。 拉取链上历史数据 由于Solana无法直接查询历史事件，项目实现了完整的事件同步机制，将链上事件同步到数据库 ，支持历史数据查询。 代码片段：SolanaEventFetcher - 多步查询获取事件

```typescript
// src/event-fetch/chain/solana/solana.ts
async fetchEvents(
monitorAddresses: string[],
startBlock: number,
eventsParser: TransactionEventsParser,
endBlock: number = 0
): Promise<FetchingResult> {
const connection = this.solanaEventsService.getConnection(this.chainId);
const currentSlot = await connection.getSlot();
let endSlot = currentSlot - this.config.slotToExclusion;
// 第一步：获取交易签名列表（处理倒序返回）
for (const monitorAddress of monitorAddresses) {
const sigList: string[] = [];
let beforeSignagure = endBlockInfo.endSignature;
let sigsCount = this.config.signaturesPerBatch;
while (sigsCount >= this.config.signaturesPerBatch) {
// 使用 before 参数向前追溯（因为 getSignaturesForAddress 倒序返回）
const sigs = await connection.getSignaturesForAddress(
new PublicKey(monitorAddress),
{
limit: this.config.signaturesPerBatch,
until: startBlock.startSignature,
before: beforeSignagure, // 使用 before 参数分页
}
);
sigsCount = sigs.length;
if (sigsCount > 0) {
beforeSignagure = sigs[sigsCount - 1].signature;
// 反转数组，因为返回的是倒序的
sigList.unshift(...sigs.reverse().map((sig: any) => sig.signature));
await sleep(this.config.sleepTime); // 避免RPC频率限制
}
}
// 第二步：批量获取交易详情
for (let i = 0; i < sigList.length; i +=
this.config.promiseNumberForTransactions) {
const promises = [];
const sigListForBatch = sigList.slice(i, i +
this.config.promiseNumberForTransactions);
for (const sig of sigListForBatch) {
promises.push(
this.solanaEventsService.parseTransactionEvents(
this.chainId,
sig,
eventsParser
)
);
}
// 并发获取交易，提高效率
const eventsList = await Promise.all(promises);
// 处理解析出的事件...
}
}
}
```

整体采用 Solana 特有的多步事件抓取模式，通过先使用 getSignaturesForAddress 获取账 户相关交易签名，再批量拉取交易详情并解析日志与账户变化来重建事件语义，显式弥补 Solana 无原生事件查询接口的不足；同时针对签名倒序返回的特性，通过 before 参数实现向历史回溯 分页，并在本地对结果进行反转以恢复时间正序；在性能层面，利用批量请求与 Promise.all 并发处理提升同步吞吐，同时通过主动限速与 sleep 机制规避 RPC 频率限制，确保在高数据量 场景下的稳定性与可持续运行。 代码片段：FetchScheduler - 同步状态管理和断点续传

```
// src/event-fetch/FetchScheduler.ts
private async syncVault(syncStatus: SyncStatus): Promise<void> {
const vaultId = syncStatus.vaultId;
let retries = 0;
while (retries <= this.config.maxRetries) {
try {
// 从上次同步位置继续（断点续传）
const startBlock = syncStatus.lastSyncBlock + 1;
const eventsParser =
this.config.eventParserFactory.createTransactionEventsParser(
this.config.chainId,
[vaultId],
[
PermissionlessStakedEvent,
PermissionlessUnstakedEvent,
PermissionlessRewardsClaimedEvent,
]
);
// 调用 SolanaEventFetcher 同步
const result = await this.eventFetcher.fetchEvents(
[vaultId],
startBlock,
eventsParser
);
// 转换并保存事件
if (result.events.length > 0) {
const userActivities: UserActivity[] = [];
for (const event of result.events) {
try {
const activity = this.convertEventToUserActivity(event, vaultId);
userActivities.push(activity);
} catch (error) {
// 记录日志但跳过该事件，不中断同步流程
console.warn(
`[FetchScheduler] Failed to convert event
${event.transactionHash}:`
,
error
);
}
}
// 保存所有成功转换的事件
for (const activity of userActivities) {
await this.userActivityRepository.save(activity);
}
}
// 更新 SyncStatus（关键：持久化同步进度）
const updatedSyncStatus = syncStatus.updateLastSyncBlock(
result.endBlockNumber
);
await this.syncStatusRepository.save(updatedSyncStatus);
// 成功，退出重试循环
return;
} catch (error) {
retries++;
if (retries > this.config.maxRetries) {
throw error;
}
// 等待后重试
await this.sleep(this.config.retryDelayInterval);
}
}
}
```

通过 SyncStatus 持久化同步进度（如 lastSyncBlock）实现断点续传，结合重试机制应对网络或 RPC 调用失败，并在事件解析失败时记录日志但不中断整体流程以保证系统健壮性，同时通过定 时任务（如 setInterval）定期触发同步以确保数据及时更新，实现完整的状态管理与容错同步 机制。

## 总结

从 EVM 迁移到 Solana 的后端开发，需要从“被动监听、直接查询事件”转向“主动获取交易、解析 日志与账户状态、管理同步状态”的思维模式。Solana 的账户模型和事件机制与 EVM 根本不同： 事件信息隐藏在交易日志中，无法直接按事件类型查询；程序状态存储在独立的数据账户中，核 心账户通常为 PDA，由程序确定性控制，无私钥可用。后端系统必须采用多步查询、批量并发、 倒序处理和限速机制来抓取事件，并维护同步状态、容错和重试机制以保证系统健壮性。此外， Solana 缺少 EVM 那样的高级索引服务和历史状态快照，开发者需要自建索引器、解析器和状态 管理策略，以应对 RPC 限制、交易跳过和历史数据不可直接访问的挑战。总体而言，Solana 后 端开发的核心在于理解账户模型、PDA 控制机制、多步事件抓取流程和健壮的同步策略，从而构 建高可靠性、高性能的链下事件处理与状态管理系统。

## 参考

https://solana.com/docs/rpc https://github.com/jimmyzhao-57blocks/evm-to-solana
