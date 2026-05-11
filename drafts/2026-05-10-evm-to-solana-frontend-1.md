# 如何将以太坊协议迁移到 Solana - 前端（第一部分）：高性能数据访问与交易优化

## 概览

随着 Solana 生态的成熟，越来越多的产品方开始考虑将其以太坊（EVM）项目迁移到 Solana，以获得更高的性能、更低的交易成本以及更好的用户体验，同时充分发挥 Solana 生态优势。我们公司在这一领域拥有丰富的实操经验，曾主导多个以太坊协议在不同类别的迁移与重构工作。我们理解迁移过程中在合约架构、数据模型、交易逻辑以及前后端协作上的复杂性，并已开发出一套系统化的方法论和最佳实践。

为了帮助开发者系统地掌握从以太坊迁移到 Solana 的方法与实践，我们将推出一系列文章，聚焦三个核心层面——智能合约层、后端服务以及前端交互。在系列文章中，我们将分享来自真实项目的经验教训，包括关键注意事项、最佳实践以及迁移过程中常见的问题。此外，我们还会分享案例研究，展示端到端的迁移方法和实现路径，并提供示例代码，帮助开发者快速上手。

通过这一系列文章，我们希望帮助开发者不仅能够顺利完成迁移，还能充分利用 Solana 的高性能潜力及独特机制，重新设计并原生化构建 Solana 协议。

## 文章导航

- 如何将以太坊协议迁移到 Solana — 前言：系统性介绍以太坊与 Solana 在账户模型、执行机制及费用系统上的根本差异。
- 如何将以太坊协议迁移到 Solana — 合约（第一部分）：重点讲解从以太坊到 Solana 的合约开发心态转变及最佳实践。
- **如何将以太坊协议迁移到 Solana - 前端（第一部分）：面向高性能数据访问与交易优化的前端架构设计与实战（本文）**
- 如何将以太坊协议迁移到 Solana - 前端（第二部分）：Staking Demo 实战

本文将聚焦从 EVM 到 Solana 的前端思维转变，拆解账户模型、交易构建方式以及与钱包适配的关键差异，帮助你搭建一套真正"Solana 原生"的前端基础设施。我们会从最常踩坑的几个方向切入——钱包连接与登录、安全签名流程、高效数据读取、交易尺寸与账户上限约束、手续费与优先费机制，多层级错误处理以及 jito bundle——并结合实际代码示例，给出可直接复用的工程实践，为后续的 Staking Demo 实战打下基础。本文旨在帮助开发者实现从 EVM 到 Solana 前端开发的思维转变，构建一套真正的"Solana 原生"前端基础设施。我们将深入解析 Solana 的账户模型、交易构建方式以及如何适配钱包等核心差异。

实践部分将聚焦于一系列常见痛点与优化方向，包括：

- **钱包交互与安全**：高效的钱包连接与登录流程，以及安全的签名实践。
- **数据访问优化**：如何实现高效的数据读取。
- **交易约束与费用**：解决交易尺寸与账户上限约束问题，理解并应用手续费与优先费机制。
- **错误处理**：建立多层级的错误处理机制。

通过结合实际代码示例，我们提供可直接复用的工程实践，为后续的 Staking Demo 实战打下坚实的基础。

## 1. 连接钱包

在 Solana 的前端应用里，连接钱包本身并不复杂，真正复杂的是在不同运行环境下保持一致的用户体验：

- Desktop 浏览器中，钱包通常以扩展（extension）形式注入对象；
- Mobile 浏览器中，则依赖 Deeplink / Universal Link 唤起独立的钱包 App；
- 用户在钱包 App 内打开 DApp（如 Phantom Browser）。钱包会自动注入全局对象并预连接。

同一个钱包，在 Desktop / Mobile / 钱包浏览器上的能力边界和交互方式并不完全一致，如果对每个钱包、每个平台分别适配，工程复杂度会迅速失控。为了解决这一问题，Anza 团队提供了 `@solana/wallet-adapter`，通过 Adapter 模式将钱包连接逻辑与具体钱包实现解耦，DApp 只需要面向统一接口编程即可。

这一节会先简要回顾官方文档（APP.md）中推荐的标准接入方式，然后重点讨论一个在实践中经常被忽略、但在 mobile 场景下非常关键的问题：

1. 并不是所有钱包都有官方的 wallet adapter；
2. 在 desktop 上，即使没有 adapter，往往仍然可以通过注入对象完成连接；但在 mobile 上，如果没有 adapter（或 deeplink 适配），DApp 将无法跳转并唤起对应的钱包 App；
3. 在这种情况下，如何通过自定义 wallet adapter + Deeplink，为 mobile 用户提供一个可靠、可扩展的 fallback 方案。

### 1.1 标准连接流程：ConnectionProvider + WalletProvider

在典型的 React 应用中，`@solana/wallet-adapter` 推荐的最小接入方式是：

- 使用 `ConnectionProvider` 提供 RPC 连接；
- 使用 `WalletProvider` 注册一组支持的钱包（官方已经为常见钱包实现好了 adapter）；
- 使用 UI 组件（如 `WalletModalProvider` + `WalletMultiButton`）完成交互。

在 desktop 环境下，只要钱包通过 `window.xxx` 注入了符合预期的对象，即便没有显式注册 adapter，也往往可以通过"非标准方式"完成连接或签名。

但这种方式并不能自然迁移到 mobile 场景。

### 1.2 现实问题：mobile 场景下的 adapter 缺口

在真实项目中，很快会遇到下面几个问题：

1. 有些钱包尚未提供官方的 wallet adapter；
2. 有些多链钱包虽然支持 Solana，但其注入对象、连接流程或签名接口，与标准 adapter 的预期并不完全一致；
3. 在 mobile 浏览器中，钱包通常不会注入全局对象，DApp 唯一可行的入口是 deeplink。

这就导致一个关键差异：

- 在 desktop 上，"没有 adapter"通常只是体验问题；
- 在 mobile 上，"没有 adapter"往往意味着用户根本无法连接钱包。

官方文档中更多是从"钱包方应当如何实现 adapter"的角度来描述问题，但在现实中，钱包侧的适配进度并不完全可控。而作为 DApp，一旦已经采用了 `@solana/wallet-adapter` 体系，就会自然地遇到这样的问题：如果某个钱包暂时没有官方 adapter，但我仍然希望在 mobile 上给用户一个明确、可用的连接入口，该怎么办？

### 1.3 解决思路：自定义 Wallet Adapter 作为 mobile fallback

`@solana/wallet-adapter` 的核心抽象是 `WalletAdapter` 接口（定义在 `@solana/wallet-adapter-base` 中）。

这意味着：只要实现了这个接口，应用层并不关心它是官方 adapter，还是自定义 adapter。

因此，我们可以自行实现一个基于 Deeplink 的自定义钱包适配器，使其具备以下能力：

