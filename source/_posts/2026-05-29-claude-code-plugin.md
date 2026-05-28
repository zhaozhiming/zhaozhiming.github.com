---
layout: post
title: Claude Code 完全指南——Plugin
date: 2026-05-29 16:00:00
description: 深入解析 Claude Code 的 Plugin 插件系统，包括插件概念、安装使用、市场机制、限制不足，以及与其他 Coding Agent 插件系统的对比
keywords: claude code, plugin, ai coding, anthropic
comments: true
categories: ai
tags: [claude-code, plugin, ai-coding, anthropic]
---

{% img /images/post/2026/06/claude-code-plugin.jpg 400 300 %}

在这个系列里，我们已经陆续聊过几样东西，用 CLAUDE.md 把项目背景和协作约束提前讲清楚，用 Slash Command 和 Skill 把反复出现的任务收敛成稳定的工作流，再用 Hooks 在执行的关键节点上守住行为边界。把这些放在一起看，你会发现我们其实一直在做同一件事，不断给 Claude Code 添加各种各样的定制，让它越来越懂你的项目，越来越贴合你的工作方式。但定制做得越多，一个很现实的问题也会跟着冒出来，这些散落在各个文件和目录里的配置、命令、技能和钩子，到底要怎么才能完整地复用起来？当你换一台机器、开一个新项目，或者想把这一整套用得很顺手的配置分享给同事时，难道每次都得一个文件一个文件地手动拷贝吗？这一篇，我们就来聊聊 Claude Code 为这个问题准备的答案——Plugin。

<!--more-->

## Plugin 介绍

Plugin 其实就是把前面这些散落的能力打包到一起的一个分发单元。我们这个系列前面讲过的 CLAUDE.md、Slash Command、Skill、Hook，再加上 Subagent 和 MCP Server，都是 Claude Code 的可定制能力，但它们在文件系统里是分开存放的。Plugin 做的事情，就是把这些组件按约定的目录结构收进同一个文件夹，再配上一份描述自己身份的 `plugin.json` 清单，让它们成为一个能被整体安装、启用和分发的单位。所以 Plugin 是一种组织和分发已有能力的方式，如果说 CLAUDE.md、Skill、Hook 是一件件单独的工具，那 Plugin 就是把它们打包好的一个 App，装上它，这一整套能力就同时生效了。

> Claude Code 的 MCP Server 可以理解为一种通过标准协议接入外部工具、数据源或内部系统的服务，让 Claude Code 能像调用工具一样访问这些能力。

之所以需要 Plugin，核心原因是复用。只在一个项目里给自己配置 Claude Code 时，直接把命令、技能、钩子写在 `.claude/` 下确实最省事，但只要你想换台机器、开个新项目，或者把这套配置分享给同事，如果靠的就是手动拷贝那就很痛苦了。手动拷贝既没有版本概念、又容易遗漏文件，有些组件甚至拷过去也没法直接用，比如你只拷贝了技能目录，但它依赖的命令、Hook、Subagent 等，还需要通过插件机制自动注册后才能生效。Plugin 就是把一套能力固定成有名字、有版本、结构清晰的整体，让分享、安装、更新、卸载都变成可管理、可重复的操作。

这样一来，使用者无需关心 Plugin 内部到底是通过命令还是 Hook 实现的，清单中的版本号还能让团队明确每个人正在使用的具体版本，插件也可以安装到项目范围内，随仓库一起共享。团队的 AI 协作规范，也因此第一次拥有了可打包、可分发的载体。此外，插件中的每个组件都会以插件名作为命名空间，比如 `/my-plugin:hello` 而不是 `/hello`，这样即便同时装了多个来源不同、且包含同名组件的插件也不会发生相互覆盖。

## Plugin 的安装与使用

要理解安装流程，先得分清 Marketplace 和 Plugin 的关系。Plugin 是你最终要安装的那个能力包，而 Marketplace 是一个收录了很多 Plugin 的目录，本身并不包含能力，只是告诉 Claude Code，这里有哪些插件可以装。所以安装插件通常是两步：先把市场添加进来，让 Claude Code 能浏览它收录的内容，再从里面挑选具体的插件安装。官方文档给的类比很贴切，市场就像应用商店，添加商店让你能逛它的货架，但你仍然要单独挑选要下载的那个 App（Plugin）。

