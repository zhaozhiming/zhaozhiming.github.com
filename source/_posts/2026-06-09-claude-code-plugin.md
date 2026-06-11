---
layout: post
title: Claude Code 完全指南——Plugin
date: 2026-06-09 16:00:00
description: 深入解析 Claude Code 的 Plugin 插件系统，包括插件概念、安装使用、市场机制、限制不足，以及与其他 Coding Agent 插件系统的对比
keywords: claude code, plugin, ai coding, anthropic
comments: true
categories: ai
tags: [claude-code, plugin, ai-coding, anthropic]
---

{% img /images/post/2026/06/claude-code-plugin.jpg 400 300 %}

在这个系列里，我们已经陆续聊过几样东西，用 CLAUDE.md 把项目背景和协作约束提前讲清楚，用 Slash Command 和 Skill 把反复出现的任务收敛成稳定的工作流，再用 Hooks 在执行的关键节点上守住行为边界。把这些放在一起看，你会发现我们其实一直不断在给 Claude Code 添加各种各样的能力，让它越来越懂你的项目，越来越贴合你的工作方式。但这些东西说到底还是各自为战，就像钢铁侠、蜘蛛侠、雷神，他们单打独斗个个一流，但真正让世界免于毁灭的，是他们组成的复仇者联盟。Claude Code 里有没有类似的东西，能把这些强大能力组成一个整体呢？有，这就是 Plugin，它把 Claude Code 各种能力拧成一股绳，变成一个可分发、可安装的单元，从而让复用和分享变得简单。

<!--more-->

## Plugin 介绍

我们来看下 Claude Code Plugin 最初的官方定义：

> Plugins are a lightweight way to package and share any combination of: Slash commands, Subagents, MCP servers, Hooks.

并且官方明确表态：

> plugins will be our standard way to bundle and share Claude Code customizations, and we'll continue to evolve the format as we add more extension points.

也就是说 Plugin 被定位为分发定制能力的标准方式，且格式会随扩展点增加而演进（事实上后来确实加入了 LSP、monitor、theme 等）。

{% img /images/post/2026/06/claude-code-plugin-package-concept.jpg 1000 563 %}

之所以需要 Plugin，核心原因是复用，只在一个项目里给自己配置 Claude Code 时，直接把命令、技能、钩子写在 `.claude/` 下确实最省事，但只要你想换台机器、开个新项目，或者想把这套配置分享给同事，如果靠的就是手动拷贝那就很痛苦了。手动拷贝既没有版本概念、又容易遗漏文件，有些组件甚至拷过去也没法直接用，比如你只拷贝了技能目录，但它依赖的命令、Hook、Subagent 等，还需要通过插件机制自动注册后才能生效。Plugin 就是把一套能力固定成有名字、有版本、结构清晰的整体，让分享、安装、更新、卸载都变成可管理、可重复的操作。

这样一来，使用者无需关心 Plugin 内部到底是通过命令还是 Hook 实现的，清单中的版本号还能让团队明确每个人正在使用的具体版本，插件也可以安装到项目范围内，随仓库一起共享。此外，插件中的每个组件都会以插件名作为命名空间，比如 `/team-plugin:hello` 而不是 `/hello`，这样即便同时装了多个来源不同、但名字相同的组件的插件也不会发生冲突。

## Plugin 的安装与使用

要理解安装流程，先得分清 Marketplace 和 Plugin 的关系。Plugin 是你最终要安装的那个能力包，而 Marketplace 是一个收录了很多 Plugin 的目录，本身并不包含能力，只是告诉 Claude Code，这里有哪些插件可以装。所以安装插件通常是两步：先把市场添加进来，让 Claude Code 能浏览它收录的内容，再从里面挑选具体的插件安装。官方文档给的类比很贴切，市场就像应用商店，添加商店让你能逛它的货架，但你仍然要单独挑选要下载的那个 App（Plugin）。