- 基本标识信息：`name` / `url` / `icon` / `deepLink`；
- 连接状态：`publicKey` / `connecting` / `readyState`；
- 核心方法：
  - `connect()` / `disconnect()` / `isConnected()`
  - `sendTransaction(tx, connection, options)` 等。

从应用层来看：

- 自定义 adapter 与 Phantom、Solflare 等官方 adapter 是完全等价的；
- 它们可以一起注册到 `WalletProvider`；
- UI 层（如 `WalletMultiButton`）无需做任何特殊判断。

这使得自定义 adapter 非常适合作为："官方 adapter 尚未覆盖，但 mobile 用户必须有入口"的 fallback 方案。

只要实现了相同的接口，就可以像注册 Phantom 一样注册你自己的 `CustomWalletAdapter`，并参与到 `WalletProvider` 的钱包列表里。

```typescript
export class CustomWalletAdapter extends BaseWalletAdapter {
  readonly name: WalletName;
  readonly icon: string;
  readonly url: string;
  readonly deepLink: string;
  readonly supportedTransactionVersions = new Set<"legacy" | 0>(["legacy", 0]);

  private _connecting: boolean = false;
  private _publicKey: PublicKey | null = null;
  private _readyState: WalletReadyState;

  constructor(config: CustomWalletConfig) {
    super();
    this.name = config.name;
    this.icon = config.icon;
    this.url = config.url;
    this.deepLink = config.deepLink;
    // Loadable: The wallet can be loaded (via deep link) but isn't installed as browser extension
    this._readyState = WalletReadyState.Loadable;
  }

  get publicKey(): PublicKey | null {
    return this._publicKey;
  }

  get connecting(): boolean {
    return this._connecting;
  }

  get connected(): boolean {
    return !!this._publicKey;
  }

  get readyState(): WalletReadyState {
    return this._readyState;
  }

  async autoConnect(): Promise<void> {
    // Deep link adapters should never auto-connect
    // Auto-redirect without user interaction is bad UX
    // Do nothing - require explicit user action to connect
  }

  async connect(): Promise<void> {
    try {
      // For deep link adapters, we only redirect to the wallet app
      // The actual connection happens in the wallet's in-app browser
      // We don't emit "connect" here because the connection isn't established yet
      window.location.href = this.deepLink;
    } catch (error) {
      this.emit("error", new WalletConnectionError((error as Error).message));
      throw error;
    }
  }

  async disconnect(): Promise<void> {
    this._publicKey = null;
    this.emit("disconnect");
  }

  async sendTransaction<T extends Transaction | VersionedTransaction>(
    _transaction: T,
    _connection: Connection,
    _options?: SendOptions
  ): Promise<TransactionSignature> {
    // Deep link adapters handle transactions within the wallet app
    // The actual signing happens after the deep link redirect
    throw new Error(
      "sendTransaction is handled by the wallet app after deep link redirect"
    );
  }

  async signTransaction<T extends Transaction | VersionedTransaction>(
    _transaction: T
  ): Promise<T> {
    throw new Error(
      "signTransaction is handled by the wallet app after deep link redirect"
    );
  }

  async signAllTransactions<T extends Transaction | VersionedTransaction>(
    _transactions: T[]
  ): Promise<T[]> {
    throw new Error(
      "signAllTransactions is handled by the wallet app after deep link redirect"
    );
  }

  async signMessage(_message: Uint8Array): Promise<Uint8Array> {
    throw new Error(
      "signMessage is handled by the wallet app after deep link redirect"
    );
  }
}
```

基于这个基础的 adapter，我们可以再封装一层工具方法，用来根据配置创建不同的钱包适配器，例如 Backpack。

```typescript
/**
 * Build the Backpack deep link URL for mobile.
 * Uses Universal Link format for iOS/Android.
 */
const buildBackpackDeepLink = (): string => {
  const currentUrl = getCurrentUrl();
  const origin = getCurrentOrigin();
  // Backpack Universal Link format
  return `https://backpack.app/ul/v1/browse/${encodeURIComponent(
    currentUrl
  )}?ref=${encodeURIComponent(origin)}`;
};

/**
 * Create a Backpack wallet adapter.
 * On mobile: Uses deep link to open Backpack app
 * On desktop: Redirects to Chrome extension installation page
 */
export function createBackpackWalletAdapter(): CustomWalletAdapter {
  return createCustomWalletAdapter({
    name: "Backpack" as WalletName<"Backpack">,
    icon: BACKPACK_ICON,
    url: BACKPACK_URL,
    deepLinkBuilder: () =>
      isMobile() ? buildBackpackDeepLink() : BACKPACK_CHROME_EXTENSION_URL,
  });
}

/**
 * Create Backpack adapter only for mobile devices.
 * Returns null on desktop (where the official adapter should be used).
 */
export function createBackpackMobileAdapter(): CustomWalletAdapter | null {
  if (!isMobile()) {
    return null;
  }
  return createBackpackWalletAdapter();
}
```

通过这种方式，我们就可以在 `WalletProvider` 中把 `BackpackWalletAdapter()` 当作一个「自定义钱包」注册进去，用来兜底官方 adapter 尚未覆盖的场景。

### 1.4 Deeplink：连接移动端钱包的最后一公里

在 mobile 环境下，大多数 Solana 钱包都以独立 App 的形式存在，DApp 与钱包之间通常通过 Deeplink / Universal Link 通信。典型流程是：

1. DApp 构造一个由钱包定义的 URL，例如：`mywallet://connect?dapp_url=...&session=...`
2. 浏览器跳转到该 URL，系统唤起钱包 App；
3. 钱包完成授权或签名后，通过回调 URL 将结果带回 DApp。

在上面的自定义 adapter 设计里：

- 自定义 adapter 会把 deeplink 协议的细节全部封装在内部，对外暴露的仍然是统一的 `connect()`、`sendTransaction()` 等方法；
- 对应用来说，无论是浏览器扩展钱包，还是通过 deeplink 唤起的移动端钱包，它们在代码层面都只是不同实现的 `WalletAdapter` 而已；
- 因此，当官方 adapter 暂时覆盖不到某个钱包时，你依然可以通过「deeplink + 自定义 adapter」这条路线，为用户提供一个可发现、可用的连接入口，而不破坏现有的 `@solana/wallet-adapter` 接入模式。

因此，当官方 adapter 暂时无法覆盖某个钱包时：你仍然可以通过「自定义 adapter + Deeplink」的方式，在不破坏现有 `@solana/wallet-adapter` 架构的前提下，为 mobile 用户提供一个清晰、可发现、可维护的连接入口。

## 2. 使用签名实现钱包登录：Sign Message vs Sign Transaction

在 Solana DApp 中，仅通过「连接钱包」拿到用户的 `publicKey`，并不能证明该地址一定由当前用户控制。更安全、标准的做法是引入「挑战–响应（Challenge–Response）」协议，让用户对后端生成的消息进行签名来完成登录验证。

