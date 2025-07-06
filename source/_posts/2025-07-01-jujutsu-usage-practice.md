---
layout: post
title: 新一代的版本管理工具 Jujutsu 使用实践
date: 2025-07-01 14:51:12
description: 通过和 Git 的对比来介绍 Jujutsu 的基本操作和高级特性
keywords: jujutsu, version control, git, mercurial, svn
comments: true
categories: code
tags: [jujutsu, git, version-control]
---

{% img /images/post/2025/07/jj-intro.jpg 400 300 %}

在当今软件开发领域，Git 已成为事实上的版本控制标准。虽然 Git 功能强大，但在日常使用中，我们常常会遇到一些令人头疼的情况：比如正在专注开发功能 feature 时，突然需要处理紧急 bug，不得不手忙脚乱地执行 `git stash` 保存工作；合并远端分支大量代码时，在复杂的 `git rebase -i` 操作中一步走错，整个提交历史变得混乱不堪；或者提交了一个庞大的 PR 后，被要求拆分成多个小 PR 再提交。如果这些痛点让你感同身受，那么 Jujutsu 值得你关注。这个基于 Rust 开发的新一代版本控制工具，正在改变开发者对版本管理的思维方式。本文将深入探讨 Jujutsu 在日常的开发中如何优雅地实现 Git 的操作，以及它如何通过创新的设计理念提升我们的开发效率和体验。

<!--more-->

## Jujutsu 简介

