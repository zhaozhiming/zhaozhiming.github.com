---
layout: post
title: 都 2024 年了，你还在用 pip 吗？
date: 2024-07-15 10:49:25
description: 介绍新一代 Python 项目常用工具，以及如何在项目中使用
keywords: python, miniconda, poetry, ruff, uv
comments: true
categories: ai
tags: [python, miniconda, poetry, ruff, uv]
---

{% img /images/post/2024/07/next-generation-python-tools.jpg 400 300 %}

编程语言 Python 随着 AI 的发展越来越受开发人员的喜爱，目前已经是最流行的编程语言，但由于 Python 是一门相对较老的语言，并且经过 Python2 到 Python3 这一漫长而复杂的迁移历程，使得一些 Python 开发人员可能还在使用一些过时的工具和库。今天我们就来带大家了解当前 Python 生态系统中最流行最实用的开发工具，让你彻底告别那些`老古董`。

<!--more-->

## Python 环境管理工具

Python 项目中虚拟环境的管理非常重要，虚拟环境可以帮助我们在不同项目中使用不同的 Python 版本和依赖库，避免不同项目之间的依赖冲突。Python 最早使用的是 `virtualenv` 工具来管理虚拟环境，到了 Python3.3 后自带了内置的创建虚拟环境模块 `venv`，但它们都存在一个问题，就是只能使用同一个 Python 版本创建虚拟环境。假如你在 A 项目想使用 Python3.10，而在 B 项目想使用 Python3.9，那么你就需要安装两个不同版本的 Python，然后分别使用 `virtualenv` 或 `venv` 来创建虚拟环境。