### 2.1 挑战–响应（Challenge–Response）基本流程

典型的钱包登录流程如下：

1. **用户连接钱包**
   用户在前端点击「Connect Wallet」，DApp 通过钱包适配器与钱包建立连接，并获取用户的 `publicKey`。
   这一步仅表示用户同意暴露地址，并不构成身份验证。
2. **前端向后端请求 Challenge**
   前端将已获取的 `publicKey` 发送给后端，请求生成登录用的 Challenge。
3. **后端生成 Challenge（挑战信息）**
   Challenge 通常包含：
   - 钱包地址（publicKey）
   - 随机数（nonce）
   - 过期时间（timestamp / TTL）
   - DApp 身份标识（如域名、应用名等）
4. **前端请求钱包签名**
   前端将 Challenge 传递给钱包，请求用户对其进行签名。
5. **前端提交签名结果**
   前端将原始 Challenge 与用户生成的签名一并发送给后端。
6. **后端验证签名与状态**
   后端执行以下校验：
   - 使用 `publicKey` 验证签名是否有效；
   - 校验 Challenge 中的钱包地址与请求中的 `publicKey` 是否一致；
   - 检查 nonce 是否未被使用（防止重放攻击）；
   - 检查 Challenge 是否在有效期内。
7. **登录成功**
   验证通过后，后端为该钱包地址签发会话凭证（如 JWT 或 Session），用于后续的身份识别。

### 2.2 签名方式的兼容性问题

- **signMessage**：大多数软件钱包支持任意消息签名，实现登录最简单直接。
- **signTransaction**：部分硬件钱包（如 Ledger、Trezor 等）通常不支持对任意消息进行签名，而是只允许签名标准的 Solana Transaction。为了兼容这类钱包，我们会构造一笔只包含 Memo 等无副作用指令的"特殊交易"，让用户通过 `signTransaction` 来完成登录签名。需要强调的是，这笔交易仅用于本地签名和后端验证，不会被发送到链上，属于 off-chain 签名流程，因此不会在链上产生记录，也不会消耗任何 gas / 手续费。

下面分别介绍两条路径，并尽量在前后端复用一套验证逻辑。

**构造并签名消息**

可以遵循类似「Sign-In with Solana」的格式构造一条可读性较好的消息，例如：

```javascript
const generateSignInMessage = useCallback((walletAddress: PublicKey) => {
  const timestamp = new Date().toISOString();
  const nonce = Math.random().toString(36).substring(7);
  return `${appName} wants you to sign in with your Solana account: ${walletAddress.toBase58()}

Please sign in to verify your ownership of this wallet

URI: https://${domain}
Version: 1
Network: Solana
Nonce: ${nonce}
Issued At: ${timestamp}`;
});
```

然后用钱包的 `signMessage` 进行签名。基于 `@solana/wallet-adapter` 时，`signMessage` 是可选方法，需要先判断是否存在：

```typescript
const signMessageForAuth = useCallback(
  async (message: string): Promise<SignMessageResult> => {
    if (!publicKey || !signMessage) {
      throw new Error(
        "Wallet not connected or does not support message signing"
      );
    }
    const encodedMessage = new TextEncoder().encode(message);
    const signature = await signMessage(encodedMessage);
    const signatureBase58 = bs58.encode(signature);
    return {
      signature: signatureBase58,
      publicKey: publicKey.toBase58(),
      success: true,
    };
  },
  [publicKey, signMessage]
);
```

这里选择把 `messageBytes` 做一次 base58 编码（`serializedMessage`），这样后端只需要统一处理「签名 + 原始被签名的字节」两项数据，即可兼容 `signMessage` 和后文的 `signTransaction` 方案。

### 2.3 后端：验证 signMessage 签名

后端逻辑本质是标准的 Ed25519 验签。以 Node.js 为例：

```typescript
const publicKeyObj = new PublicKey(publicKeyStr);

// Decode the base58 encoded inputs
const signatureBytes = bs58.decode(signature);
const messageBytes = bs58.decode(serializedMessageBase58);

// Verify the signature using the ed25519 algorithm
const isValid = ed25519.verify(
  signatureBytes,
  messageBytes,
  publicKeyObj.toBytes()
);

// Note: The commented-out code below shows an alternative verification method using nacl.
// const isValid = nacl.sign.detached.verify(
//   messageBytes,
//   signatureBytes,
//   publicKeyObj.toBytes()
// );
return isValid;
```

在调用签名验证函数之前，后端还需要：

- 从存储中读出这条 Challenge；
- 校验：
  - nonce 未被使用；
  - 当前时间在有效期内；
  - Challenge 中声明的地址与 `publicKeyStr` 一致；
- 验证通过后，立刻作废这条 Challenge，并签发自己的登录会话。

这一整套就是基于 `signMessage` 的「签名登录」，适用于绝大多数软件钱包。

### 2.4 硬件钱包：signTransaction + Memo 伪装成 Sign Message

在很多硬件钱包的安全模型中，只允许对交易（Transaction）签名，而不允许对任意字节序列签名；因此在适配层中，你可能会遇到：

- `wallet.signMessage` 不存在或直接抛出 "not supported"；
- 用户使用 Ledger / Trezor 时无法走 `signMessage` 登录流程。

为了兼容这类钱包，可以把 Challenge 塞进一笔"只用于签名、不一定上链"的交易里，让钱包通过 `signTransaction` 完成同样意义的签名。

#### 2.4.1 钱包通过 signTransaction + Memo 签名

示例实现（前端）：

```typescript
const signTransactionForAuth = useCallback(
  async (message: string): Promise<SignMessageResult> => {
    if (!publicKey || !signTransaction) {
      throw new Error(
        "Wallet not connected or does not support transaction signing"
      );
    }

    const blockhashResponse = await connection.getLatestBlockhash();
    const lastValidBlockHeight = blockhashResponse.lastValidBlockHeight - 150;

    const memoInstruction = new TransactionInstruction({
      keys: [],
      programId: MEMO_PROGRAM_ID,
      data: Buffer.from(message, "utf-8"),
    });

    const tx = new Transaction({
      feePayer: publicKey,
      blockhash: blockhashResponse.blockhash,
      lastValidBlockHeight: lastValidBlockHeight,
    }).add(memoInstruction);

    // Sign the transaction
    const signedTx = await signTransaction(tx);

    // Get the signature from the signed transaction
    const txSignature = signedTx.signatures[0];
    const signatureBase58 = bs58.encode(txSignature.signature!);

    // Get the serialized message that was actually signed
    // This is what the wallet signed - the serialized transaction data
    const serializedMessage = signedTx.serializeMessage();
    const serializedMessageBase58 = bs58.encode(serializedMessage);

    return {
      signature: signatureBase58,
      publicKey: publicKey.toBase58(),
      success: true,
      serializedMessage: serializedMessageBase58,
    };
  },
  [publicKey, signTransaction, connection]
);
```

