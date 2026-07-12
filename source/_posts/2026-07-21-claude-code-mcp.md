---
layout: post
title: Claude Code 完全指南——MCP
date: 2026-07-21 16:00:00
description: 深入解析 Claude Code 的 MCP，包括协议原理、服务接入、上下文机制，以及与 Skill 的区别
keywords: claude code, mcp, connector, ai coding, anthropic
comments: true
categories: ai
tags: [claude-code, mcp, connector, ai-coding, anthropic]
---

{% img /images/post/2026/07/claude-code-mcp.jpg 400 300 %}

[上一篇文章](/2026/06/09/claude-code-plugin)里我们聊了 Plugin，看到它如何把命令、Skill、Hook 打包成一个可分发的整体，在那份打包的清单里，还有一样我们一直没单独展开的东西，那就是 MCP 服务。到目前为止我们讲的所有能力，其实都还局限在 Claude Code 自己的范围内，它只能读你本地的仓库、跑你本地的命令，对外面世界发生的事情一无所知。如果你想让它在写代码时直接查 Notion 里的需求文档、读 GitHub 上的 Issue、甚至连上数据库查真实数据，那就需要 MCP 登场了。MCP 给 Claude Code 开了一扇通往外部数据和工具的门，让它不必再靠我们复制粘贴信息，就能直接读写外部系统。这篇文章我们就来聊聊 MCP 到底是什么、背后的协议长什么样、在 Claude Code 里怎么用，并看看 MCP 与 Skill 有哪些区别。

<!--more-->

## 什么是 MCP

MCP 这个词在不同语境下其实指向两种含义。第一种是指 Model Context Protocol（模型上下文协议）的缩写，由 Anthropic 在 2024 年底开源，官方对它的定义是：

> MCP is an open-source standard for connecting AI applications to external systems.

第二种是在 Claude Code 的具体使用场景里，我们常说的**一个 MCP** 往往指的是一个可安装的 MCP 服务，比如 Notion MCP、GitHub MCP，它更像是一个连接器，装上之后 Claude Code 就能读写对应平台的数据。

> 连接器（Connector）：这是 Claude 产品线里对 MCP 服务的另一种叫法，后面会详细介绍，本文中 MCP 服务和连接器两个词会一起使用。

这两层含义并不矛盾，协议是底层规范，服务是基于规范的具体实现。这就像 HTTP 与网站的关系，HTTP 是底层通信协议，网站则是基于协议构建出来的具体服务。两者在日常表达中偶尔会被混用，但处在不同的抽象层次。

MCP 官方有一个很贴切的比喻：MCP 就像 USB 接口。在 USB 出现之前，每种电脑设备都有自己的接口标准，电脑要为每种设备预留专门的插槽。有了 USB 之后，设备和计算机只要分别遵循同一套标准，就可以相互连接。MCP 之于 AI 也是同样的道理，在 MCP 出现之前，想让 AI 接入一个外部工具，每个工具都要单独写一套对接逻辑，N 个 AI 产品加 M 个工具就是 N×M 种集成，有了 MCP 之后，每个 AI 产品实现一次 MCP 客户端，每个工具实现一次 MCP 服务端，集成成本就降到了 N+M。

{% img /images/post/2026/07/claude-code-mcp-connector-map.jpg 1000 563 %}

## MCP 协议

> 如果你着急想在 Claude Code 里把 MCP 用起来，可以先跳过这一节直接看后面的章节，之后再回来看协议细节也不迟。毕竟你不会因为不懂 USB 协议就插不上 U 盘，MCP 也一样，先用起来、再理解原理，是更实际的入门路径。

如果你打算自己写一个 MCP 服务，或者遇到连接问题需要排查，那我们就需要了解 MCP 协议到底是怎么运作的了。

### 协议分层

MCP 协议在设计上分成了数据层和传输层，这种分层思路在很多网络协议里都能看到，好处是让**传输内容**和**传输方式**相互隔离，互不干扰。

