---
name: blog-english-translation
description: 将中文博客文章翻译成英文，并保留原始 Markdown、Hexo/Liquid 标签、图片路径、代码块和整体结构。适用于用户要求把中文技术博客、科普文章或 Markdown 文档翻译成自然英文，同时尽量维持原文版式、术语和可发布格式的场景。
---

# Blog English Translation

## Overview

将用户提供的中文博客 Markdown 翻译成可直接发布的英文版本，保持原始结构不变，优先输出清晰、自然、偏科普杂志风格的英文，而不是逐字直译。

## Workflow

1. 通读输入内容，先区分哪些部分需要翻译，哪些部分必须原样保留。
2. 保留 Markdown 结构、标题层级、列表、表格、引用块、代码块、图片标签、Liquid/Hexo 标签、HTML 标签与空行布局。
3. 翻译自然语言内容，包括正文、标题、说明文字和需要面向读者展示的文本。
4. 输出完整英文 Markdown，不追加解释、注释或额外前言。

## Translation Rules

- 准确传达原文事实、背景和技术含义，不为了文风牺牲事实。
- 使用自然、清晰、偏科普杂志风格的英文，避免生硬直译。
- 保留常见技术术语和缩写，例如 `LLM`、`RAG`、`Transformer`。
- 保留代码块内容、命令、配置键名、路径、变量名、日志和错误信息的原文，除非用户明确要求翻译它们。
- 保留图片标签和图片路径原文，不要改写，例如 `{% img /images/post/2024/01/trulens.jpg 400 300 %}`。
- 保留链接目标、URL 和资源路径，必要时只翻译读者可见的链接文字。
- 将全角括号统一改为半角括号，并在相邻文本存在时，使用正常空格处理 `(` 前和 `)` 后的间距。
- 只在公司名称是举例说明、而不是文章事实主体时，才可将阿里、腾讯这类本土科技公司替换为更通用的英文语境示例，例如 Google、Microsoft。

## Markdown Preservation

- 保持输入和输出都为 Markdown。
- 保持原始 front matter 结构和字段顺序。
- 可以翻译面向读者展示的 front matter 文本值，例如 `title`、`description`，但不要改动字段名、时间、布尔值、数组结构和未要求修改的元数据。
- 不要删除或新增标题、图片、代码块、引用块、分隔线或模板标签。
- 不要把 Markdown 改写成纯文本，也不要重新排版成不同结构。

## Terminology Mapping

- `Transformer` -> `Transformer`
- `大语言模型` -> `LLM` / `Large Language Model`
- `生成式 AI` -> `Generative AI`

## Output Contract

- 只输出翻译后的英文 Markdown。
- 不输出“下面是翻译结果”之类的包裹语。
- 如果输入本身已经包含 Markdown 示例或模板标签，继续保持原样格式。

## Example

输入：

```markdown
### 生命短暂，时光飞逝；江河轻阵，船只远行。

{% img /images/post/2024/01/rag_triad.jpg 1000 600 %}

我们约若眨眼间即逝的时光；我们的相遇如同天际与江水的融合。
```

输出：

```markdown
### Life is short, and time moves quickly; rivers run wide, and ships sail far.

{% img /images/post/2024/01/rag_triad.jpg 1000 600 %}

We pass through time in the blink of an eye; we meet like the joining of sky and river.
```