这样，硬件钱包路径下你同样拿到了两样核心数据：

- `signatureBase58`：对交易 message 的 Ed25519 签名；
- `serializedMessageBase58`：交易 `serializeMessage()` 得到的原始 message 字节。

其形式与 `signMessage` 路径保持一致，后端可以用同一套验签逻辑处理。

#### 2.4.2 后端如何验证 signTransaction 的签名

后端收到这类请求（可以视为 `type: 'transaction'`）时，首先需要验证签名是否由该地址对应的私钥产生：

```typescript
const publicKeyObj = new PublicKey(publicKeyStr);
const signatureBytes = bs58.decode(signature);
const messageBytes = bs58.decode(serializedMessageBase58);

// Verify the signature using ed25519
const isValid = ed25519.verify(
  signatureBytes,
  messageBytes,
  publicKeyObj.toBytes()
);
return isValid;

// The commented-out nacl verification is also provided for reference:
/*
// Verify the signature using nacl
const isValidNacl = nacl.sign.detached.verify(
  messageBytes,
  signatureBytes,
  publicKeyObj.toBytes()
);
*/
```

在此基础上，还可以配合 Challenge 做额外校验：

- Challenge（以及其中的 nonce / 过期时间 / address 等）仍然由后端生成并保存在服务端；
- 前端在构造 Memo 时，将 Challenge 写入 `data` 字段；
- 如果需要更强的安全性，可以在后端进一步解析 `messageBytes` 对应的交易内容，找到 Memo 指令并比对其 `data` 是否与 Challenge 一致（这一步属于增强版校验，这里没有展开具体解析代码）。

整体上，这条路径的安全语义与 `signMessage` 路径相同：用户对一段由后端发起的 Challenge 做了不可抵赖的签名，后端在验证签名和 Challenge 合法性后，为该地址建立登录会话。

## 3. 数据与索引层

在区块链应用中，获取链上数据并进行高效查询是构建用户友好界面的关键。不同于 EVM 生态中使用 The Graph 进行数据索引，Solana 生态也有其对应的解决方案。

### 3.1 EVM 中的数据索引：Subgraphs

在 EVM 生态中，Subgraph 是一种用于构建自定义 GraphQL API 的工具，专门用于索引区块链数据。开发者可以使用 Subgraph 来：

- 聚合应用特定的区块链数据，供前端快速访问。
- 监听智能合约事件，并将数据存储在 Graph 节点中。
- 提供实时的查询接口，支持复杂的前端需求。

#### 3.1.1 How to use subgraph

构建 Subgraph 的具体步骤可以参考官方教程：The Graph Quick Start。

#### 3.1.2 查询数据

创建好 Subgraph 后，前端可以通过 GraphQL 查询链上数据，而无需直接调用 RPC 节点，极大提高查询效率。

**查询最新的奖励领取记录**

```typescript
import { gql, request } from "graphql-request";
import { useQuery } from "@tanstack/react-query";

const REWARD_HISTORY_QUERY = gql`
  {
    rewardClaimeds(first: 10, orderBy: blockNumber, orderDirection: desc) {
      id
      user
      reward
      blockNumber
    }
  }
`;

const useRewardHistory = () => {
  const { data, refetch, isLoading, error, isRefetching } = useQuery<{
    rewardClaimeds: RewardRecord[];
  }>({
    queryKey: ["reward-history"],
    queryFn: async () => {
      const graphqlUrl = process.env.NEXT_PUBLIC_GRAPH_URL || "";
      return await request(graphqlUrl, REWARD_HISTORY_QUERY, {}, headers);
    },
    refetchInterval: 30000, // Refetch every 30 seconds
    refetchOnWindowFocus: true, // Refetch when window gains focus
    staleTime: 10000, // Data is considered stale after 10 seconds
  });

  return { data, refetch, isLoading, error, isRefetching };
};
```

### 3.2 Solana 生态：RPC 与第三方服务

Solana 原生 RPC 节点擅长快速状态读取，适合单账户查询或简单过滤。通过 `@solana/rpc-graphql`，开发者可以直接使用 GraphQL 查询接口，实现多账户聚合和复杂数据访问，无需额外遍历 RPC 数据。

然而，原生 RPC 与 rpc-graphql 仍有局限：

- 跨程序的复杂查询或事件回溯仍然需要额外处理
- 大量历史数据查询可能效率不如专门的索引服务

因此，第三方索引服务仍然有价值，例如：Helius 高性能 API 与事件订阅，支持 NFT、交易事件、staking 数据查询。

Solana 原生 RPC + `rpc-graphql` 可以解决大部分查询需求，但第三方索引服务在复杂查询、事件订阅和历史数据访问上仍不可替代。

不依赖第三方索引服务，也可以基于 Solana RPC 实现"事件回溯"。核心思路是：

1. **确定回溯区间**：以 slot 为时间轴，确定回溯范围（通常会避开最新的 slot 以确保数据稳定）。
2. **拉取交易签名**：针对需要监控的地址集合，调用 RPC 的 `getSignaturesForAddress` 方法，在指定范围内分页获取交易签名（signatures）。
3. **获取交易详情**：拿到 signatures 后，可并发调用 `getParsedTransaction` 拉取每笔交易详情。
4. **事件提取**：使用自定义的事件解析器，从交易指令、内部指令或日志中提取业务事件。
5. **数据整合**：将分散事件按 slot / 时间合并排序，并输出新的 checkpoint（同步到的 slot）用于下一轮增量同步。

这种方式可以覆盖"历史回放 + 断点续跑"的需求，但在跨程序复杂分析、实时订阅与海量历史数据检索等场景下，专门的索引服务仍然更高效。

## 4. Address Lookup Table (ALT)

在 Solana 上开发 DApp 时，常见的限制是：单个交易最多只能引用 32 个账户并且每个交易的大小限制为 1232 字节。1232 字节的交易大小限制并非任意设定，而是直接来源于真实网络环境下的 MTU 约束，用于确保交易能够在不发生数据包分片（packet fragmentation）的情况下被可靠传播（参见 Solana 官方的 transaction）。

这在一些复杂场景下会成为瓶颈，例如：

- DeFi 协议调用涉及多个 Token 账户和辅助账户
- 批量 NFT Mint 或 Transfer 操作

为了解决这一问题，Solana 提供了 Address Lookup Table (ALT) —— 一种专门的链上结构，用来存储账户地址。

### 4.1 核心机制

- **账户存储**：你可以把需要频繁使用的账户地址写入 ALT。
- **索引引用**：交易中不再携带完整的账户地址，而是通过索引从 ALT 中解析。
- **扩展能力**：
  - 每个 ALT 最多存储 256 个账户地址
  - 每次 extend 最多写入 20 个地址