- 数据层：就像我们生活中的快递包裹，它决定包裹里面装的是什么，以什么形式打包。数据层基于 JSON-RPC 2.0 定义了消息的内容，包括 Tools（可执行函数，让 AI 调用外部能力）、Resources（数据源，提供上下文信息）、Prompts（交互模板，帮助 AI 理解如何使用这些能力）等，这些是 MCP 服务向外暴露能力的主要方式
- 传输层：就像快递的运送方式，决定包裹是走陆运还是空运。传输层不关心消息内容，只负责把这些消息从客户端运送到服务端，管理消息通过什么通道发送出去，客户端和服务端如何建立连接以及两端如何认证等事情。

### 协议角色

在 MCP 的架构里，涉及三种角色，它们各自有不同的职责：

- MCP Host（宿主）：运行 AI 模型的应用本身，比如 Claude Code 本身就是一个 Host。Host 负责接收用户输入、调用模型推理，并把 MCP 服务提供的能力注册成模型可调用的工具。
- MCP Client（客户端）：Host 内部的一个组件，负责与某一个 MCP 服务维持一对一的连接。一个 Host 可以同时管理多个 Client，每个 Client 对接一个 Server。Client 不直接面向用户，它只是 Host 和 Server 之间的消息通道。
- MCP Server（服务端）：提供外部能力的程序，比如 Notion MCP 服务、GitHub MCP 服务等。Server 启动后等待 Client 连接，通过协议暴露自己有哪些能力，并在收到调用请求时执行实际操作。

下面是 Host、Client、Server 三者从启动到断开连接的完整交互流程，图中还包含了 Server 背后的外部系统：

{% img /images/post/2026/07/mcp-sequence-diagram.jpg 1000 600 %}

整个流程可以分成六个阶段：

1. 连接阶段：Host 启动后，MCP Client 去连接对应的 MCP Server，建立传输通道，连接就绪后通知 Host
2. 能力协商：Client 向 Server 发送 initialize 请求，获取协议版本和服务支持的能力，Server 返回自己暴露的能力列表
3. 使用工具：Host 收到用户请求后，让 Client 向 Server 发起工具调用请求（tools/call）或资源读取请求（resources/read），Server 收到后去访问外部系统（数据库、API、文件系统等）完成实际操作
4. 返回结果：Server 把操作结果返回给 Client，Client 再交给 Host，最后由 Host 生成回答或展示给用户
5. 会话保持与通知：在会话期间，Server 还可以在资源变更等场景下主动推送通知给 Client，Client 接收到通知后再更新 Host 的 UI 或上下文
6. 断开连接：会话结束时，Host 通知 Client 关闭连接，Client 向 Server 发送关闭请求，连接断开。

### 传输类型

MCP 目前支持两种标准传输：

- stdio（本地）：Client 把 MCP 服务作为子进程启动，消息通过标准输入输出传递。因为是本机进程间通信，没有网络开销，性能最好，这也是 Claude Code 默认采用的模式，Server 跑在你自己的机器上，由 Claude Code 帮你管理进程的启动和关闭。
- Streamable HTTP（远程）：Server 暴露一个单一 HTTP 端点，Client 向这个端点发送 JSON-RPC 请求，Server 根据场景选择两种响应方式，既可以单次返回完整 JSON 结果，也可以走 SSE（Server-Sent Events 是基于 HTTP 的单向流式推送机制）流持续推送消息。

> Streamable HTTP 的演进历史：以前 MCP 的远程传输用的是 HTTP + SSE 双端点方案，一个端点处理客户端请求，另一个端点处理服务器推送，实现和配置都很复杂。在 2025 年 3 月 26 日的 spec 这次修订之后，通过 Streamable HTTP 把它们合并成了单一端点，把返回是纯 JSON 还是 SSE 流变成了 Server 的运行时选择，这样大大降低了远程 MCP 服务的部署复杂度，无需再同时维护两个端点了。

### 消息格式

MCP 从头到尾都用 JSON-RPC 2.0 作为消息编码格式，每一条消息都是合法的 JSON-RPC 请求、响应或通知。既然底层就是 JSON-RPC，我们完全可以用 curl 直接调用一个远程 MCP 服务，验证一下协议是怎么工作的，假设你有一个跑在本机 3000 端口的 MCP 服务：

```sh
curl -X POST http://localhost:3000/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
        "name": "weather_current",
        "arguments": {
            "location": "Beijing",
            "units": "metric"
        }
    }
  }'
```

