---
layout: post
title: Claude Code 完全指南——Hooks
date: 2026-04-27 14:57:07
description: 深入解析 Claude Code 的 Hooks 钩子系统，包括事件体系、合并与决策机制、不同层级中的 Hook，以及真实插件中的实际应用案例
keywords: claude code, hooks, ai coding, anthropic
comments: true
categories: ai
tags: [claude-code, hooks, ai-coding, anthropic]
---

{% img /images/post/2026/04/hooks.jpeg 300 400 %}

[上一篇文章](/2026/04/03/claude-code-slash-command/)里，我们聊了 Slash Command 和 Skill 的关系，看到 Claude Code 正在把原本零散的任务入口，收进一套更完整的结构化体系里。不过当我们把越来越多的能力交给 Claude Code，让它去读代码、改文件、跑命令，甚至调用外部服务时，一个更深层的问题也会跟着浮出水面，我们到底靠什么来控制它的行为边界？`CLAUDE.md` 可以告诉它应该怎么工作，Skill 可以让它按一套更稳定的流程执行任务，但这些归根到底都还是在引导模型，而不是在真正约束执行过程。你可以在 `CLAUDE.md` 里写 `不要修改 prod.env`，但这条规则能不能被稳定执行，最终仍然取决于模型自己的判断。

Hooks 就是 Claude Code 给出的另一种答案，它不是另一种提示词，也不是另一种上下文注入方式，而是一套运行在 Claude Code 执行流程中的程序化控制机制。比如当 Claude Code 准备调用工具、写入文件或执行命令时，Hooks 可以在动作真正发生之前介入，决定是放行、拦截，还是要求人工确认。它的判断不依赖模型是否理解并记住了规则，而是依赖你预先写好的代码逻辑，所以它提供的是一种更稳定的行为边界控制方式。本篇文章会从 Hooks 的基本结构开始，依次梳理它的事件体系、合并与决策机制、不同层级中的行为差异，以及真实插件里的实际用法。

<!--more-->

## Hook 的故事

我们先来看一个故事，看完这个故事，你会对 Hook 这个概念有一个深刻的理解。

{% img /images/post/2026/04/hook-fable.jpg 1000 600 %}

故事里那些**套在每件事外面的规矩**，就是 Hooks。

Claude Code 本身是一个很能干的**阿青**——它会读文件、写代码、执行命令、调用工具。但它做事的方式依赖于它每一次自己想起来，而你没法时时刻刻盯着它。你也许希望它每次写完 Python 文件都自动跑一遍格式化，每次执行危险命令前都先让你确认一下，每次读某类敏感文件时都先记录一笔日志——这些要求如果靠提示词反复叮嘱，它总有忘的时候。

Hooks 的机制，就是让你在 Claude Code 的执行过程中，注册一些必定会执行的处理逻辑，它们不是 Claude 决定要不要跑的，而是由系统强制触发的钩子。

### 从 settings.json 看 Hook 的结构

理解 Hook 最直接的方式，是看它在配置文件里长什么样，Claude Code 的 Hook 配置写在 `settings.json` 中，结构大致是这样的：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "echo 'a write happened'"
          }
        ]
      }
    ]
  }
}
```

第一次看到这个结构，很多人会感觉有点困惑，因为它有好几层嵌套，而且 `hooks` 这个词在不同层级里反复出现，指代的东西还不一样，我们从外往里一层层拆开来看。

- 第一层：`hooks` 对象是整个 Claude Code Hook 系统的入口，里面的每个 key 都是一个事件名称，比如 `PreToolUse`、`PostToolUse`、`Notification` 等，你可以把它理解成一张事件注册表，也就是想在哪些时机介入，就在这里注册哪些事件，更多的事件可以参考[这里](https://code.claude.com/docs/en/hooks#hook-lifecycle)。
- 第二层：每个事件名下面是一个数组，数组中的每个元素代表一组匹配规则，它包含一个 `matcher` 字段和一个 `hooks` 数组。`matcher` 决定这组规则在什么条件下生效，比如上面的例子里，`matcher: Write` 表示只有当工具名称是 `Write` 时，这组规则才会被触发。如果不写 `matcher`，则表示匹配该事件下的所有情况，关于 `matcher` 更多信息可以参考[这里](https://code.claude.com/docs/en/hooks#matcher-patterns)。
- 第三层：第二层里的 `hooks` 数组，里面的每一个元素才是一个真正的 Hook。每个 Hook 需要指定自己的类型和具体行为，比如 `type: command` 表示它会执行一条 shell 命令，而 `command` 字段就是具体要跑的命令内容，不同的 type 有不同的字段，Hook 元素的具体字段可以参考[这里](https://code.claude.com/docs/en/hooks#hook-handler-fields)。

用一张更直观的对应关系来看：

```text
hooks                          <- 整个 Hook 系统的入口
  └── [Event]                  <- 事件名，比如：PreToolUse，在工具调用前触发
        └── [Matcher]          <- 匹配规则，比如：matcher: Write，只匹配写文件操作
              └── [Hook]       <- 真正的处理逻辑，比如执行一条命令