- **交易兼容性**：ALT 依赖 v0 版本的交易格式 (Versioned Transaction)，可突破 32 账户限制并缩减交易体积。

### 4.2 使用场景

- **突破账户上限**：复杂跨协议交互、批量 NFT 操作都能一次完成。
- **降低交易体积**：通过索引压缩账户地址，减少字节数 → 手续费更低。

### 4.3 使用流程与示例（Anchor 前端调用代码示例）

**1. 创建并扩展 Address Lookup Table**

```typescript
/**
 * Create an Address Lookup Table (ALT) for stake accounts
 */
export const createLookupTable = async (
  connection: Connection,
  payer: PublicKey,
  accounts: AltAccountInfo,
  signTransaction: <T extends Transaction | VersionedTransaction>(tx: T) => Promise<T>
): Promise<AddressLookupTableAccount | null> => {
  const recentSlot = await connection.getSlot();

  // Create lookup table instruction
  const [lookupTableInst, lookupTableAddress] =
    AddressLookupTableProgram.createLookupTable({
      authority: payer,
      payer,
      recentSlot,
    });

  // Add accounts to lookup table instruction
  const addAccountsInst = AddressLookupTableProgram.extendLookupTable({
    payer: payer,
    authority: payer,
    lookupTable: lookupTableAddress,
    addresses: [
      accounts.state,
      accounts.userStakeInfo,
      accounts.userTokenAccount,
      accounts.stakingVault,
      accounts.rewardVault,
      accounts.userRewardAccount,
      accounts.tokenProgram,
      accounts.blacklistEntry,
      accounts.systemProgram,
      accounts.clock,
    ],
  });

  // Combine both instructions in a single transaction
  const combinedTx = new Transaction()
    .add(lookupTableInst)
    .add(addAccountsInst);

  combinedTx.recentBlockhash = (
    await connection.getLatestBlockhash()
  ).blockhash;
  combinedTx.feePayer = payer;

  const signedTx = await signTransaction(combinedTx);
  await sendAndConfirmTransaction(connection, signedTx.serialize());

  // Fetch the created lookup table
  const lookupTableResponse = await connection.getAddressLookupTable(
    lookupTableAddress
  );

  return lookupTableResponse.value;
};
```

**2. 使用 ALT 执行 Stake 交易 (v0 transaction)**

```typescript
try {
  const accountInfo = await createStakeAccountInfo(publicKey!, program!);

  const accounts: AltAccountInfo = {
    state: accountInfo.statePda,
    userStakeInfo: accountInfo.userStakeInfoPda,
    userTokenAccount: accountInfo.userTokenAccount,
    stakingVault: accountInfo.stakingVault,
    rewardVault: accountInfo.rewardVault,
    userRewardAccount: accountInfo.userRewardAccount,
    tokenProgram: TOKEN_PROGRAM_ID,
    blacklistEntry: accountInfo.blacklistPda,
    systemProgram: anchor.web3.SystemProgram.programId,
    clock: anchor.web3.SYSVAR_CLOCK_PUBKEY,
  };

  const lookupTable = await getOrCreateLookupTable(accounts);

  if (!lookupTable) {
    onError({ message: ERROR_MESSAGES.FAILED_TO_LOAD_LOOKUP_TABLE });
    return;
  }

  const versionedTx = await createVersionedStakeTransaction(
    connection,
    publicKey!,
    program!,
    stakeAmount,
    lookupTable
  );

  const signedVersionedTx = await signTransaction!(versionedTx);
  const signature = await sendAndConfirmTransaction(
    connection,
    signedVersionedTx.serialize()
  );

  setTransactionSignature(signature);
  onSuccess();
  setTransactionSignature(undefined);
} catch (err) {
  const errorInfo = formatErrorForDisplay(err);
  onError(errorInfo);
} finally {
  setIsStaking(false);
}
```

如果你的交易只涉及几十个账户，普通交易已经足够；但在需要批量操作或复杂跨协议交互时，ALT 可以显著减少开发复杂度，提升性能并降低 Gas 成本。

链接 1 是使用没有使用 lookup 的 transaction，链接 2 是使用了 lookup transaction 的，可以看出费用大幅降低，从 0.000025 SOL 降到 0.000005 SOL，费用节省了 80%。

## 5. Solana 手续费优化：Compute Units 与 Priority Fee

在 Solana 中，交易手续费由两部分组成：**基础费用（base fee）** 和 **优先费用（priority fee）**。基础费用固定很低，而优先费用则允许用户在账户竞争（account contention）激烈时，通过支付额外费用来提升交易的打包优先级。

### 5.1 账号争用与优先费

- 交易竞争的单位是**账户（account）**，而不是交易本身。
- 当多个交易在同一个区块内同时访问同一个账户（例如写入某个流动性池的状态账户），就会发生 account contention。
- 在这种情况下，验证者会优先打包出价更高（priority fee 更多）的交易。

如果你交互的账户没有竞争（例如个人钱包转账，或者一个只由你自己使用的 staking 账户），则无需额外支付优先费，交易仍能正常落地。

### 5.2 典型需要优先费的场景

- 热门 NFT mint，数千用户同时写入相同的全局状态账户。
- AMM 流动性池或 DEX 下单，所有人都在访问同一个市场状态账户。
- 高频交易或套利机器人，在相同账户上并发竞争。

### 5.3 Compute Units

每笔交易在网络中消耗的计算资源是以 Compute Units (CU) 计量的。默认情形如果 CU 设置过高，会造成不必要的浪费；设置过低又有可能导致交易执行失败。Solana 提供两种机制帮助开发者找到"合适的 CU 请求"：

使用 `simulateTransaction` 来估算交易执行所需的 CU。你可以先用 `simulateTransaction` + 包含所有指令的 transaction 模拟执行，然后读取耗费的单位数。在这个基础上加上 10%。

```typescript
export const estimateComputeUnits = async (
  connection: Connection,
  transaction: VersionedTransaction
): Promise<number> => {
  try {
    const simulation = await connection.simulateTransaction(transaction, {
      sigVerify: false,
      replaceRecentBlockhash: true,
    });

    if (simulation.value.err) {
      console.warn("Simulation failed:", simulation.value.err);
      return DEFAULT_COMPUTE_UNITS;
    }

    const estimatedCU = simulation.value.unitsConsumed || 0;
    if (estimatedCU === 0) {
      return DEFAULT_COMPUTE_UNITS;
    }

    // Add 10% safety margin
    return addSafetyMargin(estimatedCU, 0.1);
  } catch (error) {
    console.warn("Failed to estimate compute units:", error);
    return DEFAULT_COMPUTE_UNITS;
  }
};
```

注意 Compute Unit Limit 是上限，而优先费是基于这个上限计算，不是实际消耗的 CU。如果你设置的 CU Limit 很高但实际用得很少，也可能支付"高估"的优先费。

