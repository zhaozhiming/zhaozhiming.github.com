---
name: cover-image
description: Use when the user wants to generate or create a cover image (封面 / 封面图 / 题图 / 配图 / cover) for a blog post in this Hexo blog, typically a newly written article that still needs its pixel-art, animal-themed title image. Triggers include 生成封面 / 做封面 / 给文章配封面 / generate cover.
user-invocable: true
---

# Cover Image（博客封面生成）

## Overview

为本博客文章生成「像素风 + 动物 + 标题文字」的封面图，风格与历史封面一致。核心约束：每篇封面的主角动物**不能与历史封面重复**；prompt **严格使用固定模板**；最终图片必须是 **4:3** 且**小于 400 KB**。优先使用 JPG 在较小体积下保持清晰度，PNG 仅在无需明显降色时使用。生成后把图片归位到正确目录并接入文章。

图片生成走 Codex 内置 `image_gen` 工具（gpt-image-2，无需 API key），归位与接入由本技能负责。

## When to use

- 用户要求给某篇文章生成/制作封面、题图、配图、cover
- 新写完的文章还没有封面（front matter 后缺 `{% img %}` 题图行，或题图指向的图片文件不存在）

## When NOT to use

- 用户要的是正文里的流程图/架构图/截图（不走本技能）
- 用户明确要非动物、非像素风的封面

## 硬约束（必须全部满足）

1. **动物不重复**：动物必须避开 `.codex/skills/cover-image/used-animals.md` 里所有已用动物（含近亲，如 llama/alpaca、marmot/gopher 视为同类）。
2. **Prompt 模板逐字使用**：只替换 4 个占位符，其余词一字不改、不增删。
3. **生成前必须确认**：把动物 / 物品 / 英文标题 / 最终 prompt / 目标路径展示给用户，得到确认后才生成。
4. **生成后必须归位**：图片复制到 `source/images/post/YYYY/MM/<slug>.jpg` 或 `.png`，不能只留在 `$CODEX_HOME/generated_images/`。
5. **尺寸与体积合规**：最终图片必须是 4:3，且严格小于 400 KB（`409600` bytes）。
6. **接入文章**：在 front matter 后插入或替换 `{% img %}` 题图行。
7. **更新登记表**：把本次动物追加进 `used-animals.md`，否则下次会重复。

## Workflow

### 1. 确定目标文章
用户通常会指定文章（slug 或文件名）。若未指定，询问，或默认取 `source/_posts/` 里最近修改的那篇并向用户确认。

### 2. 读取
- 读文章（front matter + 正文），理解主题。
- 读 `.codex/skills/cover-image/used-animals.md`，把 `动物` 列里所有非 `none` 的值汇总为「已用动物清单」。

### 3. 联想动物（排除已用）
据文章主题联想 **1 个契合且不在已用清单里**的动物，给一句中文理由。
> 类比原提示词：「给你一篇文章，联想对应的一个动物，并解释原因」——但本技能额外要求：不能是历史用过的动物。

### 4. 联想 2 件物品
据该动物联想 **2 件**关联物品（可小如书 / 笔，也可大如森林 / 海洋），各给一句理由。
> 类比原提示词：「给你一种动物，联想这种动物相关联的 2 种物品，并解释原因」。

### 5. 提炼英文标题
从中文标题提炼简洁英文标题（例：「Claude Code 完全指南——Plugin」→ `Claude Code Plugins`）。

### 6. 拼最终 prompt（模板逐字）
把 `{title}`（英文标题）、`{object 1}`、`{object 2}`、`{animal}`（英文动物名）填入下面模板，其余词原样保留：

```
"{title}" is in the background, {object 1}, {object 2}, {animal}, pixel, typography, cinematic, light fantasy, vibrant, motion blur
```

### 7. 确认 gate（停下，等用户）
向用户展示以下内容，**得到确认前不要生成**：
- 动物 + 理由
- 2 件物品 + 理由
- 英文标题
- 最终 prompt（逐字）
- 目标路径：默认使用 `source/images/post/<文章年>/<文章月>/<slug>.jpg`。若文章已有题图行，可沿用原路径；但当 PNG 为满足 400 KB 需要明显降色时，改用同名 `.jpg` 并同步更新题图行。横版 4:3。
用户可修改动物、物品、标题和路径，但最终图片必须保持横版 4:3，且小于 400 KB。