```

之所以要理解这三层结构，是因为它决定了我们后面怎么读懂 Hook 的配置，只有先分清事件、匹配规则和真正的执行逻辑，后面看具体例子时才不会混淆。

### Hook 速览

我们对官方文档里的信息做一个快速归纳，以便让我们对 Claude Code 的 Hook 有一个更全面的了解，截至目前为止，官方的 Hook 系统的统计信息如下：

{% img /images/post/2026/04/hook-stats.png 1000 600 %}

- 官方文档当前列出了 `28` 种事件，覆盖会话、工具调用、权限、subagent、任务、压缩、工作目录变化和文件变化等关键节点。
- Hook 一共有 `5` 种类型：`command`、`http`、`mcp_tool`、`prompt`、`agent`。其中 `command` 是最常见的形式，但它已经不是唯一的形式。
- Hook 有 `6` 类定义位置，包括 user settings、project settings、local project settings、managed policy settings、plugin hooks，以及 skill / agent frontmatter。
- 所有 Hook type 共享 `5` 个通用字段：`type`、`if`、`timeout`、`statusMessage`、`once`。
- Decision control 可以分成 `9` 类，不同事件能返回的控制结果不同，所以后面看具体事件时，不能只记一个通用的 `allow` / `deny` 模型。

## Hook 的事件体系

了解了 Hook 的结构之后，接下来我们来看 Hook 的事件体系。Claude Code 官方文档当前列出了 28 种事件，这些事件覆盖了 Claude Code 执行过程中的各个关键节点，在众多 Agent 工具中，Claude Code 的事件体系是最全的，这也是它能够实现复杂流程控制的关键。

### 事件之间是平级的

在理解 Hook 事件时，有一个容易误解的地方需要先澄清一下，事件之间没有父事件和子事件的关系，也不存在某个事件涵盖另一个事件的嵌套结构。Claude Code 只是根据当前所处的生命周期位置，去触发对应的事件，再把匹配到的 Hook 跑起来。

之所以要特别强调这一点，是因为有些事件从语义上看很容易让人产生从属关系。比如 `PreToolUse` 和 `PermissionRequest` 这两个事件，前者在工具调用之前触发，后者在模型请求用户授权时触发。在很多实际场景中，Claude Code 准备调用一个工具时，先触发 `PreToolUse`，紧接着又因为这次调用需要用户授权而触发 `PermissionRequest`，两个事件几乎前后脚发生。这很容易让人觉得 `PermissionRequest` 是 `PreToolUse` 的子流程，或者后者是由前者产生的，但实际上它们是两个完全独立的事件，各自有各自的匹配规则，各自独立执行，互不干扰。

理解了这一点，你在设计 Hook 时就不会试图用一个事件去覆盖或代替另一个事件，而是会把它们当作执行链路上各自独立的介入点来分别处理。

那既然所有事件都是平级的，它们之间的区别到底在哪里？归结起来其实就是三点：

- 触发位置不同：有的在主流程上，有的在旁路上
- 触发时机不同：它们有先后顺序，但先后不等于嵌套
- 能力不同：有的能阻断流程（**Blocking**），有的只能观察（**Non-blocking**）

### 主流程事件与旁路事件

{% img /images/post/2026/04/hooks-lifecycle.jpg 1000 600 %}

官方的 Hook 生命周期（上图）把事件分成了两条路径：一条是贯穿整个执行过程的主流程，另一条则对应旁路事件。

主流程从 `SessionStart` 开始，一路经过 `PreToolUse`、`PermissionRequest`、`PostToolUse`、`Stop` 这些关键节点，直接决定 Claude Code 接下来会做什么。只要这个链路上的某个事件支持阻断，它就有机会在动作真正发生之前把流程拦下来。

旁路事件则更像观察和通知通道，比如 `Notification`、`ConfigChange` 等事件。它们并不挂在主流程的关键控制点上，因此通常不会改变这次执行的主线，只是在合适的时刻把信息传出来，或者做一些不影响主流程的额外处理。

### Blocking 与 Non-blocking

Blocking 意味着 Hook 的执行结果会直接影响 Claude Code 接下来的行为。以 `PreToolUse` 为例，当 Claude Code 准备调用一个工具时，它会先执行所有匹配到的 Hook，然后等待这些 Hook 返回结果，再根据结果决定这次工具调用是放行、拦截，还是需要用户确认。在 Hook 结果返回之前，主流程是暂停的。

Non-blocking 则相反，以 `Notification` 为例，当 Claude Code 需要通知用户时，它会触发注册在这个事件上的 Hook，但不会因为这个 Hook 的结果而停住主流程。你可以用它来记录日志、推送消息、同步状态，但不会拦截任何一个已经在执行链上的关键动作。

对于 Blocking 事件，Hook 有两种方式来阻断流程，它们的含义完全不同。

第一种是让脚本以 `exit 2` 退出，Claude Code 会把这种情况理解为一次系统层面的错误，比如工具不可用、资源缺失、环境出了问题等，模型感知到这个操作执行失败了，所以它可能会尝试理解错误原因，甚至尝试换一种方式绕过去。

> 注意：`exit 1` 在 Unix 惯例里通常表示失败，但在 Claude Code 的 Hook 体系里，`exit 1` 是非阻塞的，Claude Code 会忽略它继续执行，只有 `exit 2` 才能真正拦住流程。

第二种是让脚本正常退出（`exit 0`），但在 stdout 里输出一个包含类似 `"decision": "deny"` 的 JSON（不同的事件有不同的字段）。Claude Code 会把这种情况理解为一次策略层面的拒绝，不是系统出错，而是规则不允许。模型感知到这是一条明确的业务规则，所以它通常不会尝试绕过，而是会接受这个决策并调整自己的行为。JSON 方式还支持通过类似 `reason` 字段附带拒绝原因，甚至可以通过 `updatedInput` 字段修改工具的输入参数，这些都是 `exit 2` 方式做不到的。

## 不同层级中的 Hook

了解了 Hook 的一些概念后，我们现在涉及到一个很实际的问题：Hook 到底写在哪些地方？

在 Claude Code 里，Hook 并不只能写在用户自己的 `settings.json` 中，随着 Skill、Plugin、Subagent 这些机制的引入，Hook 的注册位置也变得更加多样，它们大体使用同一套配置格式和事件体系，但在生效范围、生命周期、支持字段和运行时转换上会有一些差异。

### Settings 中的 Hook

最基础的 Hook，就是直接写在 `settings.json` 里的那些，它们可以出现在用户级配置 `~/.claude/settings.json`、项目级配置 `.claude/settings.json`，或者项目本地配置 `.claude/settings.local.json` 中，除此之外，组织也可以通过 Managed policy settings 定义强制策略。

这类 Hook 从 Claude Code 启动开始就生效，贯穿整个会话，不会因为某个任务结束而被清理，前面两节里讲到的 Hook 都是这一类，你可以把它们理解成 Claude Code 的**常驻 Hook**，不管当前在执行什么任务，不管有没有 Skill 或 Plugin 参与，这些 Hook 始终在那里。

这几种 settings 层级的差异，可以简单看成作用范围不同：

| 定义位置                      | 作用范围             | 适合放什么                     |
| ----------------------------- | -------------------- | ------------------------------ |
| `~/.claude/settings.json`     | 当前用户的所有项目   | 个人习惯、跨项目通用的安全限制 |
| `.claude/settings.json`       | 当前项目             | 团队共享规则、仓库级流程       |
| `.claude/settings.local.json` | 当前项目，但不进仓库 | 个人临时覆盖、本机专用规则     |
| Managed policy settings       | 整个组织             | 公司级合规要求、强制安全策略   |

- `user` 级 Hook 会跟着你的机器走，适合放自己反复会用到、但又不想每个项目都重复写一遍的规则
- `project` 级 Hook 属于仓库的一部分，适合团队一起遵守的项目规则
- `local` 级 Hook 作用范围还是当前项目，但文件本身不会进仓库，更适合本机专用的补充规则
- Managed policy settings 则通常由管理员统一控制，用来定义组织级别的安全边界

### Plugin 中的 Hook

Plugin 是 Claude Code 的扩展机制，一个 Plugin 通常包含自己的 `CLAUDE.md`、Skill、命令，以及 Hook。Plugin 中的 Hook 写在 Plugin 自己的配置里，Claude Code 在加载 Plugin 时会把这些 Hook 和主体 Hook 合并在一起执行，所以从运行时的角度看，Plugin Hook 和主体 Hook 是平等参与的，不存在谁优先级更高的问题。

但 Plugin 中有一个明确的限制值得注意，Plugin 的 Subagent 不支持 Hook，官方在 Subagent 文档里明确说过，出于安全原因，Plugin Subagent frontmatter 中的 `hooks`、`mcpServers`、`permissionMode` 字段会被忽略。

这个限制的逻辑也不难理解，Subagent 是一个权限更受限的执行单元，如果允许 Plugin Subagent 自行注册 Hook，就等于给了一个更低权限的角色修改执行流程控制规则的能力，这会破坏整个权限模型的安全边界。

### Skill 中的 Hook

Skill 可以在自己的 frontmatter 中定义 Hook，和 settings 中的常驻 Hook 不同，Skill Hook 有一个更明确的生命周期，它们会在这次 **Skill 被调用时注册，Skill 执行结束后自动清理**，不会残留成全局 Hook。

下面是 [`planning-with-files`](https://github.com/othmanadi/planning-with-files) 这个 Skill 的一段 frontmatter，它定义了三个事件的 Hook：

```yaml
---
name: planning-with-files
hooks:
  PreToolUse:
    - matcher: "Write|Edit|Bash|Read|Glob|Grep"
      hooks:
        - type: command
          command: "cat task_plan.md 2>/dev/null | head -30 || true"
  PostToolUse:
    - matcher: "Write|Edit"
      hooks:
        - type: command
          command: "echo '[planning-with-files] File updated. If this completes a phase, update task_plan.md status.'"
  Stop:
    - hooks:
        - type: command
          command: |
            ...