### 5.4 使用 Priority Fees 提升交易优先度

Solana 提供了 `getRecentPrioritizationFees` RPC 方法，用于查询近期成功落地的交易在指定账户上的优先费情况。

- 调用时应传入你的交易涉及的账户；
- RPC 会返回近期落地交易的最低优先费；
- 这个值可以作为基准，再根据业务的重要性适当加价。

```typescript
/**
 * Fetch recent priority fees for given accounts
 */
export const getRecentPriorityFees = async (
  connection: Connection,
  publicKey: PublicKey,
  accountInfo: StakeAccountInfo
): Promise<number> => {
  try {
    const response = await connection.getRecentPrioritizationFees({
      lockedWritableAccounts: [
        publicKey,
        accountInfo.statePda,
        accountInfo.userStakeInfoPda,
        accountInfo.userTokenAccount,
        accountInfo.stakingVault,
        accountInfo.rewardVault,
        accountInfo.userRewardAccount,
        accountInfo.blacklistPda,
      ],
    });

    if (!response || response.length === 0) {
      return DEFAULT_PRIORITY_FEE;
    }

    const allFees = response.map((item) => item.prioritizationFee);
    const validFees = filterValidFees(allFees);

    if (areAllFeesZero(validFees)) {
      return DEFAULT_PRIORITY_FEE;
    }

    return calculateRecommendedFee(validFees);
  } catch (error) {
    console.warn("Failed to fetch priority fees:", error);
    return DEFAULT_PRIORITY_FEE;
  }
};
```

### 5.5 最佳计算预算 (Optimal Compute Budget)

优先费的计算公式为：

```
Prioritization Fee = Compute Unit Limit × Compute Unit Price
```

在构建交易时，可以通过指令明确指定优先费参数：

```typescript
const instructions = [
  createComputeUnitPriceInstruction(priorityFee),
  createComputeUnitLimitInstruction(estimatedCU),
  instruction,
];

const versionedTx = await createVersionedTransaction(
  connection,
  publicKey!,
  instructions
);

const signedTx = await signTransaction!(versionedTx);
const signature = await sendAndConfirmTransaction(
  connection,
  signedTx.serialize()
);
```

## 6. Transaction Retry

在 Solana 中，交易广播与确认机制和 EVM 有显著不同。

- **EVM 模式**：交易提交后，只要进入 mempool，矿工最终会打包执行。交易 nonce 严格有序，不存在"丢包"问题。
- **Solana 模式**：采用高并发 + UDP 广播，没有 mempool，交易必须在有限的区块高度内（blockhash 有效期）被打包，否则会直接过期。

这意味着在高负载场景下，交易可能因为丢包、队列溢出或分叉回滚而无法落块，因此需要开发者在客户端实现 retry logic 来保证交易可靠送达。

### 6.1 Blockhash 的角色

每笔 Solana 交易必须携带一个 `recentBlockhash`，它有两个作用：

1. **防重放**：防止同一笔交易无限次广播。
2. **设置过期时间**：一个 blockhash 的有效期是大约 151 slots（约 60–75 秒）。超过这个时间，交易会被拒绝执行。

因此，retry 的核心就是：在 blockhash 过期之前，不断重试广播交易。

### 6.2 Retry 逻辑

常见的 Retry 逻辑如下：

1. 获取 `recentBlockhash` 和其对应的 `lastValidBlockHeight`。
2. 计算一个安全边界，比如 `lastValidBlockHeight - 150`，避免接近过期才重试。
3. 在 `blockHeight < lastValidBlockHeight` 期间，不断重发交易，直到交易被确认或过期。
4. 如果交易最终仍未落块，则需要重新签名（使用新的 blockhash）并再次广播。

```typescript
/**
 * Executes the stake transaction with blockhash retry logic.
 * Returns the transaction signature if successful, or undefined on error.
 */
const executeStakeWithRetry = async (): Promise<string | undefined> => {
  // Get latest blockhash and set lastValidBlockHeight
  const blockhashResponse = await connection.getLatestBlockhash();

  // Subtract a small safety buffer to ensure we stop retrying
  // before the transaction actually expires, avoiding failures near expiration
  const SAFETY_BUFFER = 100; // blocks
  const lastValidBlockHeight = blockhashResponse.lastValidBlockHeight - SAFETY_BUFFER;

  // Create stake instruction using the common utility
  const { instruction } = await createStakeInstruction({
    publicKey: publicKey!,
    program: program!,
    stakeAmount,
  });

  // Create transaction with blockhash and lastValidBlockHeight
  const transaction = new Transaction({
    feePayer: publicKey,
    blockhash: blockhashResponse.blockhash,
    lastValidBlockHeight: lastValidBlockHeight,
  }).add(instruction);

  const signedTransaction = await signTransaction!(transaction);
  const rawTransaction = signedTransaction.serialize();

  // Get current block height
  let blockHeight = await connection.getBlockHeight();
  let confirmedSignature: string | undefined;

  // Keep sending transaction until block height exceeds lastValidBlockHeight
  while (blockHeight < lastValidBlockHeight) {
    try {
      const signature = await connection.sendRawTransaction(rawTransaction, {
        // skipPreflight: true is REQUIRED for retry logic
        // Reasons:
        // 1. We intentionally send the same transaction multiple times
        // 2. Preflight checks would fail on repeat sends with "Transaction already exists"
        // 3. During network congestion, transactions may be dropped and need resending
        // 4. We want to maximize chances of the transaction being accepted
        // 5. Final confirmation is handled separately with proper validation
        skipPreflight: true,
      });

      const confirmation = await connection.confirmTransaction(
        {
          signature,
          blockhash: blockhashResponse.blockhash,
          lastValidBlockHeight: lastValidBlockHeight,
        },
        "confirmed"
      );

      if (!confirmation.value.err) {
        confirmedSignature = signature;
        onSuccess();
        break;
      }

      await sleep(500);
      blockHeight = await connection.getBlockHeight();
    } catch (error) {
      // Ignore send errors, continue retrying
      await sleep(500);
      blockHeight = await connection.getBlockHeight();
    }
  }

  // Check if we had a successful confirmation
  if (!confirmedSignature) {
    onError({
      message: "Transaction failed to send during the valid block height window. Please try again with a new transaction.",
      title: "Transaction Failed",
    });
    return;
  }

  return confirmedSignature;
};
```

Solana 官方针对 transaction retry 也给了相关实现，详情请点击这里查看。

## 7. 错误处理（Error Handling）

Solana 相关的错误大致可以分为三类：

1. **钱包适配层错误**：连接失败、用户拒绝签名、不支持的能力（如 signMessage）。
2. **RPC / 网络层错误**：节点不可达、区块哈希过期、速率限制等。
3. **链上 Program 错误**：包括系统 Program 抛出的错误和业务合约自定义错误。

