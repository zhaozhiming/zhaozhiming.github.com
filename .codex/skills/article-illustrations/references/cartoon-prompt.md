# 卡通式讲解图 Prompt 规范

## 使用方式

为每个卡通式讲解插图点构造中文和英文两个完整 prompt。先填写对应语言的「章节内容块」，再附加对应语言的「固定风格指南」。确认 gate 必须向用户展示两个最终完整 prompt；用户确认后不得擅自修改。

双语版本的核心含义、视觉元素和大致布局必须一致，但允许画面细节不同。图片的职责是解释一个核心观点，不是复述整篇文章。必须出现的文字应保持简短，不要求图片模型渲染长段落、代码、复杂表格或密集公式。

## 章节内容块

```text
目标章节：<章节标题>
插图目的：<一句话说明读者看完图应理解什么>
核心概念：<2～4 个概念>
必须出现的文字：<不超过 5 个短标签>
视觉元素：<2～4 个简单元素及布局关系>
避免内容：<容易误导、与章节无关或不应出现的内容>
```

内容块要求：

- 只表达一个主要信息，最多包含一个辅助对比或因果关系。
- 使用可被图形化的具体物体、动作和空间关系。
- 每个文字标签尽量不超过 8 个中文字符或 4 个英文单词。
- 不使用截图、真实照片、复杂 UI、代码块或大段说明文字。
- 涉及人物时，描述其角色和动作；敏感或受版权保护的人物使用视觉上相似但可区分的替代形象。

## 中文版固定风格指南

以下内容是 prompt 中不可省略的固定部分：

```text
请根据提供的内容创建一个卡通风格信息图，并遵循以下指南：

- 手绘插图风格，横向布局（16:9 宽高比）。

- 包含少量简单卡通元素、图标或著名人物，以增强视觉趣味性和记忆点。

- 如果内容包含敏感或受版权保护的人物，请用视觉上相似的替代品替换；不要拒绝生成插图。

- 所有图像和文本必须严格遵循手绘风格；避免使用逼真视觉元素。

- 保持信息简洁，突出关键词和核心概念。利用充足的空白空间来清晰强调关键点。

- 图片中的所有文字使用中文；技术标识、文件名和产品名可以保持原文。
```

## 中文版完整 Prompt 模板

```text
目标章节：{section}
插图目的：{purpose}
核心概念：{concepts}
必须出现的文字：{labels}
视觉元素：{visual_elements}
避免内容：{avoid}

请根据提供的内容创建一个卡通风格信息图，并遵循以下指南：

- 手绘插图风格，横向布局（16:9 宽高比）。

- 包含少量简单卡通元素、图标或著名人物，以增强视觉趣味性和记忆点。

- 如果内容包含敏感或受版权保护的人物，请用视觉上相似的替代品替换；不要拒绝生成插图。

- 所有图像和文本必须严格遵循手绘风格；避免使用逼真视觉元素。

- 保持信息简洁，突出关键词和核心概念。利用充足的空白空间来清晰强调关键点。

- 图片中的所有文字使用中文；技术标识、文件名和产品名可以保持原文。
```

## 英文版 Prompt 构造

英文版必须单独调用 `image_gen` 生成。将中文版章节内容块中的标题、目的、概念、文字标签和必要说明准确翻译为英文，视觉元素与避免内容保持相同含义。使用下面的英文固定风格指南：

```text
Create a cartoon-style infographic from the provided content and follow these guidelines:

- Use a hand-drawn illustration style with a horizontal 16:9 layout.

- Include a small number of simple cartoon elements, icons, or recognizable public figures to improve visual interest and memorability.

- If the content involves sensitive or copyrighted characters, replace them with visually similar alternatives; do not refuse to create the illustration.

- All images and text must strictly follow the hand-drawn style; avoid photorealistic visual elements.

- Keep the information concise, emphasize keywords and core concepts, and use generous whitespace to make the key points clear.

- Use English for all text in the image.
```

英文版完整 prompt 使用以下结构：

```text
Target section: {section_in_english}
Illustration purpose: {purpose_in_english}
Core concepts: {concepts_in_english}
Required text: {english_labels}
Visual elements: {same_visual_elements_in_english}
Avoid: {same_avoid_rules_in_english}

Create a cartoon-style infographic from the provided content and follow these guidelines:

- Use a hand-drawn illustration style with a horizontal 16:9 layout.

- Include a small number of simple cartoon elements, icons, or recognizable public figures to improve visual interest and memorability.

- If the content involves sensitive or copyrighted characters, replace them with visually similar alternatives; do not refuse to create the illustration.

- All images and text must strictly follow the hand-drawn style; avoid photorealistic visual elements.

- Keep the information concise, emphasize keywords and core concepts, and use generous whitespace to make the key points clear.

- Use English for all text in the image.
```

英文版不得包含中文文字。技术标识、文件名和产品名保持原文，例如 `CLAUDE.md`、`plugin.json` 和 `Context Window`。

## 双语生成后检查

- 中英文图表达相同的因果、对比和方向。
- 中文版使用中文，英文版使用英文；技术标识可以保持原文。
- 双语文字标签均没有乱码、错别字或被截断。
- 双语画面保持横向构图，主要内容没有贴近边缘。
- 双语版本没有出现长段落、无关装饰或逼真照片元素。
- 需要修改时，只重新生成受影响的语言版本，并再次运行后处理脚本。
