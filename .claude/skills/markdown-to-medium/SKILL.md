---
name: markdown-to-medium
description: 将 Markdown 文本或 Markdown 文件转换成适合粘贴到 Medium 编辑器的 HTML。适用于用户要求把博客文章、技术文档、Hexo 文章或其他 `.md` 内容导出为 Medium 兼容格式，尤其适合当前仓库中需要复用 `markdown-to-medium-tool` 转换逻辑，而不是手动打开网页工具的场景。
---

# Markdown To Medium

## Overview

调用本 skill 时，优先使用 `scripts/convert.js` 执行真实转换，并保持行为与仓库内 `markdown-to-medium-tool` 的核心逻辑一致。默认输出 HTML 到标准输出，必要时再写入目标文件。

对于 Hexo 文章中的 `{% img %}` 这类模板标签，默认保持原样，不要额外替换成 `<img>`、Markdown 图片语法或绝对链接，除非用户明确要求这样做。

## Workflow

1. 确认输入来源，是现有 Markdown 文件，还是用户直接提供的 Markdown 文本。
2. 优先运行 `scripts/convert.js` 完成转换，不要手工把 Markdown 改写成 HTML。
3. 默认把结果输出到标准输出，除非用户明确要求保存到文件。
4. 如果用户要求落盘，使用 `--output` 生成目标 HTML 文件，再向用户说明路径。
5. 如果用户还要求进一步适配 Medium 排版，再在转换结果基础上做少量手工调整，但不要声称这些调整来自原始工具。
6. 遇到 Hexo 模板标签时，默认保留原文转换结果，不要自作主张做兼容性替换或预处理。

## Script

- Script path: `scripts/convert.js`
- Input modes:
  - `node .claude/skills/markdown-to-medium/scripts/convert.js path/to/post.md`
  - `cat post.md | node .claude/skills/markdown-to-medium/scripts/convert.js`
- Optional output file:
  - `node .claude/skills/markdown-to-medium/scripts/convert.js source/_posts/post.md --output /tmp/post-medium.html`

## Output Contract

- 输出为 HTML，不附带解释性前言。
- 如果用户没有要求写文件，直接返回 HTML 内容或将其写到标准输出。
- 保留原始 Markdown 的语义结构，由 `marked` 负责转换。
- 保留 Hexo `{% img %}` 等模板标签的原始文本，不做额外改写，除非用户明确要求自定义处理。

## Limits

- 仓库中的 `markdown-to-medium-tool` README 提到 inline codespan 格式选项，但当前源码并未真正实现该选项，本 skill 不应假设它存在。
- 当前转换逻辑本质上是 `marked(input)`，因此如果用户要求超出原工具能力的 Medium 定制排版，需要明确说明那属于额外处理。
- 不要因为“更适合 Medium”就擅自修改 Hexo 模板标签，这类替换属于额外处理，不是本 skill 的默认行为。
- 本 skill 依赖仓库内 `markdown-to-medium-tool/node_modules/marked`，如果该依赖被移除，需要先恢复依赖或调整脚本实现。
