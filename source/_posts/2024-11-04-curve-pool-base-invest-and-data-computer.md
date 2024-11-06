---
layout: post
title: 深入浅出 Curve 流动性池子：投资指南与数据分析
date: 2024-11-04 15:13:08
description: 介绍 Curve 平台的基本操作，以及其中相关数据的计算方法
keywords: curve, defi, web3, blockchain, finance
comments: true
categories: code
tags: [curve, defi, web3, blockchain, finance]
---

{% img /images/post/2024/11/curve-defi.jpg 400 300 %}

对于不熟悉 Web3 的朋友来说，可能会觉得 DeFi (Decentralized Finance，去中心化金融) 是一个充满风险的赌场或骗局。但实际上，DeFi 是一个去中心化的金融体系，在某些方面与投资股票、基金等传统金融产品具有相似性。DeFi 利用区块链技术提供公开透明、无需中介的金融服务，降低了信任成本和交易费用。目前，主流的 DeFi 平台包括 Curve、 Uniswap、Aave 和 Compound 等。今天我们将介绍 Curve 这个平台的基本操作，并探讨其中涉及的智能合约方法，希望通过这篇文章帮助大家更好地理解 DeFi，增强对 DeFi 投资的信心，并找到一种新的财富增长方式。

<!--more-->

## Curve 介绍