- `-X POST` 指定请求方法，Streamable HTTP 只用 `POST` 和 `GET`
- `-H` 设置 `Content-Type` 为 JSON，MCP 的消息体都是 JSON 格式
- `jsonrpc` 固定为 `2.0`
- `id` 用于关联请求和响应，必须是字符串或数字，且不能为 null，同一会话内不能重复
- `method` 是调用的方法名，比如 `tools/list`、`tools/call`、`initialize`
- `params` 是方法参数，按具体方法定义

对应的 JSON-RPC 响应有两种，成功的返回 result，失败的返回 error，两者不会同时出现，下面是成功的返回结果：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [{ "type": "text", "text": "Beijing: 28°C, partly cloudy" }]
  }
}
```

## Claude Code 中的 MCP 服务

了解了协议层面的机制，我们回到 Claude Code 本身，看看在 Claude Code 里是怎么安装和使用 MCP 服务的。

### 安装与使用

Claude Code 安装 MCP 服务最常用的命令是 `claude mcp add`，根据服务器的运行方式不同，主要有以下几种模式。

第一种是本地 stdio 服务，服务器作为子进程在你本机跑，Claude Code 通过标准输入输出跟它通信，适合需要本地文件系统访问、或者自己写的定制工具：

```bash
claude mcp add playwright -- npx -y @playwright/mcp@latest
```

- stdio 模式是 `--transport` 参数的默认值，所以不用传也可以
- `--`（双横线）后面的内容才是真正传给服务器的命令和参数，前面是 Claude Code 自己的选项
- `--` 一定要写，否则 Claude Code 会把 stdio 服务的参数当成自己的选项来解析

第二种是远程 HTTP 服务，服务器跑在云端，Claude Code 通过网络连接，这是目前最推荐的方式，大部分 SaaS 类 MCP 都用这种模式：

```bash
claude mcp add --transport http notion https://mcp.notion.com/mcp
```

需要认证的话，可以通过 `--header` 传 token，也可以运行 `/mcp` 走 OAuth 浏览器登录。

还有一种安装方式是 `claude mcp add-json`，它接受一个完整的 JSON 配置，适合更复杂的场景，比如 WebSocket 传输。`claude mcp add` 的 `--transport` 参数只支持 `http`、`stdio` 和 `sse` 三种，不支持 `ws`，这时可以用 `claude mcp add-json` 直接传 JSON 配置：

```bash
claude mcp add-json events-server \
  '{"type":"ws","url":"wss://mcp.example.com/socket","headers":{"Authorization":"Bearer YOUR_TOKEN"}}'
```

这会绕过 CLI 的限制，直接写入底层配置，WebSocket 的优势在于持久化的双向连接，服务器可以主动推送事件到 Claude Code，适合实时通知类的场景。

如果 MCP 服务需要环境变量（比如 GitHub MCP 需要 Token），可以用 `--env` 传入：

```bash
claude mcp add github --env GITHUB_TOKEN=ghp_xxx -- npx -y @modelcontextprotocol/server-github
```

但这里要注意，如果你把服务名紧跟在 `--env` 后面，CLI 会误把服务名当成另一个环境变量的值：

```bash
# error: github will be treated as an env value
claude mcp add --env GITHUB_TOKEN=ghp_xxx github -- npx -y @modelcontextprotocol/server-github
```

解决办法是在 `--env` 和服务名之间插入至少一个其他选项，比如 `--transport` 或 `--scope`，或者将服务名写到 `--env` 参数前面。

另外要注意的一点是，`workspace` 是 Claude Code 内部保留关键字，如果你用它做 MCP 服务名，Claude Code 会拒绝加载并提示你改名。此外 `claude-in-chrome`、`computer-use` 等几个名字也被保留了，取名时不要用这些名字。

### 作用域

MCP 服务有三个安装作用域，决定了它在哪些项目中生效、能不能共享给团队：

- local：适合你自己用的实验性服务或不希望进仓库的私密配置，保存在 `~/.claude.json` 该项目条目下面，不提交到 Git
- project：适合团队共享，保存在项目根目录下的 `.mcp.json`，文件提交到 Git 后，队友 clone 下来就能看到同样的 MCP 配置
- user：适合你个人跨项目都要用的通用工具，保存在 `~/.claude.json` 顶层 mcpServers 这个键下面

```json
// ~/.claude.json
{
  // MCP server in user scope
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/",
      "headers": {
        "Authorization": "Bearer ghp_xxxxx"
      }
    }
  },
  // MCP server in local scope
  "projects": {
    "/your/project/path": {
      "mcpServers": {
        "sentry": {
          "type": "http",
          "url": "https://mcp.sentry.dev/mcp"
        }
      }
    }
  }
}
```

指定作用域很简单，加个 `--scope` 即可：

```bash
# install as project scope
claude mcp add --scope project --transport http sentry https://mcp.sentry.dev/mcp