---
```

这个 Skill 在每次调用 `Write`、`Edit`、`Bash` 等工具之前，先把 `task_plan.md` 的前 30 行打出来，让模型始终能看到当前的任务计划，然后在每次写入或编辑文件之后，提醒模型去更新计划状态，同时在 Skill 结束时跑一个收尾检查脚本。

### Subagent 中的 Hook

Subagent 可以在自己的 frontmatter 中定义 Hook，机制和 Skill 类似，在 Subagent 开始执行时临时注册，执行结束后自动清理。

官方文档里的 `code-reviewer` 就是一个典型例子：

```yaml
---
name: code-reviewer
description: Review code changes with automatic linting
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "./scripts/validate-command.sh $TOOL_INPUT"
  PostToolUse:
    - matcher: "Edit|Write"
      hooks:
        - type: command
          command: "./scripts/run-linter.sh"
---
```

这个 Subagent 的逻辑是，每次要执行 `Bash` 命令之前，先调用一个校验脚本来检查命令是否安全，同时每次执行了文件编辑或写入之后，自动跑一遍 linter。当这个 `code-reviewer` 被调用时，这两个 Hook 会临时注册进当前的执行环境，等它执行完毕，这些 Hook 就会被清理掉，不会影响后续其他任务或其他 Subagent 的行为。

这里还有一个细节值得留意，如果你在 Subagent 的 frontmatter 里注册了 `Stop` 事件的 Hook，运行时它会被自动转换成 `SubagentStop` 事件， 因为 Subagent 结束的是自己的执行，不是整个会话，所以它的停止事件在语义上对应的是 `SubagentStop`，而不是主流程的 `Stop`。

### 四种层级 Hook 的对比

目前为止，我们已经看到了四种不同位置的 Hook，它们的语法大体一致，事件体系也基本相同，真正的差异在于生效范围和生命周期：

| 层级          | 生命周期                   | 主要特点                             |
| ------------- | -------------------------- | ------------------------------------ |
| Settings Hook | 从会话开始到结束           | 常驻生效，适合个人、项目或组织级规则 |
| Plugin Hook   | 随 Plugin 加载而生效       | 可随插件分发，和主体 Hook 合并执行   |
| Skill Hook    | 随 Skill 调用注册和清理    | 适合任务级流程控制                   |
| Subagent Hook | 随 Subagent 执行注册和清理 | 适合代理内部的局部约束               |

主体 Hook 是常驻的，从会话开始到结束始终生效，Plugin Hook 随 Plugin 加载而生效，Skill Hook 和 Subagent Hook 都是临时的，跟随各自的执行周期注册和清理，不会污染全局环境。

这意味着 Claude Code 在任何一个时刻实际生效的 Hook 可能来自多个不同的层级，一个工具调用发生时，可能同时命中了主体配置里的 Hook、当前活跃 Plugin 的 Hook，以及正在执行的 Skill 里的 Hook。那么问题就来了：当这些来自不同层级的 Hook 同时生效时，Claude Code 是怎么处理它们之间的关系的？

## Hook 的合并与决策机制

下面我们来详细回答刚才那个问题，也就是当多个 Hook 同时生效时，Claude Code 是怎么处理它们之间的关系的。

{% img /images/post/2026/04/hook-merge.jpg 1000 600 %}

### 并行执行

最常见的情况是多个 Hook 注册在同一个事件上，匹配条件也相同或重叠，但各自定义了不同的处理逻辑。比如你在用户级配置里注册了一个 `PreToolUse` 的 Hook，用来记录所有写文件操作的日志。同时项目级配置里也注册了一个 `PreToolUse` 的 Hook，用来检查写入的目标路径是否在受保护目录中，这两个 Hook 都会匹配到同一次 `Write` 操作，但它们做的事情完全不同。

对于这种情况，Claude Code 的处理方式是，两个 Hook 都执行，而且是并行执行，它不会因为某个 Hook 来自更高的层级就跳过另一个，也不会按照注册顺序串行跑，所有命中的 Hook 会同时启动，各自独立完成自己的逻辑。

### 自动去重

另一种情况是，不同层级的配置里恰好注册了完全相同的 Hook，同一个事件、同一个匹配条件、连处理逻辑也一模一样，这种情况在多层配置共存时并不少见，比如一个 Plugin 和项目级配置里恰好都写了同一条规则。

对于这种情况，Claude Code 会自动去重，相同的 Hook 只会执行一次，官方文档里明确说明了去重的判断标准，对于 `command` 类型的 Hook，看命令字符串是否完全相同，对于 `http` 类型的 Hook，看 URL 是否完全相同。只要这些标识一致，Claude Code 就认为它们是同一个 Hook，合并后只保留一份。

这个去重逻辑在实际使用中很重要，这意味着你不需要担心多个层级意外重复注册同一个 Hook 导致脚本被重复执行，Claude Code 会在底层帮你处理这种冗余。但反过来说，如果你确实希望同一个脚本在不同条件下被分别触发，就需要确保它们在命令字符串或 URL 上有所区别，否则会被误判为重复而被合并掉。

### 最严格优先

前面两种情况处理的是谁该执行的问题，而当多个 Hook 都执行完了，却给出了不同的决策结果时，Claude Code 会听谁的呢？

这里的合并规则非常明确：`deny > ask > allow`，只要有一个 Hook 返回 `deny`，最终结果就是 `deny`，如果没有 `deny` 但有 `ask`，最终就是 `ask`，只有当所有 Hook 都返回 `allow` 时，最终才是 `allow`。

所以，这里的规则跟优先级和 Hook 来自哪个层级没有关系，Claude Code 不关心每个决策来自哪里，它只关心所有决策结果中**最严格**的那个。

用一个具体的场景来说明，假设你现在要执行一次 `Write` 操作，目标文件是 `./prod.env`，同时命中了三个不同层级的 Hook：

- 用户级配置里有一个 Hook，规则是普通写文件操作都放行，它返回了 `allow`。
- 项目级配置里有一个 Hook，规则是写 `.env` 文件前必须人工确认，它返回了 `ask`。
- 当前活跃的 Plugin 里也有一个 Hook，规则是禁止修改 `prod.env`，它返回了 `deny`。

三个 Hook 都参与了判断，各自独立给出了结果，Claude Code 拿到这三个结果之后，不会去比较它们各自来自哪个层级，而是直接根据决策结果 进行判断，因为有一个 `deny`，最终结果就是 `deny`，这次写入操作被直接拦截。

```text
用户级 Hook  -> allow
项目级 Hook  -> ask
Plugin Hook  -> deny
---------------------
最终决策     -> deny（只要有一个 deny 就拦住）
```

这样的设计是因为安全控制应该是向最严格的方向收敛，在多层配置共存的环境里，任何一个层级认为某个操作不应该发生，就足以阻止它发生，不需要获得其他层级的同意。这和很多安全系统的设计思路是一样的，允许需要所有人都同意，拒绝只需要一票否决。

理解了这一点，你在设计 Hook 时就会更清楚每一层应该承担什么角色，用户级配置适合放宽松的默认策略，项目级配置适合放针对当前仓库的具体约束，Plugin 和 Skill 里的 Hook 则适合放更聚焦的安全规则。即使它们各自的判断标准不同，最终 Claude Code 都会按最严格的结果来执行，你不需要在每一层都重复写全所有的限制条件。

## 实际案例

接下来我们来看两个实际案例，看看 Hook 在真实项目里到底是怎么样的。

这里我们看两个例子，一个是大名鼎鼎的 [`superpowers` Skill](https://github.com/obra/superpowers)，它的核心思路是，不要让 AI 一上来就写代码，而是让它按一套工程流程来工作，从而保证代码的质量和可维护性。另一个是 [`claude-code-warp`](https://github.com/warpdotdev/claude-code-warp)，[Warp](https://warp.dev/) 是我比较喜欢的一款终端工具，但它之前在使用 Claude Code 时都无法感知运行状态，导致每次 Claude Code 完成任务或者需要确认权限时用户都不知道，需要时不时切到 Warp 终端进行检查。所以 Warp 团队开发了 `claude-code-warp` 这个插件，用 Hook 把 Claude Code 的运行状态通过 Warp 终端通知给用户。两个组件的目标完全不同，但都很好地说明了 Hook 在真实项目里的用法。

### Superpowers：用 Hook 注入上下文

`superpowers` 是一个围绕软件开发方法论构建的插件，它提供了一整套从需求梳理、计划编写、测试驱动到代码评审的 Skill 体系，但如果你打开它的 `hooks/hooks.json`，会发现它注册的 Hook 出人意料地少，只有一个：

```json
{
  "hooks": {
    "SessionStart": [
      {
        "matcher": "startup|clear|compact",
        "hooks": [
          {
            "type": "command",
            "command": "\"${CLAUDE_PLUGIN_ROOT}/hooks/run-hook.cmd\" session-start"
          }
        ]
      }
    ]
  }
}
```

整个插件只在 `SessionStart` 这一个事件上挂了 Hook，触发后会运行一个 `session-start` 脚本，这个脚本做的事情就是把 Superpowers 的技能说明（`using-superpowers` 的 `SKILL.md` 内容）作为 `additionalContext` 注入到会话中。具体来说，它会检测当前运行平台，然后按对应格式输出 JSON，在 Claude Code 环境下会输出结构化的 `hookSpecificOutput`，Claude Code 读取其能力说明并作为后续工作的上下文。

Superpowers 的核心能力并不依赖 Hook 来实现流程控制，它的 Skill 体系本身就足够完整，Hook 在这里只承担了一个很轻的职责，就是确保每次会话启动时，模型能拿到正确的初始上下文。这也是 Hook 一个容易被低估的用法，它不一定要用来拦截或审批，有时候只是在正确的时机把正确的信息带进来，就已经足够有价值了。

### Warp：把 Claude Code 的生命周期转成终端体验

`claude-code-warp` 的思路完全不同，它不是在 Claude Code 中注入上下文，而是为了让 Warp 终端知道 Claude Code 现在处在什么状态，是正在工作、等待用户输入、请求权限，还是已经完成任务。

它注册的 Hook 明显更多：

```text
SessionStart       -> on-session-start.sh
Stop               -> on-stop.sh
Notification       -> on-notification.sh
PermissionRequest  -> on-permission-request.sh
UserPromptSubmit   -> on-prompt-submit.sh
PostToolUse        -> on-post-tool-use.sh
```

这组 Hook 覆盖了 Claude Code 会话里的几个关键状态变化。

- `SessionStart`：会话开始时发送插件版本和初始化信息
- `UserPromptSubmit`：用户提交 prompt 后，通知 Warp Claude Code 开始工作
- `PostToolUse`：工具调用结束后，通知 Warp 当前阻塞状态已经解除
- `Notification`：当 Claude Code 因为空闲等待用户输入时触发通知
- `PermissionRequest`：当 Claude Code 请求工具权限时，把工具名称和输入预览发给 Warp
- `Stop`：任务结束时读取会话信息，提取用户 prompt 和 Claude 的回复摘要，再发送完成通知

这些 Hook 把 Claude Code 内部的生命周期事件翻译成 Warp 能理解的状态事件，插件的说明文件也明确写到，这个插件的 Hook 脚本会构造一个结构化的 JSON 对象，然后交给 Warp 解析，用来驱动通知中心、系统通知等。

比如 `PermissionRequest` 对应的脚本会从 Hook 输入里取出工具名称和参数输入，再生成一段可读的摘要，这样当 Claude Code 想执行某个工具、需要你确认时，Warp 不只是知道有一个权限请求，还能显示它大概想做什么。

`Stop` 的脚本则更典型，它不是简单地说任务结束了，而是会读取本次会话内容，从会话内容里提取最后一次用户输入和 Claude 的回复，并截断成适合通知展示的长度，这样一个 Hook 就把 Claude Code 的内部记录变成了用户能感知的完成通知。

这个例子介绍了 Hook 另外一种常见用法，它可以作为一个事件桥，把 Claude Code 的执行过程同步给外部系统。

### 多了解真实的 Hook

通过 Superpowers 和 Warp 这两个例子，我们可以看到，Hook 的写法本身并不复杂，真正值得学习的是它们选择在什么事件上介入，以及把这次介入设计成什么样的行为。

学习 Hook 只看语法和文档很难形成直觉，更有效的方法，是多看一些真实插件里的 Hook 配置，它们为什么选择这种事件，为什么使用这种匹配规则，为什么把某些逻辑放在 Hook 里，而不是写进 Skill 或 Slash Command。看得多了之后，你会慢慢建立一种判断，哪些问题适合用 Hook 解决，哪些问题其实应该交给 `CLAUDE.md` 或 Skill 来解决。

## 总结

本文从 Claude Code 如何控制 AI 的行为边界出发，依次梳理了 Hook 的配置结构、事件体系、不同层级中的注册方式、合并与决策机制，以及它在真实插件中的实际用法。

和前面两篇文章里介绍的 `CLAUDE.md`、Slash Command、Skill 不同，Hook 不是在引导模型应该怎么理解任务，而是在模型执行任务的过程中，提供一层程序化的介入能力。它的判断不依赖模型的理解力，而是运行你预先写好的代码逻辑，所以它提供的是确定性的保障。这也是它在整个 Claude Code 体系里不可替代的位置，`CLAUDE.md` 负责让模型理解项目，Skill 负责组织复杂任务，而 Hook 负责在关键节点上守住边界。

不过也正因为 Hook 运行的是真实代码，它的影响是即时且不可撤回的，一个写错了退出码的脚本可能会让流程意外中断，一个没有处理好退出逻辑的 `Stop` Hook 可能会让会话陷入死循环。所以在设计 Hook 时，除了想清楚它应该在什么时机介入、做出什么决策，也要像对待任何生产代码一样，认真处理好边界情况和错误路径。

## 参考

- [Automate workflows with hooks](https://code.claude.com/docs/en/hooks-guide)
- [Hooks reference](https://code.claude.com/docs/en/hooks)
- [claude-code-hooks-mastery](https://github.com/disler/claude-code-hooks-mastery)
- [Claude Code Hooks: A Practical Guide to Workflow Automation](https://www.datacamp.com/tutorial/claude-code-hooks)
- [Hooks & Lifecycle Automation](https://patrykgolabek.dev/guides/claude-code/hooks/)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