[Curve](https://curve.fi/) 是一个去中心化的交易平台，专注于提供稳定币和类似资产之间的交换服务。它通过流动性池子模型运作，允许用户在没有买卖撮合的情况下进行低滑点和低费用的交易，即在交易过程中，价格波动较小，用户可以更好地控制交易成本。Curve 的设计使其成为跨不同稳定币和相关资产交换的理想选择，特别是在加密货币市场中，稳定币之间的交换需求日益增长。流动性提供者可以通过向这些池子中投入代币来赚取交易费用和额外的激励奖励，如 CRV 代币等。Curve 平台是非托管的，意味着用户始终控制自己的资金，而智能合约的不可升级特性增加了资金安全性。Curve 的用户界面展示了关键的财务指标，帮助用户评估和优化他们的投资回报。此外，Curve 支持多条边链，并允许通过 Curve Factory 让用户可以无需权限地创建新的流动性池子，这促进了平台的去中心化和创新。

### 流动性池子

Curve 的流动性池子好比股市中的 ETF 基金，它通过一个组合集合了许多股票，投资者可以通过购买 ETF 的方式获得多只股票的收益，降低投资的风险。在 Curve 中，用户可以将自己的代币投入到池子中，其他用户可以在这个池子中进行代币的交换，交易的价格则由智能合约的算法自动确定，而不是由买卖双方直接撮合。这种方式减少了交易摩擦，提高了流动性，尤其是在稳定币之间的兑换中具有明显优势。

Curve 有两种主要类型的流动性池子：**Stableswap 池子** 和 **Cryptoswap 池子**。

Stableswap 池子主要用于稳定币或价值相对稳定的代币，如 USDC、USDT 等。Curve 上比较出名的 Stableswap 池子有 [3pool](https://curve.fi/#/ethereum/pools/3pool/deposit)。3pool 包含 USDC、USDT 和 DAI，这些都是最常见的稳定币。这类池子利用了一种特殊的算法，以维持资产之间的平衡和近似 1:1 的汇率。

{% img /images/post/2024/11/curve-3pool.png 1000 600 %}

而 Cryptoswap 池子则用于价值波动较大的资产，如 BTC 、 ETH 等，比较出名的 Cryptoswap 池子有 [TricryptoUSDC](https://curve.fi/#/ethereum/pools/factory-tricrypto-0/deposit)。TricryptoUSDC 包含 BTC、ETH 和 USDC，用于跨加密货币之间的高效交换。这类池子通过维持资产的市场价值平衡来适应价格波动。

{% img /images/post/2024/11/curve-tricrptousdc.png 1000 600 %}

### 奖励

流动性池子的奖励和收益主要包括两种类型：

- Base vAPY（可变年收益率）：指 LP（流动性提供者）通过存入资产并从池子中累积的交易费用获得的收益。随着 LP 代币价值的增加，这些收益也会直接增加 LP 的财富。
- Reward tAPR（总年化收益率）：主要由 CRV 代币奖励和其他代币激励组成。LP 可以将获得的 CRV 代币用于参与 Curve DAO 的治理，从而进一步提高其投资回报，但需要将 CRV 代币锁定一段时间。

流动性池子中产生的收益完全归 LP 所有，不会因手续费或池子中的其他成本而被削减。

{% img /images/post/2024/11/curve-reward.png 1000 600 %}

### 支持链

Curve 支持多个区块链网络，这使其成为一个非常灵活且兼容性强的去中心化金融平台。目前，Curve 支持包括主链和侧链在内的多个区块链，如 Ethereum、Polygon 和 Arbitrum 等。通过支持这些不同的链，Curve 为用户提供了更多选择，让用户可以在不同的链上分散投资，降低风险并优化收益。同时，用户能够根据自己的需求选择适合的链进行操作，例如选择更低的费用、更快的交易速度或更丰富的资产组合。Curve 的多链支持不仅增强了平台的灵活性和兼容性，还确保用户能够在不同区块链生态中获得最佳的流动性和收益体验。不同链上的 Curve 流动性池子功能一致，这使得用户在不同链之间自由转移资产变得更加方便和高效。

{% img /images/post/2024/11/curve-support-chain.png 300 800 %}

## 投资流程

Curve 的投资主要分为 2 个主要过程，分别是投资和提现。在进行投资之前，用户需要将钱包中的代币转换成流动性池子支持的代币，然后才能进行投资。在对流动性池进行投资之后，还可以抵押 LP 代币来获取更多的奖励。在提现时，用户需要先将 LP 代币解除质押，然后再将代币从流动性池中提现到钱包中。下面是一个 Curve 投资的基本流程图：

{% img /images/post/2024/11/curve-deposit-flow.png 1000 600 %}

在开始投资 Curve 之前，你需要完成一些基本的准备工作。首先是钱包准备，你要安装支持以太坊网络的 [MetaMask](https://metamask.io/) 或其他钱包，并确保钱包中存有足够的 ETH 用于支付后续操作的 gas 费用。同时，将钱包网络切换到对应的区块链网络。接着是资金准备，根据你的投资计划准备好相应数量的稳定币，比如 USDT、USDC 或 DAI 等，并将这些资金转入到你的钱包地址中。建议首次操作时先用**小额资金**测试，以熟悉整个流程。

完成准备工作后，就可以开始连接 Curve 平台了。首先打开浏览器访问 Curve 的[官网](https://curve.fi)，在页面右上角找到 **Connect Wallet（连接钱包）** 按钮并点击。这时会弹出钱包连接请求，仔细检查连接地址是否正确后，在钱包中确认授权连接。成功连接后，你就可以看到自己钱包地址显示在页面右上角了，这表示你已经可以开始使用 Curve 平台的各项功能了。在进行任何操作之前，建议先浏览一下平台的界面布局，熟悉各个功能模块的位置。

下面我们以投资 `Arbitrum` 链上的 `2BTC-ng` 池子为例，介绍 Curve 投资的基本操作流程。

### 货币转换

流动性池子 `2BTC-ng` 需要投资的代币是 `WBTC` 或 `tBTC`，你可以投资任意一种代币或两种代币的组合到流动性池子中。如果你的钱包中这两种代币都没有的话，可以通过 Curve 的 Swap 功能进行兑换。

首先在右上角选择 `Arbitrum` 网络，然后在 Curve 主页面点击 **SWAP** 菜单，在交易界面的上方选择你要支付的代币，下方方选择你要获得的代币。输入你想要兑换的数量，系统会自动计算你将获得的代币数量，并显示兑换比率和价格影响。在确认兑换之前，请注意查看 **Exchange Rate（兑换率）**和 **Price Impact（价格影响）**，确保它们在合理范围内。第一次兑换某个代币时，需要先点击 **Approve** 按钮授权 Curve 合约使用你的代币，授权成功后点击 **Swap** 按钮进行兑换。整个过程需要支付 gas 费用，建议在 gas 价格较低的时候进行操作。

{% img /images/post/2024/11/curve-swap.png 1000 600 %}

### 投资池子

接下来我们回到 Curve 首页，选择 **POOLS** 菜单，然后在搜索框中输入 `2BTC-ng` 关键字进行检索，找到对应的流动性池子。

选定流动性池子后，你需要先完成代币授权才能注入流动性。在流动性池页面，选择左上角的 **Deposit** 选项卡，在你要存入的代币（`2BTC-ng` 池子是 `WBTC` 和 `tBTC`）输入框中输入存入金额，系统会自动计算并显示你将获得的 LP 代币数量。然后点击 **Approve Spending** 按钮，允许 Curve 合约使用你的代币。这个授权操作需要在钱包中确认并支付一笔 gas 费，这是一次性的操作，同一代币在后续投资时无需重复授权。

完成授权后，就可以开始注入流动性了，在点击 **Deposit** 按钮之前，请仔细核对显示的数据，包括存入金额、LP 代币数量以及兑换比率等信息，确保一切参数都符合预期。确认无误后点击 **Deposit** 按钮，在钱包中确认交易并支付 gas 费，等待交易完成后，你就会收到对应的 LP 代币，这标志着你已经成功在这个流动性池中注入了流动性。

{% img /images/post/2024/11/curve-pool-deposit.png 1000 600 %}

完成流动性注入后，你会获得对应的 LP 代币，并且你会持续地获得 Base vAPY 奖励。如果你想获得更多的奖励，这时你可以选择将这些 LP 代币进行质押以获取额外的 CRV 奖励。在 Curve 平台上，每个流动性池都有对应的 Gauge（质押合约）。在 Deposit 选项卡中，选择 **Stake** 标签就可以对 LP 代币进行质押。在质押页面中，第一次质押时需要授权 Gauge 合约使用你的 LP 代币，授权确认后，输入要质押的 LP 代币数量，确认交易即可完成质押。

{% img /images/post/2024/11/curve-pool-stake.png 1000 600 %}

当然你可以将 **Deposit** 和 **Stake** 操作合并在一起，即在 **Deposit** 选项卡中直接选择 **Deposit & Stake** 标签，输入存款金额和质押数量，确认交易并支付 gas 费，系统会自动完成存款和质押两个操作。

完成质押后，你的 LP 代币会开始持续产生 CRV 代币奖励，也就是 Reward tAPR 奖励。除了 CRV 奖励外，有些流动性池还会额外提供其他代币的奖励，比如 `2BTC-ng` 流动性池除了 CRV 奖励外，还会提供 ARB 代币奖励。

### 领取收益

在 Curve 平台上领取奖励的操作相对简单直观，用户只需进入流动性池页面，这里我们进入 `2BTC-ng` 的流动性池子页面，选择 **Withdraw/Claim** 选项卡，再选择 **Claim Rewards** 标签，这时你可以看到所有可领取的奖励，包括 CRV、ARB 等代币奖励。

{% img /images/post/2024/11/curve-pool-claim.png 1000 600 %}

你可以选择领取 CRV 奖励，点击 **Claim CRV** 按钮就能将 CRV 奖励发送到你的钱包地址，或者你也可以选择领取其他代币奖励如 ARB 等，点击 **Claim Rewards** 按钮，这些奖励就会直接发送到你的钱包地址。

为了最大化投资收益，定期监控和管理你的收益非常重要。这包括经常查看流动性池子的 APY 变化情况、关注 CRV 代币的价格波动，并据此计算实际收益率。建议设置提醒，以便能够定期领取奖励，避免遗漏。

对于已经领取的 CRV 奖励，你有多种管理策略可以选择。你可以选择立即将 CRV 出售换成稳定币，也可以选择持有 CRV 等待其价格升值，还可以将 CRV 锁定为 veCRV 来参与治理并获得更多收益。具体采用哪种策略，需要根据当时的市场情况来灵活调整。

### 结束投资

从 Curve 中撤出流动性的操作和投资流程相反。第一步是解除 Gauge 质押，首先是进入流动性池页面，选择 **Withdraw/Claim** 选项卡，再选择 **Unstake** 标签，输入要解除质押的 LP 代币数量， 然后点击 **Unstake** 按钮，在钱包中确认交易并支付 gas 费，等待交易完成后，你的 LP 代币就会从 Gauge 合约返回到你的钱包中。

{% img /images/post/2024/11/curve-pool-unstack.png 1000 600 %}

第二步是从流动性池中提现，在流动性池页面选择 **Withdraw/Claim** 选项卡，再选择 **Withdraw** 标签，输入要提现的 LP 代币数量，你可以选择将所有 LP 代币都换成一种代币，或者按自定义比例换成多种代币。输入数量后，系统会显示你将收到的代币数量， 然后点击 **Approve Spending** 按钮，授权 Curve 合约使用你的 LP 代币，确认无误后点击 **Withdraw** 按钮，在钱包中确认交易并支付 gas 费，等待交易完成后，你的代币就会到达你的钱包中。

{% img /images/post/2024/11/curve-pool-withdraw.png 1000 600 %}

在完成撤资操作后，你会收到相应的代币，这时你可能需要将这些代币转换成你想要持有的目标币种。为了获得最优的兑换率，建议你同时查看其他去中心化交易所（DEX）提供的兑换率，选择对你最有利的平台进行兑换操作。

## 数据计算

如果你是一个开发人员，你可能想了解在 Curve 投资过程中智能合约是如何自动计算出相关数据，并如何将其应用到实际操作中。下面我们将以 `2BTC-ng` 流动性池子为例，介绍一些 Curve 使用智能合约计算数据的方法，希望能帮助你更好地理解 Curve 平台的运作原理。

### 合约

在每个流动性池子中，Curve 一般会包含 2 个合约，它们分别是 **LP Token 合约**和 **Gauge 合约**。

LP Token 合约是一个符合 ERC20 标准的代币合约，用来代表用户在流动性池中的份额占比。当用户向池子提供流动性时，会根据提供的资产价值铸造对应数量的 LP Token。同样地，当用户想要撤回流动性时，需要销毁这些 LP Token 来赎回原始资产。LP Token 还能帮助用户自动获得交易手续费收入，因为所有的手续费都会被按照持有的 LP Token 比例分配给流动性提供者。

Gauge 合约也是一个符合 ERC20 标准的代币合约，是 Curve 协议中负责流动性挖矿和激励分配的重要组件。当用户将 LP Token 质押到 Gauge 合约中时，就可以开始赚取 CRV 代币奖励，奖励的数量取决于质押的 LP Token 数量以及池子的权重。Gauge 合约还支持额外的奖励代币分发功能，项目方可以通过 Gauge 向特定池子的流动性提供者发放自己的代币奖励，这种机制有效地激励了用户向重要的池子提供长期流动性。

下面是 `2BTC-ng` 流动性池子的 LP Token 和 Gauge 合约：

{% img /images/post/2024/11/curve-pool-contracts.png 1000 600 %}

### 投资金额

要获取用户在 Curve 平台上流动性池子的投资金额，可以根据投资情况来调用不同合约的方法。在 LP Token 合约和 Gauge 合约中都有 `balanceOf` 方法，如果你在投资流动性池子过程中只执行了 **Deposit** 操作，那么你可以调用 LP Token 合约的 `balanceOf` 方法来获取你的账号金额。如果你执行了**Stake** 操作，那么你可以调用 Gauge 合约的 `balanceOf` 方法来获取你的账号金额。`balanceOf` 方法的参数是用户的地址，返回值是用户在合约中的余额。下面是代码示例：

```js
const { Web3 } = require("web3");

// connect the Arbitrum One mainnet
const web3 = new Web3("https://arb1.arbitrum.io/rpc");

// smart contract ABI
const abi = [
  {
    stateMutability: "view",
    type: "function",
    name: "balanceOf",
    inputs: [{ name: "arg0", type: "address" }],
    outputs: [{ name: "", type: "uint256" }],
  },
];

// pool contract address(take 2BTC-ng as an example)
const lpTokenContractAddress = "0x186cf879186986a20aadfb7ead50e3c20cb26cec";
const gaugeContractAddress = "0xB7e23A438C9cad2575d3C048248A943a7a03f3fA";
// wallet address
const walletAddress = "0xd693bc8e4a24097bbec4f7cdbc7021cf356b818c";

(async () => {
  // create smart contract
  const lpTokenContract = new web3.eth.Contract(abi, lpTokenContractAddress);
  const gaugeContract = new web3.eth.Contract(abi, gaugeContractAddress);

  // invoke contrace function
  const lpTokenBalance = await lpTokenContract.methods
    .balanceOf(walletAddress)
    .call();
  console.log("Lp token wallet balance is:", lpTokenBalance);

  const gaugeBalance = await gaugeContract.methods
    .balanceOf(walletAddress)
    .call();
  console.log("Gauge wallet balance is:", gaugeBalance);
})();

// 结果显示
// Lp token wallet balance is: 0n
// Gauge wallet balance is: 78988425012240n
```

- 我们先使用 Arbitrum One 主网创建一个 Web3 实例
- 然后定义 LP Token 和 Guage 合约的 ABI，这 2 个合约都有同样的 `balanceOf` 方法
- 接着定义 2BTC-ng 流动性池子的合约地址和用户的钱包地址
- 再通过 Web3 实例创建 LP Token 和 Guage 合约实例
- 最后调用 `balanceOf` 方法获取用户在 Curve 平台上的投资金额

在 Curve 流动性池子中进行投资后，投资的金额会根据池子的比例进行分配，这个比例是由 Curve 的算法自动计算的。我们也可以通过 LP Token 合约中的方法来显示分配后的投资金额，这样可以更直观地了解自己在 Curve 平台上的投资情况。在 LP Token 合约中，有 `totalSupply` 方法，可以获取这个流动性池子的代币总供应量，然后有另外一个方法 `balances` 可以获取池子中每种代币的供应量，最后通过计算就可以得到每种代币在总供应量中的比例，最后再根据用户的投资金额来计算用户在池子中每种代币的投资金额。下面是代码示例：

```js
// smart contract ABI
const abi = [
  {
    stateMutability: "view",
    type: "function",
    name: "totalSupply",
    inputs: [],
    outputs: [{ name: "", type: "uint256" }],
  },
  {
    stateMutability: "view",
    type: "function",
    name: "balances",
    inputs: [{ name: "i", type: "uint256" }],
    outputs: [{ name: "", type: "uint256" }],
  },
];

(async () => {
  // ...... previous code
  const totalBalance = lpTokenBalance + gaugeBalance;
  const totalSupply = await lpTokenContract.methods.totalSupply().call();

  let tokenOne = await lpTokenContract.methods.balances(0).call();
  // because WBTC's decimals is 8, so we need to convert it to ether
  tokenOne = web3.utils.toWei(tokenOne, "gwei");
  const tokenTwo = await lpTokenContract.methods.balances(1).call();

  const tokenOneRatio = Number(tokenOne) / Number(totalSupply);
  const tokenTwoRatio = Number(tokenTwo) / Number(totalSupply);

  const tokenOneBalance = web3.utils.fromWei(
    Math.floor(tokenOneRatio * Number(totalBalance)).toString(),
    "ether"
  );
  console.log("Token one balance is:", tokenOneBalance);
  const tokenTwoBalance = web3.utils.fromWei(
    Math.floor(tokenTwoRatio * Number(totalBalance)).toString(),
    "ether"
  );
  console.log("Token two balance is:", tokenTwoBalance);
})();

// 结果显示
// Token one balance is: 0.000034135788291198
// Token two balance is: 0.0000450482379098
```

- 我们首先定义了 LP Token 合约的 ABI，包括 `totalSupply` 和 `balances` 方法
- 然后使用之前获取到的 2 个合约的用户金额，计算出用户在流动性池子上的总投资金额
- 接着调用 `totalSupply` 方法获取流动性池子的总供应量
- 再调用 `balances` 方法获取池子中每种代币的供应量，其中因为代币 WBTC 的 decimals 是 8，所以需要将其转换成 ether
- 然后计算每种代币在池子总供应量中的比例
- 最后根据用户的投资金额来计算用户在池子中每种代币的投资金额

### 收益计算

在 Curve 平台上，用户可以通过质押 LP Token 来获取 CRV 代币奖励，除了 CRV 奖励外，有些流动性池还会额外提供其他代币的奖励，比如 `2BTC-ng` 流动性池除了 CRV 奖励外，还会提供 ARB 代币奖励。在 Gauge 合约中，获取可领取的 CRV 奖励和其他代币奖励的方法是不同的。获取 CRV 奖励的方法是 `claimable_tokens`，获取其他代币奖励的方法是 `claimable_reward`。**注意** `claimable_tokens` 方法在智能合约中的 `stateMutability` 属性是 `nonpayable`，这意味着你无法直接在 [Explorer 页面](https://arbiscan.io/address/0xb7e23a438c9cad2575d3c048248a943a7a03f3fa#readContract)上调用这个方法，但我们可以将这个方法的 ABI 添加到我们的代码中，然后将 `stateMutability` 属性改为 `view`，这样就可以在代码中调用这个方法了。下面是代码示例：

```js
const { Web3 } = require("web3");

const web3 = new Web3("https://arb1.arbitrum.io/rpc");

const abi = [
  {
    stateMutability: "view",
    type: "function",
    name: "claimable_tokens",
    inputs: [{ name: "addr", type: "address" }],
    outputs: [{ name: "", type: "uint256" }],
  },
  {
    stateMutability: "view",
    type: "function",
    name: "claimable_reward",
    inputs: [
      { name: "_user", type: "address" },
      { name: "_reward_token", type: "address" },
    ],
    outputs: [{ name: "", type: "uint256" }],
  },
];

// 2BTC-ng pool gauge contract address
const contractAddress = "0xB7e23A438C9cad2575d3C048248A943a7a03f3fA";
const walletAddress = "0xd693bc8e4a24097bbec4f7cdbc7021cf356b818c";
const arbTokenAddress = "0x912ce59144191c1204e64559fe8253a0e49e6548";

(async () => {
  const contract = new web3.eth.Contract(abi, contractAddress);

  const crvRewards = await contract.methods
    .claimable_tokens(walletAddress)
    .call();
  console.log("CRV tokens amount is:", crvRewards);

  const arbRewards = await contract.methods
    .claimable_reward(walletAddress, arbTokenAddress)
    .call();
  console.log("ARB tokens amount is:", arbRewards);
})();

// 结果显示
// CRV tokens amount is: 25937502765335321n
// ARB tokens amount is: 589955392324932n
```

- 我们首先定义了 Gauge 合约的 ABI，包括 `claimable_tokens` 和 `claimable_reward` 方法
- 定义 Gauge 合约地址、用户的钱包地址和 ARB 代币地址，**注意 ARB 代币在不同的网络上有不同的地址**，这里我们需要使用 Arbitrum 网络上的 ARB 代币地址
- 使用 Web3 实例创建 Gauge 合约实例
- 调用 `claimable_tokens` 方法，传入用户的钱包地址，获取用户可领取的 CRV 奖励
- 调用 `claimable_reward` 方法，传入用户的钱包地址和 ARB 代币地址，获取用户可领取的 ARB 奖励

之前的方法是获取用户在流动性池子中**可领取**的奖励，这里的**可领取**表示用户还未对奖励进行 **Claim** 操作，如果想获取用户已经 **Claim** 过的 CRV 奖励，则需要使用 Curve 的 `ChildChainGaugeFactory` 合约中的方法，这个合约是 Curve 的子合约，用来管理用户在 Curve 平台上的奖励，合约中的方法说明可以看[这个文档](https://docs.curve.fi/curve_dao/liquidity-gauge-and-minting-crv/evm-sidechains/ChildGaugeFactory/)。这个智能合约在每个区块链网络上都有部署，这里是各个网络上的[合约地址](https://docs.curve.fi/references/deployed-contracts/#evm-sidechain-gauges)，你可以根据自己的网络选择对应的合约地址，这里我们使用 Arbitrum 网络上的合约地址`0xabC000d88f23Bb45525E447528DBF656A9D55bf5`。下面是代码示例：

```js
const abi = [
  {
    stateMutability: "view",
    type: "function",
    name: "minted",
    inputs: [
      { name: "arg0", type: "address" },
      { name: "arg1", type: "address" },
    ],
    outputs: [{ name: "", type: "uint256" }],
  },
];

// arbitrum network childchain guage factory address
const contractAddress = "0xabC000d88f23Bb45525E447528DBF656A9D55bf5";
const walletAddress = "0xd693bc8e4a24097bbec4f7cdbc7021cf356b818c";
// 2BTC-ng pool gauge contract address
const guageContractAddress = "0xB7e23A438C9cad2575d3C048248A943a7a03f3fA";

(async () => {
  const contract = new web3.eth.Contract(abi, contractAddress);

  const crvRewards = await contract.methods
    .minted(walletAddress, guageContractAddress)
    .call();
  console.log("Claimed CRV tokens amount is:", crvRewards);
})();

// 结果显示
// Cliamed CRV tokens amount is: 31262343705302473n
```

- 我们首先定义了 Curve 的 `ChildChainGaugeFactory` 合约的 ABI，这里是 `minted` 方法
- 定义 Curve 的 `ChildChainGaugeFactory` 合约地址、用户的钱包地址和流动性池子的 Gauge 合约地址
- 使用 Web3 实例创建 `ChildChainGaugeFactory` 合约实例
- 调用 `minted` 方法，传入用户的钱包地址和 流动性池子的 Gauge 合约地址，获取用户已经 **Claim** 过的 CRV 奖励

## 总结

在 DeFi 的世界里，Curve 只是众多流动性池平台之一，但它的稳定性和高效性使得它成为了很多用户的首选。这篇文章为大家介绍了 Curve 平台的基本概念和投资操作流程，同时也为开发人员提供了 Curve 流动性池子中智能合约的使用方法，希望能帮助大家更好地了解 Curve 平台的运作原理和数据计算方法。

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