一个可靠的 DApp 需要在这三个层次上都做到「分类、降噪、友好提示」。

### 7.1 前端统一错误处理入口

将所有前端或交易过程中捕获到的错误，统一交由一个"分类器"进行归类，从而简化 UI 层的错误处理逻辑。

UI 层只需关注错误属于以下四种类型之一：

1. 钱包适配器问题 (`ErrorLevel.WalletAdapter`)
2. RPC / 网络问题 (`ErrorLevel.RpcNetwork`)
3. 链上合约业务逻辑问题 (`ErrorLevel.OnChainProgram`)
4. 未知错误 (`ErrorLevel.Unknown`)

工具函数 `classifyError(error)` 就是这个统一入口，负责将任意 error 归类到上述四种 `ErrorLevel` 中。

```typescript
/**
 * Classifies any error into one of four error levels.
 * This is the unified entry point for all error handling.
 */
export function classifyError(error: unknown): ClassifiedError {
  if (!error) {
    return {
      level: ErrorLevel.Unknown,
      error: null,
      originalError: error,
    };
  }

  const errorObj = error as { message?: string; msg?: string };
  const errorMessage = errorObj?.message || errorObj?.msg || String(error);

  // Level 1: Check for wallet adapter errors first
  const walletError = matchWalletAdapterError(errorMessage);
  if (walletError) {
    return {
      level: ErrorLevel.WalletAdapter,
      error: walletError,
      originalError: error,
    };
  }

  // Level 2: Check for RPC/network errors
  const rpcError = matchRpcNetworkError(errorMessage);
  if (rpcError) {
    return {
      level: ErrorLevel.RpcNetwork,
      error: rpcError,
      originalError: error,
    };
  }

  // Level 3: Check for on-chain program errors
  const programError = parseProgramErrorInternal(error);
  if (programError) {
    return {
      level: ErrorLevel.OnChainProgram,
      error: programError,
      originalError: error,
    };
  }

  // Unknown error
  return {
    level: ErrorLevel.Unknown,
    error: null,
    originalError: error,
  };
}
```

`formatErrorForDisplay(error)`：在对错误进行分类的基础上，输出一个可供 UI 直接使用的结构，包含以下字段：

- `title`
- `message`
- `code?` (optional)
- `level?` (optional)

```typescript
/**
 * Formats an error for display in the UI.
 * This function classifies the error and returns a structure suitable for ErrorModal.
 */
export function formatErrorForDisplay(error: unknown): FormattedError {
  const classified = classifyError(error);
  const userMessage = getUserFriendlyError(error);
  const title = getTitleForLevel(classified.level);

  const result: FormattedError = {
    title,
    message: userMessage,
    level: classified.level,
  };

  // Add error code if available
  if (classified.error?.code) {
    result.code = classified.error.code;
  }

  return result;
}
```

## 8. Jito Bundle

### 8.1 Solana 原生调度模型的局限

在 Solana 中，大多数交易通过 RPC 节点提交。RPC 会将交易转发给当前及下一任 Leader，交易随后进入 Leader 的执行队列并被打包进区块；在被 Leader 实际执行并获得网络投票之前，交易仅存在于客户端与转发节点的内存队列中。该流程决定了一个关键事实：调度的最小单位是单笔交易，且其排序竞争发生在公共转发路径中。

这种模型在高并行执行下具备极强吞吐能力，但对复杂链式操作并不理想。若一个业务流程包含多笔存在依赖关系的交易（如借贷→兑换→还款），这些交易会被独立传播、独立排序、独立执行，系统无法保证它们作为一个整体成功提交，也无法确保严格按预期顺序执行。因此，原生模型缺乏"多交易原子提交"与"确定性顺序调度"的能力。

### 8.2 Jito Bundle：调度层的扩展抽象

Jito Bundle 正是针对这一调度层语义缺失提出的扩展抽象。它将一组交易封装为一个有序序列：`Bundle = [Tx1, Tx2, ..., TxN]`，并定义统一的执行语义：按给定顺序执行所有交易，仅当全部成功时才提交状态，否则整体回滚。通过将调度单位从单笔 Transaction 提升为"交易序列"，Bundle 在不改变底层执行引擎的前提下，为复杂多步骤操作提供了原子性与顺序保证。

该机制通过一条独立于公共 RPC 的私有调度通道实现，由 Jito Labs 提供的 Block Engine 负责接收客户端提交的交易序列，并直接路由至运行 Jito-Solana 客户端的验证者。与标准路径中逐笔交易进入候选队列不同，Bundle 在进入区块构建流程前即被视为不可拆分的执行单元，其内部顺序不会再参与公共排序竞争。

从系统设计角度看，Jito Bundle 并不是新的交易类型，而是在现有执行模型之上增加的一层调度抽象：将"Transaction-level scheduling"扩展为"Bundle-level scheduling"，从而为多交易依赖流程提供原子执行与顺序确定性。

### 8.3 使用场景

在实际系统中，Bundle 的价值不仅体现在"原子性"这一抽象语义上，更体现在对复杂链式策略的工程化支持。典型的使用场景包括：

- **MEV Arbitrage**：将用户的交易与套利交易打包为一个原子序列执行，从而确保在同一价格状态下完成买入与卖出操作，避免中间状态被打断或价格滑点导致策略失效。
- **Liquidations**：将预言机价格更新交易与清算交易打包提交，确保清算逻辑基于最新价格原子执行，避免因价格更新与清算分离而导致的执行失败或竞争冲突。
- **Batching Complex Operations**：当单笔交易受限于交易大小或 Compute Budget 时，可将多个步骤拆分为多笔交易并封装进同一 Bundle 中，以顺序执行且整体提交的方式完成复杂操作。

这些场景的共同特征是：都依赖"多交易之间的顺序确定性与原子提交语义"。在公开传播与独立排序的标准模型下，这类策略容易受到调度竞争与状态变化影响；而通过将交易序列封装为 Bundle 并经由 Jito Labs 的私有调度通道提交，可以在进入区块构建阶段前锁定执行顺序并保证整体一致性。

### 8.4 本文实践

基于上述能力，后文将聚焦于 **MEV 保护** 这一典型场景，展示如何通过 Jito Bundle 的私有通道提交 Staking 交易，在价格敏感执行环境下提升结果确定性与策略稳定性。

#### 8.4.1 交易提交方式

Jito Block Engine 提供两种交易提交接口：

- `sendBundle` 用于提交包含多笔关联交易的 Bundle，适用于需要原子执行多个操作的复杂场景（如闪电贷套利、多步骤 DeFi 操作）。
- `sendTransaction` 则是单笔交易的简化接口，配合 `bundleOnly=true` 参数，同样能获得 MEV 保护和回滚保护能力。

对于本项目的 Stake 操作，由于只涉及一笔交易，我们选择 `sendTransaction` 而非 `sendBundle`，在保持相同保护级别的同时简化了实现：

