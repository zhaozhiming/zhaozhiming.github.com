---
name: article-illustrations
description: 为本 Hexo 博客文章选择 2～3 个正文插图点，每个插图点生成中文和英文两个版本，包括卡通式讲解图和 Excalidraw 风格流程图，并仅将合规中文版 JPG 插入对应章节。用户要求给文章生成插图、配插图、正文图片、章节配图、流程图或 article illustrations 时使用；文章封面使用 cover-image skill，不使用本 skill。
---

# Article Illustrations（文章正文插图）

## 概述

为文章核心章节选择 2～3 个解释性插图点，每个插图点同时生成中文和英文两个版本。卡通式讲解使用内置 `image_gen`；流程图使用程序化创建的可编辑 SVG。最终图片统一为横向 16:9 JPG，严格小于 `307200` bytes，仅将中文版插入文章中已确认的位置。

本 skill 不生成文章封面、截图或纯装饰图片。封面使用 `cover-image` skill。

## 硬约束

1. **生成前必须确认**：展示完整插图计划并等待用户明确确认。在确认前，不调用 `image_gen`、不创建正式 SVG、不修改文章。
2. **只选择核心内容**：优先解释文章最重要且较难仅靠文字理解的章节，不重复已有图片或截图已经清楚覆盖的内容。
3. **每篇 2～3 个插图点**：核心章节不超过 4 个时选择 2 个；核心章节达到 5 个，或存在独立的架构、流程和总结内容时选择 3 个。没有足够价值时不得强行凑数。
4. **风格按内容选择**：概念、对比、观点、比喻和人物故事使用卡通式讲解；步骤、架构、数据流、关系和生命周期使用 Excalidraw 风格流程图。
5. **每个插图点双语交付**：中文版使用无语言后缀文件名，英文版使用 `-en` 后缀。文章只引用中文版，英文版仅作为附加交付。
6. **流程图保留双语 SVG**：先创建中文 SVG，再复制为英文 SVG，仅翻译文字；两者节点、连线、颜色和布局必须完全一致。卡通图只保留双语最终 JPG。
7. **最终文件合规**：每张中英文最终图片必须是 JPEG、严格 16:9、严格小于 `307200` bytes，并保持文字和线条可读。
8. **双语验证通过后再接入**：同一插图点的任何语言版本验证失败时，不得将中文版插入文章。

## Workflow

### 1. 确定目标文章

用户通常会指定文章文件或 slug。未指定时，选择 `source/_posts/` 中最近修改的文章并向用户确认。

读取完整文章，包括：

- front matter 中的标题和日期
- 全部章节标题和正文
- 已有 `{% img %}` 标签、截图及其上下文

使用 front matter 日期确定 `YYYY/MM`，使用文章文件名去除日期后的部分作为 article slug。

### 2. 识别核心章节

列出文章的核心章节，判断哪些章节最需要视觉辅助。排除：

- 已经被现有图片或截图清楚解释的章节
- 纯参考资料、安装命令列表或简短总结
- 无法通过一张图增加理解的内容

选择数量：

- 核心章节不超过 4 个：选择 2 个插图点。
- 核心章节达到 5 个，或存在可分别表达的架构、流程和总结内容：选择 3 个插图点。
- 不得为次要章节或已有图片覆盖的内容配图；应从文章核心内容中选择互不重复的 2～3 个解释角度。

### 3. 推荐风格并准备完整方案

风格选择：

- **卡通式讲解**：概念解释、观点对比、比喻、人物故事。读取 `references/cartoon-prompt.md`，构造中文和英文完整 prompt。
- **Excalidraw 风格流程图**：步骤、架构、数据流、关系、生命周期。读取 `references/flowchart-style.md`，构造中文绘图方案和英文文字翻译方案。

文件名使用简短、小写、连字符分隔的英文 topic：

```text
source/images/post/YYYY/MM/<article-slug>-<topic>.jpg
source/images/post/YYYY/MM/<article-slug>-<topic>.svg
source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg
source/images/post/YYYY/MM/<article-slug>-<topic>-en.svg
```

无 `-en` 后缀的是中文版，带 `-en` 后缀的是英文版。只有流程图填写双语 SVG 路径。

### 4. 确认 Gate（停下，等待用户）

一次性展示所有候选图片：

```markdown
### 插图 N：<topic>
- 目标章节：
- 选择原因：
- 推荐风格：
- 核心概念与关键词：
- 中文版完整 prompt / 流程图绘制方案：
- 英文版完整 prompt / 流程图文字翻译方案：
- 中文 JPG 路径：
- 英文 JPG 路径：
- 中文 SVG 路径：仅流程图填写
- 英文 SVG 路径：仅流程图填写
- 插入位置：目标章节第一个解释性段落之后
```