# install as user scope
claude mcp add --scope user --transport http github https://api.githubcopilot.com/mcp/
```

如果多个作用域定义了同名 MCP 服务，Claude Code 会采用优先级最高的配置，其他作用域中的同名服务会被忽略，优先级从高到低如下：

```text
local > project > user > plugin > claude.ai
```

这个优先级表示，如果你在 local 和 user 作用域都装了同名的 MCP 服务，Claude Code 会用 local 的那份，user 里的配置完全被忽略。这个设计是为了让越接近当前项目的配置越优先，所以最局部的 local 反而覆盖了最全局的 user。

### MCP 在 Claude 产品中的几种形态

MCP 虽然是同一套协议，但它在 Anthropic 的几款产品里表现出来的形态不太一样，名字和配置入口都各有不同，容易让人误以为是几个东西。

{% img /images/post/2026/07/claude-code-mcp-claude-product-forms.jpg 1000 563 %}

- Claude Code 里就叫 MCP，是最贴近开发者、最灵活的方式，你可以在终端里完成从安装到调试的全部操作。
- Claude.ai（网页版）里叫 Connector（连接器），通过 OAuth 进行云端授权连接，Connector 只支持远程 HTTP 服务，不需要你手写命令行或配置文件。你在 claude.ai 上连接的 Connector 会自动同步到 Claude Code 里，只要用同一个账号登录，就会发现它们已经在 MCP 列表里了。
- Claude Desktop 里也叫 Connector，配置上区分云端和本地两种，云端配置和网页版一样连云端的远程服务，本地配置通过 [`mcpb（MCP Bundle）`](https://claude.com/docs/connectors/building/mcpb)这个打包格式安装。

三者底层跑的都是 MCP 协议，区别只在产品位置和配置方式，Claude Code 面向开发者，所以用 CLI 管理，Claude.ai 和 Claude Desktop 面向更广泛的用户，所以用更友好的界面操作方式。如果你开发了一个 MCP 服务，理论上三个产品都能用，只是接入方式有所不同。

## MCP 与上下文

如果你在 Claude Code 里面安装了很多 MCP 服务，你可能会有这样的担心：Claude Code 会不会因为加载了太多 MCP 服务而变得又慢又笨？毕竟每个 MCP 都有一堆工具定义等信息，如果全部塞进上下文窗口，那留给实际对话的空间就所剩无几了。

在早期的 Claude Code 版本里，MCP 服务确实是这样工作的，不管用不用，每个 MCP 的全部工具定义都在会话启动时一股脑加载进上下文。装的 MCP 服务越多，可以使用的工具就越多，上下文被占据得就越多，留给代码分析、对话记忆的空间就越少。更麻烦的是，工具一多，模型选错工具的概率也跟着上升。所以早期社区里流传着一条经验，说 MCP 服务别安装超过五个，有的人为了节省上下文甚至不安装任何 MCP 服务。

但现在情况已经完全不同了，Claude Code 默认启用了 tool search 机制，从根本上解决了这个问题。

### Tool search 机制

[Tool search](https://claude.com/docs/third-party/claude-desktop/gateway#mcp-tool-search) 的核心思路很简单，会话启动时只加载每个 MCP 服务的工具名称和服务说明（server instructions），不加载完整的工具定义和参数 Schema。只有当 Claude Code 判断某个工具确实需要被调用时，才会通过一次额外的搜索获取那个工具的完整定义。这样一来，你就算安装再多的 MCP 服务，启动时的上下文占用也只是多了一串工具名而已，影响微乎其微，真正进入上下文的，只有模型实际用到的那几个工具。

Tool search 机制在 Claude Code 里是默认开启的，如果你想调整它，可以通过环境变量 `ENABLE_TOOL_SEARCH` 来设置：

- `false`：关闭 Tool Search，所有 MCP 工具定义每轮全部加载
- `auto`：会根据 MCP 工具定义占用比例决定是否启用，超过上下文的 10% 时启用 tool search

我们可以做一个很直接的对比实验，下面的两张图来自同一台机器，同一个项目的 Claude Code 会话，区别只在于启动方式不同。

第一张是正常启动（tool search 默认开启）Claude Code，然后输入 `/context` 命令后看到的上下文占用情况：

{% img /images/post/2026/07/cc-context-no-mcp.png 1000 600 %}

总上下文占用只有 22.4k tokens（2%），细项里能看到 System prompt、System tools、Skills 等常规内容，但没有 MCP tools 这一项，因为工具定义全都被延迟加载了，不占启动上下文。

第二张是关闭 tool search 后启动（使用 `ENABLE_TOOL_SEARCH=false claude` 命令）Claude Code，同样的项目，同样的 MCP 服务配置：

{% img /images/post/2026/07/cc-context-has-mcp.png 1000 600 %}

总上下文占用飙升到了 74.5k tokens（7%），多出了一整行 MCP tools: 37.2k tokens（3.7%）。不仅如此，System tools 也从 8.6k 涨到了 23.5k，因为 tool search 关闭后，不仅 MCP 工具定义全部涌入，连系统工具的 Schema 也跟着一起膨胀了。

也就是说，tool search 帮我们省掉了超过 5 万 tokens 的启动上下文，这个数字会随着你安装的 MCP 服务数量继续增加，但如果开启了 tool search 那就几乎没有什么影响了。

### alwaysLoad 配置

如果你希望某个 MCP 服务的工具始终完整加载，不受 tool search 机制影响，可以用 alwaysLoad 配置，把它设为 true，Claude Code 就会自动加载该 MCP 的所有工具定义到上下文里：

```json
{
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/",
      "alwaysLoad": true
    }
  }
}
```

适用于你高频使用，希望 Claude Code 总能第一时间发现并调用的 MCP 服务。

最后提一下，tool search 并不是 Claude Code 特有的功能，它实际上是根据 MCP 客户端实现里两条最佳实践开发出来的，这意味着其他 MCP 客户端也可以具备这个功能：

- [渐进式发现（progressive discovery）](https://modelcontextprotocol.io/docs/develop/clients/client-best-practices#progressive-tool-discovery)：意思是客户端不该一股脑把所有能力都发送给模型，而应该在需要时再逐层揭示，tool search 正是把这条原则用在了工具定义上。
- [程序化工具调用（programmatic tool calling）](https://modelcontextprotocol.io/docs/develop/clients/client-best-practices#programmatic-tool-calling-/-code-mode)：让模型通过写代码来编排、调用工具，而不是一个个手动调，从而进一步压缩上下文和往返次数。

两者结合，让 MCP 既能大规模接入外部能力，又不会压垮上下文窗口。

## MCP vs. Skill

在这个系列里我们简单介绍过 Skill，它和 MCP 都能扩展 Claude Code 的能力，也都能被打包进 Plugin 分发给团队成员，如果只看表面效果，很多人都分不清一个功能到底该用哪个来实现更好，今天我们就用一个数据库场景的例子来对比一下。

我们先看用 MCP 怎么做，首先我们需要找一个数据库的 MCP 服务进行安装，比如 [dbhub](https://github.com/bytebase/dbhub)：

```bash
claude mcp add --transport stdio postgres \
  -- npx -y @bytebase/dbhub --dsn "postgresql://readonly@localhost:5432/analytics"