### 8. 生成图片
用内置 `image_gen` 工具，按确认后的 prompt 生成：
- 优先生成横向图片；内置工具不保证 4:3，生成后必须执行下一步后处理
- quality `high`（封面含标题文字，需清晰）
- prompt 内容不要改动

### 9. 后处理并归位
使用技能内置脚本把生成图片裁切为 4:3、压缩到严格小于 400 KB，并直接写入目标路径。优先把目标路径设为 `.jpg`，脚本会以较高 JPEG quality 开始尝试；需要保留 PNG 时才使用 `.png`：

```shell
bash .codex/skills/cover-image/scripts/prepare-cover.sh \
  "$CODEX_HOME/generated_images/.../generated.png" \
  "source/images/post/YYYY/MM/<slug>.jpg"
```

脚本根据目标扩展名选择压缩方式：

- `.jpg` / `.jpeg`：依次尝试 `1200x900`、`1000x750`、`800x600`，从高到低调整 JPEG quality，优先保留颜色和清晰度。
- `.png`：依次尝试相同尺寸并逐级减少调色板颜色数；如果需要显著降色才能达标，应改用 JPG。

找到第一个小于 `409600` bytes 的结果。脚本失败时不能继续接入或汇报完成。

归位后必须验证：

```shell
sips -g pixelWidth -g pixelHeight source/images/post/YYYY/MM/<slug>.jpg
wc -c source/images/post/YYYY/MM/<slug>.jpg
```

确认宽高比为 4:3，文件格式与文章题图路径一致，且文件大小严格小于 `409600` bytes。

### 10. 接入文章
在文章 front matter 结尾的 `---` 之后、空一行处，插入或替换题图行：

```
{% img /images/post/YYYY/MM/<slug>.jpg 400 300 %}
```

- 文章已有题图行：保持其引用路径不变（图片已按该路径归位），如无需要不必改动该行。
- 文章没有题图行：在 front matter 后新插入该行。
- 题图行与下方引言之间空一行。

### 11. 更新登记表 + 汇报
- 在 `used-animals.md` 表格末尾追加一行：`日期 | slug | 动物 | 本次生成 | 备注（2 件物品 + 英文标题）`。
- 向用户汇报：图片路径、所用动物、文章是否已接入。

## 路径与文件名约定

- **图片格式与题图路径必须一致**：默认使用 JPG。若从已有 PNG 路径改为 JPG，必须同步更新文章中的 `{% img %}` 题图行。
- `YYYY/MM`：默认取文章日期的年 / 月；用户可在确认时调整（历史上偶有文章日期与图片目录月份不一致的情况）。
- `<slug>`：取文章文件名里日期之后的部分（`2026-05-29-claude-code-plugin.md` → `claude-code-plugin`）。

## 常见错误

- ❌ 改动模板里的固定词（pixel、cinematic 等）→ 必须逐字，只换 4 个占位符。
- ❌ 动物与历史重复 → 生成前务必比对 `used-animals.md`。
- ❌ 图片只留在 `$CODEX_HOME/generated_images/` → 必须复制进 `source/images/post/`。
- ❌ 为满足体积限制过度压缩 PNG、导致明显降色 → 改用高质量 JPG。
- ❌ 最终格式与文章题图路径扩展名不一致 → 必须同步更新题图行。
- ❌ 使用 `1536x1024` 等非 4:3 成品 → 必须运行 `prepare-cover.sh` 后处理。
- ❌ 图片达到或超过 400 KB → 必须继续压缩，`wc -c` 结果要小于 `409600`。
- ❌ 跳过确认直接生成 → 必须先经确认 gate。
- ❌ 忘记更新 `used-animals.md` → 直接导致下次重复。

## 完成检查清单

- [ ] 动物不在 `used-animals.md` 已用清单里
- [ ] prompt 用模板逐字，仅替换 4 个占位符
- [ ] 图片已复制到 `source/images/post/YYYY/MM/<slug>.jpg` 或 `.png`
- [ ] 最终图片宽高比为 4:3
- [ ] 最终图片严格小于 `409600` bytes
- [ ] 最终格式与文章题图路径扩展名一致
- [ ] 文章 `{% img %}` 题图行已插入 / 更新
- [ ] `used-animals.md` 已追加新行
