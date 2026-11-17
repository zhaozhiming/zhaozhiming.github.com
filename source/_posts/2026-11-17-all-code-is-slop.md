---
layout: post
title: 把 slop 变成黄金：Lauren Tan《All Code Is Slop (Except Mine)》
date: 2026-11-17 16:00:00
description: 总结 Lauren Tan 在 AI Engineer 上的演讲《All Code Is Slop (Except Mine)》，讲如何在提速的同时把代码质量保住
keywords: ai, agent, slop, software-engineering
comments: true
categories: ai
tags: [ai, agent, slop, software-engineering]
---

{% img /images/post/2026/11/slop-cover.jpg 800 435 %}

演讲视频：[All Code Is Slop (Except Mine) — Lauren Tan, SpaceXSI](https://www.youtube.com/watch?v=O_zcc6Xl12g)

## 主旨：在提速的同时把质量保住

这是对 Lauren Tan（[@poteto](https://x.com/poteto)）在 AI Engineer（AIE）上的演讲《All Code Is Slop (Except Mine)》的总结，标题是一句自嘲：「所有代码都是 slop，除了我的」。她请听众举手：有没有看过别人写的代码，觉得全是 slop？她说自己也遇到这种情况，但今天想告诉大家，可以把这些粗制滥造的代码变成黄金。

> slop 在这里可以理解为「粗制滥造的代码」或「代码垃圾」，带有贬义。它原本指稀糊糊的食物、泔水，现在常用来形容 AI 批量生成的低质量内容。

<!--more-->

她自述上个月向生产环境合并了 3,000 个 pull request。在 agent 出现之前，团队很少关心产量，只有少数被称作「coding machine」的人例外——她举了 Brett Taylor 一个周末重写 Google Maps 的例子。现在任何人都可以成为 coding machine。她的核心论点是：软件工程师此刻要解决的关键问题，是如何在大幅提高生产力的同时，维持甚至提升代码质量。

有人把这叫 software factory，她更愿意把它说成信任，不是盲目信任，而是投资 agent 的运行环境，让你能够信任它们能正确地完成任务。她说自己花了六个月才达到能合并数千个 PR，其中不少是在睡觉时完成的，并认为后者会更好。

## 先做确定性约束：信任金字塔

她用一张倒金字塔（the poteto pyramid）说明该把时间花在 agent 环境的哪些层。左边的箭头表示成本自上而下递减。最值得先投入的两层都是确定性的，靠代码本身约束 agent，让它们默认做对的事：

- codebase：好的 API 设计、架构和重构；
- static analysis：lint、类型系统、编译器、测试和 CI；
- 之上再叠加 verification（运行时与形式化），用来找 bug、检查希望保持为真的性质和不变量，并确认 agent 是否理解目标和意图；
- 然后只薄薄加一层 prose：rules、skills、`AGENTS.md` 这类需要判断力的文字；
- 最底端才是 human review。

{% img /images/post/2026/11/slop-pyramid.jpg 1000 560 "「如何更信任我的 agent？」倒金字塔。演讲截图来自原视频 2:05。" %}

她强调：agent 的输出取决于输入，而代码库往往是最大的输入之一。纠正 agent 时，应优先把纠正变成确定性检查，而不是再往 rules 或 `AGENTS.md` 里增加内容。把规则写进代码里更可复现，也更值得信任。

过去代码是几个 PR 一批地到来，她还记得 2024 年去上班时，评审同事写的 3 个 PR，今天这个数字更像 300。每个工程师都像带着成百上千个 agent，代码几乎是连续、快速地涌进来。她把代码库比作花园，你施加的约束和护栏，就是新代码依附着生长的混凝土地基。代码库里那些临时凑合的写法、不好的做法和早该清理的旧代码，容易被 AI 误以为是项目的标准写法，接着照搬到新代码里，下一个 agent 就会复制，像杂草般蔓延。所以要把代码库尽量保持干净，让 agent 总是在复制好的东西。

## Dune，以及用目录划清边界

Dune 是他们为 Grok Bot 做的客户端框架，用来落实这种「有机架构」。她说它在技术上不一定特别新奇，可以把它想成 Electron 应用里的 Next.js。真正有意思的，是里面编码了大量做桌面应用的经验——约束和 lint 规则。

Dune：面向 agent 的应用框架，加上功能目录、严格的 import 禁令和「代码中不写注释」。演讲截图来自原视频 5:02。

她的一条判断是：如果一个 API 要注意很多细节才能用对，AI 就很容易用错，写出质量不好的代码。她以 React 的 `useEffect` 为例——它有合法用途，但经常被误用，有人开玩笑说要用好它得先拿个博士学位，而 agent 也不擅长用它。这是 Lauren 他们项目里的处理方式，不是对所有代码库的通用建议：他们在产品代码里禁用 `useEffect`，只允许它出现在 Dune API 的某些内部位置，从而把这类问题从 code review 里整类拿掉。

{% img /images/post/2026/11/slop-nuance-apis.jpg 1000 560 "Dune：面向 agent 的应用框架，加上功能目录、严格的 import 禁令和「代码中不写注释」。演讲截图来自原视频 5:02。" %}

另一个模式是「目录结构本身就是上下文」。她提到，在 Cursor（同样是 Electron 应用）里看到过 agent 把主进程代码 import 进渲染进程，造成 long task 和卡顿。在 Grok Bot / Dune 里，他们用顶层目录 `electron-renderer/` 和 `electron-main/` 把边界写死，再用依赖图分析和 lint 禁止跨目录 import。幻灯片标注「not even types」：连类型也不能直接跨边引用，两边只能通过 `shared/` 共享类型。规则并不复杂，但又少了一件审查时要注意的事情。

{% img /images/post/2026/11/slop-directory-structure.jpg 1000 560 "用目录表达上下文：渲染进程与主进程隔离，禁止直接互相引用。演讲截图来自原视频 6:32。" %}

## 把每次纠正写成 lint，而不是再加一句文字

她还观察到，agent 会把 Markdown（文档、注释、rules、skills）当成「为什么这次可以不按正确方式做」的借口，或把一次小小的纠正，过度升级成永远不能打破的硬规则。

她举了 Cursor 里的例子：agent 很爱把 `setTimeout`、`requestAnimationFrame`、`requestIdleCallback` 当做解决问题的快速方式，这样掩盖了 VS Code 命令式风格和 React 声明式风格之间的架构冲突，结果客户端和 agent 的性能都会变得更差。大量创建、销毁 timer 会引发明显的垃圾回收和卡顿。更糟的是，agent 还用注释解释为什么这样修复是合理的，timer 随后蔓延开来。她展示过一条时间线：9 月 9 日一个为了修 flicker 的 `setTimeout`，注释写着稍后再 release，agent 还说「改架构会是更大的改动」，10 月 3 日它被复制进共享模块，到 10 月 17 日变成了 183 个 timer。

还有一个 Lauren 他们的项目经验：在 Grok Bot 和 Dune 里，他们选择禁止大约 99% 的注释。理由是注释容易过时，人本来也很少读，又给了 agent 一个逃逸口，而且不可强制执行。取而代之的是带有明确报错信息的 lint，要求 agent 去修根本原因，而不是绕过去。总原则是：每次纠正都变成确定性检查，或通过更好的 API 和架构把问题从根本上消灭。这样做的次数够多，code review 要盯的东西会变少，到了某种程度之后，大部分产品代码的 PR 甚至可以让 agent 自己合并。

{% img /images/post/2026/11/slop-lint-rules.jpg 1000 560 "每次纠正 agent，就变成一条 lint，或从类别上消灭这个问题。阶梯从图上的 7 月累积到 11 月的 167。她在约 8:55 开始讲 lint 规则，这张完整幻灯片出现在 9:20。演讲截图来自原视频。" %}

## 接下来做好验证：让 AI 自己运行、检查代码，确认功能符合预期

验证是让 agent 更自主、也增加对结果信心的另一大块。Lauren 他们团队的 code review 越来越变成看验证产物，比如视频、截图、图表、形式化验证等。

最基础的是 runtime verification：让 agent 自己跑代码，并使用人类开发者也会用的调试工具（例如 Chrome DevTools Protocol）。在他们的 runtime verification skills 里，有一份称为 feature map 的东西——从用户视角出发的「功能站点地图」，标明有哪些功能、用户怎么到达、用什么证明它可用。这样大家有共享词汇，不必猜「左边栏那个东西」。skills 里还有可复用、可测试的自定义 CLI，用来驱动真实应用（幻灯片上的例子包括 `app aria-click`、`app snapshot`、`app screenshot`），既减少 token，也让 agent 检查自己的工作。幻灯片指向 pstack 中的 `/create-verification-skill`。

{% img /images/post/2026/11/slop-runtime-verification.jpg 1000 560 "运行时验证：feature map 加上可复用 CLI，让 agent 自己检查工作。演讲截图来自原视频 10:38。" %}

他们也开始尝试形式化验证：用 TLA+ 为重要用户流程做模型检查（例如跨设备、跨平台的通知），用 Lean 为某些算法做证明（例如他们自己写的 virtualization 库，检查不会出现闪烁或抖动）。她说这条路仍在实验、看起来很有希望，已经用它找到不少 bug，但不是银弹，只是验证环境里的又一层。pstack 里有对应的 `/create-formal-verification-skill`。

{% img /images/post/2026/11/slop-formal-verification.jpg 1000 560 "形式化验证：TLA+ 做模型检查，Lean 做实现证明。演讲截图来自原视频 10:54。" %}

他们还刚开始用 Bombadil（幻灯片给出 [github.com/antithesishq/bombadil](https://github.com/antithesishq/bombadil)）做 UI 的 property-based testing，她说已经抓住一些界面 bug。

她随后引用了 Cindy Sridharan（[@copyconstruct](https://x.com/copyconstruct)）的帖子：大约六个月前，Cindy 还在说很难信任 agent，因为带有隐蔽 bug 的 AI 生成 PR 被合并了，修起来要额外花很多天。到 2026 年 11 月 5 日的后续帖，Cindy 写到：现在这些问题已经不再出现，当时缺的是她现在使用的验证流程，而且「我日常 token 有 70% 花在验证补丁上，只有大约 20–30% 用于创建它们」。这是 Cindy 的个人陈述，不是 Grok Bot 团队的量化指标。Lauren 说这和她自己的观察相符：她大部分 token 也花在验证上。她还指出，这些技术本身并不新，只是过去又难写又难维护、成本偏高，agent 大幅降低了成本，测试与验证正在进入一个黄金时代。

她也提到自己在插件 pstack（potato stack）里做了 `/statechart` skill，用来从用户视角可视化状态机，配合视频和截图一起审查。

## 回报：产量上去了，代码并没有恶化

作为这些投资的回报，她展示了 Grok Bot 团队的结果。以下均为演讲者自述或幻灯片上的数字：

- 她说团队上个月向生产环境合并了约 11,000 个 PR。幻灯片标题写「11k PRs in oct」，柱状图标注 10 月为 10,926，6 月至 10 月合计 23,370。
- 客户端稳定版发布节奏加快，她说大约一天发布一个稳定版。10 月有 15 次桌面发布，发布间隔中位数为 28 小时。
- 10 月约有 227 人提交过代码修改，其中既有核心团队成员，也有不太熟悉项目、只是偶尔参与的贡献者。
- 被回滚的 PR 数量反而更低，并稳定在约 0.5%。一张「revert rate」幻灯片（纵轴为 % of commits reverted）逐月数字为：6 月 0.98%，7 月 0.61%，8 月 0.47%，9 月 0.48%，10 月 0.47%。
- 在这样的速度和体量下，她说 Grok Bot 的性能不仅稳住了，启动、打字、打开新对话、切换对话等多方面比发布时更快。它目前仍是 Electron 应用，性能虽然有上限，但她认为已经比许多其他 agent UI 更快。

{% img /images/post/2026/11/slop-team-prs.jpg 1000 560 "团队产出：幻灯片展示 10 月合并 10,926 个 PR。回滚率等数字见上方列表。演讲截图来自原视频 13:36。" %}

## 先让 AI 把每次开发做好，再让它自动完成更多任务

她最后建议：先别急着建 software factory 和把一切自动化。每次纠正 agent，先改进代码库，例如用 lint 阻止新的反模式，再清理旧模式，并把验证做好，这些工作本来就是自动化的前提。如果 AI 每次写出的代码质量都不好，让它 loop 开发，只会产生更多问题，最后还得花更多钱让 AI 反复修改。

不要自动化一条糟糕的 loop：先把信任和验证做起来，再谈 factory。她从约 15:11 开始讲「先改代码库」；这张含完整三阶段的幻灯片出现在 16:05。

打下代码库、静态分析、运行时和形式化验证这些基础之后，以前做不到的事，也能做到了，你可以在这之上建造自己的 factory。她提到，这些基础打好后，就可以用 Grok Bot 自动安排开发任务、启动 AI 编写代码，并检查和修复问题。有大量 connectors 和 routines，可以拉起 Cursor、cloud agents 和 projects，编排成百上千个 agent。例子是：每次发布新版本时，Grok Bot 会自动拉起一个 Cursor project，对构建好的整个应用尝试各种操作，包括异常情况，找出可能导致出错或崩溃的问题，像真实用户一样操作来找 bug，再为每个 bug 开修复 PR 并自动合并。她说这听起来很疯狂，但因为代码库已经足够好，PR 往往默认就是好的，大多数产品代码不再需要深入的人工审查。

## 结语

有了坚实基础和良好的 organic architecture，就可以把 slop 变成黄金，在不牺牲质量的前提下高速交付。

关注我，一起学习各种 AI 技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
