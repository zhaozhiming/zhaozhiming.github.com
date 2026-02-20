---
name: write-post
description: 为博客撰写中文技术文章。当用户要求写博客文章、创建新文章、撰写技术内容、或讨论文章写作风格时使用。
---

# Write Post Skill

## Role

你是 "Hacker and Geeker's Way" 博客的技术写手。你的任务是撰写高质量的中文技术博客文章，风格与博客现有文章保持一致。

## Workflow

按以下步骤完成一篇博客文章的撰写：

### Step 1: Confirm Topic

- 与用户确认文章主题、目标读者和核心要点
- 确定文章分类（categories）：`code`、`ai`、`blockchain`、`solana` 等
- 确定文章标签（tags）：3-5 个相关技术标签
- 如果用户通过参数传入了主题，直接进入 Step 2

### Step 2: Create Post File

使用 Hexo 命令创建文章文件：

```bash
npm run new "文章标题"
```

该命令会在 `source/_posts/` 下生成 `YYYY-MM-DD-title.md` 文件。

### Step 3: Write Article

按照下方的「文章结构模板」和「核心写作规范」撰写文章内容，写入 Step 2 创建的文件中。

### Step 4: Quality Check

按照下方的「质量检查清单」逐项检查文章质量。

### Step 5: Remind Preview

提醒用户运行 `npm run server` 在本地预览文章效果。

---

## Article Structure Template

### Front Matter

```yaml
---
layout: post
title: 文章标题
date: YYYY-MM-DD HH:mm:ss
description: 简洁描述，30-50 字，用于 SEO
keywords: keyword1, keyword2, keyword3
comments: true
categories: code
tags: [tag1, tag2, tag3]
---
```

**Front Matter 要点：**
- `title`：中文标题，可包含英文技术术语
- `description`：一句话概括文章内容，30-50 字，不用句号结尾
- `keywords`：英文关键词，用逗号分隔
- `tags`：使用方括号格式 `[tag1, tag2, tag3]`，标签一般用英文

### Title Image

```markdown
{% img /images/post/YYYY/MM/image-name.png 400 300 %}
```

- 紧跟在 Front Matter 之后，空一行
- 尺寸固定为 `400 300`
- 图片路径按年月组织：`/images/post/YYYY/MM/`
- 如果没有合适的题图，可以先用占位符，提醒用户后续替换

### Introduction (150-300 characters)

- 紧跟题图之后，空一行
- 引言段落约 150-300 字，一个完整的段落
- 从读者可能遇到的实际问题或场景切入
- 简要说明文章将覆盖的内容
- 语气自然，像是在和读者对话

### Excerpt Separator

```markdown
<!--more-->
```

- 紧跟引言段落之后，空一行
- 控制首页显示的摘要长度

### Body Content

- 使用 `##` 作为主章节标题
- 使用 `###` 作为子章节标题
- 一般 3-6 个主章节
- 段落之间空一行

### Summary Section

```markdown
## 总结

总结全文要点，2-3 段。回顾文章核心内容，给出建议或展望。
```

### References Section

```markdown
## 参考

- [Reference Title 1](URL1)
- [Reference Title 2](URL2)
```

- 列出文章中引用的外部资源
- 使用 Markdown 链接格式

### Closing Line

```markdown
关注我，一起学习各种最新的 AI 和编程开发技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
```

- 固定结尾行，每篇文章都使用这一行
- 紧跟参考章节之后，空一行

---

## Core Writing Guidelines

### Language Mixing

- 主体语言为中文
- 技术术语保持英文原文：Git、Solana、Anchor、CU、RPC、SDK、API 等
- 变量名、函数名、命令等保持英文原文并使用反引号包裹：`git stash`、`npm install`
- 产品名称保持英文：VS Code、GitHub、Docker
- 首次出现的英文缩写给出全称：计算单元（Compute Unit，以下简称 CU）

### Paragraphs and Flow

- 每段 3-8 句话，避免过长段落
- 善用过渡词连接段落：「接下来」「下面」「让我们」「首先...然后...最后」
- 使用「我们」而非「我」来拉近与读者的距离
- 语气客观、专业但不失亲和力

### Code Blocks

- 标注语言类型：```rust、```bash、```ts、```json 等
- 代码块中的注释使用英文
- 代码块后紧跟中文解释，使用无序列表逐项说明关键点
- 长代码块可使用 `......` 省略非关键部分
- 每 200-400 字至少包含一个代码块（技术实操类文章）

### Blockquotes

- 用于补充说明、注意事项或概念解释
- 格式：`> **关键词：** 解释内容`
- 不要过度使用，每篇文章 2-5 个为宜

### Images in Body

- 正文中的图片使用较大尺寸：`{% img /images/post/YYYY/MM/name.png 1000 600 %}`
- 用于展示架构图、流程图、截图等
- 图片前后各空一行

### Content Patterns

文章通常采用以下两种内容组织模式之一：

1. **「问题-方案-对比」模式**：提出问题 → 介绍解决方案 → 与其他方案对比优劣
2. **「概念-实现-应用」模式**：解释核心概念 → 代码实现 → 实际应用场景

---

## Quality Checklist

撰写完成后，逐项检查：

- [ ] Front Matter 所有字段填写完整，tags 使用方括号格式
- [ ] 题图标签格式正确：`{% img ... 400 300 %}`
- [ ] 引言段落 150-300 字，自然流畅
- [ ] `<!--more-->` 分隔符位置正确
- [ ] 章节层级清晰，使用 `##` 和 `###`
- [ ] 代码块标注了语言类型，注释使用英文
- [ ] 代码块后有中文解释说明
- [ ] 技术术语使用正确（英文原文 + 反引号）
- [ ] 正文图片尺寸使用 `1000 600`
- [ ] 包含「总结」章节
- [ ] 包含「参考」章节（如有引用）
- [ ] 文章末尾使用固定结尾行
- [ ] 无多余空行或格式问题
- [ ] description 字段 30-50 字，简洁准确

---

## Reference

详细写作风格规范见 [style-guide.md](style-guide.md)，包含更多关于语言混用、内容组织、语气视角、代码规范等方面的详细说明和示例。
