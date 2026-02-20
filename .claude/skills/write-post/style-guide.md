# Writing Style Guide

本文档是 "Hacker and Geeker's Way" 博客的详细写作风格参考，基于 2024-2026 年间多篇文章的风格分析提炼而成。

---

## 1. Language Mixing Rules

### General Principles

- 主体语言为中文，技术术语保持英文原文
- 中英文之间不需要添加空格（遵循博客现有习惯）
- 英文术语在句子中自然嵌入，不刻意翻译

### Technical Terms (Keep English)

以下类别的词汇保持英文原文：

- **编程语言和框架**：Rust、Go、TypeScript、Python、Anchor、React
- **工具和产品**：Git、Docker、VS Code、GitHub、Cursor、Solana Explorer
- **技术概念缩写**：API、SDK、RPC、CPI、PDA、CU、DeFi、DApp
- **命令和代码标识符**：用反引号包裹，如 `git stash`、`npm install`、`jj squash`

### First Mention Pattern

首次提及英文缩写时给出完整说明：

```
计算单元（Compute Unit，以下简称 CU）
跨程序调用（Cross Program Invocation，简称 CPI）
程序派生地址（Program Derived Address，PDA）
```

### Translation Patterns

某些概念可以中英文混用：

```
布隆过滤器（Bloom Filter）
交互式 rebase
地址查找表（Address Lookup Table）
```

---

## 2. Content Organization Patterns

### Pattern A: Problem-Solution-Comparison

适合介绍新工具、新方法的文章。

**结构**：

1. 引言中描述读者常遇到的痛点问题
2. 介绍解决方案的核心概念
3. 通过与现有方案的对比展示优劣
4. 总结并给出选择建议

**示例**（Jujutsu 文章）：

- 引言描述 Git 的痛点（stash、rebase 混乱、拆分 PR 困难）
- 介绍 JJ 的核心理念（自动 revision）
- 逐一对比 Git vs JJ 的操作方式
- 总结 JJ 的优势和不足

### Pattern B: Concept-Implementation-Application

适合深入讲解某个技术概念的文章。

**结构**：

1. 解释核心概念和原理
2. 完整的代码实现
3. 性能分析或实际运行结果
4. 应用场景和最佳实践

**示例**（布隆过滤器文章）：

- 解释布隆过滤器原理和属性
- Solana 上的完整代码实现
- 性能分析（存储大小、CU 消耗）
- 应用场景建议

### Comparison Writing Technique

当进行两种技术对比时，采用以下格式：

```markdown
**Git 操作：**

（代码块）

**JJ 操作：**

（代码块）

两者区别的解释文字...
```

- 先展示旧方案 / 基准方案
- 再展示新方案 / 推荐方案
- 最后说明差异和优势

---

## 3. Tone and Perspective

### Voice

- 使用第一人称复数「我们」为主，拉近与读者距离
- 偶尔使用「你」直接对话读者
- 避免使用「我」（除非描述个人经历）

### Common Transition Phrases

```
接下来，让我们通过...来深入了解...
下面我们就来...
首先...然后...最后...
让我们看看两种工具如何处理这个场景
现在我们知道了...那么...
通过以上测试，我们可以得到以下结论
从测试结果中我们可以看到...
如果这些痛点让你感同身受，那么...值得你关注
```

### Introducing External Resources

```
在这里推荐一个...在线计算工具：[Tool Name](URL)，可以帮助你...
完整代码可以参考 [repo-name](URL) 仓库。
可以参考[官方文档](URL)
我们可以通过查看[这些源码](URL)来了解...
```

### Tone Characteristics

- **客观专业**：数据说话，给出具体的数字和测试结果
- **循序渐进**：从简单到复杂，每一步都有解释
- **不过度夸大**：对新技术既介绍优势也指出不足
- **务实导向**：关注实际开发中能用到的内容

---

## 4. Code Block Guidelines

### Frequency

- 技术实操类文章：每 200-400 字至少一个代码块
- 概念介绍类文章：适量使用，以说明问题为准

### Language Tags

始终标注代码语言：

```
```rust
```bash
```ts / ```typescript
```json
```toml
```yaml
```

### Comments in Code

- 代码块内的注释使用**英文**
- 注释简洁明了，解释关键逻辑

```rust
// initialize the bloom filter
let filter = &mut ctx.accounts.filter;
filter.init(new_filter)?;
```

### Post-Code Explanation

代码块后紧跟中文解释，使用无序列表逐项说明：

```markdown
- `NewFilter` 结构用于表示初始化指令的参数
- `validate` 方法用于验证参数的有效性
- `init` 方法用于初始化布隆过滤器
```

### Omitting Code

长代码块中可以省略非关键部分：

```json
{
  "meta": {
    "computeUnitsConsumed": 169182,
    "innerInstructions": [...],
    "logMessages": [...],
    // ......
  }
}
```

### Command Output

展示命令及其输出时使用以下格式：

```bash
$ anchor test