得到用户明确确认前必须停下。用户修改后的方案视为新的已批准方案；后续不得擅自改变章节、风格、含义、路径或插入位置。

### 5. 生成卡通式讲解图

1. 读取 `references/cartoon-prompt.md`。
2. 使用确认后的中文完整 prompt 调用内置 `image_gen`。
3. 使用确认后的英文完整 prompt 再次调用内置 `image_gen`。英文版保持与中文版相同核心含义、视觉元素和大致布局，但允许画面细节不同。
4. 分别检查双语版本的画面含义、文字、构图和边缘留白。
5. 使用后处理脚本分别生成双语目标 JPG：

```bash
bash .codex/skills/article-illustrations/scripts/prepare-illustration.sh \
  "<generated-chinese-image-path>" \
  "source/images/post/YYYY/MM/<article-slug>-<topic>.jpg"

bash .codex/skills/article-illustrations/scripts/prepare-illustration.sh \
  "<generated-english-image-path>" \
  "source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg"
```

### 6. 创建 Excalidraw 风格流程图

1. 读取 `references/flowchart-style.md`。
2. 根据确认后的中文绘图方案，使用 `apply_patch` 直接创建中文 SVG。
3. 复制相同结构为 `-en.svg`，仅将标题、节点、条件和辅助文字翻译为英文。不得改变节点、连线、颜色、坐标或布局。
4. 检查双语 SVG 的文字、连线方向、条件标签、颜色含义和布局。
5. 使用后处理脚本分别生成双语 JPG，并保留双语 SVG：

```bash
bash .codex/skills/article-illustrations/scripts/prepare-illustration.sh \
  "source/images/post/YYYY/MM/<article-slug>-<topic>.svg" \
  "source/images/post/YYYY/MM/<article-slug>-<topic>.jpg"

bash .codex/skills/article-illustrations/scripts/prepare-illustration.sh \
  "source/images/post/YYYY/MM/<article-slug>-<topic>-en.svg" \
  "source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg"
```

### 7. 验证图片

每个插图点的中英文 JPG 均执行：

```bash
file source/images/post/YYYY/MM/<article-slug>-<topic>.jpg
sips -g pixelWidth -g pixelHeight source/images/post/YYYY/MM/<article-slug>-<topic>.jpg
wc -c source/images/post/YYYY/MM/<article-slug>-<topic>.jpg

file source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg
sips -g pixelWidth -g pixelHeight source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg
wc -c source/images/post/YYYY/MM/<article-slug>-<topic>-en.jpg
```

必须确认：

- `file` 报告 JPEG。
- `pixelWidth * 9 == pixelHeight * 16`。
- 文件大小严格小于 `307200` bytes。
- 图片缩小后文字、节点和连线仍然可读。
- 流程图的中英文 SVG 源文件存在，且除文字外结构一致。

任何检查失败时，只修复或重新生成受影响的语言版本。同一插图点的双语版本均验证通过前不得修改文章。

### 8. 接入文章

在已确认目标章节的第一个解释性段落之后插入：

```text
{% img /images/post/YYYY/MM/<article-slug>-<topic>.jpg 1000 563 %}
```

只插入无 `-en` 后缀的中文版。英文版不得插入文章。图片标签前后各保留一个空行。不得移动、替换或改写无关文章内容和现有图片。

### 9. 最终验证与汇报

检查：

- [ ] 每个插图点均生成中英文版本
- [ ] 所有中英文 JPG 均为 16:9 且严格小于 `307200` bytes
- [ ] 所有流程图均保留结构一致的中英文 SVG
- [ ] 双语图片内容与批准方案一致且可读
- [ ] 所有文章引用路径存在
- [ ] 文章只引用中文版，没有引用 `-en` 文件
- [ ] 图片插入位置与批准方案一致
- [ ] 没有改动无关文章内容

向用户汇报每个插图点的风格、中英文 JPG 路径与大小、插入章节，以及流程图的中英文 SVG 路径。

## 失败处理

- 缺少 `ffmpeg` 时，报告依赖缺失并在修改文章前停止。
- SVG 输入缺少 `rsvg-convert` 时，报告依赖缺失并在修改文章前停止。
- 后处理脚本无法生成任一语言的合规 JPG 时，不插入该插图点的中文版；调整受影响源图后重试。
- 生成内容存在错误文字、错误连线或错误含义时，修复或重新生成后重新验证。
- 已验证通过的其他图片可以保留，但所有批准插图点的双语文件和文章引用有效前，不得汇报任务完成。