举个例子，Anthropic 的官方市场 [`claude-plugins-official`](https://github.com/anthropics/claude-plugins-official) 在你启动 Claude Code 时就自动可用，所以安装官方插件不需要额外添加市场，直接一条命令即可，比如装 `frontend-design` 插件，要写成这种格式 `plugin-name@marketplace-name`：

```shell
/plugin install frontend-design@claude-plugins-official
```

如果是第三方市场，就要先添加市场再安装插件，比如安装 [`planning-with-files`](https://github.com/OthmanAdi/planning-with-files) 插件，它的市场名和插件名都是 `planning-with-files`，添加市场时填写的市场来源要写 Github 仓库的所有者和仓库名：

```shell
/plugin marketplace add OthmanAdi/planning-with-files
/plugin install planning-with-files@planning-with-files
```

装完后运行 `/reload-plugins` 让它生效，就能用 `/planning-with-files:plan` 这样带命名空间的命令了。

添加市场支持好几种来源，最常见的是 GitHub 仓库的 `owner/repo` 简写形式（也就是之前示例代码中的那种），此外也可以用任意 Git 仓库的完整 URL（GitLab、Bitbucket、自建都行，还能在 `#` 后面附上分支或 tag），或者指向本地目录、远程 `marketplace.json` 文件的路径。它们的共同点是，这个来源里需要有一份 [`.claude-plugin/marketplace.json`](https://code.claude.com/docs/en/plugin-marketplaces#create-the-marketplace-file) 来描述市场收录了哪些插件。

```shell
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

安装和管理插件有两个入口，一个是在 Claude Code 内部用 `/plugin` 命令，它会打开一个带四个标签页的交互界面，适合浏览和挑选。另一个是在 Claude Code 外部用 `claude plugin` 这个 CLI 命令，适合写进脚本或在终端里批量操作。添加市场和安装插件时还能选择范围，用户范围对自己所有项目生效，项目范围会写进 `.claude/settings.json` 跟仓库共享给协作者，本地范围则只在当前仓库对自己生效。

{% img /images/post/2026/06/cc-plugin-command.png 800 600 %}

```shell
# install from outside of Claude Code
claude plugin install planning-with-files@planning-with-files
# select the installation scope, default is user
claude plugin install planning-with-files@planning-with-files --scope user|project|local
```

还有一种不走市场的方式，就是用 `--plugin-dir` 直接加载本地插件目录，这种方式不需要先添加市场，启动时指定一下就行，主要用于开发和调试自己的插件，现在它也支持直接加载 `.zip` 压缩包，无需先解压。需要注意的是，当 `--plugin-dir` 加载的插件和已安装的市场插件同名时，本地这份会在当前会话里优先，方便你在不卸载已装版本的前提下测试改动。

```shell
# load local plugin directory
claude --plugin-dir /Users/jimmy/work/dev-helper
# load local plugin compressed file
claude --plugin-dir /Users/jimmy/work/dev-helper.zip
```

日常管理也很直接，安装、启用、禁用之后记得用 `/reload-plugins` 应用变更，不想用的插件可以 `/plugin disable` 临时禁用或 `/plugin uninstall` 彻底卸载。最后需要注意的一点，删除一个市场会连带卸载你从这个市场安装过的所有插件，所以清理市场前最好先确认一下里面的插件还需不需要。

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

- `name` 既是唯一标识，也是命令执行时的命名空间
- `version` 用语义化版本号管理发布
- `description` 会显示在插件管理器里

有了这份清单，插件就有了自己的身份信息，剩下的就是往里面放能力。

插件的能力按约定的目录摆放，命令放 `commands/`、技能放 `skills/`、Subagent 放 `agents/`、钩子放 `hooks/hooks.json`、MCP 配置放 `.mcp.json`，需要代码智能还可以加 `.lsp.json`。这里有个最容易踩的坑要特别提醒，除了 `plugin.json` 之外，其它这些目录都必须放在插件根目录下，不要塞进 `.claude-plugin/` 里面，否则 Claude Code 加载不到。整体结构大致是这样：

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

> **什么是 LSP？**Claude Code 的代码智能 LSP 是指通过接入语言服务器，让 Claude Code 获得项目级代码理解能力，比如跳转定义、查找引用、类型信息和符号分析，从而更准确地读代码、改代码。

{% img /images/post/2026/06/claude-code-plugin-directory-structure.jpg 1000 563 %}

开发时还有一个细节需要注意，就是 `${CLAUDE_PLUGIN_ROOT}` 这个变量，插件被安装后会被 Claude Code 复制到缓存目录，所以你在 Hook 脚本或配置里如果要引用插件自己带的文件，不能写死相对路径或绝对路径，而要用 `${CLAUDE_PLUGIN_ROOT}` 来定位插件根目录。我们在 [讲 Hook 那篇文章](/2026/04/27/claude-code-hooks/) 里看到的 `superpowers` 插件就是这么做的，它的 SessionStart Hook 调用脚本时写的是 `"${CLAUDE_PLUGIN_ROOT}/hooks/run-hook.cmd"`，这样无论插件被装到哪台机器、哪个缓存路径下，都能稳定找到自己的脚本。

写好之后怎么调试？最方便的方式是用 `--plugin-dir` 直接加载本地目录，不用先发布到市场，启动时指定一下插件目录即可，多个插件就多写几次这个参数：

```shell
claude --plugin-dir ./my-plugin
```

在测试的过程中，如果发现问题对插件进行了修改，在 Claude Code 中运行 `/reload-plugins` 就能在不重启的情况下重新加载插件修改的内容，省去反复重启的麻烦。

如果你本来就在 `.claude/` 里攒了不少命令和技能，想转成插件也很简单，新建一个带清单的插件目录，把原来 `.claude/commands/`、`.claude/agents/`、`.claude/skills/` 的内容拷过去，再把 `settings.json` 里的 hooks 对象搬进 `hooks/hooks.json`（格式完全一致），就完成了从独立配置到插件的迁移。等插件稳定了想分发出去，再给它配一份描述市场内容的 `marketplace.json`，把它放进自己的市场仓库，别人就能像装其它插件一样安装它了。至于市场怎么组织、怎么提交到官方市场，官网有 [完整说明](https://code.claude.com/docs/en/plugin-marketplaces)，这里就不展开了。

## Plugin 的限制与不足

Plugin 作为分发机制确实很方便，但它仍然是一个相对早期的能力，等到真正把它用在团队协作、项目规范和复杂工作流里，就会发现它在一些地方仍有明显限制。

{% img /images/post/2026/06/claude-code-plugin-tradeoffs.jpg 1000 563 %}

### Plugin Subagent 的能力收敛

第一个限制，是 Plugin 里的 Subagent 会被收敛掉一部分能力，之前 [讲 Hook 的那篇文章](/2026/04/27/claude-code-hooks/) 里我们其实已经提到过，出于安全原因，在 Plugin Subagent 的 frontmatter 里即使写了 hooks、mcpServers、permissionMode 这几个字段，运行时也会被直接忽略，这表示同一份 Subagent 定义，写在你自己的 `.claude/agents/` 里能用的能力，一旦被打包进插件分发出去，就可能悄悄失效。

这个限制背后的逻辑不难理解，Subagent 本身是一个权限更受限的执行单元，如果允许它在插件里自行注册 Hook、挂载 MCP、或者改权限模式，就等于让一个低权限角色去修改执行流程的控制规则，这会破坏整个权限模型的安全边界。所以从安全角度看这是合理的，但对插件作者来说，它确实是一个需要提前知道的坑，你不能假设插件里的 Subagent 和本地 Subagent 拥有完全一样的能力，相关逻辑得换个地方实现。

### Marketplace 缺少分类标签，Plugin 内容不透明

第二个问题出在发现和信任这两个环节上。先说发现，目前的市场更像一个扁平的插件清单，缺少成体系的分类标签和筛选能力，浏览时基本只能靠名字和描述去找。插件少的时候还好，可一旦生态变大、同类插件越来越多，想快速找到合适的那个、或者横向比较几个相似插件，就会变得越来越费劲。

更要小心的是内容不透明，安装一个插件，等于在你的机器上引入一段能用你的权限执行任意代码的东西，它的 Hook 会跑 shell 命令，MCP Server 会向外建立连接，命令会往上下文里注入提示词。但你在安装前往往很难清楚地看到盒子里到底装了什么，官方文档自己也明确说了，Anthropic 并不控制、也无法验证插件里包含的 MCP Server、文件和其他软件，安装前请务必确认你信任这个来源。

### 插件粒度

第三个问题和插件本身的粒度有关，观察目前已发布的插件，你会发现一个有意思的现象，就是有些插件非常轻量，里面可能只有一个 Skill（比如之前介绍过的 `planning-with-files` 插件），一个组件就占一个插件的名头，还要附带上 `plugin.json`、目录结构、版本管理这一整套仪式感，实际效果等同于用集装箱运一个信封，有种杀鸡用牛刀的感觉。

事实上，业界安装 Skill 已经有了更轻量的路线：`npx skills add <package>`。它是 Vercel Labs 发布的一个开源 CLI 工具，定位是 skill 的包管理器，有点像 npm 之于 JavaScript，只不过这里管理的是 AI agent 的 skill 包。关键在于它是 **agent-agnostic** 的，这意味着一份 skill 可以同时给 Claude Code、Codex、Cursor、Gemini CLI 使用，不绑定在任何单一工具上。

这跟 Plugin 的本质区别在于它们操作的是不同层级，Skill 是内容，一份 SKILL.md 加上可选的脚本和资源，而 Plugin 是容器，它把多个 skill 连同 Hook、Subagent、MCP Server 编排在一起。`npx skills` 从 GitHub 仓库把 SKILL.md 拉下来放到指定目录，是纯粹的 skill 文件搬运。Plugin 是在 Claude Code 的原生扩展体系里注册一整套能力组件。

那什么时候该用 Plugin，什么时候又该用 `npx skills`？如果你手里只有一个孤立的 Skill，用 `npx skills` 足够了，轻量、干净，而且同一份 skill 还能复用给其他 agent，但如果你要分发的是一整套协作规范，那 Plugin 就是正确的选择。

## Claude Code Plugin 与其他 Coding Agent Plugin 的对比

如果把视角放到整个 Coding Agent 生态，会发现一件挺有意思的事，进入 2026 年后，主流工具几乎不约而同地走向了同一个方向——插件化。Claude Code 在这件事上算是比较早的，而到了 2026 年上半年，Codex、Cursor、Gemini CLI 也都陆续补齐了各自的对应机制，插件化已经从 Claude Code 的一个特色，变成了这一代 Coding Agent 的标配。

Codex 在 2026 年 3 月推出了 Plugins，Cursor 在 2.5 版本（2026 年 2 月）跟进，Gemini CLI 则以 Extensions 来承载类似能力，把它们和 Claude Code 放在一起比较：

| 工具         | 对应概念  | 打包内容                                      | 分发与入口                                 | 风格定位                        |
| :----------- | :-------- | :-------------------------------------------- | :----------------------------------------- | :------------------------------ |
| Claude Code  | Plugin    | 命令、Skill、Subagent、Hook、MCP、LSP         | 市场 + `/plugin` / `claude plugin`         | 终端 agent 工作流、团队流程沉淀 |
| OpenAI Codex | Plugin    | Skill、App 集成、MCP、Hook                    | Plugin Directory + `/plugins`              | 应用集成、跨服务自动化          |
| Cursor       | Plugin    | Rules、Skill、Subagent、命令、MCP、Hook       | Cursor Marketplace + `/add-plugin`         | IDE 内产品开发全流程            |
| Gemini CLI   | Extension | Prompts、MCP、命令、Hook、子代理、Skill、主题 | GitHub / 本地 + `gemini extension install` | CLI 扩展、Google / 云生态       |

在表格中我们可以看到，各家的插件机制都很像，大多是一份清单文件加 `skills/`、`hooks/hooks.json`、`.mcp.json` 这套结构，但我们要更多留意的是它们的产品定位：

- Claude Code 的重心在终端里的 agentic coding 能力和团队工作流沉淀
- Codex 把 App 连接提到了和 skill、MCP 并列的位置，更偏应用集成和跨服务自动化
- Cursor 的插件则和编辑器深度绑定，强调从读 Linear issue、把 Figma 设计稿转成代码到一键部署的整条产品开发链路
- Gemini CLI 的 extension 更像一个 CLI 扩展包格式，在 MCP 之上包了一层上下文和命令，让工具开箱即用，并和 Google 云生态结合得更紧

还有一点就是虽然它们的功能类似但不代表它们可以直接互换，现在 Coding Agent 的 Plugin 还没有形成一个真正统一的标准。一个很典型的例子，比如 Hook，Claude Code 和 Gemini CLI 都把 Hook 写在 `hooks/hooks.json` 里，但两者的事件名根本对不上，Claude Code 用的是 `UserPromptSubmit`、`PostToolUse`、`Stop` 这类名字，Gemini CLI 用的却是 `BeforeAgent`、`AfterTool`、`SessionEnd`。所以即使你把一个插件的目录原样搬过去，它也未必能在另一个工具里正常工作。

## Plugin 的未来

这个故事不是第一次发生，2023 年底 OpenAI 发布 [GPTs](https://openai.com/index/introducing-gpts/)，愿景跟今天的 Plugin 很像，每个人创建自己的定制 AI 助手，通过 Store 分发变现，**像 App Store 改变移动互联网一样改变 AI**。但结果大家都看到了，大量 GPTs 只是套了一层提示词的壳，Store 缺乏有效的质量筛选和发现机制，创作者没有可持续的收入模式。GPTs 至今仍在，但远没有成为它描绘过的那个新生态。

Plugin 有几个相似的风险信号：

- 内容高度依赖提示词驱动的 Skill 和命令，同质化的隐患不小
- Marketplace 的发现和审核机制目前还很原始
- 开发者缺乏清晰的激励，大部分 Plugin 来自开源社区贡献，没有成型的商业化路径

但和 GPTs 不同的是，Plugin 面向的不是帮写文案、画图、查资料这类泛化需求，而是开发团队的生产刚需，统一编码规范、沉淀领域知识、自动化工作流。一个已经嵌入 `CI/CD` 流程的代码审查 Plugin，不会因为热度过了就被丢掉，这意味着即便长尾部分冷下来，头部的高价值工作流 Plugin 仍有着扎实的生存基础。

所以 Plugin 会不会重蹈 GPTs 的覆辙？我觉得关键在一个问题上，Anthropic 是把 Plugin 当作功能清单上的一个选项，还是真的愿意在发现机制、质量门槛和开发者激励上持续投入。目前来看，Plugin 的底层机制是扎实的，但生态的基础设施还远没有跟上。

## 总结

Plugin 解决的核心问题就是把 Claude Code 的各种定制能力从一个散落的配置集合，变成一个可管理、可分发的单元。配合我们前面介绍的几篇文章，我们现在已经对 Claude Code 组件的使用和分发有了更加清晰的认识。需要说明的是，Claude Code Plugin 的设计已经比较完整，围绕安装、版本、市场分发和组件组织都形成了相对成熟的流程，但放到整个 Coding Agent 生态，现阶段我们还很难把一个插件直接拿到另一个 Coding Agent 里进行复用，真正意义上的跨工具互通还远未形成。

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