举个例子。Anthropic 的官方市场 `claude-plugins-official` 在你启动 Claude Code 时就自动可用，所以安装官方插件不需要额外添加市场，直接一条命令即可，比如装 GitHub 集成：

```text
/plugin install github@claude-plugins-official
```

如果是第三方市场，就要先添加再安装。比如添加 Anthropic 的演示市场，然后从中安装一个提供 git 工作流命令的插件：

```text
/plugin marketplace add anthropics/claude-code
/plugin install commit-commands@anthropics-claude-code
```

装完后运行 `/reload-plugins` 让它生效，就能用 `/commit-commands:commit` 这样带命名空间的命令了。

添加市场支持好几种来源，最常见的是 GitHub 仓库的 owner/repo 简写形式（也就是之前示例代码中的那种），此外也可以用任意 Git 仓库的完整 URL（GitLab、Bitbucket、自建都行，还能在 # 后面附上分支或 tag），或者指向本地目录、远程 `marketplace.json` 文件的路径。它们的共同点是，这个来源里需要有一份 [`.claude-plugin/marketplace.json`](https://code.claude.com/docs/en/plugin-marketplaces#create-the-marketplace-file) 来描述市场收录了哪些插件。

```text
# any Git repo
/plugin marketplace add https://gitlab.com/my-team/ai-plugins.git

# specify a branch or tag in the Git repo
/plugin marketplace add https://gitlab.com/my-team/ai-plugins.git#dev
/plugin marketplace add https://gitlab.com/my-team/ai-plugins.git#v1.0.0

# local plugin marketplace directory
/plugin marketplace add /Users/jimmy/work/ai-plugin-marketplace

# directly point to a remote marketplace.json
/plugin marketplace add https://example.com/marketplace.json
```

安装和管理插件有两个入口：在 Claude Code 内部用 `/plugin` 命令，它会打开一个带**Discover / Installed / Marketplaces / Errors**四个标签页的交互界面，适合浏览和挑选。在 Claude Code 外部则用 `claude plugin` 这个 CLI 命令，适合写进脚本或在终端里批量操作。安装时还能选择范围，用户范围对自己所有项目生效，项目范围会写进 `.claude/settings.json` 跟仓库共享给协作者，本地范围则只在当前仓库对自己生效。

{% img /images/post/2026/06/ccplugin-command.png 800 600 %}

```shell
# install from outside of Claude Code
claude plugin install commit-commands@anthropics-claude-code 
# select the installation scope, default is user
claude plugin install commit-commands@anthropics-claude-code --scope user/project/local
```

还有一种不走市场的方式，就是用 `--plugin-dir` 直接加载本地插件目录，这种方式不需要先添加市场，启动时指定一下就行，主要用于开发和调试自己的插件（现在它也支持直接加载 .zip 压缩包，无需先解压）。需要留意的是，当 `--plugin-dir` 加载的插件和已安装的市场插件同名时，本地这份会在当前会话里优先，方便你在不卸载已装版本的前提下测试改动。

```shell
# load local plugin directory
claude --plugin-dir /Users/jimmy/work/dev-helper
# load local plugin compressed file
claude --plugin-dir /Users/jimmy/work/dev-helper.zip
```

日常管理也很直接，安装、启用、禁用之后记得用 `/reload-plugins` 在不重启的情况下应用变更，不想用的插件可以 `/plugin disable` 临时禁用或 `/plugin uninstall` 彻底卸载。最后有个容易忽略的点：删除一个市场会连带卸载你从这个市场安装过的所有插件，所以清理市场前最好先确认一下里面的插件还需不需要。

## Plugin 的开发

开发一个 Plugin，门槛其实比想象中低，最小的插件就是一个目录加一份清单文件，清单放在目录下的 `.claude-plugin/plugin.json` 里，用来声明插件的身份：

```json
{
  "name": "my-first-plugin",
  "description": "A greeting plugin to learn the basics",
  "version": "1.0.0",
  "author": { "name": "Your Name" }
}
```