bloom-filter
    initialize bloom filter
account size: 10065
      ✔ initialize bloom filter (461ms)
```

---

## 5. Blockquote Usage

### Purpose

- 补充说明重要概念
- 注意事项提醒
- 术语解释

### Format Patterns

**概念解释型：**

```markdown
> **什么是 Revision？** 在 JJ 中，revision（修订版本）是核心概念，类似于 Git 的 commit 但更智能...
```

**注意事项型：**

```markdown
> **注意：** `jj log` 和 `jj op log` 是两个不同的命令，前者是查看 revision 历史，后者是查看操作历史。
```

**补充说明型：**

```markdown
> [Anchor](https://www.anchor-lang.com/) 是 Solana 区块链上广泛使用的开发框架，用于简化程序和去中心化应用的开发流程。
```

### Frequency

- 每篇文章 2-5 个引用块
- 不要过度使用，只在需要额外强调或解释时使用

---

## 6. Image Guidelines

### Title Image

- 位置：紧跟 Front Matter 之后
- 尺寸：`400 300`
- 格式：`{% img /images/post/YYYY/MM/image-name.ext 400 300 %}`

### Body Images

- 尺寸：`1000 600`（用于截图、架构图、流程图）
- 偶尔使用其他尺寸如 `600 400`
- 格式：`{% img /images/post/YYYY/MM/image-name.ext 1000 600 %}`
- 图片前后各空一行

### Image Directory

- 按年月组织：`source/images/post/YYYY/MM/`
- 文件名使用英文，小写，连字符分隔

### Image Types

- `.png`：截图、示意图
- `.jpg` / `.jpeg`：照片、题图
- 根据实际情况选择格式

---

## 7. List Usage

### Unordered Lists

用于解释代码、列举特征：

```markdown
- `bit_array` 是一个布尔类型的数组，用于存储布隆过滤器的位图
- `m` 是位图的大小，是 bit_array 的实际长度
- `k` 是哈希函数的数量
```

### Ordered Lists

用于步骤说明、流程描述：

```markdown
1. 初始化布隆过滤器，长度 m = 10
2. 插入元素 `Foo`，假设哈希函数得到索引分别为 0、2、4
3. 插入元素 `Bar`，哈希函数得到索引分别为 4、7、9
```

### Nested Lists

支持嵌套但不要超过 2 层：

```markdown
- `config`：Object 类型的配置参数：
  - `commitment`: 交易确认的级别
  - `maxSupportedTransactionVersion`: 最大支持的交易版本号
  - `encoding`: 交易编码格式
```

---

## 8. Article Length Guide

- **标准技术文章**：3000-6000 字（不含代码块）
- **深度教程**：5000-10000 字（含代码块可达更长）
- **简短分享**：1500-3000 字

文章的核心价值在于内容质量而非长度，确保每一段都有信息增量。

---

## 9. Description Writing Examples

description 字段的写法示例（30-50 字，简洁准确）：

```
通过和 Git 的对比来介绍 Jujutsu 的基本操作和高级特性
深入解析 Solana 交易，包括查看方式、交易结构、交易解析等。
通过一个完整的代码示例来介绍布隆过滤器在 Solana 上如何实现。
深入浅出地介绍 Solana 开发中的账号存储限制。
介绍在使用 Cursor 开发新项目时的一些心得和体会
```

**规律**：
- 以动词开头：「通过」「深入」「介绍」「详细」
- 说明文章核心内容和方法
- 不使用句号结尾（部分文章有句号，但新文章建议不加）

---

## 10. Summary Section Patterns

总结章节通常包含以下内容：

1. **回顾全文**：概括文章讲了什么
2. **核心结论**：提炼关键发现或建议
3. **展望或建议**：给出后续学习方向或使用建议

**示例结构**：

```markdown
## 总结

本文通过对比 A 和 B 的工作流程，全面介绍了...的核心特性和实际使用方法。
我们可以看到，...通过...等创新设计，有效解决了...的诸多痛点。

虽然...目前仍是...的事实标准，但...展示了...的另一种可能性。
对于...来说，...都值得投入时间去学习和尝试。
```

---

## 11. Common Structural Elements

### Section Dividers

文章内部不使用分割线（`---`），依靠标题层级来组织内容。

### Emphasis

- 使用 `**粗体**` 强调关键概念
- 使用反引号 `` ` `` 标记代码、命令、变量名
- 避免使用 *斜体*（中文排版中不常用）

### Links

- 首次提及工具/框架时给出官方链接：`[Solana](https://solana.com/)`
- 引用外部资源使用内联链接
- 参考部分使用列表链接

### Sub-section Pattern: "小结"

长章节末尾可以使用加粗的「小结」来总结该章节：

```markdown
**小结：** 布隆过滤器的存储大小与账号初始化时设置的位图数组最大长度有关...
```