```
POST https://<region>.block-engine.jito.wtf/api/v1/transactions?bundleOnly=true
```

`bundleOnly=true` 参数确保交易作为单交易 Bundle 处理。这意味着如果执行失败，所有状态变更都会回滚，不会出现部分执行的尴尬情况。

```typescript
export const sendJitoTransaction = async (
  signedTransaction: Transaction
): Promise<string> => {
  try {
    // Serialize and encode the transaction for the API body
    const serialized = signedTransaction.serialize();
    const base58Tx = bs58.encode(serialized);

    const endpointUrl = `${JITO_BLOCK_ENGINE_URL}/api/v1/transactions?bundleOnly=true`;

    const requestBody = {
      jsonrpc: "2.0",
      id: 1,
      method: "sendTransaction",
      params: [base58Tx],
    };

    // Send the transaction via a POST request to Jito's API
    const response = await fetch(endpointUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    // Check for HTTP errors
    if (!response.ok) {
      throw new Error(
        `Jito API request failed: ${response.status} ${response.statusText}`
      );
    }

    const result = await response.json();

    // Check for JSON-RPC errors returned in the response body
    if (result.error) {
      throw new Error(
        `Jito sendTransaction error: ${result.error.message || JSON.stringify(result.error)}`
      );
    }

    // Success: Return the transaction signature
    const signature = result.result;
    return signature;
  } catch (error) {
    // Log and re-throw a standardized error
    console.error("Failed to send transaction via Jito:", error);
    throw new Error(
      `Jito transaction failed: ${error instanceof Error ? error.message : "Unknown error"}`
    );
  }
};
```

#### 8.4.2 费用策略

`sendBundle` 和 `sendTransaction` 在费用策略上有本质区别：

- **sendBundle**：只需要 Jito Tip。Bundle 直接发送到 Jito Block Engine，由 Jito 验证者优先打包。Priority Fee 对 Bundle 的处理优先级没有影响，验证者根据 Tip 金额决定是否接受和处理 Bundle。
- **sendTransaction**：推荐采用 70/30 分配原则，将总费用的 70% 作为 Priority Fee，30% 作为 Jito Tip。Priority Fee 通过 `ComputeBudgetProgram.setComputeUnitPrice()` 设置，提升交易在验证者队列中的优先级；Jito Tip 则激励 Jito 验证者优先处理该交易。

| 提交方式 | Priority Fee (优先费用) | Jito Tip (激励费) | 核心说明 |
|---------|-----------------------|------------------|---------|
| sendBundle | 无需 | 必需 | Tip 决定 Bundle 的处理优先级 |
| sendTransaction | 70% | 30% | 两者协同以提高成功率 |

**关于 Tip 的重要注意事项：**

1. **不要对 Tip 账户使用 Address Lookup Table (ALT)**。Tip 指令必须直接包含完整的账户地址，使用 ALT 压缩会导致 Tip 无法被正确识别。
2. **Tip 仅对 Jito-Solana Leader 有效**。当前 slot 的 Leader 如果不是运行 Jito-Solana 客户端的验证者，Tip 将不会产生任何优先处理效果，相当于白白浪费资金。好在 Jito 验证者控制着约 95% 的网络质押，绝大多数情况下你的交易都能遇到 Jito Leader。

费用金额的确定依赖 `tip_floor` API：

```
GET https://bundles.jito.wtf/api/v1/bundles/tip_floor
```

该接口返回最近成功落地交易的 Tip 统计分布。对于 `sendTransaction`，我们取 `landed_tips_50th_percentile`（中位数）作为基准，按 70/30 比例计算：

```typescript
export const calculateJitoFees = async () => {
  const tipFloor = await fetchTipFloor();
  const baseFee = Math.ceil(tipFloor.landed_tips_50th_percentile * LAMPORTS_PER_SOL);

  const totalFee = Math.max(JITO_MIN_TIP_LAMPORTS, baseFee);

  return {
    priorityFee: Math.ceil(totalFee * 0.7),
    jitoTip: Math.max(JITO_MIN_TIP_LAMPORTS, Math.ceil(totalFee * 0.3)),
  };
};
```

#### 8.4.3 Tip 账户与负载均衡策略

Jito Tip（小费）必须转账至特定的 Tip 账户。获取这些账户的 API 接口如下：

```
POST https://<region>.block-engine.jito.wtf/api/v1/getTipAccounts
```

Jito 维护了 8 个 Tip 账户用于接收小费。为了实现负载均衡，在构建交易时，应当随机选择其中一个账户：

```typescript
export const getRandomTipAccount = async (): Promise<PublicKey> => {
  const tipAccounts = await fetchTipAccounts();
  const randomIndex = Math.floor(Math.random() * tipAccounts.length);
  return new PublicKey(tipAccounts[randomIndex]);
};
```

**备用方案**：如果上述 API 不可用，可以查阅这里中列出的静态账户作为替代。

#### 8.4.4 网络环境与 CORS 处理

Jito Block Engine 的测试环境仅支持 Solana Testnet，不支持 Devnet。开发调试时需确保合约部署到 testnet，并且钱包和 RPC 都连接到 Testnet。

另外，`tip_floor` API 存在 CORS 限制，浏览器无法直接访问。开发环境可通过 Vite 代理解决，生产环境则需配置 Nginx 反向代理或通过后端服务转发请求。

```typescript
// vite.config.ts
import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    proxy: {
      // Proxy requests starting with "/api/v1/bundles/tip_floor"
      // to the Jito Bundles API
      "/api/v1/bundles/tip_floor": {
        target: "https://bundles.jito.wtf",
        changeOrigin: true, // Needed for cross-origin requests
      },
    },
  },
});
```

## 9. 结语

对于有 EVM 经验的开发者来说，Solana 的一些机制，例如 ALT、优先费、blockhash 过期、账户争用，初期可能会感到"反直觉"。然而，一旦将这些机制内化到前端工程设计中，就能够构建出真正的"Solana 原生"用户体验：即使是复杂的交互也能在单笔交易中完成，交易成本可控，并在高负载环境下依然保持稳定可靠。

迁移到 Solana 的核心价值，远不止于"更便宜的 Gas"，而是通过其独特设计重新定义协议的性能上限和产品形态。

在本系列后续文章中，我们将发布《如何将以太坊协议迁移到 Solana - 前端（第二部分）：Staking Demo 实战》。该文会通过一个完整的 Staking 示例，串联前端核心交互场景等核心功能，从而展示从零开始构建 Solana 前端的完整落地流程。

如果你正在着手搭建或重构 Solana 前端，建议从文中提到的任一关键点（如 ALT、Priority Fee 或统一错误处理）开始实践。当你逐步将这些能力整合串联起来时，一个真正"为 Solana 而生"的 DApp 便会应运而生。如果你想直接对照代码实现，可以在这里查看完整示例项目。