其中 `name` 既是唯一标识，也是前面提到的命名空间，`version` 用语义化版本号管理发布，`description` 会显示在插件管理器里，有了这份清单，插件就有了自己的身份信息，剩下的就是往里面放能力。

插件的能力按约定的目录摆放，命令放 `commands/`、技能放 `skills/`、Subagent 放 `agents/`、钩子放 `hooks/hooks.json`、MCP 配置放 `.mcp.json`，需要代码智能还可以加 `.lsp.json`。这里有个新手最容易踩的坑要特别提醒，除了 `plugin.json` 之外，其它这些目录都必须放在插件根目录下，不要塞进 `.claude-plugin/` 里面，否则 Claude Code 加载不到。整体结构大致是这样：

> Claude Code 的代码智能 LSP 是指通过接入语言服务器，让 Claude Code 获得项目级代码理解能力，比如跳转定义、查找引用、类型信息和符号分析，从而更准确地读代码、改代码。

```text
my-plugin/
├── .claude-plugin/
│   └── plugin.json     # the only file allowed in this directory
├── commands/
├── skills/
├── agents/
├── hooks/
│   └── hooks.json
└── .mcp.json
```

开发时还有一个细节值得记住，就是 `${CLAUDE_PLUGIN_ROOT}` 这个变量，插件被安装后会被 Claude Code 复制到缓存目录，所以你在 Hook 脚本或配置里如果要引用插件自己带的文件，不能写死相对路径或绝对路径，而要用 `${CLAUDE_PLUGIN_ROOT}` 来定位插件根目录。我们在[讲 Hook 那篇文章](/2026/04/27/claude-code-hooks/)里看到的 superpowers 插件就是这么做的，它的 SessionStart Hook 调用脚本时写的是 `"${CLAUDE_PLUGIN_ROOT}/hooks/run-hook.cmd"`，这样无论插件被装到哪台机器、哪个缓存路径下，都能稳定找到自己的脚本。

写好之后怎么调试？最方便的方式是用 `--plugin-dir` 直接加载本地目录，不用先发布到市场，启动时指定一下插件目录即可，多个插件就多写几次这个参数：

```shell
claude --plugin-dir ./my-plugin
```