```

安装好之后，Claude Code 就多出了一个操作这个数据库的工具，你问：用户表里有多少条记录，Claude Code 就会调用 MCP 工具执行 `SELECT COUNT(*) FROM users`，拿到真实结果然后回答你。数据库连接和 SQL 的实际执行都发生在 MCP 服务那一端，Claude Code 本来并不具备连数据库的能力，是这个 MCP 服务提供了这份能力。

再看用 Skill 是如何实现的，在 Skill 里面写的是一个技能文件，告诉 Claude Code 在数据库查询时该遵循什么规范、SQL 该怎么写、哪些表可以用、有哪些注意事项等。

```text
db-query-skill/
├── SKILL.md
├── schema.md
└── conventions.md
```

`SKILL.md` 作为主入口，说明这个 Skill 负责什么、什么场景下自动触发，`schema.md` 和 `conventions.md` 记录了表结构、查询规范这些领域知识，需要时才加载。你同样问：用户表里有多少条记录，Claude Code 会读这些材料，按照你定的规范生成一条合乎约定的 SQL。但 Skill 本身并不能连接数据库，它只是帮你写 SQL 语句，这条 SQL 最终通过 Claude Code 自带的 Bash 能力去跑（前提是本机安装了 psql 并且能连得上这个数据库库）。

在这两个例子中 MCP 解决的是数据库连接和查询的问题，它给 Claude Code 接入了原本没有的外部能力，而 Skill 规范数据库查询标准，它记录的是知识和流程，下面是两者的对比情况：

| 维度       | MCP                             | Skill                                          |
| ---------- | ------------------------------- | ---------------------------------------------- |
| 能力来源   | 外部真实服务，直接连接数据源    | 本地指令文件，提供规范和流程                   |
| 执行方式   | AI 调用工具，自动执行并返回结果 | AI 按指令工作，产出文本或代码                  |
| 适用场景   | 需要真实数据交互、读写外部系统  | 规范化重复任务、复用团队经验                   |
| 上下文消耗 | tool search 管理下按需加载      | 只加载名称和描述，触发后再加载 SKILL.md 和材料 |

这里要注意的是 Skill 和 MCP 一样，现在都是按需加载了，所以不用太过担心上下文占用的问题。

**那我们到底什么时候使用 MCP，什么时候使用 Skill 呢？**

一个简单的判断是，看你缺的是**能力**还是**规范**。如果你希望 Claude Code 能真正去读写数据库、调用 API、操作外部系统，装一个对应的 MCP 服务是最直接的方式，AI 拿到的是真实的能力。如果你希望 Claude Code 在某类任务上遵循固定的流程和规范，比如写 SQL 查哪些表、写 commit message 时符合团队格式、做 code review 时关注特定问题，用 Skill 把这些经验固化下来更合适。

而更多时候，两者并不是二选一，而是结合起来使用。以我们的数据库这个例子，你可以用 MCP 把连接和查询工具接进来，并同时创建一个 Skill，把数据库查询规范固化下来。MCP 负责打通能力，Skill 负责规范用法，各司其职，这往往才是复杂场景下最理想的组合。

## 总结

本文从协议到实操，完整介绍了 MCP 在 Claude Code 中的使用，它就是一套连接 AI 与外部世界的开放协议，在 Claude Code 里通过几条命令就能安装和管理，还能以连接器的形式跨 Claude.ai、Claude Desktop 等产品存在。得益于 tool search 机制，你不必再担心 MCP 数量挤占上下文，按需安装即可。

随着越来越多的服务提供官方 MCP 服务，MCP 正在从一个能连外部工具的新协议，慢慢成长为 AI 应用与真实世界之间标准。现在可以看到，MCP 已经开始向垂直行业渗透，广告领域的 [AdCP](https://docs.adcontextprotocol.org/docs/intro)、电商领域的 UCP、支付领域的 ACP 等协议纷纷选择在 MCP 协议之上定义各自的领域语义，可以预见，随着 MCP 成为协议标准，基于它构建的上层协议只会越来越多——而这一切，只是刚刚开始。

## 参考资料

- [Connect to MCP servers - Claude Code Docs](https://code.claude.com/docs/en/mcp-quickstart)
- [Connect Claude Code to tools via MCP - Claude Code Docs](https://code.claude.com/docs/en/mcp)
- [Control MCP server access for your organization](https://code.claude.com/docs/en/managed-mcp)
- [What is the Model Context Protocol (MCP)?](https://modelcontextprotocol.io/docs/getting-started/intro)
- [Building custom connectors](https://claude.com/docs/connectors/building)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