[Jujutsu](https://jj-vcs.github.io/jj/latest/)（ 以下简称 JJ） 是由前 Google 工程师 Martin von Zweigbergk 于 2019 年作为个人兴趣而发起的项目，是一个兼容 Git 的新一代分布式版本控制系统，采用 Rust 开发，目标是简化用户体验并提供更高性能。随着项目的发展，目前 JJ 已成为功能成熟、社区活跃、潜力十足的下一代版本控制系统。

JJ 的核心理念是**每次文件修改都会自动创建一个新的 revision（修订版本）**，你无需手动执行 `git add` 和 `git commit`，所有的修改都会被自动记录和保存。

这种设计带来了几个重要优势：首先，即使忘记提交，你也不会丢失任何工作；其次，你可以在任何时候自由地重组、拆分或合并你的修改历史；最后，冲突不会阻塞你的工作流程，你可以先完成其他任务，稍后再处理冲突。

更重要的是，JJ 与 Git 仓库**完全兼容**，你可以在现有的 Git 项目中使用 JJ，也可以随时切换回纯 Git 模式。这意味着你可以在个人工作中享受 JJ 带来的便利，同时与使用 Git 的团队成员无缝协作。

下面我们就来一探 JJ 的究竟吧！

## 基本操作

接下来，让我们通过对比 Git 的常见工作流程，来深入了解 JJ 的使用方法。每个环节我都会先展示 Git 的操作方式，再介绍 JJ 的做法，让大家对 JJ 的用法有更直观的感受。

JJ 的安装和配置非常简单，可以参考[官方文档](https://jj-vcs.github.io/jj/latest/install-and-setup/)，JJ 的配置存储在 `~/.config/jj/config.toml` 文件中，使用 TOML 格式。你可以通过命令行修改配置，也可以直接编辑这个文件，配置示例如下：

```toml
[user]
name = "Your Name"
email = "you@example.com"

[ui]
editor = "vim"
```

### 仓库创建和克隆

通常使用 JJ 集成 Git 仓库，最好的做法是使用 JJ 的 `jj git init --colocate` 命令，操作命令如下：

```bash
# create new git repository
mkdir repo
cd repo
git init
jj git init --colocate

# or clone remote repository
git clone https://github.com/user/repo.git
cd repo
jj git init --colocate
```

- 第一种是新建一个 Git 仓库，然后使用 `jj git init --colocate` 命令将 JJ 集成到 Git 仓库中
- 第二种是克隆一个远程 Git 仓库，然后使用 `jj git init --colocate` 命令将 JJ 集成到 Git 仓库中

当你运行该命令时，JJ 会在 项目根目录下创建一个 `.jj` 目录，它和 `.git` 目录是可以和谐共存的，你可以随时在 Git 和 JJ 之间切换。这种设计让你可以渐进式地采用 JJ，而不用担心影响现有的 Git 工作流程。
如果在使用 JJ 的过程中，由于误操作导致工作区混乱，可以随时删除 `.jj` 目录，这样项目又会回复成 Git 项目，不会影响现有的 Git 工作流程。

### 拉取远程代码和合并

在团队协作中，同步远程代码是最常见的操作之一，让我们看看两种工具如何处理这个场景。

**Git 操作：**

```bash
# fetch remote updates
git fetch origin
# merge remote main branch to current branch
git merge origin/main
# or rebase from remote main branch to current branch
git rebase origin/main
```

> **关于 Merge 和 Rebase 的区别：** Merge 策略会保留完整的分支历史，通过创建一个新的合并提交来连接两个分支，这样历史图谱会呈现分叉结构，能够清晰地看到分支的开发过程。而 Rebase 策略则是将你的提交重新应用到目标分支之上，形成一条线性的历史记录，虽然看起来更加整洁清晰，但需要注意的是，这个过程会改写原有的提交历史。

{% img /images/post/2025/07/merge-vs-rebase.png 1000 600 %}

在合并代码过程中，如果是 `merge` 方式遇到冲突，需要手动解决冲突，然后执行 `git add` 和 `git commit` 完成合并。 如果是 `rebase` 方式遇到冲突，需要手动解决冲突，然后执行 `git rebase --continue` 完成合并。

**JJ 操作：**

```bash
# fetch remote updates
jj git fetch
# merge remote main branch to current branch
jj new <current-revision> main@origin
# or rebase from remote main branch to current branch
jj rebase -d main@origin
```

其中 `current-revision` 是当前的 revision，可以通过 `jj log` 查看，也可以使用 `@` 符号表示当前的 revision。

> **什么是 Revision？** 在 JJ 中，revision（修订版本）是核心概念，类似于 Git 的 commit 但更智能：你的工作副本本身就是一个 revision，文件修改会自动保存，无需手动 add/commit，每个 revision 有稳定的 Change ID（如 `qzsvtxpv`）和可变的 Commit Hash（如 `e18f7532`）；当前工作位置用 `@` 符号标识，支持随时编辑、拆分、合并而不破坏历史完整性。这种设计让你专注代码开发，无需担心提交时机或丢失进度。

在合并代码过程中，如果遇到冲突，JJ 会自动创建冲突标记，但与 Git 不同的是，JJ 不会阻塞你的工作，你可以继续工作，稍后再解决冲突。也可以通过 `jj st` 查看冲突文件，然后手动解决冲突。

### 代码修改工作流

这是日常开发中最核心的部分，两种工具的差异在这里体现得最为明显，常见的代码修改工作流如下：

- 首先修改文件
- 然后检查状态
- 然后检查差异
- 然后添加文件
- 然后提交
- 然后创建并切换到新分支
- 最后推送远程仓库

**Git 操作：**

```bash
# 1. change file
vim src/main.py
# 2. check status
git status
# 3. check diff
git diff
# 4. add file
git add .
# 5. commit
git commit -m "Update main.py with new feature"
# 6. create and switch to new branch
git checkout -b feature/awesome-feature
# 7. push to remote
git push origin feature/awesome-feature
```

**JJ 操作：**

```bash
# 1. create new working revision
jj new
# 2. change file
vim src/main.py
# 3. check status
jj status
# 4. check diff
jj diff
# 5. update revision description
jj describe -m "Update main.py with new feature"
# 6. create branch to current revision
jj bookmark create feature/awesome-feature
# 7. push to remote
jj git push --allow-new
```

这里体现了 revision 的好处，你无需再像 Git 那样手动执行 `git add`，所有修改都会自动保存到当前的工作 revision 中。另外你也无需像 Git 那样使用 `git stash` 来缓存工作，因为 JJ 的 revision 是自动保存的，你可以在任何时候回退到之前的 revision。

> **什么是 Bookmark？** JJ 的 bookmark 与 Git 的 branch 功能上类似，但它们不会自动随工作改变移动，Git 的 branch 好比一条自动向前延伸的道路，当你 commit 时，它就自己跟着走。JJ 的 bookmark 更像是你在地图上的一个**旗帜**标记，当你走远了旗帜还留在原地，只有你自己去把它挪到新地点。这样做的好处是你可以随时创建和删除 bookmark，也可以随时将 bookmark 映射到不同的 revision，而不会影响工作流程。

比如在前面提到的场景，当你提交了庞大的 PR 后（这个 PR 包含了多个 commit），被要求拆分成多个小的 PR 再提交。如果是使用 Git 的话，你需要使用 `git cherry-pick` 等复杂命令来选择不同的 commit 创建分支，然后再提交 PR。而使用 JJ 的话，你可以使用 `jj bookmark` 命令来对不同的 revision 创建不同的分支，然后分别对这些分支进行提交 PR 即可，操作相对简单很多。

{% img /images/post/2025/07/jj-big-pr.png 1000 600 %}

针对这个场景的 JJ 操作如下：

```bash
jj edit <revision-id1>
jj bookmark create feature/feature-1
jj git push
# repeat the above steps for other branches
```

- 假设要为某个 commit 创建分支，先使用 `jj edit` 命令选中该 commit
- 然后使用 `jj bookmark create` 命令创建分支
- 最后使用 `jj git push` 命令将分支推送到远程仓库
- 重复以上步骤，为其他 commit 创建分支

## 高级操作

熟悉了基本的工作流程后，让我们深入了解 JJ 的一些高级特性，这些功能将帮助你更高效地管理代码历史。

JJ 最强大的功能之一是灵活的 revision 操作，让你可以随时重组、拆分或合并你的修改历史。

### 合并 Revision（Squash）

**Git 操作：**

在 Git 中，合并多个提交通常需要使用交互式 rebase：

```bash
# Git: Interactive rebase to squash commits
git rebase -i HEAD~3
# Then mark commits as 'squash' in the editor
```

**JJ 操作：**

而在 JJ 中，这个操作更加直观：

```bash
# Squash current revision into parent
jj squash
# Squash specific revision into another
jj squash --from <revision1> --into <revision2>
```

`jj squash` 命令可以将当前的 revision 合并到父 revision 中，也可以将指定的 revision 合并到另一个 revision 中。

假设你已经完成了 feature 的开发，这个 feature 包含了 3 个小的 revision，现在需要将多个小的 revision 合并成一个大的 revision，这种情况下你就可以使用 `jj squash` 命令进行操作，下面是操作示例：

```bash
jj log
# @  qqymurpk your-email@example.com 2025-06-26 13:45:42 default@ 078fbfb4
# │  (no description set) # revision 1
# ○  qyxmswok your-email@example.com 2025-06-26 13:45:42 98321f9e
# │  (no description set) # revision 2
# ○  vqmpqpmu your-email@example.com 2025-06-26 13:45:42 7a515876
# │  (no description set) # revision3
# ◆  yozmsuoy your-email@example.com 2025-06-26 13:40:24 main@origin git_head() 99f00e34
jj squash # this will squash the revision 1 into revision 2
# jj squash --from qqymurpk --into qyxmswok # same effect as above
jj log
# @  qyxmswok your-email@example.com 2025-06-26 13:45:42 98321f9e
# │  (no description set) # revision 2
# ○  vqmpqpmu your-email@example.com 2025-06-26 13:45:42 7a515876
# │  (no description set) # revision3
# ◆  yozmsuoy your-email@example.com 2025-06-26 13:40:24 main@origin git_head() 99f00e34
jj squash # this will squash the revision 2 into revision 3
# jj squash --from qyxmswok --into vqmpqpmu # same effect as above
jj log
# @  vqmpqpmu your-email@example.com 2025-06-26 13:45:42 7a515876
# │  (no description set) # revision3
# ◆  yozmsuoy your-email@example.com 2025-06-26 13:40:24 main@origin git_head() 99f00e34
```

- 首先查看当前的 revision 历史，可以看到有 3 个 revision，当前的 revision 是 `@` 符号标识的 revision，也就是 revision 1
- 执行 `jj squash` 命令，会将 revision 1 合并到 revision 2 中
- 再次查看 revision 历史，可以看到 revision 1 已经被合并到 revision 2 中，这时当前的 revision 变成了 revision 2

* 然后再次执行 `jj squash` 命令，会将 revision 2 合并到 revision 3 中

- 再次查看 revision 历史，可以看到 revision 2 已经被合并到 revision 3 中，最终我们将这 3 个 revision 合并成了 1 个 revision

{% img /images/post/2025/07/jj-squash.png 1000 600 %}

`jj squash` 如果在合并过程中遇到冲突，JJ 会自动创建冲突标记，你可以立即处理冲突，也可以稍后再处理冲突。

`jj squash` 命令不仅适用于合并 revision，也适用于合并 bookmark，因为 bookmark 名称可以作为 revision 的简洁别名出现在任何接受 revision 参数的命令里，这也就意味着你可以快速地使用该命令进行分支的合并。

`jj squash` 命令还可以将某个 revision 移动到另一个 revision 的前面或后面，只需在目标 revision 前后 new 一个新的 revision，然后使用 `jj squash` 命令将当前的 revision 合并到新的 revision 中即可。

### 拆分 Revision（Split）

**Git 操作：**

Git 中拆分提交相对复杂：

```bash
# Git: Split a commit
git rebase -i HEAD~1
# Mark as 'edit', then:
git reset HEAD~
git add -p  # Interactively stage parts
git commit -m "First part"
git add .
git commit -m "Second part"
git rebase --continue
```

**JJ 操作：**

JJ 的拆分操作更加优雅：

```bash
# Interactive split current revision
jj split
```

使用 `jj split` 命令需要指定交互式编辑器，JJ 官方推荐使用 [Meld](https://meldmerge.org/) 作为交互式编辑器，工具的安装方法可以参考[官方文档](https://jj-vcs.github.io/jj/latest/config/#using-meld-as-a-diff-editor)，安装完成后在 JJ 的配置文件里面添加如下配置：

```toml
[ui]
diff-editor = "meld"
```

假设你在一个 revision 中修改了 3 个文件，现在需要将这 3 个文件拆分成 3 个新的 revision，可以使用 `jj split` 命令进行操作，下面是操作示例：

```bash
jj log
# @  xlylmopt your-email@example.com 2025-06-30 15:50:41 0568d673
# │  (no description set) # source revision
jj st
# Working copy changes:
# M a.txt
#M b.txt
#M c.txt
jj split
```

- 首先查看当前的 revision 历史，可以看到只有 1 个 revision
- 再查看当前的工作区状态，可以看到有 3 个文件被修改
- 执行 `jj split` 命令，会出现 Meld 的 GUI 界面，你可以选择将哪个文件拆分到新的 revision 中

{% img /images/post/2025/07/jj-split.png 1000 600 %}

这里我们选中 `b.txt` 和 `c.txt` 文件并点击 `>` 按钮将它们移动到新的 revision 中，然后点击右上角的关闭按钮，这样就会自动创建一个新的 revision，并切换到新的 revision 中。操作完成后，我们再次查看 revision 历史，可以看到已经创建了 2 个新的 revision。

```bash
jj log
# @  xuwrtktq your-email@example.com 2025-06-30 15:57:00 b97b256b
# │  (no description set)
# ○  xlylmopt your-email@example.com 2025-06-30 15:56:43 git_head() 590965ef
# │  (no description set)
jj st
# Working copy changes:
# M b.txt
# M c.txt
jj edit xl
jj st
# Working copy changes:
# M a.txt
```

重复以上的操作再将 `xuwrtktq` 这个 revision 拆分成 2 个 revision，这样我们的操作就完成了。

## 实用技巧

### 操作历史和回滚（Operation Log）

在日常开发中，我们有时会因为误操作而需要撤销之前的命令，比如错误地删除了分支、执行了错误的 rebase 等。这时候查看操作历史和回滚操作就显得非常重要。

**Git 操作：**

Git 提供了 `reflog` 命令来查看引用的历史记录，但它只能追踪特定引用（如分支）的变化：

```bash
# show reflog of current branch
git reflog
# show reflog of specific branch
git reflog show main
```

Git 的 reflog 有一些限制：只能追踪引用的变化，不能看到完整的操作历史；分支删除后 reflog 也会丢失；无法查看全局的操作记录。

**JJ 操作：**

JJ 的操作日志（operation log）功能更加强大，它记录了你在仓库中执行的每一个操作：

```bash
# show operation log
jj op log
# show detailed information of specific operation
jj op show <operation-id>
# undo to previous state of specific operation
jj op undo <operation-id> # `jj undo` is a shortcut for `jj op undo`
# restore to the state of specific operation
jj op restore <operation-id>
```

JJ 的操作日志命令有查看、撤销和恢复等操作，让我们通过一个实际例子来看看 JJ 的操作历史功能：

```bash
# do some operations
jj new -m "Feature A"
vim file1.txt
jj new -m "Feature B"
vim file2.txt

# show operation log
jj op log
# @  93d01c6ba9e4 user@example.local 2 minutes ago, lasted 2 milliseconds
# │  new empty commit
# │  args: jj new -m 'Feature B'
# ○  37ca6e5da150 user@example.local 2 minutes ago, lasted 18 milliseconds
# │  new empty commit
# │  args: jj new -m 'Feature A'

# restore the operation to the state of the operation "Feature A"
jj op restore 37ca6e5da150
# show revision history
jj log
# @  wxsrykxn your-email@example.com 2025-07-01 18:20:12 440e094d
# │  (empty) Feature A
```

- 首先执行了两个操作，分别是创建了 2 个新的 revision，一个是 `Feature A`，一个是 `Feature B`，分别在这 2 个 revision 中修改了文件 `file1.txt` 和 `file2.txt`
- 操作完成后当前的 revision 是 `Feature B`
- 然后使用 `jj op restore` 命令将操作历史恢复到 `Feature A` 的状态
- 最后可以用 `jj log` 命令查看 revision 记录，可以看到 `Feature B` 的 revision 的已经被撤销了，只有 `Feature A` 的 revision 被保留了

> **注意：** `jj log` 和 `jj op log` 是两个不同的命令，前者是查看 revision 历史，后者是查看操作历史。

JJ 提供了全面的操作记录追踪，不仅记录分支引用的变化，还会完整保存每一个执行的操作。这种设计让你可以精确地撤销任何特定操作而不影响其他工作，同时保证操作历史永不丢失，即使删除了分支也能完整保留。每个操作都会显示完整的命令行参数，让你清楚地了解当时执行了什么操作，极大地提升了操作的可追溯性和安全性。

### Cherry-pick 和复制修改

当你有一个大型功能分支，但只想将其中部分提交合并到主分支时，可以使用 Git 的 `cherry-pick` 命令来选择特定的 commit 并合并到主分支。

**Git 操作：**

Git 的 cherry-pick 操作如下：

```bash
# Git: Apply specific commits
git cherry-pick <commit-hash>
```

**JJ 操作：**

与 `git cherry-pick` 类似，JJ 也提供了 `duplicate` 命令来复制特定的 revision 到另一个 revision 中：

```bash
# Duplicate a revision
jj duplicate <revision>
# Duplicate multiple revisions
jj duplicate <rev1> <rev2> <rev3>
# Duplicate and rebase onto different parent
jj duplicate <revision> -d <new-parent>
```

### 忽略文件跟踪

JJ 直接使用 `.gitignore` 文件，与 Git 完全兼容，但对于已经被跟踪的文件，JJ 提供了更便捷的操作：

```bash
# Stop tracking a file (but keep it locally)
jj file untrack <file-path>
```

`jj file untrack` 这个命令相当于 Git 的 `git rm --cached` 命令。

## JJ 的不足

前面我们看到了 JJ 的诸多优势，但任何新技术都不是完美的。在实际使用 JJ 的过程中也会经常遇到了一些问题，这些问题可能会影响你的使用体验。

- VS Code、SourceTree 等开发者常用的工具对 JJ 的支持还比较有限，而且 CI/CD 集成也需要额外配置，生态系统不够成熟
- 从 Git 迁移到 JJ 需要重新理解 revision、bookmark 等概念，学习曲线比较陡峭
- 相比 Git 成熟的功能体系，JJ 在 hooks、复杂工作流支持等方面还有不足

尽管有这些问题，我觉得 JJ 仍然值得尝试，但建议采取渐进的方式：

- 首先可以在个人项目中试用，熟悉 JJ 的工作方式
- 如果觉得确实能提升效率，再考虑在小团队中推广
- 对于大型项目或企业环境，可以考虑混合使用的方式——个人开发时用 JJ，团队协作时仍然用 Git

## 总结

本文通过对比 Git 和 JJ 的日常工作流程，从仓库初始化、配置管理、代码修改到高级操作，全面介绍了 JJ 的核心特性和实际使用方法。我们可以看到，JJ 通过自动保存 revision、灵活的历史管理和非阻塞的冲突处理等创新设计，有效解决了 Git 在日常使用中的诸多痛点。

虽然 Git 目前仍是版本控制的事实标准，但 JJ 展示了版本控制工具的另一种可能性。对于追求高效开发体验的个人开发者，或者需要处理复杂代码审查流程的团队来说，JJ 都值得投入时间去学习和尝试。毕竟，好的工具应该让我们更专注于创造，而不是被繁琐的操作所束缚。

虽然 JJ 还有这样那样的一些问题， 但是 JJ 还在快速发展中，很多现在的问题可能在未来的版本中会得到解决。如果你对版本控制工具的未来发展感兴趣，关注 JJ 的进展是个不错的选择。

## 参考

- [Steve's Jujutsu Tutorial](https://steveklabnik.github.io/jujutsu-tutorial/)
- [What I've learned from jj](https://zerowidth.com/2025/what-ive-learned-from-jj/)
- [JJ Tips and Tricks](https://zerowidth.com/2025/jj-tips-and-tricks/#bookmarks-and-branches)
- [JJ Git Command Table](https://jj-vcs.github.io/jj/latest/git-command-table/?utm_source=chatgpt.com)

关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