进去之后改了内容，在 Claude Code 中运行 `/reload-plugins` 就能在不重启的情况下重新加载命令、技能、Subagent、Hook 和 MCP，省去反复重启的麻烦。如果你本来就在 `.claude/` 里攒了不少命令和技能，想转成插件也很简单，新建一个带清单的插件目录，把原来 `.claude/commands/`、`.claude/agents/`、`.claude/skills/` 的内容拷过去，再把 `settings.json` 里的 hooks 对象搬进 `hooks/hooks.json`（格式完全一致），就完成了从独立配置到插件的迁移。等插件稳定了想分发出去，再给它配一份描述市场内容的 `marketplace.json`，把它放进自己的市场仓库，别人就能像装其它插件一样安装它了，至于市场怎么组织、怎么提交到官方市场，官网有[完整说明](https://code.claude.com/docs/en/plugin-marketplaces)，这里就不展开了。

## Plugin 的限制与不足

Plugin 作为分发机制确实很方便，但它仍然是一个相对早期的能力，等到真正把它用在团队协作、项目规范和复杂工作流里，就会发现它在一些地方仍有明显限制。

### Plugin Subagent 的能力收敛

第一个限制，是 Plugin 里的 Subagent 会被收敛掉一部分能力，之前[讲 Hook 的那篇文章](/2026/04/27/claude-code-hooks/)里我们其实已经提到过，出于安全原因，在 Plugin Subagent 的 frontmatter 里即使写了 hooks、mcpServers、permissionMode 这几个字段，运行时也会被直接忽略，这表示同一份 Subagent 定义，写在你自己的 `.claude/agents/` 里能用的能力，一旦被打包进插件分发出去，就可能悄悄失效。

这个限制背后的逻辑不难理解，Subagent 本身是一个权限更受限的执行单元，如果允许它在插件里自行注册 Hook、挂载 MCP、或者改权限模式，就等于让一个低权限角色去修改执行流程的控制规则，这会破坏整个权限模型的安全边界。所以从安全角度看这是合理的，但对插件作者来说，它确实是一个需要提前知道的坑，你不能假设插件里的 Subagent 和本地 Subagent 拥有完全一样的能力，相关逻辑得换个地方实现。

### Marketplace 缺少分类标签，Plugin 内容不透明

第二个问题出在发现和信任这两个环节上。先说发现，目前的市场更像一个扁平的插件清单，缺少成体系的分类标签和筛选能力，浏览时基本只能靠名字和描述去找。插件少的时候还好，可一旦生态变大、同类插件越来越多，想快速找到合适的那个、或者横向比较几个相似插件，就会变得越来越费劲。

更要小心的是内容不透明，安装一个插件，等于在你的机器上引入一段能用你的权限执行任意代码的东西，它的 Hook 会跑 shell 命令，MCP Server 会向外建立连接，命令会往上下文里注入提示词。但你在安装前往往很难清楚地看到盒子里到底装了什么，官方文档自己也明确说了，Anthropic 并不控制、也无法验证插件里包含的 MCP Server、文件和其他软件，安装前请务必确认你信任这个来源。也就是说，插件机制目前缺少一个内置的、方便审计内容的手段，安全更多是建立在你信任发布者这个前提上，这在生态早期是一个真实存在的供应链风险。

### Context Window 占用问题

第三个限制和上下文有关。我们在前面几篇文章里反复强调过，给 Claude Code 的任何额外信息都会占用上下文窗口，插件也不例外。装上一个插件，哪怕这次任务用不到这个插件，但插件的技能描述、命令清单、Subagent 说明、甚至 CLAUDE.md 这些元信息，都会进入上下文。插件装得越多，会话一开始就被占掉的基础上下文就越多，真正留给当前任务的空间反而越少。

有些插件还会更进一步，比如通过 SessionStart 这类 Hook 在每次会话开始时无条件注入一整套说明（像 superpowers 就是这么做的）。这种「常驻加载」的好处是可靠，它一定会生效，不依赖模型自己想不想得起来调用，但代价就是不管你这次用不用得上，它都实打实地占着上下文。相比之下，那种只在提示词命中时才按需触发的方式更省上下文，却也更容易漏掉。所以装插件时，除了看它能干什么，也得掂量一下它会持续占用多少上下文，尤其是当你同时装了一堆插件的时候，这笔账很容易被忽略，但它会实实在在影响 Claude Code 在长任务里的表现。

## Claude Code Plugin 与其他 Coding Agent Plugin 的对比

如果把视角放到整个 Coding Agent 生态，会发现一件挺有意思的事，进入 2026 年后，主流工具几乎不约而同地走向了同一个方向，把可复用的指令 / 工作流 + 外部工具和 MCP + 项目规范 / 子代理 / Hook 通通打包成一个可安装、可分发的能力单元。Claude Code 在这件事上算是比较早的，而到了 2026 年上半年，OpenAI 的 Codex、Cursor、Gemini CLI 也都陆续补齐了各自的对应机制。也就是说，插件化已经从 Claude Code 的一个特色，变成了这一代 Coding Agent 的标配。

Codex 在 2026 年 3 月前后推出了官方的 Plugins，把 skills、app 集成（Gmail、Slack、Google Drive 这类应用连接）和 MCP Server 打包成可复用工作流，用 `.codex-plugin/plugin.json` 作清单，在 CLI 里通过 `/plugins` 浏览安装，并有自己的 Plugin Directory。Cursor 在 2.5 版本（2026 年 2 月）也上线了 Plugins，用 `.cursor-plugin/plugin.json` 把 rules、skills、subagents、commands、MCP Server、hooks 打包，通过 Cursor Marketplace 分发。Gemini CLI 对应的概念叫 Extensions，用 `gemini-extension.json` 打包 prompts、MCP、自定义命令、主题、hooks、子代理和 agent skills，直接 `gemini extension install <url>` 从 GitHub 或本地安装。把它们和 Claude Code 放在一起比较：

| 工具 | 对应概念 | 打包内容 | 分发与入口 | 风格定位 |
|:-----|:--------|:--------|:----------|:--------|
| Claude Code | Plugin | 命令、Skill、Subagent、Hook、MCP、LSP | 市场 + `/plugin` / `claude plugin` | 终端 agent 工作流、团队流程沉淀 |
| OpenAI Codex | Plugin | Skill、App 集成、MCP、Hook | Plugin Directory + `/plugins` | 应用集成、跨服务自动化 |
| Cursor | Plugin | Rules、Skill、Subagent、命令、MCP、Hook | Cursor Marketplace + `/add-plugin` | IDE 内产品开发全流程 |
| Gemini CLI | Extension | Prompts、MCP、命令、Hook、子代理、Skill、主题 | GitHub / 本地 + `gemini extension install` | CLI 扩展、Google / 云生态 |

在表格中我们可以看到，各家的插件机制都很像，大多是一份清单文件加 `skills/`、`hooks/hooks.json`、`.mcp.json` 这套结构，但我们要更多留意的是它们的产品定位。Claude Code 的重心在终端里的 agentic coding 和团队工作流沉淀。Codex 把 App 连接提到了和 skill、MCP 并列的位置，更偏应用集成和跨服务自动化。Cursor 的插件则和编辑器深度绑定，强调从读 Linear issue、把 Figma 设计稿转成代码到一键部署的整条产品开发链路。Gemini CLI 的 extension 更像一个 CLI 扩展包格式，在 MCP 之上包了一层上下文和命令，让工具开箱即用，并和 Google 云生态结合得更紧。

还有一点就是虽然它们的功能类似但不代表它们可以直接互换，现在 Coding Agent 的 Plugin 还没有形成一个真正统一的标准。一个很典型的例子，比如 Hook，Claude Code 和 Gemini CLI 都把 Hook 写在 `hooks/hooks.json` 里，但两者的事件名根本对不上，Claude Code 用的是 `UserPromptSubmit`、`PostToolUse`、`Stop` 这类名字，Gemini CLI 用的却是 `BeforeAgent`、`AfterTool`、`SessionEnd`。所以即使你把一个插件的目录原样搬过去，它也未必能在另一个工具里正常工作。虽然形态在收敛，但底层的能力模型和事件语义却还各自一套。

从这些工具的 Plugin 里我们可以看到，这些工具都具备把其中的所有组件一起打包，再通过市场分发出去的能力，但我们在选择它们时要看的是插件的具体使用场景，是跑在终端还是 IDE 里、以及生态和分发是否契合你的团队。从复用性的角度看，如果你的目标只是把内部工具接给 agent，那么优先做成 MCP Server 复用性最高，因为这几家都支持 MCP，如果你想沉淀的是团队的开发规范和流程，写成遵循开放标准的 Agent Skill 通常最通用，也最有机会跨工具复用。

## 总结

Plugin 的本质是把 Claude Code 的各种组件打包成一个可命名、可版本、可安装、可分发的整体，配合我们前面介绍的几篇文章，我们现在已经对 Claude Code 组件的使用和分发有了更加清晰的认识。需要说明的是，Claude Code Plugin 的设计已经比较完整，围绕安装、版本、市场分发和组件组织都形成了相对成熟的流程，但放到整个 Coding Agent 生态，现阶段我们还很难把一个插件直接拿到另一个 Coding Agent 里进行复用，真正意义上的跨工具互通还远未形成。希望本文能帮助你对 Claude Code Plugin 有一个整体的认识，如果有疑问或者补充，欢迎在评论区留言。

## 参考资料

- [Discover and install prebuilt plugins through marketplaces - Claude Code Docs](https://code.claude.com/docs/en/discover-plugins)
- [Create plugins - Claude Code Docs](https://code.claude.com/docs/en/plugins)
- [Create and distribute a plugin marketplace - Claude Code Docs](https://code.claude.com/docs/en/plugin-marketplaces)
- [Claude Code settings - Claude Code Docs](https://code.claude.com/docs/en/settings)
- [Plugins reference - Claude Code Docs](https://code.claude.com/docs/en/plugins-reference)
- [Plugins - OpenAI Codex Docs](https://developers.openai.com/codex/plugins)
- [Plugins - Cursor Docs](https://cursor.com/docs/plugins)
- [Gemini CLI extensions](https://geminicli.com/docs/extensions/)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