在使用了一些 Python 环境管理工具后，我比较推荐 [Miniconda](https://docs.anaconda.com/miniconda/) 这个工具，它是一个轻量级的 Conda 版本，可以帮助我们管理 Python 环境和依赖库，但我一般不使用它来管理依赖库，主要使用它来管理 Python 环境。

虽然 Miniconda 与其他 Python 环境管理工具（比如 [pyenv](https://github.com/pyenv/pyenv)）相比会重量级一些（包含了一些数据科学相关的库），而且功能也不是很纯粹（即有环境管理也有依赖管理等功能）， 但是它的优势在于环境管理的功能非常出色，可以轻松地在不同操作系统上进行安装，同时适配 bash、fish、zsh 等不同的 shell 环境，而且跟 `virtualenv` 和 `venv` 一起使用也不会引起冲突。

### Miniconda 使用示例

以下是 Miniconda 的一些常用命令：

- 创建 Python 环境： `conda create -n myenv python=3.10`
- 切换 Python 环境： `conda activate myenv`
- 展示所有 Python 环境： `conda env list`
- 初始化 shell 环境： `conda init <bash/fish/zsh>`

## Python 依赖管理工具

Python 项目中最常使用的工具应该要属 Pip 了，Pip 是 Python 的依赖管理工具，用于安装和管理 Python 依赖，虽然 Pip 功能强大，但在管理项目依赖时存在一些问题，比如新增依赖时需要手动修改 `requirements.txt` 文件，而且没有版本锁定功能，导致在不同环境中安装的依赖版本可能不一致。

其他编程语言比如 JS 则能有效地处理这种情况，使用它的依赖管理工具 `npm` 会产生一个 `package.json` 文件来管理项目依赖，然后生成一个 `package-lock.json` 文件来锁定依赖版本，确保在不同环境中安装相同的依赖版本。

为了解决这一问题，在 Python 中出现了不少依赖管理工具，[Poetry](https://python-poetry.org/) 是其中一个比较流行的工具。Poetry 使用一个 `pyproject.toml` 文件来管理项目的所有依赖项和元数据，使项目配置更加简洁明了，它会自动处理依赖项的版本冲突，并且能够生成锁文件 `poetry.lock`，确保在不同环境中安装相同的依赖版本。

### Poetry 安装

Poetry 有多种安装方式，最简单的是通过 Python 脚本进行安装，安装命令如下：

```bash
curl -sSL https://install.python-poetry.org | python3 -
```

安装完成后，可以通过 Miniconda 创建一个特定版本的 Python 环境，然后在这个环境中使用 Poetry，具体操作如下：

```bash
# 使用 conda 创建一个 Python 环境
conda create -n myenv python=3.10
# 切换到这个 Python 环境
conda activate myenv
# 构建一个 Poetry 虚拟环境
poetry env use python
# 进入该环境
poetry shell
```

### Poetry 使用示例

在新项目中使用 Poetry 可以使用 `poetry new <project_name>` 来创建一个新的 Python 项目，如果是已有项目，可以在项目目录中使用 `poetry init` 命令来初始化项目，这两个命令都会生成一个 `pyproject.toml` 文件，用于管理项目的依赖项和元数据。

你可以使用 `poetry install` 来安装项目的所有依赖，如果是初次运行该命令，Poetry 会生成一个 `poetry.lock` 文件，用于锁定项目的依赖版本，确保在不同环境中安装相同的依赖版本。也可以使用 `poetry add <package_name>` 命令来单独添加一个依赖，这样 Poetry 会自动更新 `pyproject.toml` 和 `poetry.lock` 文件。

我们通过一个简单的例子来看下 Poetry 对依赖管理的方法，假设我们在项目中安装了一个新的依赖 `requests`，那么可以使用如下命令：

```bash
poetry add requests
```

安装完成后，在 `pyproject.toml` 文件中只会添加 `requests` 这个依赖的信息：

```toml
[tool.poetry.dependencies]
requests = "^2.32.3"
```

但是在 `poetry.lock` 文件会中除了添加 `requests` 这个依赖外，还会添加 `requests` 这个依赖所需的其他依赖库的信息，可以使用以下命令查看依赖之间的关系：

```bash
$ poetry show --tree
requests 2.32.3 Python HTTP for Humans.
├── certifi >=2017.4.17
├── charset-normalizer >=2,<4
├── idna >=2.5,<4
└── urllib3 >=1.21.1,<3
```

可以看到 `requests` 是项目根目录下的依赖，而其他几个依赖是 `requests` 所需的依赖。

如果你用 Pip 来安装依赖，那么 `pip install requests` 后再用 `pip freeze > requirements.txt` 命令生成的 `requirements.txt` 文件会包含所有依赖的信息，使得你分不清哪些是项目的依赖，哪些是衍生的依赖。

Poetry 还允许你将 lock 文件导出成 `requirements.txt` 文件，这样你就可以使用 `pip install -r requirements.txt` 来安装项目的依赖，具体命令如下：

```bash
poetry export --without-hashes --format=requirements.txt --output requirements.txt
```

更多的 Poetry 使用方法可以参[考官方文档](https://python-poetry.org/docs/)。

### 其他依赖管理工具

除了 Poetry 外，还有一些其他的 Python 依赖管理工具，比如 [Pdm](https://pdm-project.org/en/latest/) 就是不错的选择，它整体和 Poetry 类似，它还包含了 Python 环境管理的功能，这样就不需要和 Miniconda 配合也可直接使用，但是在流行度上比 Poetry 稍微逊色一些（截止时间 2024 年 7 月，Poetry 的 Github Star 数是 30K，而 Pdm 的 Github Star 数是 7.6K），可能是 Pdm 比 Poetry 发布时间晚的关系（Poetry 是 2018 年发布的，而 Pdm 是 2020 年发布的），后面如果 Pdm 发展的好的话，说不定会超过 Poetry。

另外一个值得推荐的 Python 依赖管理工具是 [Uv](https://github.com/astral-sh/uv)，它是一款用 Rust 编写的极速 Python 包安装和解析工具，旨在作为 Pip 和 Pip-tools 的替代品，并逐步发展成为一个全面的 Python 项目和包管理器，下面是它和其他工具安装依赖的速度对比图：

{% img /images/post/2024/07/uv-compare.png 1000 600 %}

得益于 Rust 的高性能，在速度上 Uv 完全碾压了其他工具，但可惜的是 Uv 目前还是使用 `requirements.txt` 来管理依赖，这样就无法保证可以在不同环境安装相同的依赖，而且 Uv 也不能和 Poetry 一起使用，因为 Uv 是按照兼容 Pip 的思路进行开发，而 Poetry 内部已经不用 Pip 做依赖管理了。

尽管 Uv 无法和 Poetry 一起使用，但是我们可以在 CI/CD 中使用 Uv 来加速依赖安装，比如我们先用 Poetry 导出 `requirements.txt` 文件，然后在 CI/CD 中使用 Uv 来安装依赖，这样就可以大大缩短依赖安装的时间。

### Pip 是否过时

虽然这些依赖管理工具很强大，但一些小型项目可能更多开发人员还是会选择 Pip，因为 Pip 无需额外安装其他工具，一般有 Python 环境就可以直接使用。另外一点是编程语言的原生工具也在不断发展，以 JS 为例，npm 刚开始时也不支持 lock 文件，但后面参考了一些 JS 的流行工具，在社区的共同努力下慢慢完善了这个功能，所以 Pip 也有可能在未来的某个版本中加入类似 Poetry 的功能。

所以在 Pip 没有发展完善之前，我们可以使用 Poetry 这样的工具来解决依赖管理的问题，同时也能简化项目配置，提高开发效率。

## 代码规范工具

另外一种常用的工具是代码规范类工具，因为 Python 的语法比较灵活，所以在团队协作中可能会出现代码风格不一致的问题，为了解决这个问题，每种编程语言都会有一些代码规范工具，这类工具包括代码格式化工具、代码检查工具等。

在 Python 中代码格式化工具有 [Black](https://github.com/psf/black)、[YAPF](https://github.com/google/yapf)、[autopep8](https://github.com/hhatto/autopep8) 等，而代码检查工具有 [Flake8](https://github.com/PyCQA/flake8)、[Pylint](https://github.com/pylint-dev/pylint)、[mypy](https://github.com/python/mypy) 等，这些工具都可以通过 Pip 来安装，然后在项目中使用。

而最近比较流行的一个代码规范工具 [Ruff](https://github.com/astral-sh/ruff)，它同时集成了代码格式化和代码检查功能，可以帮助我们更好地在项目中统一代码风格。Ruff 是 Uv 开发团队开发的另外一款工具，同样是使用 Rust 语言进行编写，从而使它的性能远远高于其他同类型的工具，下面是 Ruff 和其他工具的性能对比图：

{% img /images/post/2024/07/ruff-compare.png 1000 600 %}

Ruff 内部集成了 Black 和 Flake8 等工具，下面我们就来介绍下 Ruff 如何安装及使用。

### Ruff 安装

Ruff 可以通过 Pip 或 Poetry 进行安装，在项目中使用的话，推荐使用 Poetry 安装到 dev 开发依赖中，表示这个工具只在开发环境中使用，具体命令如下：

```bash
poetry add --dev ruff
```

如果想让 Ruff 在本地 IDE 中使用的话，建议是进行全局安装，这样就可以在任何项目中使用 Ruff，具体命令如下：

```bash
curl -LsSf https://astral.sh/ruff/install.sh | sh
```

安装完成后，可以通过 IDE 的插件来使用 Ruff，比如在 VSCode 中安装 [Ruff 插件](https://marketplace.visualstudio.com/items?itemName=charliermarsh.ruff)，然后在 VSCode 中使用快捷键 `Ctrl+Shift+P` 打开命令面板，输入 `user config` 打开用户配置文件，然后添加 Ruff 安装后的路径：

```json
{
  "ruff.path": ["/your/ruff/path"]
}
```

### Ruff 使用示例

Ruff 主要有 2 个命令，分别是 `ruff check` 和 `ruff format`，前者用于代码检查，后者用于代码格式化。

为了保证团队的代码风格一致，我们可以在 Git 的 hook 中添加 Ruff 检查命令，这样每个人在执行 `git commit` 命令时就会自动执行 Ruff 命令，如果检查失败则无法提交代码。

首先我们需要安装 [pre-commit](https://pre-commit.com/) 工具，这个工具可以让我们在 Git hook 上轻松配置命令，同样将其安装到 dev 开发依赖即可：`poetry add --dev pre-commit`，然后在项目根目录下添加 `.pre-commit-config.yaml` 文件，内容如下：

```yaml
repos:
  - repo: https://github.com/astral-sh/ruff-pre-commit
    # Ruff 版本
    rev: v0.5.1
    hooks:
      # 执行 ruff check
      - id: ruff
        # 可选参数，自动修复代码
        args: [--fix]
      # 执行 ruff format
      - id: ruff-format
```

- 在文件中我们添加 Ruff 工具来处理代码提交时的检查
- 我们添加了 2 个工具，第一个工具 id 是 `ruff` 表示执行 `ruff check` 命令，第二个工具 id 是 `ruff-format` 表示执行 `ruff format` 命令
- 第一个工具还有一个可选参数 `args: [--fix]`，表示会自动修复检查出有误的代码，但也不是所有代码都能自动修复，有些代码还是需要手动修复的

最后我们在项目中执行 `pre-commit install` 命令，将文件内容添加到 Git hook 中：

```bash
$ pre-commit install
pre-commit installed at .git/hooks/pre-commit
```

配置完成后，我们故意写一些代码错误，然后提交代码， 检查结果如下：

```bash
$ git commit -m 'add some feature'
ruff.....................................................................Failed
- hook id: ruff
- exit code: 1

main.py:1:8: F401 [*] `requests` imported but unused
  |
1 | import requests
  |        ^^^^^^^^ F401
  |
  = help: Remove unused import: `requests`

Found 1 error.
[*] 1 fixable with the `--fix` option.

ruff-format..............................................................Passed
```

可以看到代码检查任务失败了，报了 `imported but unused` 的错误，错误编号 `F401`，第二个任务代码格式化检查通过。

如果想在代码中不检查某行代码，可以在代码行后面加上 `# noqa: {error_code}`，比如：

```py
import requests # noqa: F401
```

这样就可以在 Ruff 检查时忽略这个错误。更多的 Ruff 使用方法可以参[考官方文档](https://docs.astral.sh/ruff/tutorial/)。

## 总结

今天我们介绍了 Python 中新一代的项目工具，新工具带来的好处是开发效率的提升，因为每个新工具都是在解决旧工具的不足之处，是在旧工具的基础上进行了优化和改进。作为一个热衷提高生产力的开发人员，可以在适当时机尝试使用这些新工具，如果觉得新工具不合适，也可以退回去重新使用旧工具，关键在于尝试。

关注我，一起学习各种人工智能和 GenAI 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
